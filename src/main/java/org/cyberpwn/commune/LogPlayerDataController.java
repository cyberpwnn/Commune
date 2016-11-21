package org.cyberpwn.commune;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerQuitEvent;
import org.cyberpwn.changelog.LogPlayer;
import org.phantomapi.clust.DataController;
import org.phantomapi.construct.Controllable;

public class LogPlayerDataController extends DataController<LogPlayer, Player>
{
	public LogPlayerDataController(Controllable parentController)
	{
		super(parentController);
	}
	
	@Override
	public LogPlayer onLoad(Player p)
	{
		LogPlayer l = new LogPlayer(p);
		loadMysql(l);
		return l;
	}
	
	@Override
	public void onSave(Player p)
	{
		LogPlayer l = get(p);
		saveMysql(l);
	}
	
	@Override
	public void onStart()
	{
		
	}
	
	@Override
	public void onStop()
	{
		saveAll();
	}
	
	@EventHandler
	public void on(PlayerQuitEvent e)
	{
		save(e.getPlayer());
	}
	
}
