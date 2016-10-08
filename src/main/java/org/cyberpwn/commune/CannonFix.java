package org.cyberpwn.commune;

import java.util.Iterator;
import org.bukkit.Chunk;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.phantomapi.clust.Comment;
import org.phantomapi.clust.ConfigurableController;
import org.phantomapi.clust.Keyed;
import org.phantomapi.construct.Controllable;
import org.phantomapi.construct.Ticked;
import org.phantomapi.lang.GList;
import org.phantomapi.lang.GMap;

@Ticked(20)
public class CannonFix extends ConfigurableController
{
	@Keyed("cooldown-seconds")
	@Comment("The time to cool down in seconds")
	public int cooldownSeconds = 10;
	
	@Keyed("ignored-worlds")
	@Comment("Add worlds here that should be ignored")
	public GList<String> ignoredWorlds = new GList<String>().qadd("shitty_world");
	
	@Keyed("enabled")
	@Comment("Should cannon fixes be enabled?")
	public boolean enabled = false;
	
	private GMap<Chunk, Integer> chunked;
	private final BlockFace[] faces = new BlockFace[] { BlockFace.SELF, BlockFace.UP, BlockFace.DOWN, BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST };
	
	public CannonFix(Controllable parentController)
	{
		super(parentController, "cannon-tweaks");
		
		chunked = new GMap<Chunk, Integer>();
	}
	
	public void onStart()
	{
		loadCluster(this);
	}
	
	public void onStop()
	{
		
	}
	
	public void onTick()
	{
		if(!enabled)
		{
			return;
		}
		
		Iterator<Chunk> it = chunked.k().iterator();
		
		while(it.hasNext())
		{
			Chunk i = it.next();
			chunked.put(i, chunked.get(i) - 1);
			
			if(chunked.get(i) < 0)
			{
				chunked.remove(i);
			}
		}
	}
	
	public void target(Chunk chunk)
	{
		if(!enabled)
		{
			return;
		}
		
		pop(chunk);
		pop(chunk.getWorld().getChunkAt(chunk.getX() + 1, chunk.getZ()));
		pop(chunk.getWorld().getChunkAt(chunk.getX() - 1, chunk.getZ()));
		pop(chunk.getWorld().getChunkAt(chunk.getX(), chunk.getZ() + 1));
		pop(chunk.getWorld().getChunkAt(chunk.getX(), chunk.getZ() - 1));
		pop(chunk.getWorld().getChunkAt(chunk.getX() + 1, chunk.getZ() + 1));
		pop(chunk.getWorld().getChunkAt(chunk.getX() - 1, chunk.getZ() - 1));
		pop(chunk.getWorld().getChunkAt(chunk.getX() + 1, chunk.getZ() - 1));
		pop(chunk.getWorld().getChunkAt(chunk.getX() - 1, chunk.getZ() + 1));
	}
	
	public void pop(Chunk chunk)
	{
		if(!enabled)
		{
			return;
		}
		
		chunked.put(chunk, cooldownSeconds);
	}
	
	public boolean coolingDown(Block block)
	{
		if(!enabled)
		{
			return false;
		}
		
		return chunked.containsKey(block.getChunk());
	}
	
	@EventHandler
	public void on(EntityExplodeEvent e)
	{
		if(!enabled)
		{
			return;
		}
		
		if(e.getEntityType().equals(EntityType.PRIMED_TNT))
		{
			target(e.getLocation().getChunk());
		}
	}
	
	@EventHandler
	public void on(BlockFromToEvent e)
	{
		if(!enabled)
		{
			return;
		}
		
		@SuppressWarnings("deprecation")
		int id = e.getBlock().getTypeId();
		Block b = e.getToBlock();
		
		if(generatesCobble(id, b) && coolingDown(b))
		{
			e.setCancelled(true);
		}
	}
	
	@SuppressWarnings("deprecation")
	public boolean generatesCobble(int id, Block b)
	{
		if(!enabled)
		{
			return false;
		}
		
		int mirrorID1 = (id == 8 || id == 9 ? 10 : 8);
		int mirrorID2 = (id == 8 || id == 9 ? 11 : 9);
		
		for(BlockFace face : faces)
		{
			Block r = b.getRelative(face, 1);
			
			if(r.getTypeId() == mirrorID1 || r.getTypeId() == mirrorID2)
			{
				return true;
			}
		}
		
		return false;
	}
}