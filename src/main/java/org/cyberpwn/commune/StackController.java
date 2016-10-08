package org.cyberpwn.commune;

import org.cyberpwn.commune.util.ItemManipulator;
import org.phantomapi.command.CommandController;
import org.phantomapi.command.CommandFilter;
import org.phantomapi.command.PhantomCommand;
import org.phantomapi.command.PhantomCommandSender;
import org.phantomapi.construct.Controllable;
import org.phantomapi.inventory.PhantomPlayerInventory;
import org.phantomapi.sync.TaskLater;

public class StackController extends CommandController 
{
	public StackController(Controllable parentController)
	{
		super(parentController, "kit");
	}

	@Override
	public void onStart()
	{
		
	}

	@Override
	public void onStop()
	{
		
	}

	@CommandFilter.PlayerOnly
	@Override
	public boolean onCommand(PhantomCommandSender sender, PhantomCommand cmd)
	{
		int left = new PhantomPlayerInventory(sender.getPlayer().getInventory()).getSlotsLeft();
		
		new TaskLater(3)
		{
			@Override
			public void run()
			{
				if(new PhantomPlayerInventory(sender.getPlayer().getInventory()).getSlotsLeft() != left && sender.isPlayer() && ((CommuneController)parentController).kitStack)
				{
					ItemManipulator.stack(sender.getPlayer(), ((CommuneController)parentController).maxStack);
				}
			}
		};
		
		return false;
	}
}
