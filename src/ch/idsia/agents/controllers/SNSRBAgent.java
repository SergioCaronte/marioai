package ch.idsia.agents.controllers;

import java.util.Vector;

import com.sun.xml.internal.bind.v2.schemagen.xmlschema.List;

import ch.idsia.agents.Agent;
import ch.idsia.benchmark.mario.engine.sprites.Mario;
import ch.idsia.benchmark.mario.engine.sprites.Sprite;
import ch.idsia.benchmark.mario.environments.Environment;
import ch.idsia.utils.SNSRule;

public class SNSRBAgent extends BasicMarioAIAgent implements Agent {

	private Vector<SNSRule> rules;
	private SNSRule base_rule;
	
	private int row, col;
	private int lrow, lcol;
	private int lstuck;
	
	public SNSRBAgent() 
	{
		super("SNS RuleBased");
		reset();
		
	    rules = new Vector<SNSRule>();
	    	    
	    base_rule = new SNSRule();
	    base_rule.setAction(Mario.KEY_RIGHT, true);
	}
	
	public void addRule(SNSRule r){
		rules.add(r);
	}
	
	public void setRule(int idx, SNSRule r){
		rules.set(idx, r);
	}
	
	public void swapRule(int f, int t){
		SNSRule tmp = rules.get(t);
		rules.set(t, rules.get(f));
		rules.set(f, tmp);
	}
	
	public void setRules(SNSRule[] r){
		for(int i = 0; i < r.length; i++)
			rules.add(r[i]);
	}
	
	public SNSRule getRule(int idx){
		return rules.get(idx);
	}
	
	public SNSRule[] getRules(){
		SNSRule[] rs = new SNSRule[rules.size()];
		for(int i = 0; i < rules.size(); i++)
			rs[i] = rules.get(i);
		return rs;
	}
	
	public boolean[] getAction()
	{	
		row = (int)marioFloatPos[0];
	    col = (int)marioFloatPos[1];
	    if(row == lrow && col == lcol)	lstuck++;
	    else					    	lstuck = 0;
		lrow = row;
		lcol = col;
	    
	    //printScenario();
	    
	    if(lstuck > 5){
	    	lstuck = 0;
	    	return action;
	    }
	    		
		boolean[] world_state = new boolean[10];
		world_state[SNSRule.IS_MARIO_FIRE] = (marioMode == 2);
		world_state[SNSRule.MAY_MARIO_JUMP] = isMarioAbleToJump;
		world_state[SNSRule.IS_MARIO_ON_GROUND] = isMarioOnGround;
		world_state[SNSRule.IS_ENEMY_UPPER_LEFT] = isEnemyUpperLeft();
		world_state[SNSRule.IS_ENEMY_UPPER_RIGHT] = isEnemyUpperRight();
		world_state[SNSRule.IS_ENEMY_LOWER_LEFT] = isEnemyLowerLeft();
		world_state[SNSRule.IS_ENEMY_LOWER_RIGHT] = isEnemyLowerRight();
		world_state[SNSRule.IS_OBSTACLE_AHEAD] = isObstacleAhead();
		world_state[SNSRule.IS_PIT_AHEAD] = isPitAhead();
		world_state[SNSRule.IS_PIT_BELOW] = isPitBelow();
		
		for(int i = 0; i < rules.size(); i++){
			if(rules.get(i).validate(world_state)){
				return rules.get(i).getAction();
			}
		}
		
		return base_rule.getAction();
	}
	
	private boolean isEnemyUpperLeft(){
	    return (isCreature(enemies[marioEgoRow - 1][marioEgoCol - 2]) || 
	    		isCreature(enemies[marioEgoRow - 1][marioEgoCol - 1]) ||
	    		isCreature(enemies[marioEgoRow - 2][marioEgoCol - 2]) || 
	    		isCreature(enemies[marioEgoRow - 2][marioEgoCol - 1]));
	}
	
	private boolean isEnemyUpperRight(){
	    return (isCreature(enemies[marioEgoRow - 1][marioEgoCol + 2]) || 
	    		isCreature(enemies[marioEgoRow - 1][marioEgoCol + 1]) ||
	    		isCreature(enemies[marioEgoRow - 2][marioEgoCol + 2]) || 
	    		isCreature(enemies[marioEgoRow - 2][marioEgoCol + 1]));
	}
	
	private boolean isEnemyLowerLeft(){
	    return (isCreature(enemies[marioEgoRow][marioEgoCol - 2]) || 
	    		isCreature(enemies[marioEgoRow][marioEgoCol - 1]) ||
	    		isCreature(enemies[marioEgoRow + 1][marioEgoCol - 2]) || 
	    		isCreature(enemies[marioEgoRow + 1][marioEgoCol - 1]));
	}
	
	private boolean isEnemyLowerRight(){
	    return (isCreature(enemies[marioEgoRow][marioEgoCol + 2]) || 
	    		isCreature(enemies[marioEgoRow][marioEgoCol + 1]) ||
	    		isCreature(enemies[marioEgoRow + 1][marioEgoCol + 2]) || 
	    		isCreature(enemies[marioEgoRow + 1][marioEgoCol + 1]));
	}
	
	private boolean isObstacleAhead(){	    
		return (levelScene[marioEgoRow][marioEgoCol+1] != 0);
	}
	
	private boolean isPitAhead(){
		int rowBelow1 = Math.min(marioEgoRow+1, levelScene.length-1);
		int rowBelow2 = Math.min(marioEgoRow+2, levelScene.length-1);
		int rowBelow3 = Math.min(marioEgoRow+3, levelScene.length-1);
		int rowBelow4 = Math.min(marioEgoRow+4, levelScene.length-1);
		int rowBelow5 = Math.min(marioEgoRow+5, levelScene.length-1);
		
		return (levelScene[rowBelow5][marioEgoCol+1] == 0 &&
				levelScene[rowBelow4][marioEgoCol+1] == 0 &&
				levelScene[rowBelow3][marioEgoCol+1] == 0 &&
				levelScene[rowBelow2][marioEgoCol+1] == 0 &&
				levelScene[rowBelow1][marioEgoCol+1] == 0);
	}
	
	private boolean isPitBelow(){
		int rowBelow1 = Math.min(marioEgoRow+1, levelScene.length-1);
		int rowBelow2 = Math.min(marioEgoRow+2, levelScene.length-1);
		int rowBelow3 = Math.min(marioEgoRow+3, levelScene.length-1);
		int rowBelow4 = Math.min(marioEgoRow+4, levelScene.length-1);
		int rowBelow5 = Math.min(marioEgoRow+5, levelScene.length-1);
		
		return (levelScene[rowBelow5][marioEgoCol] == 0 &&
				levelScene[rowBelow4][marioEgoCol] == 0 &&
				levelScene[rowBelow3][marioEgoCol] == 0 &&
				levelScene[rowBelow2][marioEgoCol] == 0 &&
				levelScene[rowBelow1][marioEgoCol] == 0);
	}
	
	private void printScenario(){
		System.out.print("\n");
		for(int r = 0; r < levelScene.length; r++){
			for(int c = 0; c < levelScene[r].length; c++)
				System.out.print("|" + String.format("%03d", levelScene[r][c]) + "");
			System.out.print("|\n");
		}
		System.out.print(isObstacleAhead());
		
		
	}
	
	public void reset()
	{
	    action = new boolean[Environment.numberOfKeys];
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

}
