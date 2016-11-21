package org.cyberpwn.commune.frtp;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerQuitEvent;
import org.phantomapi.async.Callback;
import org.phantomapi.clust.Configurable;
import org.phantomapi.clust.DataCluster;
import org.phantomapi.construct.Controllable;
import org.phantomapi.construct.Controller;
import org.phantomapi.lang.GMap;
import org.phantomapi.lang.GTime;
import org.phantomapi.sync.Task;
import org.phantomapi.util.F;
import org.phantomapi.util.M;
import org.phantomapi.world.Area;
import org.phantomapi.world.Blocks;
import org.phantomapi.world.W;
import net.md_5.bungee.api.ChatColor;

public class RTPController extends Controller implements Configurable
{
	private DataCluster cc;
	private CommandController commandController;
	private GMap<Player, Long> last;
	
	public RTPController(Controllable parentController)
	{
		super(parentController);
		
		commandController = new CommandController(this);
		cc = new DataCluster();
		last = new GMap<Player, Long>();
		
		register(commandController);
		getPlugin().getCommand("rtp").setExecutor(commandController);
	}
	
	@Override
	public void onStart()
	{
		loadCluster(this);
	}
	
	public boolean isSafe(Player p)
	{
		if(last.containsKey(p))
		{
			return new GTime(M.ms() - last.get(p)).getSeconds() >= cc.getInt("basic.tp-cooldown");
		}
		
		return true;
	}
	
	public boolean isSafe(Player p, Chunk c)
	{
		if(Bukkit.getPluginManager().getPlugin("Factions") == null)
		{
			return true;
		}
		
		if(Blocks.canModify(p, c.getBlock(0, 0, 0)))
		{
			return true;
		}
		
		return false;
	}
	
	public boolean isSafe(Location l)
	{
		if(l.getBlock().getRelative(BlockFace.DOWN).getType().isSolid())
		{
			if(l.getBlock().getRelative(BlockFace.UP).getType().equals(Material.STATIONARY_WATER))
			{
				return false;
			}
			
			if(l.getBlock().getRelative(BlockFace.UP).getType().equals(Material.STATIONARY_LAVA))
			{
				return false;
			}
			
			if(l.getBlock().getRelative(BlockFace.UP).getType().equals(Material.LAVA))
			{
				return false;
			}
			
			if(l.getBlock().getType().equals(Material.STATIONARY_WATER))
			{
				return false;
			}
			
			if(l.getBlock().getType().equals(Material.STATIONARY_LAVA))
			{
				return false;
			}
			
			if(l.getBlock().getType().equals(Material.LAVA))
			{
				return false;
			}
			
			if(l.getBlock().getRelative(BlockFace.UP).getType().equals(Material.FIRE))
			{
				return false;
			}
			
			if(!l.getBlock().getRelative(BlockFace.UP).getType().equals(Material.AIR))
			{
				return false;
			}
			
			if(!l.getBlock().getType().equals(Material.AIR))
			{
				return false;
			}
			
			if(!l.getBlock().getRelative(BlockFace.UP).getRelative(BlockFace.UP).getType().equals(Material.AIR))
			{
				return false;
			}
			
			return true;
		}
		
		return false;
	}
	
	public Chunk randomChunk(World w)
	{
		return new Area(w.getSpawnLocation(), cc.getInt("worlds." + w.getName() + ".range")).random().getChunk();
	}
	
	public Location randomLocation(Chunk c)
	{
		int k = 250;
		
		if(c.getWorld().getName().endsWith("_nether"))
		{
			k -= 150;
		}
		
		return W.simulateFall(c.getBlock((int) (Math.random() * 15), k, (int) (Math.random() * 15)).getLocation());
	}
	
	public void findChunk(Player p, final World w, final Callback<Chunk> chunkCallback)
	{
		new Task(0)
		{
			@Override
			public void run()
			{
				long ns = M.ns();
				
				while(M.ns() - ns < 1000000 * cc.getDouble("basic.max-ms"))
				{
					Chunk c = randomChunk(w);
					
					if(isSafe(p, c))
					{
						chunkCallback.run(c);
						
						cancel();
						return;
					}
				}
			}
		};
	}
	
	public void findLocation(Player p, final World w, final Callback<Location> locationCallback)
	{
		new Task(0)
		{
			@Override
			public void run()
			{
				findChunk(p, w, new Callback<Chunk>()
				{
					@Override
					public void run()
					{
						Location l = randomLocation(get());
						
						if(isSafe(l))
						{
							l.getChunk().load();
							locationCallback.run(l);
							
							cancel();
							return;
						}
					}
				});
			}
		};
	}
	
	public void randomTeleport(final Player p)
	{
		if(!cc.getBoolean("worlds." + p.getWorld().getName() + ".enabled"))
		{
			p.sendMessage(ChatColor.RED + "You can't random teleport in this world.");
			return;
		}
		
		if(isSafe(p))
		{
			p.sendMessage(F.color(cc.getString("basic.message.searching")));
			
			last.put(p, M.ms());
			
			findLocation(p, p.getWorld(), new Callback<Location>()
			{
				@Override
				public void run()
				{
					p.teleport(get().clone().add(0.5, 0, 0.5));
					p.sendMessage(F.color(cc.getString("basic.message.teleported")));
				}
			});
		}
		
		else
		{
			String base = cc.getString("basic.message.cooldown");
			
			if(cc.getString("basic.message.cooldown").contains("%s%"))
			{
				base = base.replaceAll("%s%", String.valueOf(cc.getInt("basic.tp-cooldown") - new GTime(M.ms() - last.get(p)).getSeconds()));
			}
			
			p.sendMessage(F.color(base));
		}
	}
	
	@EventHandler
	public void on(PlayerQuitEvent e)
	{
		if(last.containsKey(e.getPlayer()))
		{
			if(new GTime(M.ms() - last.get(e.getPlayer())).getSeconds() >= cc.getInt("basic.tp-cooldown"))
			{
				last.remove(e.getPlayer());
			}
		}
	}
	
	@Override
	public void onNewConfig()
	{
		cc.set("basic.tp-cooldown", 15);
		cc.set("basic.max-ms", 0.2);
		cc.set("basic.message.searching", "&6Please Wait...");
		cc.set("basic.message.teleported", "&aBam!");
		cc.set("basic.message.cooldown", "&cPlease Wait %s% seconds.");
		
		for(World i : Bukkit.getWorlds())
		{
			cc.set("worlds." + i.getName() + ".enabled", false);
			cc.set("worlds." + i.getName() + ".range", 8192);
		}
	}
	
	@Override
	public void onReadConfig()
	{
		
	}
	
	@Override
	public DataCluster getConfiguration()
	{
		return cc;
	}
	
	@Override
	public String getCodeName()
	{
		return "frtp";
	}
	
	@Override
	public void onStop()
	{
		
	}
}
