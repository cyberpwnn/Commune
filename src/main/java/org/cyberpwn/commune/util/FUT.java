package org.cyberpwn.commune.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.phantomapi.lang.GList;

public class FUT
{
	public static Object getFaction(Location l)
	{
		try
		{
			Class<?> b = Class.forName("com.massivecraft.factions.Board");
			Class<?> fl = Class.forName("com.massivecraft.factions.FLocation");
			
			Object flocation = fl.getConstructor(Location.class).newInstance(l);
			Object board = b.getMethod("getInstance").invoke(null);
			Object faction = b.getMethod("getFactionAt", fl).invoke(board, flocation);
			
			return faction;
		}
		
		catch(Exception e)
		{
			
		}
		
		return null;
	}
	
	public static Object getFaction(Player p)
	{
		try
		{
			Class<?> fp = Class.forName("com.massivecraft.factions.FPlayers");
			Object fpl = fp.getMethod("getInstance").invoke(null);
			Object player = fpl.getClass().getMethod("getByPlayer", Player.class).invoke(fpl, p);
			
			return player.getClass().getMethod("getFaction").invoke(player);
		}
		
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		return null;
	}
	
	public static String getName(Object f)
	{
		try
		{
			return (String) f.getClass().getMethod("getTag").invoke(f);
		}
		
		catch(Exception e)
		{
			return null;
		}
	}
	
	public static List<?> getFactions()
	{
		try
		{
			Class<?> fa = Class.forName("com.massivecraft.factions.Factions");
			Object ins = fa.getMethod("getInstance").invoke(null);
			ArrayList<?> fas = (ArrayList<?>) ins.getClass().getMethod("getAllFactions").invoke(ins);
			
			return fas;
		}
		
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		return null;
	}
	
	public static GList<Chunk> getFactionClaims(Player p)
	{
		return getFactionClaims(getFaction(p));
	}
	
	public static boolean hasFaction(Player p)
	{
		Object faction = getFaction(p);
		
		try
		{
			if(!((boolean) faction.getClass().getMethod("isWilderness").invoke(faction)))
			{
				return true;
			}
		}
		
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		return false;
	}
	
	public static boolean isFaction(Object faction)
	{
		try
		{
			if(!((boolean) faction.getClass().getMethod("isWilderness").invoke(faction)) && !((boolean) faction.getClass().getMethod("isSafeZone").invoke(faction)) && !((boolean) faction.getClass().getMethod("isWarZone").invoke(faction)))
			{
				return true;
			}
		}
		
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		return false;
	}
	
	public static GList<Chunk> getFactionClaims(Object faction)
	{
		GList<Chunk> chunks = new GList<Chunk>();
		
		try
		{
			Set<?> claims = (Set<?>) faction.getClass().getMethod("getAllClaims").invoke(faction);
			
			for(Object i : claims)
			{
				Long x = (Long) i.getClass().getMethod("getX").invoke(i);
				Long z = (Long) i.getClass().getMethod("getZ").invoke(i);
				World w = (World) i.getClass().getMethod("getWorld").invoke(i);
				chunks.add(w.getChunkAt(x.intValue(), z.intValue()));
			}
		}
		
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		return chunks;
	}
	
	public static boolean isRegioned(Location l)
	{
		try
		{
			Class<?> fa = Class.forName("com.massivecraft.factions.Faction");
			Object fac = getFaction(l);
			
			if(fac != null && !((boolean) fa.getMethod("isWilderness").invoke(fac)))
			{
				return true;
			}
		}
		
		catch(Exception e)
		{
			
		}
		
		return false;
	}
	
	public static boolean isWarZone(Location l)
	{
		try
		{
			Class<?> fa = Class.forName("com.massivecraft.factions.Faction");
			Object fac = getFaction(l);
			
			if(fac != null && ((boolean) fa.getMethod("isWarZone").invoke(fac)))
			{
				return true;
			}
		}
		
		catch(Exception e)
		{
			
		}
		
		return false;
	}
	
	public static boolean isWild(Location l)
	{
		try
		{
			Class<?> fa = Class.forName("com.massivecraft.factions.Faction");
			Object fac = getFaction(l);
			
			if(fac != null && ((boolean) fa.getMethod("isWilderness").invoke(fac)))
			{
				return true;
			}
		}
		
		catch(Exception e)
		{
			
		}
		
		return false;
	}
	
	public static boolean isSafeZone(Location l)
	{
		try
		{
			Class<?> fa = Class.forName("com.massivecraft.factions.Faction");
			Object fac = getFaction(l);
			
			if(fac != null && ((boolean) fa.getMethod("isSafeZone").invoke(fac)))
			{
				return true;
			}
		}
		
		catch(Exception e)
		{
			
		}
		
		return false;
	}
}
