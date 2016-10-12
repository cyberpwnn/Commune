package org.cyberpwn.commune.frtp;

import org.phantomapi.construct.Controllable;
import org.phantomapi.construct.Controller;

public class FRTP extends Controller
{
	private RTPController chunkController;
	
	public FRTP(Controllable parentController)
	{
		super(parentController);
		
		chunkController = new RTPController(this);
		
		register(chunkController);
	}
	
	@Override
	public void onStart()
	{
		
	}
	
	@Override
	public void onStop()
	{
		
	}
}
