package org.cyberpwn.commune;

import java.io.File;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.cyberpwn.commune.object.ServerSelector;
import org.phantomapi.construct.Controllable;
import org.phantomapi.construct.Controller;
import org.phantomapi.event.PlayerDamagePlayerEvent;
import org.phantomapi.lang.GList;
import org.phantomapi.lang.GSound;
import org.phantomapi.sync.TaskLater;
import org.phantomapi.util.C;
import org.phantomapi.util.F;

public class ServerSelectionController extends Controller
{
	private File fileSel;
	private GList<ServerSelector> selectors;
	private GList<Player> fighters;
	private GList<Player> park;
	
	public ServerSelectionController(Controllable parentController)
	{
		super(parentController);
		
		fileSel = new File(getPlugin().getDataFolder(), "server-selectors");
		selectors = new GList<ServerSelector>();
		fighters = new GList<Player>();
		park = new GList<Player>();
	}
	
	@Override
	public void onStart()
	{
		new TaskLater()
		{
			@Override
			public void run()
			{
				if(((CommuneController) parentController).enableServerSelectors)
				{
					if(!fileSel.exists())
					{
						fileSel.mkdirs();
					}
					
					for(File i : fileSel.listFiles())
					{
						ServerSelector ss = new ServerSelector(i.getName().replaceAll(".yml", ""));
						loadCluster(ss, "server-selectors");
						selectors.add(ss);
						s("Loaded and Registered server selector: " + i.getName());
					}
				}
			}
		};
	}
	
	@Override
	public void onLoadComplete()
	{
		
	}
	
	@Override
	public void onStop()
	{
		
	}
	
	public boolean canJump(Player p)
	{
		return !park.contains(p);
	}
	
	public ServerSelector getSelector(String name)
	{
		for(ServerSelector i : selectors)
		{
			if(C.stripColor(i.heldName).equalsIgnoreCase(C.stripColor(name)))
			{
				return i;
			}
		}
		
		return null;
	}
	
	public ServerSelector getSelector(int slot)
	{
		for(ServerSelector i : selectors)
		{
			if(i.hbslot == slot)
			{
				return i;
			}
		}
		
		return null;
	}
	
	@EventHandler
	public void on(PlayerDropItemEvent e)
	{
		ServerSelector ss = getSelector(e.getPlayer().getInventory().getHeldItemSlot());
		
		if(ss != null)
		{
			e.setCancelled(true);
		}
		
		if(((CommuneController) parentController).lobbyMechanics)
		{
			e.setCancelled(true);
		}
	}
	
	@EventHandler
	public void on(InventoryClickEvent e)
	{
		ServerSelector ss = getSelector(e.getSlot());
		
		if(ss != null)
		{
			e.setCancelled(true);
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void on(PlayerInteractEvent e)
	{
		if(e.getAction().equals(Action.LEFT_CLICK_AIR) || e.getAction().equals(Action.LEFT_CLICK_BLOCK) || e.getAction().equals(Action.RIGHT_CLICK_AIR) || e.getAction().equals(Action.RIGHT_CLICK_BLOCK))
		{
			ServerSelector ss = getSelector(e.getPlayer().getInventory().getHeldItemSlot());
			
			if(ss != null)
			{
				ss.launch(e.getPlayer());
				e.setCancelled(true);
			}
			
			if(((CommuneController) parentController).lobbyMechanics)
			{
				if(e.getPlayer().getInventory().getHeldItemSlot() == 8)
				{
					if(e.getPlayer().getItemInHand().getType().equals(Material.SLIME_BALL))
					{
						ItemStack pvpOn = new ItemStack(Material.MAGMA_CREAM, 1);
						ItemMeta im = pvpOn.getItemMeta();
						im.setDisplayName(C.RED + "PvP ON");
						pvpOn.setItemMeta(im);
						e.getPlayer().getInventory().setItem(8, pvpOn);
						fighters.add(e.getPlayer());
						
						new GSound(Sound.ENDERDRAGON_HIT, 1.5f, 0.8f).play(e.getPlayer());
					}
					
					else
					{
						ItemStack pvpOff = new ItemStack(Material.SLIME_BALL, 1);
						ItemMeta im = pvpOff.getItemMeta();
						im.setDisplayName(C.GREEN + "PvP OFF");
						pvpOff.setItemMeta(im);
						e.getPlayer().getInventory().setItem(8, pvpOff);
						fighters.remove(e.getPlayer());
						
						new GSound(Sound.BURP, 1.5f, 1.0f).play(e.getPlayer());
					}
					
					e.setCancelled(true);
				}
				
				if(e.getPlayer().getInventory().getHeldItemSlot() == 7)
				{
					if(e.getPlayer().getItemInHand().getType().equals(Material.EYE_OF_ENDER))
					{
						ItemStack pvpOn = new ItemStack(Material.ENDER_PEARL, 1);
						ItemMeta im = pvpOn.getItemMeta();
						im.setDisplayName(C.RED + "Hub Movement OFF");
						pvpOn.setItemMeta(im);
						e.getPlayer().getInventory().setItem(7, pvpOn);
						park.add(e.getPlayer());
						e.getPlayer().setAllowFlight(false);
						
						new GSound(Sound.ENDERDRAGON_HIT, 1.5f, 0.8f).play(e.getPlayer());
					}
					
					else
					{
						ItemStack pvpOff = new ItemStack(Material.EYE_OF_ENDER, 1);
						ItemMeta im = pvpOff.getItemMeta();
						im.setDisplayName(C.GREEN + "Hub Movement ON");
						pvpOff.setItemMeta(im);
						e.getPlayer().getInventory().setItem(7, pvpOff);
						park.remove(e.getPlayer());
						
						new GSound(Sound.BURP, 1.5f, 1.0f).play(e.getPlayer());
					}
					
					e.setCancelled(true);
				}
			}
		}
	}
	
	@EventHandler
	public void on(PlayerJoinEvent e)
	{
		for(ServerSelector i : selectors)
		{
			i.buildItem(e.getPlayer());
		}
		
		if(((CommuneController) parentController).lobbyMechanics)
		{
			ItemStack pvpOff = new ItemStack(Material.SLIME_BALL, 1);
			ItemMeta im = pvpOff.getItemMeta();
			im.setDisplayName(C.GREEN + "PvP OFF");
			pvpOff.setItemMeta(im);
			e.getPlayer().getInventory().setItem(8, pvpOff);
			
			ItemStack parkOff = new ItemStack(Material.EYE_OF_ENDER, 1);
			ItemMeta imx = parkOff.getItemMeta();
			imx.setDisplayName(C.GREEN + "Hub Movement ON");
			parkOff.setItemMeta(imx);
			e.getPlayer().getInventory().setItem(7, parkOff);
		}
	}
	
	@EventHandler
	public void on(PlayerQuitEvent e)
	{
		if(((CommuneController) parentController).lobbyMechanics)
		{
			fighters.remove(e.getPlayer());
		}
	}
	
	@EventHandler
	public void on(PlayerDamagePlayerEvent e)
	{
		if(((CommuneController) parentController).lobbyMechanics)
		{
			if(!(fighters.contains(e.getPlayer()) && fighters.contains(e.getDamager())))
			{
				e.setCancelled(true);
				
				e.getDamager().sendMessage(F.color("&8&l(&4&l!&8&l) &4Both fighters must turn on pvp (hotbar)"));
			}
		}
	}
}
