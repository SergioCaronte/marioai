package ch.idsia.agents.controllers;

import java.util.Random;

import ch.idsia.agents.Agent;
import ch.idsia.benchmark.mario.engine.sprites.Mario;
import ch.idsia.benchmark.mario.engine.sprites.Sprite;
import ch.idsia.benchmark.mario.environments.Environment;

public class SergioAgent extends BasicMarioAIAgent implements Agent
{	
	private int cur_cmd = 0;
	private int[] commands;
	private Random R = null;
	
	public SergioAgent()
	{
	    super("SergioAgent");
	    reset();
	    
	    R = new Random();
	    commands = new int[3000];
	    for(int i = 0; i < 3000; i++)
	    {
	    	commands[i] = R.nextInt(32);
	    }
//	    for(int i = 0; i < 3000; i++)
//	    {
//	    	System.out.println(commands[i]);
//	    }
	}
	
	private boolean isMarioStuck()
	{
		int r = marioEgoRow;
	    int c = marioEgoCol;
		if(levelScene[r][c+1] != 0)
			return true;
		return false;
		
	}
	
	private boolean isCreature(int c)
	{
	    switch (c)
	    {
	        case Sprite.KIND_GOOMBA:
	        case Sprite.KIND_RED_KOOPA:
	        case Sprite.KIND_RED_KOOPA_WINGED:
	        case Sprite.KIND_GREEN_KOOPA_WINGED:
	        case Sprite.KIND_GREEN_KOOPA:
	            return true;
	    }
	    return false;
	}
	
	public boolean[] getAction()
	{	
		for(int i = 0; i < 6; i++)
			action[i] = false;
		
		int c = commands[cur_cmd++];
		if((c & 2) > 0)
			action[Mario.KEY_RIGHT] = true;
		if((c & 4) > 0)
			action[Mario.KEY_RIGHT] = true;
		if((c & 8) > 0 && !action[Mario.KEY_RIGHT])
			action[Mario.KEY_LEFT] = true;
		if((c & 16) > 0)
			action[Mario.KEY_JUMP] = true;
		if((c & 1) > 0)
			if(!action[Mario.KEY_RIGHT])
				action[Mario.KEY_RIGHT] = true;
			else
				action[Mario.KEY_JUMP] = true;
		if((c & 32) > 0)
			action[Mario.KEY_SPEED] = true;
		
	    /*int r = marioEgoRow;
	    int c = marioEgoCol;
		
	    action[Mario.KEY_JUMP] = (isMarioStuck() && isMarioAbleToJump) || !isMarioOnGround;
	    
	    action[Mario.KEY_SPEED] = false;
	    if(isCreature(enemies[r][c+1]) || isCreature(enemies[r][c+2]))
	    {
	    	if(marioMode == 2)
	    		action[Mario.KEY_SPEED] = true;
	    	else
	    		action[Mario.KEY_JUMP] = isMarioAbleToJump || !isMarioOnGround;
	    }*/
	    return action;
	}
		
	public void reset()
	{
	    action = new boolean[Environment.numberOfKeys];
	    //action[Mario.KEY_RIGHT] = true;
	}
}

