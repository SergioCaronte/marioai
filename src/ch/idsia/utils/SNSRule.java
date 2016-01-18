package ch.idsia.utils;

import java.util.Random;

import ch.idsia.benchmark.mario.engine.sprites.Mario;

public class SNSRule {
	
	public final static int IS_MARIO_FIRE = 0;
	public final static int MAY_MARIO_JUMP = 1;
	public final static int IS_MARIO_ON_GROUND = 2;
	public final static int IS_ENEMY_UPPER_LEFT = 3;
	public final static int IS_ENEMY_UPPER_RIGHT = 4;
	public final static int IS_ENEMY_LOWER_LEFT = 5;
	public final static int IS_ENEMY_LOWER_RIGHT = 6;
	public final static int IS_OBSTACLE_AHEAD = 7;
	public final static int IS_PIT_AHEAD = 8;
	public final static int IS_PIT_BELOW = 9;
	
	public final static int FALSE = -1;
	public final static int TRUE = 1;
	public final static int DONT_CARE = 0;
	
	private int[] conditions;
	private boolean[] actions;
	private Random R = null;
		
	public SNSRule()	{
		conditions = new int[10];
		actions = new boolean[6];
		R = new Random();
	}
	
	public SNSRule copy(){
		SNSRule cpy = new SNSRule();
		for(int i = 0; i < 10; i++)
			cpy.conditions[i] = this.conditions[i];
		for(int i = 0; i < 6; i++)
			cpy.actions[i] = this.actions[i];
		return cpy;
	}
	
	public boolean validate(boolean[] sensor){
		boolean isValid = true;
		for(int i = 0; i < conditions.length; i++){
			// Se sensor é verdadeiro e condição tem que não acontencer, então é inválida
			if(sensor[i] && conditions[i] == FALSE)
				isValid = false;
			// Se sensor é falso e condição tem que acontecer, então é inválida
			else if(!sensor[i] && conditions[i] == TRUE)
				isValid = false;
		}
		return isValid;
	}
	
	public void mutate(){
		this.conditions[R.nextInt(10)] = R.nextInt(3) -1;
		this.actions[R.nextInt(6)] = (R.nextInt(100)%2 == 0);
		if(this.actions[Mario.KEY_RIGHT])
			this.actions[Mario.KEY_LEFT] = false;
		
	}
	
	public void setCondition(int idx, int condition){
		this.conditions[idx] = condition;
	}
	
	public void setConditions(int[] conditions){
		for(int i = 0; i < this.conditions.length; i++)
			this.conditions[i] = conditions[i];
	}
	
	public void randomConditions(){
		for(int i = 0; i < conditions.length; i++){
			this.conditions[i] = R.nextInt(3) -1;
		}
	}
	
	public void setAction(int idx, boolean action){
		this.actions[idx] = action;
	}
	
	public void setActions(boolean[] actions){
		for(int i = 0; i < this.actions.length; i++)
			this.actions[i] = actions[i];
		
		if(this.actions[Mario.KEY_RIGHT])
			this.actions[Mario.KEY_LEFT] = false;
	}
	
	public void randomActions(){
		for(int i = 0; i < actions.length; i++){
			this.actions[i] = (R.nextInt(100)%2 == 0);
		}
	}
	
	public boolean[] getAction(){
		return actions;
	}
	
	@Override
	public String toString(){
		String r = "";
		for(int i = 0; i < conditions.length; i++)
		{
			switch(conditions[i])
			{
			case DONT_CARE: r += "\t\t\t"; break;
			case TRUE: r += "TRUE\t\t"; break;
			case FALSE: r += "FALSE\t\t"; break;
			}
		}
		for(int i = 0; i < actions.length; i++)
		{
			if(actions[i])
				r += "T\t\t\t";
			else
				r += "\t\t\t";
		}
		return r;
	}

}