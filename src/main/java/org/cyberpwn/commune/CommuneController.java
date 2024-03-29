package org.cyberpwn.commune;

import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import org.cyberpwn.commune.util.FUT;
import org.cyberpwn.commune.util.ItemManipulator;
import org.phantomapi.Phantom;
import org.phantomapi.clust.Comment;
import org.phantomapi.clust.Configurable;
import org.phantomapi.clust.DataCluster;
import org.phantomapi.clust.Keyed;
import org.phantomapi.command.Command;
import org.phantomapi.command.CommandAlias;
import org.phantomapi.command.CommandFilter;
import org.phantomapi.command.PhantomCommand;
import org.phantomapi.command.PhantomSender;
import org.phantomapi.construct.Controllable;
import org.phantomapi.construct.Controller;
import org.phantomapi.construct.ControllerMessage;
import org.phantomapi.construct.Ticked;
import org.phantomapi.event.PlayerArrowDamagePlayerEvent;
import org.phantomapi.event.PlayerDamagePlayerEvent;
import org.phantomapi.event.PlayerMoveBlockEvent;
import org.phantomapi.lang.GList;
import org.phantomapi.lang.GMap;
import org.phantomapi.lang.GSound;
import org.phantomapi.lang.GTime;
import org.phantomapi.network.NetworkedServer;
import org.phantomapi.nms.NMSX;
import org.phantomapi.sync.TaskLater;
import org.phantomapi.util.C;
import org.phantomapi.util.F;
import org.phantomapi.util.M;
import org.phantomapi.util.Players;
import org.phantomapi.util.Probe;
import org.phantomapi.vfx.ParticleEffect;
import org.phantomapi.world.Area;
import org.phantomapi.world.MaterialBlock;
import org.phantomapi.world.W;
import com.sk89q.worldguard.bukkit.WGBukkit;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

@Ticked(0)
public class CommuneController extends Controller implements Configurable, Probe
{
	private DataCluster cc;
	private ServerSelectionController serverSelectionController;
	private HubController hubController;
	private FactionController factionController;
	private SyncController syncController;
	private StackController stackController;
	private SlateController slateController;
	private StaffController staffController;
	private CannonFix cannonFix;
	private SuggestionController suggestionController;
	private CombatController combatController;
	private DropperController dropperController;
	private WorldGuardBlockHandler wgbh;
	private ChangelogController clc;
	
	private GList<Player> warm;
	private GMap<Player, Long> ms;
	private GMap<Player, Long> pvpms;
	
	@Keyed("lobby.title.title")
	public String title = "&b&lGlacial Realms";
	
	@Keyed("lobby.title.sub-title")
	public String subTitle = "&3Welcome back!";
	
	@Keyed("lobby.title.action-title")
	public String actionTitle = "&eClick the compass to begin!";
	
	@Comment("Enable Server selectors")
	@Keyed("features.server-selector")
	public boolean enableServerSelectors = false;
	
	@Comment("Enable Lobby features")
	@Keyed("features.lobby-mechanics")
	public boolean lobbyMechanics = false;
	
	@Comment("Enable Factions features")
	@Keyed("features.faction-mechanics")
	public boolean factionMechanics = false;
	
	@Comment("Enable Slate features")
	@Keyed("features.slate-mechanics")
	public boolean slateMechanics = true;
	
	@Comment("Kit autostacking features")
	@Keyed("features.kit-stack-mechanics")
	public boolean kitStack = true;
	
	@Comment("Make snow")
	@Keyed("features.snowing")
	public boolean snowing = false;
	
	@Comment("Purge Connection Messages (Join/quit)")
	@Keyed("interface.purge-connection-messages")
	public boolean qjqj = true;
	
	@Comment("Players need the permission commune.stack aswell")
	@Keyed("interface.allow-potion-stack")
	public boolean allowStack = false;
	
	@Comment("Purge Kill Death Messages")
	@Keyed("interface.purge-kill-death-messages")
	public boolean kdkd = true;
	
	@Comment("Cancel village interactions")
	@Keyed("interface.purge-villager-interaction")
	public boolean vil = false;
	
	@Comment("Chat delay between messages without chat.delay")
	@Keyed("interface.chat-delay")
	public double chatDelay = 2.6;
	
	@Comment("Delay commands\nexample: - 'cmd args,cmd2 args2;seconds'")
	@Keyed("interface.command-delay")
	public GList<String> commandLimits = new GList<String>().qadd("efix,fix all;600");
	
	@Comment("Delay item usage\nexample: - 'ITEM:3;seconds'")
	@Keyed("interface.item-delay")
	public GList<String> itemLimits = new GList<String>().qadd("ENDER_PEARL;12");
	
	@Comment("Prevent players from teleporting OUT of a warzone via pearls")
	@Keyed("interface.enderpearl-teleportation-limits")
	public boolean tpep = false;
	
	@Comment("Allow fly only in own faction")
	@Keyed("interface.fly.own-faction")
	public boolean allowFlightInOwnFaction = true;
	
	@Comment("Max Stack Size")
	@Keyed("interface.max-stack-size")
	public int maxStack = 8;
	
	@Comment("Delay consume usage\nexample: - 'ITEM:3;seconds'")
	@Keyed("interface.consume-delay")
	public GList<String> consumeLimits = new GList<String>().qadd("GOLDEN_APPLE:1;20");
	
	@Comment("Command warmups\nexample: - 'cmd args,cmd2 args2;seconds'\ncancels if moved")
	@Keyed("interface.cmd-warms")
	public GList<String> cmdWarm = new GList<String>().qadd("wild,rtp,frtp,randomtp;3");
	
	@Keyed("interface.wither.allow-everywhere")
	public boolean allowWitherSpawns = false;
	
	@Keyed("interface.fix-price")
	public double fixPrice = 100.0;
	
	@Keyed("interface.handle-physics")
	public boolean handlePhysics = false;
	
	@Keyed("interface.handle-spawners")
	public boolean handleSpawners = true;
	
	@Keyed("interface.wither.only-world-allowed")
	public String witherworld = "end";
	
	@Keyed("interface.deny-teleport-into")
	public GList<String> denyRegion = new GList<String>().qadd("lootroom");
	
	private GMap<UUID, GMap<String, Long>> times;
	private GMap<UUID, GMap<String, Long>> itimes;
	private GMap<UUID, GMap<String, Long>> ctimes;
	private boolean muted;
	
	public CommuneController(Controllable parentController)
	{
		super(parentController);
		
		cc = new DataCluster();
		
		serverSelectionController = new ServerSelectionController(this);
		hubController = new HubController(this);
		factionController = new FactionController(this);
		syncController = new SyncController(this);
		stackController = new StackController(this);
		slateController = new SlateController(this);
		staffController = new StaffController(this);
		dropperController = new DropperController(this);
		suggestionController = new SuggestionController(this);
		cannonFix = new CannonFix(this);
		wgbh = new WorldGuardBlockHandler();
		combatController = new CombatController(this, "combat-tagger");
		muted = false;
		warm = new GList<Player>();
		clc = new ChangelogController(this);
		new PhantomPlaceholderHook().hook();
		
		register(serverSelectionController);
		register(stackController);
		register(hubController);
		register(factionController);
		register(syncController);
		register(slateController);
		register(staffController);
		register(cannonFix);
		register(suggestionController);
		register(combatController);
		register(dropperController);
		register(clc);
		
		ms = new GMap<Player, Long>();
		pvpms = new GMap<Player, Long>();
		times = new GMap<UUID, GMap<String, Long>>();
		itimes = new GMap<UUID, GMap<String, Long>>();
		ctimes = new GMap<UUID, GMap<String, Long>>();
	}
	
	@EventHandler
	public void on2(PlayerInteractAtEntityEvent e)
	{
		if(e.getRightClicked() != null && e.getRightClicked().getType().equals(EntityType.VILLAGER) && vil)
		{
			e.getPlayer().closeInventory();
			e.getPlayer().sendMessage(F.color("&8&l(&4&l!&8&l) &4Villagers Disabled!"));
			e.setCancelled(true);
			
			new TaskLater(1)
			{
				@Override
				public void run()
				{
					e.getPlayer().closeInventory();
				}
			};
		}
	}
	
	@Override
	public void onTick()
	{
		checkTimes();
		
		for(Player i : onlinePlayers())
		{
			Area a = new Area(i.getLocation().clone().add(0, 9, 0), 4);
			
			double y = i.getLocation().getY() / 256;
			
			for(int j = 0; j < y; j++)
			{
				Location l = a.random();
				ParticleEffect.SNOWBALL.display(0f, 4, l, i);
			}
		}
	}
	
	@EventHandler
	public void on(InventoryCloseEvent e)
	{
		try
		{
			Player p = (Player) e.getPlayer();
			ItemManipulator.clean(p, maxStack);
		}
		
		catch(Exception ex)
		{
			
		}
	}
	
	@SuppressWarnings("deprecation")
	@EventHandler
	public void craftItem(PrepareItemCraftEvent e)
	{
		Material itemType = e.getRecipe().getResult().getType();
		
		if((itemType.equals(Material.GOLDEN_APPLE) && e.getRecipe().getResult().getData().getData() == 1))
		{
			e.getInventory().setResult(new ItemStack(Material.AIR));
			
			for(HumanEntity he : e.getViewers())
			{
				if(he instanceof Player)
				{
					((Player) he).sendMessage(C.RED + "You cannot craft this!");
				}
			}
			
			return;
		}
		
		if((itemType.equals(Material.HOPPER) || itemType.equals(Material.ANVIL) || itemType.equals(Material.BEACON)) && !e.getRecipe().getResult().hasItemMeta())
		{
			e.getInventory().setResult(new ItemStack(Material.AIR));
			
			for(HumanEntity he : e.getViewers())
			{
				if(he instanceof Player)
				{
					((Player) he).sendMessage(C.RED + "You cannot craft this!");
				}
			}
		}
	}
	
	@EventHandler
	public void on2(PlayerInteractEvent e)
	{
		try
		{
			if(e.getAction().equals(Action.RIGHT_CLICK_BLOCK) && e.getClickedBlock() != null && e.getItem().getType().equals(Material.MONSTER_EGG))
			{
				Location a = e.getPlayer().getLocation().clone().add(0, 2, 0);
				Location b = e.getClickedBlock().getLocation().add(0, 2, 0);
				
				for(int i = 0; i < a.distance(b); i++)
				{
					Location c = a.clone().add(a.getDirection().multiply(new Vector(i, i, i)));
					
					int k = 0;
					
					for(Block j : W.blockFaces(c.getBlock()))
					{
						if(j.getType().equals(Material.AIR))
						{
							k++;
						}
					}
					
					if(k < 1)
					{
						e.setCancelled(true);
						return;
					}
				}
			}
		}
		
		catch(Exception ex)
		{
			
		}
	}
	
	@EventHandler
	public void on(PlayerTeleportEvent e)
	{
		if(e.getCause().equals(TeleportCause.ENDER_PEARL))
		{
			try
			{
				if(tpep)
				{
					if(FUT.isWarZone(e.getFrom()))
					{
						ApplicableRegionSet set = WGBukkit.getRegionManager(e.getTo().getWorld()).getApplicableRegions(e.getTo());
						
						if(set.size() > 0)
						{
							e.setCancelled(true);
							e.getPlayer().sendMessage(F.color("&8&l(&c&l!&8&l) &cCannot teleport warzone <-> spawn"));
							return;
						}
					}
				}
			}
			
			catch(Exception ex)
			{
				
			}
		}
		
		if(e.getCause().equals(TeleportCause.UNKNOWN))
		{
			return;
		}
		
		ApplicableRegionSet rm = WorldGuardPlugin.inst().getRegionManager(e.getTo().getWorld()).getApplicableRegions(new com.sk89q.worldedit.Vector(e.getTo().getX(), e.getTo().getY(), e.getTo().getZ()));
		
		for(ProtectedRegion i : rm.getRegions())
		{
			if(denyRegion.contains(i.getId()))
			{
				e.setCancelled(true);
				e.getPlayer().sendMessage(F.color("&8&l(&c&l!&8&l) &cCannot teleport you |-> " + i.getId()));
				return;
			}
		}
	}
	
	@CommandFilter.PlayerOnly
	@Command("connect")
	@CommandAlias("go")
	public void onConnect(PhantomSender sender, PhantomCommand cmd)
	{
		if(cmd.getArgs().length == 0)
		{
			GList<String> names = new GList<String>();
			
			for(NetworkedServer i : Phantom.getBungeeNetwork().getServers())
			{
				names.add(i.getName());
			}
			
			sender.sendMessage(C.GRAY + "To: " + C.WHITE + names.toString(", "));
		}
		
		if(cmd.getArgs().length == 1)
		{
			String q = cmd.getArgs()[0];
			
			for(NetworkedServer i : Phantom.getBungeeNetwork().getServers())
			{
				if(q.equalsIgnoreCase(i.getName()))
				{
					sender.sendMessage(C.GRAY + "Connecting to " + C.WHITE + i.getName());
					i.sendPlayer(sender.getPlayer());
				}
			}
			
			for(NetworkedServer i : Phantom.getBungeeNetwork().getServers())
			{
				if(i.getName().toLowerCase().contains(q.toLowerCase()))
				{
					sender.sendMessage(C.GRAY + "Connecting to " + C.WHITE + i.getName());
					i.sendPlayer(sender.getPlayer());
				}
			}
		}
	}
	
	@EventHandler
	public void on(EntityDamageByEntityEvent e)
	{
		if(e.getEntityType().equals(EntityType.PLAYER))
		{
			if(e.getDamager() != null)
			{
				if(e.getDamager().getType().equals(EntityType.PLAYER))
				{
					pvpms.put((Player) e.getEntity(), M.ms());
				}
				
				if(e.getDamager().getType().equals(EntityType.ARROW))
				{
					Arrow a = (Arrow) e.getDamager();
					
					if(a.getShooter() instanceof Player)
					{
						pvpms.put((Player) e.getEntity(), M.ms());
					}
				}
			}
		}
	}
	
	public void resetTime(Material matte, Player p)
	{
		for(String i : itemLimits)
		{
			MaterialBlock mb = W.getMaterialBlock(i.split(";")[0]);
			
			if(mb.getMaterial().equals(matte))
			{
				if(!itimes.containsKey(p.getUniqueId()))
				{
					itimes.put(p.getUniqueId(), new GMap<String, Long>());
				}
				
				if(itimes.get(p.getUniqueId()).containsKey(i.split(";")[0]))
				{
					itimes.get(p.getUniqueId()).remove(i.split(";")[0]);
				}
				
				return;
			}
		}
	}
	
	@Override
	public ControllerMessage onControllerMessageRecieved(ControllerMessage message)
	{
		if(message.contains("reset-pearl"))
		{
			resetTime(Material.ENDER_PEARL, Players.getPlayer(message.getString("reset-pearl")));
		}
		
		return message;
	}
	
	public void checkTimes()
	{
		for(String i : itemLimits)
		{
			MaterialBlock mb = W.getMaterialBlock(i.split(";")[0]);
			Integer seconds = Integer.valueOf(i.split(";")[1]);
			
			for(Player j : onlinePlayers())
			{
				UUID u = j.getUniqueId();
				
				if(itimes.containsKey(u))
				{
					if(M.ms() - itimes.get(u).get(i.split(";")[0]) < seconds * 1000)
					{
						double left = (seconds * 1000 - (M.ms() - itimes.get(u).get(i.split(";")[0])));
						
						if(left < 100)
						{
							new GSound(Sound.AMBIENCE_CAVE, 1f, 1.6f).play(j);
							NMSX.sendActionBar(j, C.AQUA + "You can use " + mb.getMaterial().toString().toLowerCase().replaceAll("_", " ") + "s again!");
						}
					}
				}
			}
		}
		
	}
	
	@EventHandler
	public void on(ProjectileLaunchEvent e)
	{
		for(String i : itemLimits)
		{
			MaterialBlock mb = W.getMaterialBlock(i.split(";")[0]);
			Integer seconds = Integer.valueOf(i.split(";")[1]);
			
			if(mb.getMaterial().equals(Material.ENDER_PEARL))
			{
				if(e.getEntity().getType().equals(EntityType.ENDER_PEARL) && e.getEntity().getShooter() instanceof Player)
				{
					Player p = (Player) e.getEntity().getShooter();
					
					if(!itimes.containsKey(p.getUniqueId()))
					{
						itimes.put(p.getUniqueId(), new GMap<String, Long>());
					}
					
					if(!itimes.get(p.getUniqueId()).containsKey(i.split(";")[0]))
					{
						itimes.get(p.getUniqueId()).put(i.split(";")[0], M.ms());
						return;
					}
					
					if(M.ms() - itimes.get(p.getUniqueId()).get(i.split(";")[0]) < seconds * 1000)
					{
						String left = F.f((seconds * 1000 - (M.ms() - itimes.get(p.getUniqueId()).get(i.split(";")[0]))) / 1000, 2);
						p.sendMessage(F.color("&8&l(&8&l!&8&l) &4You have to wait " + left + "s to use " + mb.getMaterial().toString().toLowerCase().replaceAll("_", " ") + "s" + " again!"));
						e.setCancelled(true);
						p.getInventory().addItem(new ItemStack(Material.ENDER_PEARL));
					}
					
					else
					{
						itimes.get(p.getUniqueId()).put(i.split(";")[0], M.ms());
						return;
					}
				}
			}
		}
	}
	
	@EventHandler
	public void on(BlockPhysicsEvent e)
	{
		if(!handlePhysics)
		{
			return;
		}
		
		if(!e.getChangedType().isSolid() && !e.getBlock().isLiquid())
		{
			e.setCancelled(!hasSourceBlock(e.getBlock()));
		}
	}
	
	public boolean hasSourceBlock(Block b)
	{
		return !b.getRelative(BlockFace.DOWN).getType().equals(Material.AIR) && b.getRelative(BlockFace.DOWN).getType().isSolid();
	}
	
	@Override
	public void onStart()
	{
		loadCluster(this);
		
		wgbh = new WorldGuardBlockHandler();
		
		if(Bukkit.getPluginManager().getPlugin("ASkyBlock") != null)
		{
			Phantom.instance().getBlockCheckController().registerBlockHandler(new ASkyblockBlockHandler());
		}
		
		Phantom.instance().getBlockCheckController().registerBlockHandler(wgbh);
	}
	
	@Override
	public void onStop()
	{
		Phantom.instance().getBlockCheckController().unRegisterBlockHandler(wgbh);
	}
	
	@Override
	public String getCodeName()
	{
		return "config";
	}
	
	@Override
	public DataCluster getConfiguration()
	{
		return cc;
	}
	
	@Override
	public void onNewConfig()
	{
		cc.set("interface.deny-commands", new GList<String>().qadd("rl").qadd("reload").qadd("stop").qadd("pl").qadd("plugins"), "Denies commands. Make sure they are one worded. No Spaces!");
	}
	
	@SuppressWarnings("deprecation")
	@EventHandler
	public void on(PlayerInteractEvent e)
	{
		if(e.getClickedBlock() == null)
		{
			return;
		}
		
		e.getClickedBlock().getLocation().add(0, 1, 0);
		
		if(e.getAction().equals(Action.RIGHT_CLICK_AIR) || e.getAction().equals(Action.RIGHT_CLICK_BLOCK))
		{
			for(String i : itemLimits)
			{
				MaterialBlock mb = W.getMaterialBlock(i.split(";")[0]);
				Integer seconds = Integer.valueOf(i.split(";")[1]);
				
				if(e.getPlayer().getItemInHand().getType().equals(mb.getMaterial()) && e.getPlayer().getItemInHand().getData().getData() == mb.getData())
				{
					Player p = e.getPlayer();
					
					if(!itimes.containsKey(p.getUniqueId()))
					{
						itimes.put(p.getUniqueId(), new GMap<String, Long>());
					}
					
					if(!itimes.get(p.getUniqueId()).containsKey(i.split(";")[0]))
					{
						itimes.get(p.getUniqueId()).put(i.split(";")[0], M.ms());
						return;
					}
					
					if(M.ms() - itimes.get(p.getUniqueId()).get(i.split(";")[0]) < seconds * 1000)
					{
						String left = F.f((seconds * 1000 - (M.ms() - itimes.get(p.getUniqueId()).get(i.split(";")[0]))) / 1000, 2);
						p.sendMessage(F.color("&8&l(&8&l!&8&l) &4You have to wait " + left + "s to use " + mb.getMaterial().toString().toLowerCase().replaceAll("_", " ") + "s" + " again!"));
						e.setCancelled(true);
					}
					
					else
					{
						itimes.get(p.getUniqueId()).put(i.split(";")[0], M.ms());
						return;
					}
				}
			}
		}
	}
	
	@SuppressWarnings("deprecation")
	@EventHandler
	public void on(PlayerItemConsumeEvent e)
	{
		if(e.getItem().getType().equals(Material.POTION))
		{
			new TaskLater()
			{
				@Override
				public void run()
				{
					if(e.getPlayer().getItemInHand() != null && e.getPlayer().getItemInHand().getType().equals(Material.GLASS_BOTTLE))
					{
						e.getPlayer().setItemInHand(new ItemStack(Material.AIR));
					}
				}
			};
		}
		
		for(String i : consumeLimits)
		{
			MaterialBlock mb = W.getMaterialBlock(i.split(";")[0]);
			Integer seconds = Integer.valueOf(i.split(";")[1]);
			
			if(e.getPlayer().getItemInHand().getType().equals(mb.getMaterial()) && e.getPlayer().getItemInHand().getData().getData() == mb.getData())
			{
				Player p = e.getPlayer();
				
				if(!ctimes.containsKey(p.getUniqueId()))
				{
					ctimes.put(p.getUniqueId(), new GMap<String, Long>());
				}
				
				if(!ctimes.get(p.getUniqueId()).containsKey(i.split(";")[0]))
				{
					ctimes.get(p.getUniqueId()).put(i.split(";")[0], M.ms());
					return;
				}
				
				if(M.ms() - ctimes.get(p.getUniqueId()).get(i.split(";")[0]) < seconds * 1000)
				{
					String left = F.f((seconds * 1000 - (M.ms() - ctimes.get(p.getUniqueId()).get(i.split(";")[0]))) / 1000, 2);
					p.sendMessage(F.color("&8&l(&8&l!&8&l) &4You have to wait " + left + "s to use " + mb.getMaterial().toString().toLowerCase().replaceAll("_", " ") + "s" + " again!"));
					e.setCancelled(true);
				}
				
				else
				{
					ctimes.get(p.getUniqueId()).put(i.split(";")[0], M.ms());
					return;
				}
			}
		}
	}
	
	public boolean process(Player p, String cmd)
	{
		if(p.hasPermission("commune.god"))
		{
			return false;
		}
		
		for(String i : commandLimits)
		{
			String cmdx = i.split(";")[0];
			Integer seconds = Integer.valueOf(i.split(";")[1]);
			GList<String> cmds = new GList<String>(cmdx.contains(",") ? cmdx.split(",") : new String[] {cmdx});
			
			for(String j : cmds)
			{
				String test = "/" + j;
				
				if(cmd.toLowerCase().startsWith(test))
				{
					if(cmd.equalsIgnoreCase("/fix all"))
					{
						if(!p.hasPermission("commune.fix"))
						{
							return false;
						}
					}
					
					if(!times.containsKey(p.getUniqueId()))
					{
						times.put(p.getUniqueId(), new GMap<String, Long>());
					}
					
					if(!times.get(p.getUniqueId()).containsKey(cmdx))
					{
						times.get(p.getUniqueId()).put(cmdx, M.ms());
						return false;
					}
					
					if(M.ms() - times.get(p.getUniqueId()).get(cmdx) < seconds * 1000)
					{
						String left = F.f((seconds * 1000 - (M.ms() - times.get(p.getUniqueId()).get(cmdx))) / 1000, 2);
						p.sendMessage(F.color("&8&l(&8&l!&8&l) &4You have to wait " + left + "s to use " + test + " again!"));
						return true;
					}
					
					else
					{
						times.get(p.getUniqueId()).put(cmdx, M.ms());
						return false;
					}
				}
			}
		}
		
		if(isOnCooldown(cmd, p))
		{
			return true;
		}
		
		for(String i : cmdWarm)
		{
			String cmdx = i.split(";")[0];
			Integer seconds = Integer.valueOf(i.split(";")[1]);
			GList<String> cmds = new GList<String>(cmdx.contains(",") ? cmdx.split(",") : new String[] {cmdx});
			
			for(String j : cmds)
			{
				String test = "/" + j;
				
				if(cmd.toLowerCase().startsWith(test))
				{
					Location loc = p.getLocation().clone();
					
					if(p.hasPermission("commune.god"))
					{
						return false;
					}
					
					p.sendMessage(C.GREEN + "Warming up... " + seconds + "s Don't move!");
					warm.add(p);
					
					new TaskLater(seconds * 20)
					{
						@Override
						public void run()
						{
							if(warm.contains(p))
							{
								if(p.getLocation().getBlock().getLocation().equals(loc.getBlock().getLocation()))
								{
									Bukkit.getServer().dispatchCommand(p, j);
								}
								
								else
								{
									p.sendMessage(C.RED + "Action Cancelled (movement)");
								}
							}
							
							else
							{
								p.sendMessage(C.RED + "Action Cancelled (combat)");
							}
						}
					};
					
					return true;
				}
			}
		}
		
		return false;
	}
	
	public boolean isOnCooldown(String cmd, Player p)
	{
		for(String i : commandLimits)
		{
			String cmdx = i.split(";")[0];
			Integer seconds = Integer.valueOf(i.split(";")[1]);
			GList<String> cmds = new GList<String>(cmdx.contains(",") ? cmdx.split(",") : new String[] {cmdx});
			
			for(String j : cmds)
			{
				String test = "/" + j;
				
				if(cmd.toLowerCase().startsWith(test))
				{
					if(cmd.equalsIgnoreCase("/fix all"))
					{
						if(!p.hasPermission("commune.fix"))
						{
							return false;
						}
					}
					
					if(times.containsKey(p.getUniqueId()))
					{
						if(M.ms() - times.get(p.getUniqueId()).get(cmdx) < seconds * 1000)
						{
							return true;
						}
					}
				}
			}
		}
		
		return false;
	}
	
	@Override
	public void onReadConfig()
	{
		
	}
	
	@EventHandler
	public void on(PlayerDamagePlayerEvent e)
	{
		if(e.getDamager().equals(e.getPlayer()))
		{
			e.setCancelled(true);
			return;
		}
		
		warm.remove(e.getDamager());
		warm.remove(e.getPlayer());
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void on(PlayerArrowDamagePlayerEvent e)
	{
		if(e.getDamager().equals(e.getPlayer()))
		{
			e.setCancelled(true);
			return;
		}
		
		warm.remove(e.getDamager());
		warm.remove(e.getPlayer());
	}
	
	@EventHandler
	public void on(EntitySpawnEvent e)
	{
		try
		{
			if(e.getEntityType().equals(EntityType.WITHER))
			{
				if(!e.getLocation().getWorld().getName().equals(witherworld) && !allowWitherSpawns)
				{
					Area a = new Area(e.getLocation(), 6);
					
					for(Player i : a.getNearbyPlayers())
					{
						i.sendMessage(F.color("&8&l(&4&l!&8&l) &4Withers can only be spawned in " + witherworld));
					}
					
					e.setCancelled(true);
				}
			}
		}
		
		catch(Exception ex)
		{
			
		}
	}
	
	@EventHandler
	public void on(AsyncPlayerChatEvent e)
	{
		if(muted)
		{
			if(!e.getPlayer().hasPermission("chat.manager"))
			{
				e.setCancelled(true);
				
				e.getPlayer().sendMessage(C.RED + "The chat is currently muted!");
				
				return;
			}
		}
		
		if(!e.getPlayer().hasPermission("chat.delay"))
		{
			if(!ms.containsKey(e.getPlayer()))
			{
				ms.put(e.getPlayer(), M.ms());
			}
			
			else if(M.ms() - ms.get(e.getPlayer()) < chatDelay * 1000)
			{
				e.setCancelled(true);
				e.getPlayer().sendMessage(F.color("&8&l(&4&l!&8&l) &4You need to wait " + F.f((chatDelay * 1000 - (M.ms() - ms.get(e.getPlayer()))) / 1000, 2) + "s to chat again!"));
			}
			
			else
			{
				ms.put(e.getPlayer(), M.ms());
			}
		}
		
		if(!e.getPlayer().hasPermission("commune.itemsend"))
		{
			
		}
	}
	
	public boolean isOnPvp(Player p)
	{
		return pvpms.containsKey(p) && new GTime(M.ms() - pvpms.get(p)).getSeconds() < 15;
	}
	
	@EventHandler
	public void on(PlayerMoveBlockEvent e)
	{
		if(e.getTo().getY() > 255 && e.getTo().getWorld().getName().endsWith("nether"))
		{
			e.setCancelled(true);
			e.getPlayer().teleport(e.getTo().getWorld().getSpawnLocation());
		}
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void on(PlayerCommandPreprocessEvent e)
	{
		if(combatController.isTagged(e.getPlayer()))
		{
			return;
		}
		
		for(String i : cc.getStringList("interface.deny-commands"))
		{
			String check = i.toLowerCase();
			String m = e.getMessage().toLowerCase();
			
			if(!check.startsWith("/"))
			{
				check = "/" + check;
			}
			
			if(check.contains(" "))
			{
				check = check.split(" ")[0];
			}
			
			if(m.equals(check))
			{
				e.getPlayer().sendMessage(C.BOLD.toString() + C.RED.toString() + ";)");
				e.setCancelled(true);
				return;
			}
			
			if((m.contains(" ") ? m.split(" ")[0] : m).equals(check))
			{
				e.getPlayer().sendMessage(C.BOLD.toString() + C.RED.toString() + ";)");
				e.setCancelled(true);
				return;
			}
		}
		
		if(e.getMessage().contains(" "))
		{
			if(e.getMessage().split(" ")[0].equals("/?"))
			{
				e.setCancelled(true);
				return;
			}
		}
		
		else
		{
			if(e.getMessage().equals("/?"))
			{
				e.setCancelled(true);
				return;
			}
		}
		
		if(e.getMessage().equalsIgnoreCase("/list"))
		{
			e.getPlayer().sendMessage(F.color("&8&l(&f&l!&8&l) &b&lThere is currently &f&l" + F.f(Phantom.instance().onlinePlayers().size()) + " &b&lonline!"));
			e.setCancelled(true);
			return;
		}
		
		if(e.getMessage().equalsIgnoreCase("/updates"))
		{
			clc.viewUpdates(e.getPlayer());
			e.setCancelled(true);
			return;
		}
		
		if(e.getPlayer().hasPermission("chat.manager"))
		{
			if(e.getMessage().equalsIgnoreCase("/chat clear"))
			{
				for(Player i : Phantom.instance().onlinePlayers())
				{
					for(int j = 0; j < 120; j++)
					{
						if(j != 115)
						{
							String k = "";
							GList<C> cx = new GList<C>().qadd(C.AQUA).qadd(C.DARK_AQUA).qadd(C.WHITE);
							
							for(int l = 0; l < 75; l++)
							{
								k = k + (Math.random() < 0.11 ? cx.pickRandom() + "*" : " ");
							}
							
							i.sendMessage(k);
						}
						
						else
						{
							i.sendMessage(" ");
						}
						
						if(j == 115)
						{
							i.sendMessage(F.color("            &8&l(&e&l!&8&l) &6&l" + e.getPlayer().getName() + " &b&lcleared the chat &8&l(&e&l!&8&l)            "));
						}
					}
				}
				
				e.setCancelled(true);
				return;
			}
			
			if(e.getMessage().equalsIgnoreCase("/chat mute"))
			{
				if(muted)
				{
					for(Player i : Phantom.instance().onlinePlayers())
					{
						i.sendMessage(F.color("&8&l(&e&l!&8&l) &6&l" + e.getPlayer().getName() + " &b&lunmuted the chat."));
					}
				}
				
				else
				{
					for(Player i : Phantom.instance().onlinePlayers())
					{
						i.sendMessage(F.color("&8&l(&e&l!&8&l) &6&l" + e.getPlayer().getName() + " &b&lmuted the chat."));
					}
				}
				
				muted = !muted;
				
				e.setCancelled(true);
				return;
			}
		}
		
		if(e.getMessage().equalsIgnoreCase("/board off"))
		{
			slateController.board(e.getPlayer(), false);
			
			e.setCancelled(true);
			return;
		}
		
		if(e.getMessage().equalsIgnoreCase("/board on"))
		{
			slateController.board(e.getPlayer(), true);
			
			e.setCancelled(true);
			return;
		}
		
		if(e.getMessage().equalsIgnoreCase("/staff"))
		{
			if(Phantom.getServerName() == null)
			{
				e.getPlayer().sendMessage(F.color("&8&l(&c&l!&8&l) &c&lPlease try again later."));
				e.setCancelled(true);
				return;
			}
			
			staffController.handle(new PhantomSender(e.getPlayer()));
			
			e.setCancelled(true);
			return;
		}
		
		e.setCancelled(process(e.getPlayer(), e.getMessage()));
		
		if(e.isCancelled() && e.getPlayer().hasPermission("commune.god"))
		{
			e.setCancelled(false);
		}
		
		if(e.isCancelled())
		{
			return;
		}
		
		if(e.getMessage().equalsIgnoreCase("/stack"))
		{
			if(e.getPlayer().hasPermission("commune.stack") && allowStack)
			{
				ItemManipulator.stack(e.getPlayer(), maxStack);
				e.getPlayer().sendMessage(F.color("&8&l(&f&l!&8&l) &b&lBam! &fStacked Potions."));
			}
			
			e.setCancelled(true);
			return;
		}
		
		else if(e.getMessage().equalsIgnoreCase("/fix all"))
		{
			if(!e.getPlayer().hasPermission("commune.fix"))
			{
				e.getPlayer().sendMessage(F.color("&8&l(&c&l!&8&l) &cYou need &b&lArctic&f&l+ &cto use this"));
				e.setCancelled(true);
				return;
			}
			
			if(isOnPvp(e.getPlayer()))
			{
				e.getPlayer().sendMessage(F.color("&8&l(&c&l!&8&l) &cYou are still in combat!"));
				e.setCancelled(true);
				return;
			}
			
			ItemManipulator.fixAll(e.getPlayer());
			e.getPlayer().sendMessage(F.color("&8&l(&f&l!&8&l) &b&lBam! &fFixed Items."));
			e.setCancelled(true);
			return;
		}
		
		else if(e.getMessage().equalsIgnoreCase("/fix hand"))
		{
			Player p = e.getPlayer();
			
			if(Commune.economy.getBalance(p) >= fixPrice)
			{
				if(ItemManipulator.fixHand(e.getPlayer()))
				{
					e.getPlayer().sendMessage(F.color("&8&l(&f&l!&8&l) &b&l*snap* &fFixed Hand. &6&l$" + fixPrice));
					Commune.economy.withdrawPlayer(p, fixPrice);
				}
				
				else
				{
					e.getPlayer().sendMessage(F.color("&8&l(&c&l!&8&l) &c&lNothing to Repair."));
				}
			}
			
			else
			{
				e.getPlayer().sendMessage(F.color("&8&l(&c&l!&8&l) &c&lYou don't have &6&l$" + fixPrice));
			}
			
			e.setCancelled(true);
			return;
		}
	}
	
	@EventHandler
	public void on(PlayerJoinEvent e)
	{
		if(qjqj)
		{
			e.setJoinMessage(null);
		}
		
		new TaskLater(5)
		{
			@Override
			public void run()
			{
				e.getPlayer().sendMessage(F.color("&bYou have connected to &f" + Phantom.getServerName() + "&b, type /hub if you want to get back to the hub!"));
			}
		};
	}
	
	@EventHandler
	public void on(PlayerQuitEvent e)
	{
		if(qjqj)
		{
			e.setQuitMessage(null);
		}
		
		ms.remove(e.getPlayer());
	}
	
	@EventHandler
	public void on(PlayerDeathEvent e)
	{
		if(kdkd)
		{
			e.setDeathMessage(null);
		}
	}
	
	public DataCluster getCc()
	{
		return cc;
	}
	
	public void setCc(DataCluster cc)
	{
		this.cc = cc;
	}
	
	public ServerSelectionController getServerSelectionController()
	{
		return serverSelectionController;
	}
	
	public void setServerSelectionController(ServerSelectionController serverSelectionController)
	{
		this.serverSelectionController = serverSelectionController;
	}
	
	public HubController getHubController()
	{
		return hubController;
	}
	
	public void setHubController(HubController hubController)
	{
		this.hubController = hubController;
	}
	
	public FactionController getFactionController()
	{
		return factionController;
	}
	
	public void setFactionController(FactionController factionController)
	{
		this.factionController = factionController;
	}
	
	public SyncController getSyncController()
	{
		return syncController;
	}
	
	public void setSyncController(SyncController syncController)
	{
		this.syncController = syncController;
	}
	
	public StackController getStackController()
	{
		return stackController;
	}
	
	public void setStackController(StackController stackController)
	{
		this.stackController = stackController;
	}
	
	public SlateController getSlateController()
	{
		return slateController;
	}
	
	public void setSlateController(SlateController slateController)
	{
		this.slateController = slateController;
	}
	
	public StaffController getStaffController()
	{
		return staffController;
	}
	
	public void setStaffController(StaffController staffController)
	{
		this.staffController = staffController;
	}
	
	public CannonFix getCannonFix()
	{
		return cannonFix;
	}
	
	public void setCannonFix(CannonFix cannonFix)
	{
		this.cannonFix = cannonFix;
	}
	
	public GMap<Player, Long> getMs()
	{
		return ms;
	}
	
	public void setMs(GMap<Player, Long> ms)
	{
		this.ms = ms;
	}
	
	public GMap<Player, Long> getPvpms()
	{
		return pvpms;
	}
	
	public void setPvpms(GMap<Player, Long> pvpms)
	{
		this.pvpms = pvpms;
	}
	
	public String getTitle()
	{
		return title;
	}
	
	public void setTitle(String title)
	{
		this.title = title;
	}
	
	public String getSubTitle()
	{
		return subTitle;
	}
	
	public void setSubTitle(String subTitle)
	{
		this.subTitle = subTitle;
	}
	
	public String getActionTitle()
	{
		return actionTitle;
	}
	
	public void setActionTitle(String actionTitle)
	{
		this.actionTitle = actionTitle;
	}
	
	public boolean isEnableServerSelectors()
	{
		return enableServerSelectors;
	}
	
	public void setEnableServerSelectors(boolean enableServerSelectors)
	{
		this.enableServerSelectors = enableServerSelectors;
	}
	
	public boolean isLobbyMechanics()
	{
		return lobbyMechanics;
	}
	
	public void setLobbyMechanics(boolean lobbyMechanics)
	{
		this.lobbyMechanics = lobbyMechanics;
	}
	
	public boolean isFactionMechanics()
	{
		return factionMechanics;
	}
	
	public void setFactionMechanics(boolean factionMechanics)
	{
		this.factionMechanics = factionMechanics;
	}
	
	public boolean isSlateMechanics()
	{
		return slateMechanics;
	}
	
	public void setSlateMechanics(boolean slateMechanics)
	{
		this.slateMechanics = slateMechanics;
	}
	
	public boolean isKitStack()
	{
		return kitStack;
	}
	
	public void setKitStack(boolean kitStack)
	{
		this.kitStack = kitStack;
	}
	
	public boolean isQjqj()
	{
		return qjqj;
	}
	
	public void setQjqj(boolean qjqj)
	{
		this.qjqj = qjqj;
	}
	
	public boolean isAllowStack()
	{
		return allowStack;
	}
	
	public void setAllowStack(boolean allowStack)
	{
		this.allowStack = allowStack;
	}
	
	public boolean isKdkd()
	{
		return kdkd;
	}
	
	public void setKdkd(boolean kdkd)
	{
		this.kdkd = kdkd;
	}
	
	public double getChatDelay()
	{
		return chatDelay;
	}
	
	public void setChatDelay(double chatDelay)
	{
		this.chatDelay = chatDelay;
	}
	
	public GList<String> getCommandLimits()
	{
		return commandLimits;
	}
	
	public void setCommandLimits(GList<String> commandLimits)
	{
		this.commandLimits = commandLimits;
	}
	
	public GList<String> getItemLimits()
	{
		return itemLimits;
	}
	
	public void setItemLimits(GList<String> itemLimits)
	{
		this.itemLimits = itemLimits;
	}
	
	public boolean isTpep()
	{
		return tpep;
	}
	
	public void setTpep(boolean tpep)
	{
		this.tpep = tpep;
	}
	
	public boolean isAllowFlightInOwnFaction()
	{
		return allowFlightInOwnFaction;
	}
	
	public void setAllowFlightInOwnFaction(boolean allowFlightInOwnFaction)
	{
		this.allowFlightInOwnFaction = allowFlightInOwnFaction;
	}
	
	public int getMaxStack()
	{
		return maxStack;
	}
	
	public void setMaxStack(int maxStack)
	{
		this.maxStack = maxStack;
	}
	
	public GList<String> getConsumeLimits()
	{
		return consumeLimits;
	}
	
	public void setConsumeLimits(GList<String> consumeLimits)
	{
		this.consumeLimits = consumeLimits;
	}
	
	public GList<String> getCmdWarm()
	{
		return cmdWarm;
	}
	
	public void setCmdWarm(GList<String> cmdWarm)
	{
		this.cmdWarm = cmdWarm;
	}
	
	public boolean isAllowWitherSpawns()
	{
		return allowWitherSpawns;
	}
	
	public void setAllowWitherSpawns(boolean allowWitherSpawns)
	{
		this.allowWitherSpawns = allowWitherSpawns;
	}
	
	public double getFixPrice()
	{
		return fixPrice;
	}
	
	public void setFixPrice(double fixPrice)
	{
		this.fixPrice = fixPrice;
	}
	
	public boolean isHandlePhysics()
	{
		return handlePhysics;
	}
	
	public void setHandlePhysics(boolean handlePhysics)
	{
		this.handlePhysics = handlePhysics;
	}
	
	public String getWitherworld()
	{
		return witherworld;
	}
	
	public void setWitherworld(String witherworld)
	{
		this.witherworld = witherworld;
	}
	
	public GMap<UUID, GMap<String, Long>> getTimes()
	{
		return times;
	}
	
	public void setTimes(GMap<UUID, GMap<String, Long>> times)
	{
		this.times = times;
	}
	
	public GMap<UUID, GMap<String, Long>> getItimes()
	{
		return itimes;
	}
	
	public void setItimes(GMap<UUID, GMap<String, Long>> itimes)
	{
		this.itimes = itimes;
	}
	
	public GMap<UUID, GMap<String, Long>> getCtimes()
	{
		return ctimes;
	}
	
	public void setCtimes(GMap<UUID, GMap<String, Long>> ctimes)
	{
		this.ctimes = ctimes;
	}
	
	public boolean isMuted()
	{
		return muted;
	}
	
	public void setMuted(boolean muted)
	{
		this.muted = muted;
	}
	
	@Override
	public DataCluster onProbe(Block block, DataCluster probeSet)
	{
		return probeSet;
	}
	
	public SuggestionController getSuggestionController()
	{
		return suggestionController;
	}
	
	public CombatController getCombatController()
	{
		return combatController;
	}
	
	public DropperController getDropperController()
	{
		return dropperController;
	}
	
	public WorldGuardBlockHandler getWgbh()
	{
		return wgbh;
	}
	
	public ChangelogController getClc()
	{
		return clc;
	}
	
	public GList<Player> getWarm()
	{
		return warm;
	}
	
	public boolean isVil()
	{
		return vil;
	}
	
	public boolean isHandleSpawners()
	{
		return handleSpawners;
	}
	
	public GList<String> getDenyRegion()
	{
		return denyRegion;
	}
}
