package org.cyberpwn.commune;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Collections;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.cyberpwn.changelog.LogElement;
import org.cyberpwn.changelog.LogPackage;
import org.phantomapi.async.A;
import org.phantomapi.clust.Comment;
import org.phantomapi.clust.ConfigurableController;
import org.phantomapi.clust.DataCluster;
import org.phantomapi.clust.Keyed;
import org.phantomapi.construct.Controllable;
import org.phantomapi.construct.Ticked;
import org.phantomapi.gui.Element;
import org.phantomapi.gui.Notification;
import org.phantomapi.gui.PhantomElement;
import org.phantomapi.gui.PhantomWindow;
import org.phantomapi.gui.Slot;
import org.phantomapi.gui.Window;
import org.phantomapi.lang.GList;
import org.phantomapi.lang.GMap;
import org.phantomapi.lang.GSound;
import org.phantomapi.lang.Title;
import org.phantomapi.sync.S;
import org.phantomapi.sync.TaskLater;
import org.phantomapi.text.ColoredString;
import org.phantomapi.text.RTEX;
import org.phantomapi.text.RTX;
import org.phantomapi.text.SYM;
import org.phantomapi.text.TagProvider;
import org.phantomapi.util.C;
import org.phantomapi.util.Timer;

@Ticked(300)
public class ChangelogController extends ConfigurableController implements TagProvider
{
	@Comment("The file used to pull changelogs")
	@Keyed("log-url")
	public String url = "https://raw.githubusercontent.com/cyberpwnn/GlacialRealms/master/changelog.yml";
	
	@Comment("The file used to pull changelogs")
	@Keyed("issue-url")
	public String issue = "https://github.com/cyberpwnn/GlacialRealms/issues/[number]";
	
	private static ChangelogController inst;
	private LogPackage log;
	private LogPlayerDataController ldc;
	private GMap<Player, GList<LogElement>> elemented;
	
	public ChangelogController(Controllable parentController)
	{
		super(parentController, "changelog-hangler");
		
		inst = this;
		ldc = new LogPlayerDataController(this);
		elemented = new GMap<Player, GList<LogElement>>();
		
		register(ldc);
	}
	
	public static String issue(int id)
	{
		return inst.issue + id;
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
	
	@Override
	public void onTick()
	{
		checkForUpdates();
	}
	
	public void update()
	{
		try
		{
			Timer t = new Timer();
			URL url = new URL(ChangelogController.this.url);
			InputStream is = url.openStream();
			InputStreamReader isr = new InputStreamReader(is);
			FileConfiguration fc = new YamlConfiguration();
			DataCluster cc = new DataCluster();
			
			t.start();
			fc.load(isr);
			isr.close();
			cc.addYaml(fc);
			t.stop();
			
			new S()
			{
				@Override
				public void sync()
				{
					if(log == null || !log.equals(new LogPackage(cc)))
					{
						log = new LogPackage(cc);
						
						for(Player i : onlinePlayers())
						{
							notifyPlayer(i);
						}
					}
				}
			};
		}
		
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public GList<LogElement> sort(GList<LogElement> e)
	{
		GList<LogElement> ex = new GList<LogElement>();
		
		if(e == null || e.isEmpty())
		{
			return ex;
		}
		
		GMap<Long, LogElement> elements = new GMap<Long, LogElement>();
		GList<Long> order = new GList<Long>();
		
		for(LogElement i : e)
		{
			elements.put(i.getTime(), i);
			order.add(i.getTime());
		}
		
		order.sort();
		Collections.reverse(order);
		
		for(Long i : order)
		{
			ex.add(elements.get(i));
		}
		
		return ex;
	}
	
	public void viewUpdates(Player p)
	{
		try
		{
			GList<LogElement> logs = sort(elemented.get(p));
			
			if(!logs.isEmpty())
			{
				Window w = new PhantomWindow(C.DARK_AQUA + "Latest Updates", p);
				
				int k = 0;
				
				for(LogElement i : logs)
				{
					if(k > 53)
					{
						continue;
					}
					
					w.addElement(i.buildElement(new Slot(k)));
					
					k++;
				}
				
				Element bg = new PhantomElement(Material.STAINED_GLASS_PANE, new Slot(0), " ");
				bg.setMetadata((byte) 15);
				w.setBackground(bg);
				w.setViewport(new Slot(k).getY());
				w.open();
				ldc.get(p).view(log.getLatest());
			}
			
			else
			{
				p.sendMessage(C.RED + "No logs to view at this time.");
			}
		}
		
		catch(Exception ee)
		{
			p.sendMessage(C.RED + "No logs to view at this time.");
		}
	}
	
	public void checkForUpdates()
	{
		new A()
		{
			@Override
			public void async()
			{
				try
				{
					update();
				}
				
				catch(Exception e)
				{
					
				}
			}
		};
	}
	
	public void notifyPlayer(Player p)
	{
		GList<LogElement> elements = new GList<LogElement>();
		long last = ldc.get(p).getSeen();
		
		new A()
		{
			@Override
			public void async()
			{
				try
				{
					elements.add(log.getLogsFor(last));
					
					new S()
					{
						@Override
						public void sync()
						{
							try
							{
								if(!elements.isEmpty())
								{
									elemented.put(p, elements);
									
									RTX rt = new RTX();
									RTEX rte = new RTEX(new ColoredString(C.AQUA, "Click to view this update."));
									rt.addText("Glacial Realms updated " + log.lastUpdateFormatted(), C.GRAY);
									rt.addTextFireHoverCommand(" View Update Log", rte, "/updates", C.AQUA);
									rt.tellRawTo(p);
									Notification n = new Notification();
									Title t = new Title(C.GOLD + "" + SYM.SYMBOL_VOLTAGE + "    " + SYM.SYMBOL_VOLTAGE + "    " + SYM.SYMBOL_VOLTAGE, C.AQUA + "Updates added " + log.lastUpdateFormatted(), C.YELLOW + "Use /updates to view them!", 5, 40, 50);
									n.setTitle(t);
									n.setAudible(new GSound(Sound.LEVEL_UP, 1f, 1.7f));
									n.play(p);
								}
							}
							
							catch(Exception e)
							{
								
							}
						}
					};
				}
				
				catch(Exception e)
				{
					
				}
			}
		};
	}
	
	@EventHandler
	public void on(PlayerJoinEvent e)
	{
		new TaskLater(30)
		{
			@Override
			public void run()
			{
				notifyPlayer(e.getPlayer());
			}
		};
	}
	
	@Override
	public String getChatTag()
	{
		return C.DARK_GRAY + "[" + C.AQUA + "Updates" + C.DARK_GRAY + "]: " + C.GRAY + C.ITALIC;
	}
	
	@Override
	public String getChatTagHover()
	{
		return C.AQUA + "Recent updates for Glacial Realms";
	}
}
