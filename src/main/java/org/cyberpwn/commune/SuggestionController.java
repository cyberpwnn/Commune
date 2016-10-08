package org.cyberpwn.commune;

import org.phantomapi.command.Command;
import org.phantomapi.command.CommandAlias;
import org.phantomapi.command.CommandFilter.PlayerOnly;
import org.phantomapi.command.PhantomCommand;
import org.phantomapi.command.PhantomSender;
import org.phantomapi.construct.Controllable;
import org.phantomapi.construct.Controller;
import org.phantomapi.text.MessageBuilder;
import org.phantomapi.text.TagProvider;
import org.phantomapi.util.C;

public class SuggestionController extends Controller implements TagProvider
{
	public SuggestionController(Controllable parentController)
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
	
	@PlayerOnly
	@CommandAlias({"bug", "suggest"})
	@Command("report")
	public void on(PhantomSender sender, PhantomCommand cmd)
	{
		sender.setMessageBuilder(new MessageBuilder(this));
	}
	
	@Override
	public String getChatTag()
	{
		return C.DARK_GRAY + "[" + C.RED + "Tracker" + C.DARK_GRAY + "]" + C.GRAY + ": ";
	}
	
	@Override
	public String getChatTagHover()
	{
		return C.RED + "This helps cyberpwn track issues reported by players ingame.";
	}
}
