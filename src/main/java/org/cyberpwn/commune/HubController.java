package org.cyberpwn.commune;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.event.player.PlayerToggleSprintEvent;
import org.bukkit.util.Vector;
import org.phantomapi.construct.Controllable;
import org.phantomapi.construct.Controller;
import org.phantomapi.lang.GSound;
import org.phantomapi.lang.Title;
import org.phantomapi.physics.VectorMath;
import org.phantomapi.sfx.Audible;
import org.phantomapi.sync.Task;
import org.phantomapi.sync.TaskLater;
import org.phantomapi.util.F;
import org.phantomapi.vfx.ParticleEffect;
import org.phantomapi.vfx.PhantomEffect;
import org.phantomapi.vfx.VisualEffect;

public class HubController extends Controller
{
	private VisualEffect flyFx;
	
	public HubController(Controllable parentController)
	{
		super(parentController);
		
		flyFx = new PhantomEffect()
		{
			@Override
			public void play(Location l)
			{
				long time = l.getWorld().getTime();
				if(time > 0 && time < 12300)
				{
					return;
				}
				
				ParticleEffect.FIREWORKS_SPARK.display(l.getDirection().clone().add(l.getDirection().clone().multiply(0.2)), 0.3f, l, 64);
				ParticleEffect.FIREWORKS_SPARK.display(l.getDirection().clone().add(l.getDirection().clone().multiply(0.6)), 0.3f, l, 64);
				ParticleEffect.FIREWORKS_SPARK.display(l.getDirection().clone().add(l.getDirection().clone().multiply(0.8)), 0.3f, l, 64);
				ParticleEffect.FIREWORKS_SPARK.display(l.getDirection().clone().add(l.getDirection().clone().multiply(0.4)), 0.3f, l, 64);
				ParticleEffect.FIREWORKS_SPARK.display(l.getDirection().clone().add(l.getDirection().clone().multiply(1.0)), 0.3f, l, 64);
			}
		};
	}
	
	@Override
	public void onStart()
	{
		
	}
	
	@Override
	public void onStop()
	{
		
	}
	
	public boolean canJump(Player p)
	{
		return ((CommuneController) parentController).lobbyMechanics && ((CommuneController) parentController).getServerSelectionController().canJump(p);
	}
	
	@EventHandler
	public void on(PlayerJoinEvent e)
	{
		if(((CommuneController) parentController).lobbyMechanics)
		{
			new TaskLater(35)
			{
				@Override
				public void run()
				{
					if(((CommuneController) parentController).lobbyMechanics)
					{
						Title t = new Title(F.color(((CommuneController) parentController).title), F.color(((CommuneController) parentController).subTitle), F.color(((CommuneController) parentController).actionTitle), 5, 190, 20);
						t.send(e.getPlayer());
					}
				}
			};
		}
	}
	
	@EventHandler
	public void on(PlayerToggleSprintEvent e)
	{
		Player p = e.getPlayer();
		if(e.isSprinting() && canJump(e.getPlayer()) && p.getLocation().getBlock().getRelative(0, -1, 0).getType() != Material.AIR && !p.isFlying())
		{
			e.getPlayer().setVelocity(e.getPlayer().getLocation().getDirection().multiply(4.0).setY(0.3));
			Audible a = new GSound(Sound.ENDERDRAGON_WINGS);
			a.setPitch(0.3f);
			a.setVolume(0.7f);
			a.play(p.getLocation());
			
			a = new GSound(Sound.ENDERDRAGON_WINGS);
			a.setPitch(1.8f);
			a.setVolume(0.7f);
			a.play(p.getLocation());
			
			a = new GSound(Sound.ENDERDRAGON_WINGS);
			a.setPitch(1.3f);
			a.setVolume(0.7f);
			a.play(p.getLocation());
			
			for(int i = 0; i < 43; i++)
			{
				Location ll = p.getLocation().clone().add(0, 1.3, 0);
				ll.setDirection(VectorMath.reverse(p.getVelocity()).add(Vector.getRandom().normalize().multiply(0.992)));
				flyFx.play(ll);
			}
		}
	}
	
	@EventHandler
	public void onPlayerToggleFlight(PlayerToggleFlightEvent e)
	{
		if(!canJump(e.getPlayer()))
		{
			return;
		}
		
		Player p = e.getPlayer();
		
		if(p.getGameMode() != GameMode.CREATIVE)
		{
			e.setCancelled(true);
			p.setAllowFlight(false);
			p.setFlying(false);
			p.setVelocity(p.getLocation().getDirection().multiply(2.0).setY(1.0));
			
			long time = p.getWorld().getTime();
			
			if(time > 0 && time < 12300)
			{
				Audible a = new GSound(Sound.ENDERDRAGON_WINGS);
				a.setPitch(0.3f);
				a.setVolume(0.7f);
				a.play(p.getLocation());
				
				a = new GSound(Sound.ENDERDRAGON_WINGS);
				a.setPitch(1.8f);
				a.setVolume(0.7f);
				a.play(p.getLocation());
				
				a = new GSound(Sound.ENDERDRAGON_WINGS);
				a.setPitch(1.3f);
				a.setVolume(0.7f);
				a.play(p.getLocation());
				
				return;
			}
			
			Audible a = new GSound(Sound.FIREWORK_LARGE_BLAST2);
			a.setPitch(0.1f);
			a.setVolume(3.7f);
			a.play(p.getLocation());
			
			int[] kx = new int[] {0};
			
			new Task(0)
			{
				@Override
				public void run()
				{
					kx[0]++;
					
					if(p.getLocation().getBlock().getRelative(0, -1, 0).getType() != Material.AIR && !p.isFlying())
					{
						cancel();
					}
					
					else
					{
						Location ll = p.getLocation();
						ll.setDirection(p.getVelocity());
						flyFx.play(ll);
						
						if(p.isSneaking() && kx[0] < 100)
						{
							p.setSneaking(false);
							
							Audible a = new GSound(Sound.FIREWORK_LARGE_BLAST);
							a.setPitch(0.1f);
							a.setVolume(4.7f);
							a.play(p.getLocation());
							
							p.setVelocity(p.getLocation().getDirection().add(p.getVelocity().normalize()).multiply(3.2).setY(0.1));
							kx[0] += 20;
							
							for(int i = 0; i < 43; i++)
							{
								Location llx = p.getLocation().clone().add(0, 1.3, 0);
								llx.setDirection(new Vector((Math.random() * 2) - 1, (Math.random() * 2) - 1, (Math.random() * 2) - 1));
								flyFx.play(llx);
							}
						}
					}
					
					if(kx[0] > 300)
					{
						cancel();
					}
				}
			};
		}
	}
	
	@EventHandler
	public void onPlayerMove(PlayerMoveEvent e)
	{
		if(!canJump(e.getPlayer()))
		{
			return;
		}
		
		Player p = e.getPlayer();
		
		if(p.getGameMode() != GameMode.CREATIVE)
		{
			p.setFlying(false);
		}
		
		if(p.getGameMode() != GameMode.CREATIVE && p.getLocation().getBlock().getRelative(0, -1, 0).getType() != Material.AIR && !p.isFlying())
		{
			p.setAllowFlight(true);
		}
		
		if(p.getLocation().getBlockY() < -5)
		{
			p.teleport(p.getWorld().getSpawnLocation());
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
	public void onEntityDamage(EntityDamageEvent e)
	{
		if(e.getEntity().getType().equals(EntityType.PLAYER) && !canJump((Player) e.getEntity()))
		{
			return;
		}
		
		if(e.getEntity() instanceof Player && e.getCause().equals(DamageCause.FALL))
		{
			e.setCancelled(true);
			
			Audible a = new GSound(Sound.DIG_WOOD);
			a.setPitch(0.44f);
			a.setVolume(1.7f);
			a.play(e.getEntity().getLocation());
		}
	}
}
