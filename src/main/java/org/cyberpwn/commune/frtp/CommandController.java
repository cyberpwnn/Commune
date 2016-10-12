package org.cyberpwn.commune.frtp;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.phantomapi.construct.Controllable;
import org.phantomapi.construct.Controller;
import org.phantomapi.sync.TaskLater;
import org.phantomapi.util.C;

public class CommandController extends Controller implements CommandExecutor
{
	public CommandController(Controllable parentController)
	{
		super(parentController);
	}
	
	public boolean onCommand(CommandSender sender, Command c, String cmd, String[] args)
	{
		if(c.getName().equalsIgnoreCase("frtp"))
		{
			if(sender instanceof Player)
			{
				sender.sendMessage(C.RED + "Wait 3 seconds. Don't move.");
				Location l = ((Player) sender).getLocation().getBlock().getLocation();
				
				new TaskLater(60)
				{
					@Override
					public void run()
					{
						if(l.equals(((Player) sender).getLocation().getBlock().getLocation()))
						{
							((RTPController) parentController).randomTeleport((Player) sender);
						}
						
						else
						{
							sender.sendMessage(C.RED + "You Moved!");
						}
					}
				};
			}
			
			return true;
		}
		
		return false;
	}

	@Override
	public void onStart()
	{
		
	}

	@Override
	public void onStop()
	{
		
	}
}
