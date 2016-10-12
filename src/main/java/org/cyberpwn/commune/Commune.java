package org.cyberpwn.commune;

import java.io.IOException;
import org.bukkit.event.EventHandler;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.cyberpwn.commune.Metrics.Graph;
import org.cyberpwn.commune.frtp.FRTP;
import org.phantomapi.Phantom;
import org.phantomapi.construct.PhantomPlugin;
import org.phantomapi.core.SyncStart;
import org.phantomapi.event.BungeeConnectionEstablished;
import org.phantomapi.util.DMSRequire;
import org.phantomapi.util.DMSRequirement;
import net.milkbowl.vault.economy.Economy;

@SyncStart
@DMSRequire(DMSRequirement.SQL)
public class Commune extends PhantomPlugin
{
	public static Economy economy;
	private CommuneController communeController;
	private ImageMaps im;
	private FRTP frtp;
	private AFKController afk;
	
	@Override
	public void enable()
	{
		Phantom.instance().getTestController();
		
		frtp = new FRTP(this);
		afk = new AFKController(this);
		communeController = new CommuneController(this);
		
		register(communeController);
		register(afk);
		register(frtp);
		
		RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
		
		if(economyProvider != null)
		{
			economy = economyProvider.getProvider();
		}
		
		im = new ImageMaps(this);
		im.enable();
	}
	
	@Override
	public void disable()
	{
		im.disable();
	}
	
	@EventHandler
	public void on(BungeeConnectionEstablished e)
	{
		try
		{
			Metrics metrics = new Metrics(this);
			
			Graph weaponsUsedGraph = metrics.createGraph("Traffic on " + e.getServerName());
			
			weaponsUsedGraph.addPlotter(new Metrics.Plotter(e.getServerName())
			{
				@Override
				public int getValue()
				{
					return Phantom.instance().onlinePlayers().size();
				}
			});
			
			metrics.start();
		}
		
		catch(IOException ex)
		{
			
		}
	}
}
