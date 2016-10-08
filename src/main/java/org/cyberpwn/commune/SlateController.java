package org.cyberpwn.commune;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.phantomapi.Phantom;
import org.phantomapi.clust.Comment;
import org.phantomapi.clust.Configurable;
import org.phantomapi.clust.DataCluster;
import org.phantomapi.clust.Keyed;
import org.phantomapi.construct.Controllable;
import org.phantomapi.construct.Controller;
import org.phantomapi.construct.Ticked;
import org.phantomapi.core.SyncStart;
import org.phantomapi.lang.GList;
import org.phantomapi.slate.PlaceholderSlate;
import org.phantomapi.slate.Slate;
import org.phantomapi.util.F;

@SyncStart
@Ticked(10)
public class SlateController extends Controller implements Configurable
{
	private DataCluster cc;
	private Slate slate;
	
	@Comment("Color formatted slate name")
	@Keyed("slate.name")
	public String slateName = "&dSlate";
	
	@Comment("Color formatted, Placeholdered text")
	@Keyed("slate.text")
	public GList<String> slateText = new GList<String>().qadd("&a%react_sample_ticks_per_second_formatted%");
	
	public SlateController(Controllable parentController)
	{
		super(parentController);
		
		cc = new DataCluster();
		slate = null;
	}
	
	@Override
	public void onStart()
	{
		slate = new PlaceholderSlate(slateName);
		
		loadCluster(SlateController.this);
		
		for(Player i : Phantom.instance().onlinePlayers())
		{
			slate.addViewer(i);
		}
		
		slate.update();
	}
	
	public void onTick()
	{
		slate.update();
	}
	
	@Override
	public void onStop()
	{
		
	}
	
	@Override
	public void onNewConfig()
	{
		
	}
	
	@Override
	public void onReadConfig()
	{
		slateName = F.color(slateName);
		
		slate.setName(slateName);
		slate.setLines(slateText);
	}
	
	@Override
	public DataCluster getConfiguration()
	{
		return cc;
	}
	
	@Override
	public String getCodeName()
	{
		return "slate";
	}
	
	public void board(Player p, boolean b)
	{
		if(b)
		{
			slate.addViewer(p);
			p.sendMessage(F.color("&8&l(&a&l!&8&l) &aBoard enabled"));
		}
		
		else
		{
			slate.removeViewer(p);
			p.sendMessage(F.color("&8&l(&c&l!&8&l) &cBoard disabled"));
		}
	}
	
	@EventHandler
	public void on(PlayerJoinEvent e)
	{
		slate.addViewer(e.getPlayer());
	}
	
	@EventHandler
	public void on(PlayerQuitEvent e)
	{
		slate.removeViewer(e.getPlayer());
	}
}