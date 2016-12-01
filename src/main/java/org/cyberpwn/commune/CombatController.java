package org.cyberpwn.commune;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.ServerCommandEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;
import org.phantomapi.clust.Comment;
import org.phantomapi.clust.ConfigurableController;
import org.phantomapi.clust.Keyed;
import org.phantomapi.construct.Controllable;
import org.phantomapi.construct.Ticked;
import org.phantomapi.event.PlayerArrowDamagePlayerEvent;
import org.phantomapi.event.PlayerDamagePlayerEvent;
import org.phantomapi.lang.GList;
import org.phantomapi.lang.GMap;
import org.phantomapi.nms.NMSX;
import org.phantomapi.spawner.PhantomSpawner;
import org.phantomapi.sync.TaskLater;
import org.phantomapi.util.C;
import org.phantomapi.util.F;
import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.craftbukkit.SetExpFix;
import de.dustplanet.util.SilkUtil;
import me.libraryaddict.disguise.DisguiseAPI;

@Ticked(0)
public class CombatController extends ConfigurableController
{
	@Keyed("tag.duration")
	@Comment("The combat tag duration in seconds")
	public int tagSeconds = 15;
	
	@Keyed("tag.command-whitelist")
	@Comment("Allow these commands while tagged")
	public GList<String> whitelist = new GList<String>().qadd("/help");
	
	@Comment("Enable combat tag?")
	@Keyed("tag.enabled")
	public boolean enabled = false;
	
	private boolean stopping;
	private GMap<Player, Integer> tags;
	
	private SilkUtil s;
	
	public CombatController(Controllable parentController, String codeName)
	{
		super(parentController, codeName);
		
		stopping = false;
		s = SilkUtil.hookIntoSilkSpanwers();
		tags = new GMap<Player, Integer>();
	}
	
	@Override
	public void onTick()
	{
		if(!enabled)
		{
			return;
		}
		
		for(Player i : tags.k())
		{
			tags.put(i, tags.get(i) - 1);
			
			if(tags.get(i) <= 0)
			{
				NMSX.sendActionBar(i, C.GREEN + "Out of Combat");
				tags.remove(i);
			}
			
			else
			{
				i.removePotionEffect(PotionEffectType.INVISIBILITY);
				i.setFlying(false);
				i.setAllowFlight(false);
				NMSX.sendActionBar(i, C.RED + "Combat Tagged <> " + F.f((double) (getTimeLeft(i)) / 20.0, 1) + "s");
			}
		}
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
	
	public void tag(Player p)
	{
		if(!enabled)
		{
			return;
		}
		
		((Essentials) Bukkit.getPluginManager().getPlugin("Essentials")).getUser(p).setGodModeEnabled(false);
		DisguiseAPI.undisguiseToAll(p);
		p.setFlying(false);
		p.setAllowFlight(false);
		tags.put(p, tagSeconds * 20);
	}
	
	public int getTimeLeft(Player p)
	{
		if(isTagged(p))
		{
			return tags.get(p);
		}
		
		return -1;
	}
	
	public boolean isTagged(Player p)
	{
		return tags.containsKey(p);
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void on(ServerCommandEvent e)
	{
		if(!enabled)
		{
			return;
		}
		
		if(e.getCommand().equalsIgnoreCase("stop"))
		{
			stopping = true;
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void on(PlayerCommandPreprocessEvent e)
	{
		if(!enabled)
		{
			return;
		}
		
		if(isTagged(e.getPlayer()))
		{
			for(String i : whitelist)
			{
				if(e.getMessage().toLowerCase().startsWith(i.toLowerCase()))
				{
					return;
				}
			}
			
			e.setCancelled(true);
			e.getPlayer().sendMessage(F.color("&8&l(&4&l!&8&l)&c You are Combat Tagged. Please wait " + F.f((double) (getTimeLeft(e.getPlayer())) / 20.0, 1) + "s"));
		}
	}
	
	@EventHandler
	public void on(PlayerDamagePlayerEvent e)
	{
		if(e.isCancelled())
		{
			return;
		}
		
		if(e.getDamager().equals(e.getPlayer()))
		{
			e.setCancelled(true);
			return;
		}
		
		if(!enabled)
		{
			return;
		}
		
		tag(e.getPlayer());
		tag(e.getDamager());
	}
	
	@SuppressWarnings("deprecation")
	public ItemStack createSpawner(short id, int amt)
	{
		return s.newSpawnerItem(id, C.YELLOW + s.getCreatureName(id) + " " + C.WHITE + "Spawner", amt);
	}
	
	@EventHandler
	public void on(BlockPlaceEvent e)
	{
		try
		{
			if(e.getItemInHand().getType().equals(Material.MOB_SPAWNER))
			{
				String name = e.getItemInHand().getItemMeta().getDisplayName();
				GList<String> n = new GList<String>(C.stripColor(name).toUpperCase().split(" "));
				n.remove(n.last());
				String k = n.toString("_");
				EntityType et = EntityType.valueOf(k);
				
				new TaskLater()
				{
					@Override
					public void run()
					{
						PhantomSpawner ps = new PhantomSpawner(e.getBlock());
						ps.setType(et);
					}
				};
			}
		}
		
		catch(Exception ex)
		{
			
		}
	}
	
	@EventHandler
	public void on(PlayerArrowDamagePlayerEvent e)
	{
		if(e.isCancelled())
		{
			return;
		}
		
		if(e.getDamager().equals(e.getPlayer()))
		{
			e.setCancelled(true);
			return;
		}
		
		if(!enabled)
		{
			return;
		}
		
		tag(e.getPlayer());
		tag(e.getDamager());
	}
	
	@EventHandler
	public void on(PlayerDeathEvent e)
	{
		if(!enabled)
		{
			return;
		}
		
		tags.remove(e.getEntity());
	}
	
	@EventHandler
	public void on(PlayerQuitEvent e)
	{
		if(!enabled)
		{
			return;
		}
		
		GList<ItemStack> isx = new GList<ItemStack>();
		
		if(isTagged(e.getPlayer()) && !stopping)
		{
			isx.add(e.getPlayer().getInventory().getContents());
			isx.add(e.getPlayer().getInventory().getArmorContents());
			e.getPlayer().getInventory().clear();
			e.getPlayer().getInventory().setHelmet(new ItemStack(Material.AIR));
			e.getPlayer().getInventory().setChestplate(new ItemStack(Material.AIR));
			e.getPlayer().getInventory().setBoots(new ItemStack(Material.AIR));
			e.getPlayer().getInventory().setLeggings(new ItemStack(Material.AIR));
			int xp = SetExpFix.getTotalExperience(e.getPlayer());
			((ExperienceOrb) e.getPlayer().getWorld().spawn(e.getPlayer().getLocation(), ExperienceOrb.class)).setExperience(xp);
			SetExpFix.setTotalExperience(e.getPlayer(), 0);
			
			for(ItemStack i : isx)
			{
				try
				{
					e.getPlayer().getLocation().getWorld().dropItemNaturally(e.getPlayer().getLocation(), i);
				}
				
				catch(Exception ex)
				{
					
				}
			}
		}
	}
}
