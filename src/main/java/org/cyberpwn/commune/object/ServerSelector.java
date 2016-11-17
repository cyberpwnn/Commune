package org.cyberpwn.commune.object;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.phantomapi.Phantom;
import org.phantomapi.clust.Comment;
import org.phantomapi.clust.Configurable;
import org.phantomapi.clust.DataCluster;
import org.phantomapi.clust.Keyed;
import org.phantomapi.gui.Click;
import org.phantomapi.gui.Element;
import org.phantomapi.gui.PhantomElement;
import org.phantomapi.gui.PhantomWindow;
import org.phantomapi.gui.Slot;
import org.phantomapi.gui.Window;
import org.phantomapi.lang.GList;
import org.phantomapi.lang.GMap;
import org.phantomapi.lang.GSet;
import org.phantomapi.lang.GSound;
import org.phantomapi.network.PluginMessage;
import org.phantomapi.sync.TaskLater;
import org.phantomapi.util.C;
import org.phantomapi.util.F;
import org.phantomapi.world.MaterialBlock;
import org.phantomapi.world.W;

public class ServerSelector implements Configurable
{
	private DataCluster cc;
	private String cname;
	
	@Comment("The background style for example 1:2, Stone:2, Stone")
	@Keyed("style.gui-background")
	public String background = "0:0";
	
	@Comment("The item type held in the hotbar")
	@Keyed("style.hotbar-item")
	public String itemed = Material.COMPASS.toString().toLowerCase();
	
	@Comment("From slot 0-9, their hotbar slot to place the item in")
	@Keyed("style.hotbar-slot")
	public int hbslot = 0;
	
	@Comment("The name of the item in the hotbar")
	@Keyed("style.hotbar-name")
	public String heldName = "Something Cool";
	
	@Comment("Scale the gui viewport by height")
	@Keyed("style.viewport")
	public int viewport = 6;
	
	public ServerSelector(String cname)
	{
		this.cname = cname;
		cc = new DataCluster();
	}
	
	@Override
	public String getCodeName()
	{
		return cname;
	}
	
	@Override
	public DataCluster getConfiguration()
	{
		return cc;
	}
	
	@Override
	public void onNewConfig()
	{
		
	}
	
	@Override
	public void onReadConfig()
	{
		
	}
	
	public GMap<String, DataCluster> getServers()
	{
		GMap<String, DataCluster> map = new GMap<String, DataCluster>();
		DataCluster base = getConfiguration().crop("servers");
		
		for(String i : base.keys())
		{
			String key = i.split("\\.")[0];
			map.put(key, base.crop(key));
		}
		
		return map;
	}
	
	public GMap<String, DataCluster> getServersRoot()
	{
		GMap<String, DataCluster> map = getServers();
		GSet<String> remove = new GSet<String>();
		
		for(String i : map.k())
		{
			for(String j : getServers(i).k())
			{
				remove.add(j);
			}
		}
		
		for(String i : remove)
		{
			map.remove(i);
		}
		
		return map;
	}
	
	public GMap<String, DataCluster> getServers(String root)
	{
		GMap<String, DataCluster> map = new GMap<String, DataCluster>();
		DataCluster server = getServers().get(root);
		
		for(String i : server.getStringList("members"))
		{
			map.put(i, getServers().get(i));
		}
		
		return map;
	}
	
	public boolean hasMembers(String server)
	{
		return getServers().get(server).getStringList("members").size() > 0;
	}
	
	public String getName(String server)
	{
		return F.color(getServers().get(server).getString("name"));
	}
	
	public MaterialBlock getMaterial(String server)
	{
		return W.getMaterialBlock(getServers().get(server).getString("material"));
	}
	
	public Slot getSlot(String server)
	{
		return new Slot(getServers().get(server).getInt("x"), getServers().get(server).getInt("y"));
	}
	
	public GList<String> getFormattedLore(String server, Player player)
	{
		GList<String> lore = new GList<String>();
		
		for(String i : getServers().get(server).getStringList("lore"))
		{
			lore.add(F.p(player, F.color(i)));
		}
		
		return lore;
	}
	
	public void buildWindow(Player p, GMap<String, DataCluster> map)
	{
		Window w = new PhantomWindow(F.color(heldName), p)
		{
			@Override
			public boolean onClick(Element element, Player p)
			{
				new GSound(Sound.CLICK, 1f, 1.5f).play(p);
				
				new TaskLater(2)
				{
					@Override
					public void run()
					{
						new GSound(Sound.CLICK, 1f, 1.2f).play(p);
					}
				};
				
				return true;
			}
		};
		
		MaterialBlock bg = W.getMaterialBlock(background);
		
		for(String i : map.k())
		{
			MaterialBlock mb = getMaterial(i);
			
			PhantomElement e;
			
			try
			{
				e = new PhantomElement(mb.getMaterial(), mb.getData(), getSlot(i), getName(i))
				{
					@Override
					public void onClick(Player p, Click c, Window w)
					{
						if(hasMembers(i))
						{
							buildWindow(p, getServers(i));
						}
						
						else
						{
							w.close();
							new PluginMessage(Phantom.instance(), "ConnectOther", p.getName(), i).send();
							p.sendMessage(C.AQUA + "Warping to realm: " + C.WHITE + "" + C.BOLD + getName(i));
						}
					}
				};
			}
			
			catch(Exception eee)
			{
				e = new PhantomElement(Material.BARRIER, (byte) 0, getSlot(i), "FAILED TO GET NAME")
				{
					@Override
					public void onClick(Player p, Click c, Window w)
					{
						if(hasMembers(i))
						{
							buildWindow(p, getServers(i));
						}
						
						else
						{
							w.close();
							new PluginMessage(Phantom.instance(), "ConnectOther", p.getName(), i).send();
							p.sendMessage(C.AQUA + "Warping to realm: " + C.WHITE + "" + C.BOLD + getName(i));
						}
					}
				};
			}
			
			if(hasMembers(i))
			{
				e.setCount(getServers(i).size());
			}
			
			e.setText(getFormattedLore(i, p));
			w.addElement(e);
		}
		
		w.setBackground(new PhantomElement(bg.getMaterial(), bg.getData(), new Slot(0), " "));
		w.setViewport(viewport);
		w.open();
	}
	
	@SuppressWarnings("deprecation")
	public void buildItem(Player p)
	{
		MaterialBlock mbh = W.getMaterialBlock(itemed);
		ItemStack is = new ItemStack(mbh.getMaterial(), 1, (short) 0, mbh.getData());
		ItemMeta im = is.getItemMeta();
		im.setDisplayName(F.color(heldName));
		is.setItemMeta(im);
		p.getInventory().setItem(hbslot, is);
	}
	
	public void launch(Player p)
	{
		buildWindow(p, getServersRoot());
	}
}
