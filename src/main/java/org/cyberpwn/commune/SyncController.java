package org.cyberpwn.commune;

import java.io.IOException;
import org.bukkit.Bukkit;
import org.phantomapi.Phantom;
import org.phantomapi.command.CommandController;
import org.phantomapi.command.CommandFilter.Permission;
import org.phantomapi.command.PhantomCommand;
import org.phantomapi.command.PhantomCommandSender;
import org.phantomapi.construct.Controllable;
import org.phantomapi.lang.GList;
import org.phantomapi.transmit.Transmission;
import org.phantomapi.transmit.Transmitter;
import org.phantomapi.util.C;

public class SyncController extends CommandController implements Transmitter
{
	private PhantomCommandSender last;
	
	public SyncController(Controllable parentController)
	{
		super(parentController, "sync");
		last = null;
	}
	
	public void broadcast(String command) throws IOException
	{
		Transmission t = new Transmission("sync-command")
		{
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;
			
			@Override
			public void onResponse(Transmission t)
			{
				
			}
		};
		t.set("command", command);
		t.transmit();
	}
	
	@Override
	public void onStart()
	{
		Phantom.registerTransmitter(this);
	}
	
	@Override
	public void onStop()
	{
		
	}
	
	@Override
	public String getChatTag()
	{
		return C.DARK_GRAY + "[" + C.GREEN + "Commune" + C.DARK_GRAY + "]: " + C.GRAY;
	}
	
	@Override
	public void onTransmissionReceived(Transmission t)
	{
		if(t.getType().equals("sync-command"))
		{
			String command = t.getString("command");
			Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
			
			Transmission tx = new Transmission("sync-command-response", t.getSource())
			{
				/**
				 * 
				 */
				private static final long serialVersionUID = 1L;
				
				@Override
				public void onResponse(Transmission t)
				{
					
				}
			};
			
			try
			{
				tx.transmit();
			}
			
			catch(IOException e)
			{
				e.printStackTrace();
			}
		}
		
		if(t.getType().equals("sync-command-response"))
		{
			if(t.getString("response") != null)
			{
				last.sendMessage(t.getString("response"));
			}
		}
	}
	
	@Permission("commune.sync.god")
	@Override
	public boolean onCommand(PhantomCommandSender sender, PhantomCommand command)
	{
		try
		{
			if(command.getArgs().length > 0)
			{
				last = sender;
				broadcast(new GList<String>(command.getArgs()).toString(" "));
				Bukkit.dispatchCommand(Bukkit.getConsoleSender(), new GList<String>(command.getArgs()).toString(" "));
				
				if(sender.isPlayer())
				{
					for(String i : Phantom.getServers())
					{
						if(Phantom.getNetworkCount(i) == 0)
						{
							sender.getPlayer().sendMessage(C.YELLOW + "Queued <> " + i);
						}
					}
				}
				
				return true;
			}
		}
		
		catch(IOException e)
		{
			e.printStackTrace();
		}
		
		return false;
	}
}
