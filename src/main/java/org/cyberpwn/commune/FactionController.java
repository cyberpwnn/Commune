package org.cyberpwn.commune;

import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.cyberpwn.commune.util.FUT;
import org.phantomapi.clust.AsyncConfig;
import org.phantomapi.construct.Controllable;
import org.phantomapi.construct.Controller;
import org.phantomapi.construct.Ticked;
import org.phantomapi.event.PlayerMoveBlockEvent;
import org.phantomapi.util.C;
import org.phantomapi.util.F;

@AsyncConfig
@Ticked(5)
public class FactionController extends Controller
{
	public FactionController(Controllable parentController)
	{
		super(parentController);
	}
	
	@Override
	public void onStart()
	{
		
	}
	
	@Override
	public void onStop()
	{
		
	}
	
	public void onTick()
	{
		
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void on(ProjectileLaunchEvent e)
	{
		try
		{
			Entity entity = (Entity) e.getEntity().getShooter();
			
			if(entity.getType().equals(EntityType.PLAYER) && e.getEntity().getType().equals(EntityType.ENDER_PEARL))
			{
				Player p = (Player) entity;
				
				if(p.hasPermission("commune.god"))
				{
					return;
				}
				
				if(p.getLocation().getBlock().getType().isSolid() || p.getLocation().add(0, 1, 0).getBlock().getType().isSolid() || !(p.getLocation().add(0, -1, 0).getBlock().getType().isSolid() && p.getLocation().add(0, -1, -1).getBlock().getType().isSolid() && p.getLocation().add(0, -1, 1).getBlock().getType().isSolid() && p.getLocation().add(-1, -1, 0).getBlock().getType().isSolid() && p.getLocation().add(1, -1, 0).getBlock().getType().isSolid()) && p.getLocation().add(1, -1, 1).getBlock().getType().isSolid() && p.getLocation().add(1, -1, -1).getBlock().getType().isSolid() && p.getLocation().add(-1, -1, 1).getBlock().getType().isSolid() && p.getLocation().add(-1, -1, -1).getBlock().getType().isSolid())
				{
					p.sendMessage(C.RED + "Please exit the block you are in to use that.");
					e.setCancelled(true);
					return;
				}
				
				if(p.isFlying())
				{
					p.sendMessage(C.RED + "Please stop flying to use that");
					e.setCancelled(true);
					return;
				}
				
				if(!p.getLocation().add(0, -1, 0).getBlock().getType().isSolid() && !p.getLocation().add(0, -1, 0).getBlock().isLiquid())
				{
					p.sendMessage(C.RED + "Please stand on solid ground to use that");
					e.setCancelled(true);
					return;
				}
			}
		}
		
		catch(Exception ex)
		{
			
		}
	}
	
	@EventHandler
	public void on(PlayerToggleFlightEvent e)
	{
		if(e.isFlying() && !e.getPlayer().hasPermission("commune.god"))
		{
			if(!((CommuneController) parentController).factionMechanics)
			{
				return;
			}
			
			if(!FUT.getName(FUT.getFaction(e.getPlayer().getLocation())).equals(FUT.getName(FUT.getFaction(e.getPlayer()))))
			{
				e.getPlayer().setFlying(false);
				e.getPlayer().setVelocity(e.getPlayer().getVelocity().setY(-1));
				e.getPlayer().sendMessage(F.color("&8&l(&c&l!&8&l) &cYou can only fly in your own faction"));
			}
		}
	}
	
	@EventHandler
	public void on(PlayerMoveBlockEvent e)
	{
		if(!((CommuneController) parentController).factionMechanics)
		{
			return;
		}
		
		if(e.getPlayer().hasPermission("commune.god"))
		{
			return;
		}
		
		if(!e.getPlayer().isFlying())
		{
			return;
		}
		
		if(!FUT.getName(FUT.getFaction(e.getPlayer().getLocation())).equals(FUT.getName(FUT.getFaction(e.getPlayer()))))
		{
			e.getPlayer().setFlying(false);
			e.getPlayer().setVelocity(e.getPlayer().getVelocity().setY(-1));
			e.getPlayer().sendMessage(F.color("&8&l(&c&l!&8&l) &cYou can only fly in your own faction"));
		}
	}
}
