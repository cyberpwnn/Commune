package org.cyberpwn.commune.util;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.potion.Potion;
import org.phantomapi.lang.GMap;
import org.phantomapi.stack.Stack;
import org.phantomapi.stack.StackedInventory;
import org.phantomapi.stack.StackedPlayerInventory;

public class ItemManipulator
{
	public static boolean stack(Player p, int max)
	{
		StackedInventory inv = new StackedInventory(p.getInventory());
		GMap<Integer, Stack> stacks = inv.getNStacks().copy();
		
		for(int i : stacks.k())
		{
			if(!stacks.get(i).getType().equals(Material.POTION))
			{
				stacks.remove(i);
			}
			
			else
			{
				try
				{
					if(Potion.fromItemStack(p.getInventory().getItem(i)).isSplash())
					{
						stacks.remove(i);
					}
				}
				
				catch(Exception e)
				{
					stacks.remove(i);
				}
			}
		}
		
		if(stacks.isEmpty())
		{
			inv.thrash();
			
			return false;
		}
		
		for(int i : stacks.k())
		{
			if(!stacks.containsKey(i) || stacks.get(i).getType().equals(Material.AIR))
			{
				continue;
			}
			
			for(int j : stacks.k())
			{
				if(!stacks.containsKey(j) || stacks.get(j).getType().equals(Material.AIR))
				{
					continue;
				}
				
				Potion a = Potion.fromItemStack(p.getInventory().getItem(i));
				Potion b = Potion.fromItemStack(p.getInventory().getItem(j));
				
				if(a.isSplash())
				{
					stacks.remove(i);
				}
				
				if(b.isSplash())
				{
					stacks.remove(j);
				}
				
				if(stacks.contains(i) && stacks.get(i).getData() == 37)
				{
					stacks.remove(i);
				}
				
				if(stacks.contains(j) && stacks.get(j).getData() == 37)
				{
					stacks.remove(j);
				}
				
				try
				{
					if(stacks.contains(i) && stacks.contains(j) && i != j && !a.isSplash() && !b.isSplash() && a.getEffects().equals(b.getEffects()) && a.getLevel() == b.getLevel() && a.getType().equals(b.getType()) && stacks.get(i).getAmount() + stacks.get(j).getAmount() <= max)
					{
						stacks.get(i).setAmount(stacks.get(i).getAmount() + stacks.get(j).getAmount());
						stacks.put(j, new Stack(Material.AIR));
					}
				}
				
				catch(Exception e)
				{
					stacks.remove(i);
				}
			}
		}
		
		for(int i : stacks.k())
		{
			inv.setStack(i, stacks.get(i));
		}
		
		inv.thrash();
		
		return true;
	}
	
	public static void fix(Player p, StackedPlayerInventory inv, int slot)
	{
		Stack stack = inv.getStack(slot);
		
		if(stack.getType().getMaxDurability() == 0 || stack.getDurability() == 0 || stack.getType().equals(Material.POTION))
		{
			return;
		}
		
		stack.setDurability((short) 0);
		inv.setStack(slot, stack);
	}
	
	public static boolean fixHand(Player p)
	{
		if(p.getItemInHand() != null && p.getItemInHand().getDurability() > 0 && p.getItemInHand().getType().getMaxDurability() > 0)
		{
			StackedPlayerInventory inv = new StackedPlayerInventory(p.getInventory());
			fix(p, inv, p.getInventory().getHeldItemSlot());
			inv.thrash();
			
			return true;
		}
		
		return false;
	}
	
	public static void fixAll(Player p)
	{
		StackedPlayerInventory inv = new StackedPlayerInventory(p.getInventory());
		
		for(int i = 0; i < inv.getSize(); i++)
		{
			fix(p, inv, i);
		}
		
		Stack a = inv.getHelmetStack();
		a.setDurability((short) 0);
		inv.setHelmetStack(a);
		
		Stack b = inv.getChestplateStack();
		b.setDurability((short) 0);
		inv.setChestplateStack(b);
		
		Stack c = inv.getLeggingsStack();
		c.setDurability((short) 0);
		inv.setLeggingsStack(c);
		
		Stack d = inv.getBootsStack();
		d.setDurability((short) 0);
		inv.setBootsStack(d);
		
		inv.thrash();
	}
}
