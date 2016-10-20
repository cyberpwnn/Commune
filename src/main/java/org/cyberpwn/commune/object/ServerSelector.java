package org.cyberpwn.commune.object;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.phantomapi.clust.Comment;
import org.phantomapi.clust.Configurable;
import org.phantomapi.clust.DataCluster;
import org.phantomapi.clust.Keyed;
import org.phantomapi.lang.GList;
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
		
	}
}
