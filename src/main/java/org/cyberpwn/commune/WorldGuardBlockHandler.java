package org.cyberpwn.commune;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.phantomapi.block.BlockHandler;
import org.phantomapi.lang.GList;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

public class WorldGuardBlockHandler implements BlockHandler
{
	@Override
	public boolean canModify(Player p, Block block)
	{
		ApplicableRegionSet rm = WorldGuardPlugin.inst().getRegionManager(block.getWorld()).getApplicableRegions(new com.sk89q.worldedit.Vector(block.getX(), block.getY(), block.getZ()));
		
		return rm.getRegions().isEmpty();
	}
	
	@Override
	public boolean hasProtection(Block block)
	{
		ApplicableRegionSet rm = WorldGuardPlugin.inst().getRegionManager(block.getWorld()).getApplicableRegions(new com.sk89q.worldedit.Vector(block.getX(), block.getY(), block.getZ()));
		
		return !rm.getRegions().isEmpty();
	}
	
	@Override
	public String getProtector()
	{
		return "WorldGuard";
	}
	
	@Override
	public String getProtector(Block block)
	{
		ApplicableRegionSet rm = WorldGuardPlugin.inst().getRegionManager(block.getWorld()).getApplicableRegions(new com.sk89q.worldedit.Vector(block.getX(), block.getY(), block.getZ()));
		GList<String> m = new GList<String>();
		
		for(ProtectedRegion i : rm.getRegions())
		{
			m.add(i.getId());
		}
		
		if(m.isEmpty())
		{
			return null;
		}
		
		return m.toString(", ");
	}
}
