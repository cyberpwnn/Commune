package org.cyberpwn.commune;

import java.util.Date;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.phantomapi.lang.GTime;
import org.phantomapi.placeholder.PlaceholderHook;
import org.phantomapi.util.F;
import com.earth2me.essentials.Essentials;

public class PhantomPlaceholderHook extends PlaceholderHook
{
	@SuppressWarnings("deprecation")
	@Override
	public String onPlaceholderRequest(Player p, String q)
	{
		if(q.equalsIgnoreCase("tokens"))
		{
			try
			{
				Class<?> tm = Class.forName("com.trifractalstudios.PrisonEnchants.TokenManager");
				Object val = tm.getMethod("getBalance", Player.class).invoke(null, p);
				
				if(val != null)
				{
					return F.f((int) val);
				}
			}
			
			catch(Exception e)
			{
			
			}
			
			return "Unknown";
		}
		
		if(q.startsWith("stamp_"))
		{
			String stamp = q.split("_")[1];
			
			if(stamp.length() == 12)
			{
				int month = Integer.valueOf(stamp.substring(0, 2));
				int day = Integer.valueOf(stamp.substring(2, 4));
				int year = Integer.valueOf(stamp.substring(4, 8));
				int hour = Integer.valueOf(stamp.substring(8, 10));
				int minute = Integer.valueOf(stamp.substring(10, 12));
								
				Date nextDate = new Date(year - 1900, month - 1, day, hour, minute);
				Date currentDate = new Date();
				GTime time = new GTime(nextDate.getTime() > currentDate.getTime() ? nextDate.getTime() - currentDate.getTime() : currentDate.getTime() - nextDate.getTime());
				
				return time.to(nextDate.getTime() > currentDate.getTime() ? "left" : "ago");
			}
		}
		
		if(q.equals("player"))
		{
			Essentials e = (Essentials) Bukkit.getServer().getPluginManager().getPlugin("Essentials");
			String name = p.getName();
			String current = e.getUser(p).getNickname();
			
			if(current == null)
			{
				return name;
			}
			
			if(current != name)
			{
				return "*" + current;
			}
			
			return name;
		}
		
		return null;
	}
}
