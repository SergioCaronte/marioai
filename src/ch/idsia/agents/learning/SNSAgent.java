package ch.idsia.agents.learning;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

import ch.idsia.agents.Agent;
import ch.idsia.agents.controllers.BasicMarioAIAgent;
import ch.idsia.agents.controllers.SNSRBAgent;
import ch.idsia.benchmark.mario.engine.sprites.Mario;
import ch.idsia.benchmark.mario.environments.Environment;
import ch.idsia.evolution.Evolvable;
import ch.idsia.utils.SNSRule;

public class SNSAgent extends BasicMarioAIAgent implements Evolvable, Agent 
{
	static private final String name = "SNSAgent";
	private int cur = 0;
	private int[] dna;
	private Random R = null;
	private SNSRBAgent rule_based = null;
	private int ruleSize = 10;
	private int mutOrderingChance = 10;
	private int mutRenewChance = 10;
	private int mutSwapChance = 20;
	
	public String behavior = "";
	
	public SNSAgent(String behavior)
	{
	    super(name);
	    reset();
	    R = new Random();
	    dna = new int[3000];
	    
	    this.behavior = behavior;
	    if(behavior.equals("RuleBased"))
	    {
	    	rule_based = new SNSRBAgent();
	    	for(int i = 0; i < ruleSize; i++){
	    		SNSRule r = new SNSRule();
	    		r.randomConditions();
	    		r.randomActions();
	    		rule_based.addRule(r);
	    	}
	    }
	    else
	    {
	    	for(int i = 0; i < 3000; i++)
		    {
		    	dna[i] = R.nextInt(63);
		    }
	    }
	}
	
	public SNSAgent(int[] dna, String behavior)
	{
		super(name);
		if(behavior.equals("RuleBased"))
			System.out.println("Wrong Constructor, it should call RuleBased Method.");
		
		this.behavior = behavior;
	    reset();
	    R = new Random();
	    this.dna = dna;
	}
	
	public SNSAgent(SNSRule[] rules, String behavior)
	{
	    super(name);
		if(!behavior.equals("RuleBased"))
			System.out.println("Wrong Constructor, it should call Probabilistic Method.");
	    
	    this.behavior = "RuleBased";
	    rule_based = new SNSRBAgent();
	    rule_based.setRules(rules);
	    
	    reset();
	    R = new Random();
	    this.dna = new int[3000];
	}
	
	public String getBehavior()
	{
		return behavior;
	}
	
	@Override
	public boolean[] getAction()
	{	
		if(behavior.equals("UniformProb"))
			return getUniformProb(dna[cur++ %3000]);
		else if(behavior.equals("RJProb"))
			return getRJProb(dna[cur++ %3000]);
		else if(behavior.equals("RJSProb"))
			return getRJSProb(dna[cur++ %3000]);
		else if(behavior.equals("RSJProb"))
			return getRSJProb(dna[cur++ %3000]);
		else if(behavior.equals("RuleBased"))
			return getRuleBased();
		else
			System.out.println("WARNING: Agent Behavior not defined.");
		return getUniformProb(dna[cur++ %3000]);
	}
	
	
	/**
	 * GetUniformProb has the same probability to press any button.
	 * Thus any button has 1/6 chance.
	 * @param section
	 * @return
	 */
	public  boolean[] getUniformProb(int section)
	{
		for(int i = 0; i < 6; i++)
			action[i] = false;
				
		// if section has bit 000001, then press down
		action[Mario.KEY_DOWN] = ((section & 1) > 0);
		// if section has bit 000010, then press right
		action[Mario.KEY_RIGHT] = ((section & 2) > 0);
		// if section has bit 000100, so press up, kinda useless
		action[Mario.KEY_UP] = ((section & 4) > 0);
		// if section has bit 001000 and right is not pressed, so press left 
		action[Mario.KEY_LEFT] = ((section & 8) > 0);
		// if section has bit 010000, so press jump 
		action[Mario.KEY_JUMP] = ((section & 16) > 0);
		// if section has bit 100000, so press speed
		action[Mario.KEY_SPEED] = ((section & 32) > 0);
	    return action;	
	}
	
	/*
	 * GetRJSProb has a bigger probability to press Right, Jump and Speed.
	 * Right has 3/6 to be pressed
	 * Jump has 1/6 * 3/6(1 - Right) to be pressed.
	 * Speed has 1/6 * (1 - Jump) to be pressed. 
	 */
	public boolean[] getRJSProb(int section)
	{
		for(int i = 0; i < 6; i++)
			action[i] = false;
	
		if((section & 2) > 0)
			action[Mario.KEY_RIGHT] = true;
		if((section & 4) > 0)
			action[Mario.KEY_RIGHT] = true;
		if((section & 8) > 0 && !action[Mario.KEY_RIGHT])
			action[Mario.KEY_LEFT] = true;
		if((section & 16) > 0)
			action[Mario.KEY_JUMP] = true;
		if((section & 32) > 0)
			action[Mario.KEY_SPEED] = true;
		if((section & 1) > 0)
			if(!action[Mario.KEY_RIGHT])
				action[Mario.KEY_RIGHT] = true;
			else if((isMarioAbleToJump || !isMarioOnGround))
				action[Mario.KEY_JUMP] = true;
			else
				action[Mario.KEY_SPEED] = true;
		
		return action;	
	}
	
	/*
	 * GetRSJProb has a bigger probability to press Right, Speed and Jump.
	 * Right has 3/6 to be pressed
	 * Speed has 1/6 * 3/6(1 - Right) to be pressed.
	 * Jump has 1/6 * (1 - Speed) to be pressed. 
	 */
	public boolean[] getRSJProb(int section)
	{
		for(int i = 0; i < 6; i++)
			action[i] = false;
	
		if((section & 2) > 0)
			action[Mario.KEY_RIGHT] = true;
		if((section & 4) > 0)
			action[Mario.KEY_RIGHT] = true;
		if((section & 16) > 0)
			action[Mario.KEY_JUMP] = true;
		if((section & 32) > 0)
			action[Mario.KEY_SPEED] = true;
		if((section & 1) > 0)
			if(!action[Mario.KEY_RIGHT])
				action[Mario.KEY_RIGHT] = true;
			else if(!action[Mario.KEY_SPEED])
				action[Mario.KEY_SPEED] = true;
			else if((isMarioAbleToJump || !isMarioOnGround))
				action[Mario.KEY_JUMP] = true;
		if((section & 8) > 0 && !action[Mario.KEY_RIGHT])
			action[Mario.KEY_LEFT] = true;
		
		return action;	
	}
	
	/*
	 * GetRJProb has a bigger probability to press Right and Jump.
	 * Right has 3/6 to be pressed
	 * Jump has 1/6 * 3/6(1 - Right) to be pressed. 
	 */
	public boolean[] getRJProb(int section)
	{
		for(int i = 0; i < 6; i++)
			action[i] = false;
		
		if((section & 2) > 0)
			action[Mario.KEY_RIGHT] = true;
		if((section & 4) > 0)
			action[Mario.KEY_RIGHT] = true;
		if((section & 8) > 0 && !action[Mario.KEY_RIGHT])
			action[Mario.KEY_LEFT] = true;
		if((section & 16) > 0)
			action[Mario.KEY_JUMP] = true;
		if((section & 1) > 0)
			if(!action[Mario.KEY_RIGHT])
				action[Mario.KEY_RIGHT] = true;
			else
				action[Mario.KEY_JUMP] = true;
		if((section & 32) > 0)
			action[Mario.KEY_SPEED] = true;
		
		return action;	
	}
	
	public boolean[] getRuleBased()
	{
		boolean[] act = rule_based.getAction();
		int dna = 0;
		dna |= act[Mario.KEY_DOWN] 	? 1 : 0;
		dna |= act[Mario.KEY_RIGHT] ? 2 : 0;
		dna |= act[Mario.KEY_UP] 	? 4 : 0;
		dna |= act[Mario.KEY_LEFT] 	? 8 : 0;
		dna |= act[Mario.KEY_JUMP] 	? 16 : 0;
		dna |= act[Mario.KEY_SPEED] ? 32 : 0;
		this.dna[cur++] = dna;
	
		return act;
	}
	
	@Override
	public Evolvable getNewInstance() 
	{
		return new SNSAgent(this.behavior);
	}

	@Override
	public Evolvable copy() 
	{
		if(behavior.equals("RuleBased"))
			return new SNSAgent(this.rule_based.getRules(), this.behavior);
		return new SNSAgent(this.dna, this.behavior);
	}

	@Override
	public void mutate() 
	{
		if(rule_based != null)
			mutateRB();
		else
			mutateProb();
	}
	
	private void mutateRB()
	{
		//FH24P5BDR6
		// troca algum valor dentro de alguma regra
		if(R.nextInt(100) < mutSwapChance)
		{
			rule_based.getRule(R.nextInt(ruleSize)).mutate();
		}
		// muda a ordem de uma par de regra
		if(R.nextInt(100) < mutOrderingChance)
		{
			rule_based.swapRule(R.nextInt(ruleSize), R.nextInt(ruleSize));
		}
		// chance de gerar uma nova regra
		if(R.nextInt(100) < mutRenewChance)
		{
			SNSRule nr = new SNSRule();
			nr.randomActions(); nr.randomConditions();
			rule_based.setRule(R.nextInt(ruleSize), nr);
		}
		
	}
	
	private void mutateProb()
	{
		int mutation_count = 100 + R.nextInt(200);
		
		for(int i = 0; i < mutation_count; i++)
		{
			int at = R.nextInt(3000);
			dna[at] = mutate(dna[at]);
		}
	}
	
	private int mutate(int section)
	{
		section = R.nextInt(63);
		return section;
	}
	
	public void reset()
	{
	    cur = 0;
	    if(rule_based != null)
	    	rule_based.reset();
	}
	
	public int getCurrentMove()
	{
		return cur;
	}
	
	public void setDna(int[] dna)
	{
		this.dna = dna;
	}
	
	public int[] getDna()
	{
		return this.dna;
	}
	
	public void setRules(SNSRule[] rules)
	{
		if(rule_based == null)
			System.out.println("WARNING: Setting Rules to a non-RuleBased Agent.");
		rule_based.setRules(rules);	
	}
	
	public SNSRule[] getRules()
	{
		if(rule_based == null)
		{	
			System.out.println("WARNING: Getting Rules from a non-RuleBased Agent.");
			return new SNSRule[0];
		}
		return rule_based.getRules();	
	}
	
	public void saveDna(String id)
	{
		BufferedWriter out;
		try {
			out = new BufferedWriter(new FileWriter("evolution/" + id + "_" + behavior + "_agent.txt", true));
			for(int i = 0; i < 3000; i++)
				out.write(dna[i] + "\n");
	        out.close();
	        
	        if(behavior.equals("RuleBased"))
	        	saveRuleTable(id);
	        
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void saveRuleTable(String id){
		BufferedWriter out;
		try {
			out = new BufferedWriter(new FileWriter("evolution/" + id + "_rule_table.txt", false));

			out.write("IS_FIRE\t\tCAN_JUMP\tON_GROUND\tFOE_UP_LT\tFOE_UP_RT\tFOE_LW_LT\tFOE_LW_RT\t"
					+ "OBSTACLE\tPIT_AHEAD\tPIT_BELOW\tLEFT\t\tRIGHT\t\tDOWN\t\tJUMP\t\tSPEED\t\tUP\n");
			
			for(int i = 0; i < ruleSize; i++)
				out.write(rule_based.getRule(i).toString() + "\n");
	        out.close();
	        	        
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	// Passing data to Rule Based Agent
	public void integrateObservation(final Environment environment)
	{
		if(rule_based != null)
			rule_based.integrateObservation(environment);
	}

	public void giveIntermediateReward(final float intermediateReward)
	{
		if(rule_based != null)
			rule_based.giveIntermediateReward(intermediateReward);
	}

	public void setObservationDetails(final int rfWidth, final int rfHeight, final int egoRow, final int egoCol)
	{
		if(rule_based != null)
			rule_based.setObservationDetails(rfWidth, rfHeight, egoRow, egoCol);
	}
}
