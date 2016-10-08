package org.cyberpwn.commune;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

public class ImageMapCommand implements TabExecutor
{
	private ImageMaps plugin;
	
	public ImageMapCommand(ImageMaps plugin)
	{
		this.plugin = plugin;
	}
	
	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args)
	{
		switch(args.length)
		{
			case 1:
				return getMatches(args[0], new File(plugin.pl.getDataFolder(), "images").list());
			default:
				return null;
		}
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
	{
		if(!sender.hasPermission("papyrus.use") || !(sender instanceof Player))
		{
			return true;
		}
		
		if(args.length != 1)
		{
			sender.sendMessage("/pap <file.name>");
			return false;
		}
		
		if(args.length >= 2 && args[1].equalsIgnoreCase("reload"))
		{
			plugin.reloadImage(args[0]);
			sender.sendMessage("Image " + args[0] + " reloaded!");
			return true;
		}
		
		boolean fastsend = args.length >= 2 ? Boolean.parseBoolean(args[1]) : false;
		
		plugin.startPlacing((Player) sender, args[0], fastsend);
		
		sender.sendMessage("Placing " + args[0] + ". Right click a block (upper left corner)");
		
		return true;
	}
	
	/**
	 * Get all values of a String array which start with a given String
	 * 
	 * @param value
	 *            the given String
	 * @param list
	 *            the array
	 * @return a List of all matches
	 */
	public static List<String> getMatches(String value, String[] list)
	{
		List<String> result = new LinkedList<String>();
		
		for(String str : list)
			if(str.startsWith(value))
				result.add(str);
			
		return result;
	}
	
}
