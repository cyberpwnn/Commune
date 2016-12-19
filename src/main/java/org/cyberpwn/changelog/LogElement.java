package org.cyberpwn.changelog;

import org.apache.commons.lang.WordUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.cyberpwn.commune.ChangelogController;
import org.phantomapi.gui.Click;
import org.phantomapi.gui.Element;
import org.phantomapi.gui.PhantomElement;
import org.phantomapi.gui.Slot;
import org.phantomapi.gui.Window;
import org.phantomapi.lang.GList;
import org.phantomapi.lang.GTime;
import org.phantomapi.nms.NMSX;
import org.phantomapi.text.SYM;
import org.phantomapi.text.TXT;
import org.phantomapi.util.C;
import org.phantomapi.util.M;
import org.phantomapi.world.MaterialBlock;
import org.phantomapi.world.W;

public class LogElement
{
	private GList<String> info;
	private long time;
	
	public LogElement(long time, GList<String> info)
	{
		this.time = time;
		this.info = info;
	}
	
	public String ago()
	{
		return new GTime(M.ms() - time).ago();
	}
	
	public String getServers()
	{
		GList<String> servers = new GList<String>();
		
		for(String i : info)
		{
			if(i.startsWith("/s "))
			{
				servers.add(i.replaceAll("/s ", "").trim().toLowerCase());
			}
		}
		
		if(servers.isEmpty())
		{
			return "All Servers";
		}
		
		return servers.toString(", ");
	}
	
	public String getAuthors()
	{
		GList<String> authors = new GList<String>();
		
		for(String i : info)
		{
			if(i.startsWith("/a "))
			{
				authors.add(i.replaceAll("/a ", "").trim());
			}
		}
		
		if(authors.isEmpty())
		{
			return "Unknown";
		}
		
		return authors.toString(" & ");
	}
	
	public MaterialBlock getMaterialBlock()
	{
		for(String i : info)
		{
			if(i.startsWith("/m "))
			{
				MaterialBlock mb = W.getMaterialBlock(i.replaceAll("/m ", "").trim());
				
				if(mb != null)
				{
					return mb;
				}
			}
		}
		
		return new MaterialBlock(Material.BOOK_AND_QUILL);
	}
	
	public GList<String> wrap(String s)
	{
		String format = C.getLastColors(s);
		String wrapped = WordUtils.wrap(s, 32);
		GList<String> wr = new GList<String>();
		
		int m = 0;
		
		for(String i : wrapped.split("\n"))
		{
			if(m == 0)
			{
				wr.add((format + i.trim()).trim());
			}
			
			else
			{
				wr.add("   " + (format + i.trim()).trim());
			}
			
			m++;
		}
		
		return wr;
	}
	
	public String getLabel()
	{
		for(String i : info)
		{
			if(i.startsWith("/l "))
			{
				return i.replaceAll("/l ", "").trim();
			}
		}
		
		return "No Title";
	}
	
	public String getTagged(String line)
	{
		if(line.startsWith("w "))
		{
			return line.replaceFirst("w ", C.GOLD + "" + SYM.SYMBOL_WARNING + " " + C.GOLD + C.BOLD);
		}
		
		else if(line.startsWith("x "))
		{
			return line.replaceFirst("x ", C.RED + "" + SYM.SYMBOL_VOLTAGE + " ");
		}
		
		else if(line.startsWith("a "))
		{
			return line.replaceFirst("a ", C.GREEN + "" + SYM.SYMBOL_PENCIL + " ");
		}
		
		else if(line.startsWith("c "))
		{
			return line.replaceFirst("c ", C.YELLOW + "" + SYM.SYMBOL_GEAR + " ");
		}
		
		else
		{
			return C.WHITE + "" + SYM.SYMBOL_NIB + " " + C.GRAY + C.ITALIC + line;
		}
	}
	
	public GList<String> getInformation()
	{
		GList<String> info = new GList<String>();
		
		info.add(C.AQUA + "" + SYM.SYMBOL_DIAMOND + C.WHITE + "" + C.BOLD + " Updated " + ago());
		info.add(C.AQUA + "" + SYM.SYMBOL_SNOWFLAKE + C.WHITE + "" + C.ITALIC + " by " + getAuthors());
		info.add(C.AQUA + "" + SYM.SYMBOL_FLAG + C.WHITE + "" + C.BOLD + " Applies to " + getServers());
		info.add(TXT.line(C.GRAY, 32));
		
		for(String i : getInfo())
		{
			if(!i.startsWith("/"))
			{
				info.add(wrap(getTagged(i)));
			}
		}
		
		info.add(TXT.line(C.GRAY, 32));
		
		if(getReferenceLink() != null)
		{
			info.add(C.WHITE + "" + SYM.SYMBOL_NIB + " " + C.GRAY + C.ITALIC + "Click this to view in your browser.");
			info.add(TXT.line(C.GRAY, 32));
		}
		
		return info;
	}
	
	public String getReferenceLink()
	{
		for(String i : info)
		{
			if(i.startsWith("/r "))
			{
				Integer ref = Integer.valueOf(i.replaceAll("/r ", ""));
				
				return ChangelogController.issue(ref);
			}
		}
		
		return null;
	}
	
	public Element buildElement(Slot slot)
	{
		Element e = new PhantomElement(getMaterialBlock().getMaterial(), slot, C.AQUA + "" + SYM.SYMBOL_PENCIL + C.GRAY + " " + getLabel())
		{
			@Override
			public void onClick(Player p, Click c, Window w)
			{
				if(getReferenceLink() != null)
				{
					NMSX.openURL(p, getReferenceLink());
					w.close();
				}
			}
		};
		
		e.setMetadata(getMaterialBlock().getData());
		e.setText(getInformation());
		
		return e;
	}
	
	public GList<String> getInfo()
	{
		return info;
	}
	
	public long getTime()
	{
		return time;
	}
}
