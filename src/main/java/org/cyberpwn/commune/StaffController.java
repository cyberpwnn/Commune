package org.cyberpwn.commune;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerQuitEvent;
import org.phantomapi.Phantom;
import org.phantomapi.command.PhantomCommandSender;
import org.phantomapi.construct.Controllable;
import org.phantomapi.construct.Controller;
import org.phantomapi.lang.GList;
import org.phantomapi.sync.TaskLater;
import org.phantomapi.transmit.Transmission;
import org.phantomapi.transmit.Transmitter;
import org.phantomapi.util.F;

public class StaffController extends Controller implements Transmitter
{
	private PhantomCommandSender last;
	private Set<String> set;
	
	public StaffController(Controllable parentController)
	{
		super(parentController);
		last = null;
		set = new HashSet<String>();
	}
	
	public void broadcast()
	{
		Transmission t = new Transmission("staff-command")
		{
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			public void onResponse(Transmission t)
			{
				
			}
		};

		try
		{
			t.transmit();
		}
		
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}
	
	public void handle(PhantomCommandSender plr)
	{
		last = plr;
		
		broadcast();
		
		new TaskLater(10)
		{
			@Override
			public void run()
			{
				last = null;
			}
		};
		
		GList<String> plrx = new GList<String>();
		
		for(Player i : Phantom.instance().onlinePlayers())
		{
			if(i.hasPermission("commune.staff"))
			{
				plrx.add(i.getName());
			}
		}
		
		last.sendMessage(F.color("&8&l(&b&l" + Phantom.getServerName() + "&8&l)&e:&f&l " + new GList<String>(plrx).toString(", ")));

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
	
	@EventHandler
	public void on(PlayerQuitEvent e)
	{
		set.remove(e.getPlayer().getName());
	}
	
	@Override
	public void onTransmissionReceived(Transmission t)
	{
		if(t.getType().equals("staff-command-data"))
		{
			if(t.getSource().equals(Phantom.getServerName()))
			{
				return;
			}
			
			set.addAll(t.getStringList("staff"));
			
			if(last == null)
			{
				return;
			}
			
			if(t.getStringList("staff").isEmpty())
			{
				return;
			}
			
			last.sendMessage(F.color("&8&l(&b&l" + t.getSource() + "&8&l)&e:&f&l " + new GList<String>(t.getStringList("staff")).toString(", ")));
		}
		
		if(t.getType().equals("staff-command"))
		{
			Transmission tx = new Transmission("staff-command-data", t.getSource())
			{
				/**
				 * 
				 */
				private static final long serialVersionUID = 1L;

				public void onResponse(Transmission t)
				{
					
				}
			};
			
			GList<String> plrx = new GList<String>();
			
			for(Player i : Phantom.instance().onlinePlayers())
			{
				if(i.hasPermission("commune.staff"))
				{
					plrx.add(i.getName());
				}
			}
			
			tx.set("staff", plrx);
			set.addAll(plrx);
			
			try
			{
				tx.transmit();
			}
			
			catch(IOException e)
			{
				e.printStackTrace();
			}
		}
	}
}
