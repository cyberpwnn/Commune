package org.cyberpwn.commune;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.phantomapi.clust.Comment;
import org.phantomapi.clust.ConfigurableController;
import org.phantomapi.clust.Keyed;
import org.phantomapi.construct.Controllable;
import org.phantomapi.lang.GMap;
import org.phantomapi.sync.TaskLater;
import org.phantomapi.util.F;
import org.phantomapi.util.FinalInteger;

public class DropperController extends ConfigurableController
{
	@Comment("Enabling this will force players to double drop")
	@Keyed("enabled")
	public boolean enabled = false;
	
	@Comment("How long to wait before considered not a double tap. (ticks)")
	@Keyed("reset-threshold")
	public int resetDelay = 20;
	
	@Comment("Sends to players when then only drop once and need to drop agin")
	@Keyed("drop-message")
	public String doubleMessage = "&cDrop again to drop";
	
	private GMap<Player, Integer> dropQueue;
	
	public DropperController(Controllable parentController)
	{
		super(parentController, "double-drop");
		
		dropQueue = new GMap<Player, Integer>();
	}
	
	@Override
	public void onStart()
	{
		loadCluster(this);
	}
	
	@Override
	public void onStop()
	{
		
	}
	
	@EventHandler
	public void on(PlayerDropItemEvent e)
	{
		if(!enabled)
		{
			return;
		}
		
		if(e.getPlayer().isSneaking())
		{
			dropQueue.remove(e.getPlayer());
			return;
		}
		
		if(dropQueue.containsKey(e.getPlayer()) && dropQueue.get(e.getPlayer()) == e.getPlayer().getInventory().getHeldItemSlot())
		{
			dropQueue.remove(e.getPlayer());
		}
		
		else
		{
			dropQueue.put(e.getPlayer(), e.getPlayer().getInventory().getHeldItemSlot());
			e.getPlayer().sendMessage(F.color(doubleMessage));
			e.setCancelled(true);
			FinalInteger fi = new FinalInteger(e.getPlayer().getInventory().getHeldItemSlot());
			
			new TaskLater(resetDelay)
			{
				@Override
				public void run()
				{
					if(dropQueue.containsKey(e.getPlayer()) && dropQueue.get(e.getPlayer()) == fi.get())
					{
						dropQueue.remove(e.getPlayer());
					}
				}
			};
		}
	}
	
	@EventHandler
	public void on(PlayerQuitEvent e)
	{
		dropQueue.remove(e.getPlayer());
	}
}
