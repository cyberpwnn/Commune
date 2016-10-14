package org.cyberpwn.commune.frtp;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.phantomapi.construct.Controllable;
import org.phantomapi.construct.Controller;

public class CommandController extends Controller implements CommandExecutor
{
	public CommandController(Controllable parentController)
	{
		super(parentController);
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command c, String cmd, String[] args)
	{
		if(c.getName().equalsIgnoreCase("frtp"))
		{
			if(sender instanceof Player)
			{
				((RTPController) parentController).randomTeleport((Player) sender);
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
