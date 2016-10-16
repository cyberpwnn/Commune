package org.cyberpwn.commune.object;

import org.bukkit.Material;
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
import org.phantomapi.gui.Guis;
import org.phantomapi.gui.PhantomElement;
import org.phantomapi.gui.PhantomWindow;
import org.phantomapi.gui.Slot;
import org.phantomapi.gui.Window;
import org.phantomapi.lang.GList;
import org.phantomapi.network.PluginMessage;
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
		cc.set("title", "&aTitle");
		cc.set("items", new GList<String>(), "Format: title;type;x;y;server;multi,line,description\nName and Descriptions support colors.");
	}
	
	@Override
	public void onReadConfig()
	{
		
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
		Window w = new PhantomWindow(F.color(cc.getString("title")), p);
		
		for(String i : cc.getStringList("items"))
		{
			String[] sel = i.split(";");
			String name = F.color(sel[0]);
			MaterialBlock mb = W.getMaterialBlock(sel[1]);
			Integer x = Integer.valueOf(sel[2]);
			Integer y = Integer.valueOf(sel[3]);
			String server = sel[4];
			GList<String> text = F.color(new GList<String>(sel[5].split(",")));
			
			Element e = new PhantomElement(mb.getMaterial(), mb.getData(), new Slot(x, y), name)
			{
				@Override
				public void onClick(Player p, Click c, Window w)
				{
					if(server.contains("//"))
					{
						GList<String> names = new GList<String>(server.split("//"));
						GList<Slot> slots = Guis.getCentered(names.size(), 2);
						Window wx = new PhantomWindow(name, p);
						
						for(String j : names)
						{
							Element ex = new PhantomElement(mb.getMaterial(), mb.getData(), slots.pop(), j)
							{
								@Override
								public void onClick(Player p, Click c, Window w)
								{
									w.close();
									new PluginMessage(Phantom.instance(), "ConnectOther", p.getName(), j).send();
									p.sendMessage(C.AQUA + "Warping to realm: " + C.WHITE + "" + C.BOLD + j);
								}
							};
							
							GList<String> ttext = new GList<String>();
							
							for(String k : text)
							{
								ttext.add(F.p(p, k));
							}
							
							ex.setText(ttext);
							
							wx.addElement(ex);
						}
						
						wx.open();
					}
					
					else
					{
						w.close();
						new PluginMessage(Phantom.instance(), "ConnectOther", p.getName(), server).send();
						p.sendMessage(C.AQUA + "Warping to realm: " + C.WHITE + "" + C.BOLD + server);
					}
				}
			};
			
			GList<String> ttext = new GList<String>();
			
			for(String j : text)
			{
				ttext.add(F.p(p, j));
			}
			
			e.setText(ttext);
			w.addElement(e);
		}
		
		try
		{
			MaterialBlock mb = W.getMaterialBlock(background);
			
			w.setBackground(new PhantomElement(mb.getMaterial(), mb.getData(), new Slot(0), ""));
		}
		
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		w.setViewport(viewport);
		w.open();
	}
}
