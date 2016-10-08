package org.cyberpwn.commune;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class FastSendTask extends BukkitRunnable implements Listener
{
	private Map<UUID, Queue<Short>> status = new HashMap<UUID, Queue<Short>>();
	private final ImageMaps plugin;
	private final int mapsPerRun;
	
	public FastSendTask(ImageMaps plugin, int mapsPerSend)
	{
		this.plugin = plugin;
		this.mapsPerRun = mapsPerSend;
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public void run()
	{
		if(plugin.getFastSendList().isEmpty())
			return;
		
		for(Player p : plugin.pl.getServer().getOnlinePlayers())
		{
			Queue<Short> state = getStatus(p);
			
			for(int i = 0; i < mapsPerRun && !state.isEmpty(); i++)
				p.sendMap(plugin.pl.getServer().getMap(state.poll()));
		}
	}
	
	private Queue<Short> getStatus(Player p)
	{
		if(!status.containsKey(p.getUniqueId()))
			status.put(p.getUniqueId(), new LinkedList<Short>(plugin.getFastSendList()));
		
		return status.get(p.getUniqueId());
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerJoin(PlayerJoinEvent e)
	{
		status.put(e.getPlayer().getUniqueId(), new LinkedList<Short>(plugin.getFastSendList()));
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerQuit(PlayerQuitEvent e)
	{
		status.remove(e.getPlayer().getUniqueId());
	}
	
	public void addToQueue(short mapId)
	{
		for(Queue<Short> queue : status.values())
			queue.add(mapId);
	}
	
}
