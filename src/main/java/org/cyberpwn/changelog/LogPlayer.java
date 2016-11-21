package org.cyberpwn.changelog;

import org.bukkit.entity.Player;
import org.phantomapi.clust.ConfigurableObject;
import org.phantomapi.clust.Keyed;
import org.phantomapi.clust.Tabled;

@Tabled("last_logs_players")
public class LogPlayer extends ConfigurableObject
{
	@Keyed("l")
	public long last = -1;
	
	public LogPlayer(Player player)
	{
		super(player.getUniqueId().toString());
	}
	
	public void view(long last)
	{
		this.last = last;
	}
	
	public long getSeen()
	{
		return last;
	}
}
