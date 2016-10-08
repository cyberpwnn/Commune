package org.cyberpwn.commune;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.imageio.ImageIO;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;

public class ImageMaps implements Listener
{
	public static final int MAP_WIDTH = 128;
	public static final int MAP_HEIGHT = 128;
	
	protected Commune pl;
	private Map<String, PlacingCacheEntry> placing = new HashMap<String, PlacingCacheEntry>();
	private Map<Short, ImageMap> maps = new HashMap<Short, ImageMap>();
	private Map<String, BufferedImage> images = new HashMap<String, BufferedImage>();
	private List<Short> sendList = new ArrayList<Short>();
	private FastSendTask sendTask;
	
	public ImageMaps(Commune pl)
	{
		this.pl = pl;
	}
	
	public void enable()
	{
		if(!new File(pl.getDataFolder(), "images").exists())
			new File(pl.getDataFolder(), "images").mkdirs();
		
		int sendPerTicks = 20;
		int mapsPerSend = 8;
		
		loadMaps();
		pl.getCommand("papyrus").setExecutor(new ImageMapCommand(this));
		pl.getServer().getPluginManager().registerEvents(this, pl);
		sendTask = new FastSendTask(this, mapsPerSend);
		pl.getServer().getPluginManager().registerEvents(sendTask, pl);
		sendTask.runTaskTimer(pl, sendPerTicks, sendPerTicks);
	}
	
	public void disable()
	{
		saveMaps();
	}
	
	public List<Short> getFastSendList()
	{
		return sendList;
	}
	
	public void startPlacing(Player p, String image, boolean fastsend)
	{
		placing.put(p.getName(), new PlacingCacheEntry(image, fastsend));
	}
	
	public boolean placeImage(Block block, BlockFace face, PlacingCacheEntry cache)
	{
		int xMod = 0;
		int zMod = 0;
		
		switch(face)
		{
			case EAST:
				zMod = -1;
				break;
			case WEST:
				zMod = 1;
				break;
			case SOUTH:
				xMod = 1;
				break;
			case NORTH:
				xMod = -1;
				break;
			default:
				return false;
		}
		
		BufferedImage image = loadImage(cache.getImage());
		
		if(image == null)
		{
			return false;
		}
		
		Block b = block.getRelative(face);
		
		int width = (int) Math.ceil((double) image.getWidth() / (double) MAP_WIDTH);
		int height = (int) Math.ceil((double) image.getHeight() / (double) MAP_HEIGHT);
		
		for(int x = 0; x < width; x++)
			for(int y = 0; y < height; y++)
			{
				if(!block.getRelative(x * xMod, -y, x * zMod).getType().isSolid())
					return false;
				
				if(block.getRelative(x * xMod - zMod, -y, x * zMod + xMod).getType().isSolid())
					return false;
			}
		
		try
		{
			for(int x = 0; x < width; x++)
				for(int y = 0; y < height; y++)
					setItemFrame(b.getRelative(x * xMod, -y, x * zMod), image, face, x * MAP_WIDTH, y * MAP_HEIGHT, cache);
		}
		catch(NullPointerException e)
		{
			// God forgive me, but I actually HAVE to catch this...
			return false;
		}
		
		return true;
	}
	
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = false)
	public void onInteract(PlayerInteractEvent e)
	{
		if(!e.hasBlock())
			return;
		
		if(e.getAction() != Action.RIGHT_CLICK_BLOCK)
			return;
		
		if(!placing.containsKey(e.getPlayer().getName()))
			return;
		
		if(!placeImage(e.getClickedBlock(), e.getBlockFace(), placing.get(e.getPlayer().getName())))
			e.getPlayer().sendMessage(ChatColor.RED + "Can't place the image here!\nMake sure the area is large enough, unobstructed and without pre-existing hanging entities.");
		else
			saveMaps();
		
		placing.remove(e.getPlayer().getName());
		
	}
	
	private void setItemFrame(Block bb, BufferedImage image, BlockFace face, int x, int y, PlacingCacheEntry cache)
	{
		ItemFrame i = null;
		
		i = bb.getWorld().spawn(bb.getLocation(), ItemFrame.class);
		
		i.setFacingDirection(face, false);
		
		ItemStack item = getMapItem(cache.getImage(), x, y, image);
		i.setItem(item);
		
		short id = item.getDurability();
		
		if(cache.isFastSend() && !sendList.contains(id))
		{
			sendList.add(id);
			sendTask.addToQueue(id);
		}
		
		maps.put(id, new ImageMap(cache.getImage(), x, y, sendList.contains(id)));
	}
	
	@SuppressWarnings("deprecation")
	private ItemStack getMapItem(String file, int x, int y, BufferedImage image)
	{
		ItemStack item = new ItemStack(Material.MAP);
		
		for(Entry<Short, ImageMap> entry : maps.entrySet())
			if(entry.getValue().isSimilar(file, x, y))
			{
				item.setDurability(entry.getKey());
				return item;
			}
		
		MapView map = pl.getServer().createMap(pl.getServer().getWorlds().get(0));
		for(MapRenderer r : map.getRenderers())
			map.removeRenderer(r);
		
		map.addRenderer(new ImageMapRenderer(image, x, y));
		
		item.setDurability(map.getId());
		
		return item;
	}
	
	private BufferedImage loadImage(String file)
	{
		if(images.containsKey(file))
			return images.get(file);
		
		File f = new File(pl.getDataFolder(), "images" + File.separatorChar + file);
		BufferedImage image = null;
		
		if(!f.exists())
			return null;
		
		try
		{
			image = ImageIO.read(f);
			images.put(file, image);
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
		
		return image;
	}
	
	@SuppressWarnings("deprecation")
	private void loadMaps()
	{
		File file = new File(pl.getDataFolder(), "maps.yml");
		FileConfiguration config = YamlConfiguration.loadConfiguration(file);
		
		for(String key : config.getKeys(false))
		{
			short id = Short.parseShort(key);
			
			MapView map = pl.getServer().getMap(id);
			
			for(MapRenderer r : map.getRenderers())
				map.removeRenderer(r);
			
			String image = config.getString(key + ".image");
			int x = config.getInt(key + ".x");
			int y = config.getInt(key + ".y");
			boolean fastsend = config.getBoolean(key + ".fastsend", false);
			
			BufferedImage bimage = loadImage(image);
			
			if(bimage == null)
			{
				continue;
			}
			
			if(fastsend)
				sendList.add(id);
			
			map.addRenderer(new ImageMapRenderer(loadImage(image), x, y));
			maps.put(id, new ImageMap(image, x, y, fastsend));
		}
	}
	
	private void saveMaps()
	{
		File file = new File(pl.getDataFolder(), "maps.yml");
		FileConfiguration config = YamlConfiguration.loadConfiguration(file);
		
		for(String key : config.getKeys(false))
			config.set(key, null);
		
		for(Entry<Short, ImageMap> e : maps.entrySet())
		{
			config.set(e.getKey() + ".image", e.getValue().getImage());
			config.set(e.getKey() + ".x", e.getValue().getX());
			config.set(e.getKey() + ".y", e.getValue().getY());
			config.set(e.getKey() + ".fastsend", e.getValue().isFastSend());
		}
		
		try
		{
			config.save(file);
		}
		catch(IOException e1)
		{
			e1.printStackTrace();
		}
	}
	
	@SuppressWarnings("deprecation")
	public void reloadImage(String file)
	{
		images.remove(file);
		BufferedImage image = loadImage(file);
		
		int width = (int) Math.ceil((double) image.getWidth() / (double) MAP_WIDTH);
		int height = (int) Math.ceil((double) image.getHeight() / (double) MAP_HEIGHT);
		
		for(int x = 0; x < width; x++)
			for(int y = 0; y < height; y++)
			{
				short id = getMapItem(file, x * MAP_WIDTH, y * MAP_HEIGHT, image).getDurability();
				MapView map = pl.getServer().getMap(id);
				
				for(MapRenderer renderer : map.getRenderers())
					if(renderer instanceof ImageMapRenderer)
					{
						((ImageMapRenderer) renderer).recalculateInput(image, x * MAP_WIDTH, y * MAP_HEIGHT);
					}
				
				sendTask.addToQueue(id);
			}
		
	}
}
