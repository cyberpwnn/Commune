package org.cyberpwn.commune;

import java.util.List;
import java.util.UUID;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.phantomapi.block.BlockHandler;

public class ASkyblockBlockHandler implements BlockHandler
{
	@Override
	public boolean canModify(Player p, Block block)
	{
		Location l = block.getLocation();
		
		try
		{
			Object api = Class.forName("com.wasteofplastic.askyblock.ASkyBlockAPI").getMethod("getInstance").invoke(null);
			
			if((boolean) api.getClass().getMethod("islandAtLocation", Location.class).invoke(api, l))
			{
				Object is = api.getClass().getMethod("getIslandAt", Location.class).invoke(api, l);
				@SuppressWarnings("unchecked")
				List<UUID> ids = (List<UUID>) is.getClass().getMethod("getMembers").invoke(is);
				
				if(!ids.contains(p.getUniqueId()))
				{
					return false;
				}
			}
		}
		
		catch(Exception e)
		{
			
		}
		
		return true;
	}
	
	@Override
	public boolean hasProtection(Block block)
	{
		try
		{
			Location l = block.getLocation();
			
			Object api = Class.forName("com.wasteofplastic.askyblock.ASkyBlockAPI").getMethod("getInstance").invoke(null);
			return (boolean) api.getClass().getMethod("islandAtLocation", Location.class).invoke(api, l);
		}
		
		catch(Exception e)
		{
			
		}
		
		return false;
	}
	
	@Override
	public String getProtector()
	{
		return "SkyBlock";
	}
	
	@Override
	public String getProtector(Block block)
	{
		try
		{
			Location l = block.getLocation();
			Object api = Class.forName("com.wasteofplastic.askyblock.ASkyBlockAPI").getMethod("getInstance").invoke(null);
			
			if((boolean) api.getClass().getMethod("islandAtLocation", Location.class).invoke(api, l))
			{
				Object is = api.getClass().getMethod("getIslandAt", Location.class).invoke(api, l);
				
				return is.getClass().getMethod("getOwner").invoke(is).toString();
			}
		}
		catch(Exception e)
		{
			
		}
		
		return "";
	}
}
