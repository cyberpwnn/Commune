package org.cyberpwn.commune;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.phantomapi.clust.Configurable;
import org.phantomapi.clust.DataCluster;
import org.phantomapi.construct.Controllable;
import org.phantomapi.construct.Controller;
import org.phantomapi.construct.Ticked;
import org.phantomapi.lang.GList;
import org.phantomapi.lang.GMap;
import org.phantomapi.lang.GTime;
import org.phantomapi.sync.ExecutiveIterator;
import org.phantomapi.sync.ExecutiveTask;
import org.phantomapi.util.F;
import org.phantomapi.util.M;
import org.phantomapi.world.Area;

@SuppressWarnings("deprecation")
@Ticked(200)
public class AFKController extends Controller implements Configurable
{
	private GMap<Player, Long> lastActivity;
	private GList<Player> afk;
	private DataCluster cc;
	
	public AFKController(Controllable parentController)
	{
		super(parentController);
		
		lastActivity = new GMap<Player, Long>();
		afk = new GList<Player>();
		cc = new DataCluster();
	}
	
	@Override
	public void onTick()
	{
		new ExecutiveTask<Player>(new ExecutiveIterator<Player>(lastActivity.k())
		{
			@Override
			public void onIterate(Player next)
			{
				if(next.hasPermission("afk.immune"))
				{
					afk.remove(next);
					
					return;
				}
				
				long minutesAfk = new GTime(M.ms() - lastActivity.get(next)).getMinutes();
				
				if(minutesAfk >= cc.getInt("afk.timer-minutes") && !afk.contains(next))
				{
					afk.add(next);
					
					if(cc.getBoolean("afk.announce.radius-enabled"))
					{
						Area a = new Area(next.getLocation(), cc.getInt("afk.announce.radius-size"));
						
						for(Player i : a.getNearbyPlayers())
						{
							i.sendMessage(F.color(cc.getString("afk.message.afk").replaceAll("%player%", next.getName())));
						}
					}
					
					else
					{
						for(Player i : getPlugin().getServer().getOnlinePlayers())
						{
							i.sendMessage(F.color(cc.getString("afk.message.afk").replaceAll("%player%", next.getName())));
						}
					}
				}
				
				if(minutesAfk > cc.getInt("afk.kick-minutes") && cc.getInt("afk.kick-minutes") > 0)
				{
					if(!next.hasPermission(cc.getString("afk.permissions.afk-no-kick")))
					{
						next.kickPlayer(F.color(cc.getString("afk.kick-message")));
					}
				}
			}
			
		}, 0.05, 0, new Runnable()
		{
			@Override
			public void run()
			{
				
			}
		});
	}
	
	@Override
	public void onStart()
	{
		loadCluster(this);
		
		for(Player i : getPlugin().getServer().getOnlinePlayers())
		{
			act(i);
		}
	}
	
	@EventHandler(ignoreCancelled = true)
	public void on(PlayerChatEvent e)
	{
		if(cc.getBoolean("active.chat"))
		{
			act(e.getPlayer());
		}
	}
	
	@EventHandler
	public void on(PlayerJoinEvent e)
	{
		lastActivity.put(e.getPlayer(), M.ms());
	}
	
	@EventHandler
	public void on(PlayerQuitEvent e)
	{
		lastActivity.remove(e.getPlayer());
		afk.remove(e.getPlayer());
	}
	
	@EventHandler
	public void on(PlayerDeathEvent e)
	{
		if(cc.getBoolean("active.combat"))
		{
			act(e.getEntity());
			act(e.getEntity().getKiller());
		}
	}
	
	@EventHandler(ignoreCancelled = true)
	public void on(PlayerCommandPreprocessEvent e)
	{
		if(e.getMessage().equals("/" + cc.getString("afk.command.name")) && cc.getBoolean("afk.command.enabled"))
		{
			if(!afk.contains(e.getPlayer()))
			{
				afk.add(e.getPlayer());
				
				Player p = e.getPlayer();
				
				if(cc.getBoolean("afk.announce.radius-enabled"))
				{
					Area a = new Area(p.getLocation(), cc.getInt("afk.announce.radius-size"));
					
					for(Player i : a.getNearbyPlayers())
					{
						i.sendMessage(F.color(cc.getString("afk.message.afk").replaceAll("%player%", p.getName())));
					}
				}
				
				else
				{
					for(Player i : getPlugin().getServer().getOnlinePlayers())
					{
						i.sendMessage(F.color(cc.getString("afk.message.afk").replaceAll("%player%", p.getName())));
					}
				}
			}
			
			e.setCancelled(true);
		}
		
		else if(cc.getBoolean("active.chat"))
		{
			act(e.getPlayer());
		}
	}
	
	@EventHandler
	public void on(PlayerMoveEvent e)
	{
		if(cc.getBoolean("active.move") && !e.getFrom().getBlock().equals(e.getTo().getBlock()))
		{
			act(e.getPlayer());
		}
	}
	
	@EventHandler
	public void on(BlockBreakEvent e)
	{
		if(cc.getBoolean("active.block-modify"))
		{
			act(e.getPlayer());
		}
	}
	
	@EventHandler
	public void on(BlockPlaceEvent e)
	{
		if(cc.getBoolean("active.block-modify"))
		{
			act(e.getPlayer());
		}
	}
	
	public void act(Player p)
	{
		if(p == null)
		{
			return;
		}
		
		if(p.hasPermission("afk.immune"))
		{
			afk.remove(p);
			
			return;
		}
		
		lastActivity.put(p, M.ms());
		
		if(afk.contains(p))
		{
			if(cc.getBoolean("afk.announce.radius-enabled"))
			{
				Area a = new Area(p.getLocation(), cc.getInt("afk.announce.radius-size"));
				
				for(Player i : a.getNearbyPlayers())
				{
					i.sendMessage(F.color(cc.getString("afk.message.not-afk").replaceAll("%player%", p.getName())));
				}
			}
			
			else
			{
				for(Player i : getPlugin().getServer().getOnlinePlayers())
				{
					i.sendMessage(F.color(cc.getString("afk.message.not-afk").replaceAll("%player%", p.getName())));
				}
			}
			
			afk.remove(p);
		}
	}
	
	@Override
	public void onNewConfig()
	{
		cc.set("active.block-modify", true, "Update Status from Block modifications");
		cc.set("active.move", true, "Update Status from Movement");
		cc.set("active.chat", true, "Update Status from Chat or Commands");
		cc.set("active.combat", true, "Update Status from Combat");
		cc.set("afk.timer-minutes", 10, "Timer to define someone as afk.");
		cc.set("afk.kick-minutes", -1, "Timer to kick afk players\nThis is the total time, so this this AT or higher than the afk timer\nSet to -1 to disable this");
		cc.set("afk.command.name", "afk", "AFK Command");
		cc.set("afk.command.enabled", true, "AFK Command enabled?");
		cc.set("afk.permissions.afk-immunity", "afk.immune", "The permission to make someone immune from afk");
		cc.set("afk.permissions.afk-no-kick", "afk.kick-immune", "The permission to make someone immune from afk kicks, if kicking isnt disabled");
		cc.set("afk.message.afk", "&a~ &b%player% &ais now afk.", "The AFK message");
		cc.set("afk.message.not-afk", "&a~ &b%player% &ais no longer afk.", "The no longer AFK message");
		cc.set("afk.kick-message", "&cAFK", "AFK Kick message");
		cc.set("afk.announce.radius-enabled", true, "Radius messaging enabled?");
		cc.set("afk.announce.radius-size", 64, "Radius range in blocks");
	}
	
	@Override
	public void onReadConfig()
	{
		// Dynamic
	}
	
	@Override
	public DataCluster getConfiguration()
	{
		return cc;
	}
	
	@Override
	public String getCodeName()
	{
		return "afk";
	}
	
	@Override
	public void onStop()
	{
		
	}
}
