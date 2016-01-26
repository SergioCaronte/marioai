package ch.idsia.evolution.ea;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import ch.idsia.agents.Agent;
import ch.idsia.agents.SNSLearningAgent;
import ch.idsia.agents.learning.SNSAgent;
import ch.idsia.benchmark.tasks.Task;
import ch.idsia.evolution.EA;
import ch.idsia.evolution.Evolvable;
import ch.idsia.utils.SNSRule;

public class SNSEA implements EA 
{
	private int populationSize;
	// array of population of agents;
	private final Evolvable[] population;
	// array of calculated fitness
	private final float[] fitness;
	// number of top individuals automatically passed to new generation.
	private int elite;
	// object needed to evaluate agent
	private final Task task;
	// cross type { zipperCross, splitCross, freeSplitCross, smartCross }
	public String crossBehavior = "zipperCross";
	// generation type { halfElite, quarterElite, quarterEliteMigration, smallElite, tournamentWithElitism }
	public String generateBehavior = "halfElite";
	// random generator
	private Random R = null;
	
	private boolean generationHasWon = false;
	
	public List<Float> meanScore;
	public List<Float> maxScore;
	public List<Float> minScore;
	
	public SNSEA(Task task, Evolvable initial, int populationSize)
	{
		meanScore = new ArrayList<Float>();
		maxScore = new ArrayList<Float>();
		minScore = new ArrayList<Float>();
		
		this.elite = 1;
		this.populationSize = populationSize;
		// start population array
		this.population = new Evolvable[populationSize];
	    for (int i = 0; i < population.length; i++)
	    	// generating some random initial agent
	        population[i] = initial.getNewInstance();
	    // start fitness array
	    this.fitness = new float[populationSize];
	    // The 50% best are chosen to breed. 
	    this.elite = populationSize / 2;
	    this.task = task;
	    
	    this.R = new Random();
	}
	
	public void setRandomNumberGenerator(Random r){
		this.R = r;
	}
	
	@Override
	public Evolvable[] getBests() 
	{
		return new Evolvable[]{population[0]};
	}

	@Override
	public float[] getBestFitnesses() 
	{
		return new float[]{fitness[0]};
	}
	
	
	/**
	 * Returns an individual according to its index
	 * If they're sorted by fitness (after a sortPopulationByFitness)
	 * then index 0 returns the best individual, 1 the second best and so on
	 * @param index
	 * @return
	 */
	public Evolvable getIndividual(int index){
		return population[index];
	}

	public void evaluateGeneration()
	{
		// evaluate current population
		for (int i = 0; i < populationSize; i++)
	        evaluate(i);
		
		// sort population by fitness
		sortPopulationByFitness();
		
		maxScore.add(fitness[0]);
		minScore.add(fitness[populationSize-1]);
		
		float mean = 0;
		for (int i = 0; i < populationSize; i++)
			mean += fitness[i];
		meanScore.add(mean / populationSize);
	}
	
	private void evaluate(int which)
	{
	    population[which].reset();
	    fitness[which] = task.evaluate((Agent) population[which]);
	    if(task.hasWon())
	    	generationHasWon = true;
	    	
	}
	
	public boolean hasGenerationWon()
	{
		return generationHasWon;
	}
	
	@Override
	public void nextGeneration()
	{
		if(generateBehavior.equals("halfElite"))
			halfElite();
		else if(generateBehavior.equals("quarterElite"))
			quarterElite();
		else if(generateBehavior.equals("quarterEliteMigration"))
			quarterEliteMigration();
		else if(generateBehavior.equals("smallElite"))
			smallElite();
		else if(generateBehavior.equals("Competition"))
			competition();
		else if (generateBehavior.equals("tournamentWithElitism"))
			tournamentWithElitism();
		else
			System.out.println(String.format("WARNING: Generate Behavior '%s' not recognized.", generateBehavior));
	}
	
	public void competition()
	{
		Evolvable spring[] = new Evolvable[populationSize];
		
		for (int i = 1; i < populationSize; i++)
	    {	
			int p1 = i > 10 ? compete() : 0;	//10 first individuals will have elite as parent1
			int p2 = compete();					//2nd parent always comes from tournament
			while(p1 == p2)	p2 = compete();
			
			spring[i] = cross(population[p1], population[p2]);
			spring[i].mutate();		
	    }
		
		for(int i = 1; i < populationSize; i++)
			population[i] = spring[i];
	}
	
	private int compete()
	{
		int c1 = R.nextInt(populationSize);
		int c2 = R.nextInt(populationSize);
		int c3 = R.nextInt(populationSize);
		
		if(fitness[c1] > fitness[c2])
			if(fitness[c1] > fitness[c3])	return c1;
			else							return c3;
		else if(fitness[c2] > fitness[c3])	return c2;
		else								return c3;
	}
	
	/**
	 * Selects a number of individuals randomly
	 * and returns the index of the one with best fitness
	 * @param size number of tournament participants
	 * @return
	 */
	public int tournament(int size) {
		
		int best = 0;
		float bestFitness = Float.NEGATIVE_INFINITY;
		
		//selects the best individual from randomly sampled tournament participants
		for(int i = 0; i < size; i++){
			int index = R.nextInt(populationSize);
			if(fitness[index] > bestFitness){
				bestFitness = fitness[index];
				best = index;
			}
		}
		
		return best;
	}
	
	public void halfElite()
	{
		int popHalf = populationSize/2;
		// crossover elite and mutation phase
		// crossover phase keeps half of the population and cross the another half
		for (int i = popHalf; i < populationSize; i++)
	    {
	        population[i] = cross(population[i - popHalf], population[i - popHalf + 1]);
			population[i].mutate();
	    }	
	}
	
	public void quarterElite()
	{
		int popQuarter = populationSize/4;
		for (int j = 1; j < 4; j++)
		{
			for (int i = 0; i < popQuarter; i += 2)
			{
				population[j*popQuarter + i] = cross(population[j - 1], population[i + 1]);
				population[j*popQuarter + i].mutate();
				if(j*popQuarter + i + 1 < population.length)
				{
					population[j*popQuarter + i + 1] = cross(population[i + 1], population[j - 1]);
					population[j*popQuarter + i + 1].mutate();
				}
			}	
		}
	}
	
	public void quarterEliteMigration()
	{
		int popQuarter = populationSize/4;
		for (int j = 1; j < 3; j++)
		{
			for (int i = 0; i < popQuarter; i += 2)
			{
				population[j*popQuarter + i] = cross(population[j - 1], population[i + 1]);
				population[j*popQuarter + i].mutate();
				population[j*popQuarter + i + 1] = cross(population[i + 1], population[j - 1]);
				population[j*popQuarter + i + 1].mutate();
			}	
		}
		// Migration
		for (int j = popQuarter*3-1; j < popQuarter*4; j++)
		{
			population[j] = population[0].getNewInstance();
		}
	}
	
	public void smallElite()
	{
		int smallElite = populationSize/10;
		int child = smallElite;
		while(child < populationSize-1)
		{
			for(int i = 0; i < smallElite; i++)
			{
				for(int j = i+1; j < smallElite; j++)
				{
					if(child < population.length)
					{
						population[child] = cross(population[i], population[j]);
						population[child].mutate();
						++child;
					}
					if(child < population.length)
					{
						population[child] = cross(population[j], population[i]);
						population[child].mutate();
						++child;
					}
				}
			}
		}
	}
	
	/**
	 * Generates the next generation via tournament selection and elitism of a single individual
	 * TODO: make this method be called
	 */
	public void tournamentWithElitism(){
		Evolvable spring[] = new Evolvable[populationSize];
		
		//elite individuals go directly to new population
		for (int i = 0; i < SNSLearningAgent.elitism; i++){
			spring[i] = population[i];	
		}
		
		//other individuals are selected via tournament
		for (int i = SNSLearningAgent.elitism; i < populationSize; i+=2) {
			//parents selected via tournament
			int p1 = tournament(SNSLearningAgent.tournamentSize);	
			int p2 = tournament(SNSLearningAgent.tournamentSize);
			while(p1 == p2)	p2 = tournament(SNSLearningAgent.tournamentSize);
			
			//performs crossover if probability is matched
			if(R.nextFloat() < SNSLearningAgent.crossoverProb){
				if (crossBehavior.equals("freeSplitCross")){
					int point = R.nextInt(SNSAgent.DNA_LENGTH);
					
					spring[i] = cross(population[p1], population[p2], point);
					if (i+1 < populationSize) spring[i+1] = cross(population[p2], population[p1], point);
				}
				else {
					spring[i] = cross(population[p1], population[p2]);
					if (i+1 < populationSize) spring[i+1] = cross(population[p2], population[p1]);
				}
				
			}
			else{
				spring[i] = population[p1];
				if (i+1 < populationSize) spring[i+1] = population[p2];
			}
			
			//performs mutation if probability is reached
			if(R.nextFloat() < SNSLearningAgent.mutationProb){
				spring[i].mutate();
			}
			if(i+1 < populationSize && R.nextFloat() < SNSLearningAgent.mutationProb){
				spring[i+1].mutate();
			}
	    }
		
		//replaces old population with the new one
		for(int i = 1; i < populationSize; i++) {
			population[i] = spring[i];
		}
	}
	
	/**
	 * Performs crossover between two parents and generate ONE individual
	 * If crossBehavior is splitCross or freeSplitCross, then 
	 * it will use the crossoverPoint received as parameter
	 * @param parent1
	 * @param parent2
	 * @param crossoverpoint point at which chromosomes are swapped (for [free]splitCross only)
	 * @return
	 */
	public Evolvable cross(Evolvable parent1, Evolvable parent2, int crossoverPoint)
	{
		if(((SNSAgent)parent1).getBehavior().equals("RuleBased"))
		{	
			if(crossBehavior.equals("zipperCross"))
				return ruleZipperCross(parent1, parent2);
			else if(crossBehavior.equals("splitCross"))
				return ruleSplitCross(parent1, parent2, crossoverPoint);
			else if(crossBehavior.equals("smartCross"))
				return ruleSplitCross(parent1, parent2, crossoverPoint);
			else
				System.out.println("WARNING: Cross Behavior not defined.");
		}
		else
		{
			if(crossBehavior.equals("zipperCross"))
				return zipperCross(parent1, parent2);
			
			else if (crossBehavior.equals("splitCross"))
				return splitCross(parent1, parent2, crossoverPoint);
			
			else if (crossBehavior.equals("freeSplitCross")){
				return splitCross(parent1, parent2, crossoverPoint);
			}
			
			else if(crossBehavior.equals("smartCross")) {
				int split = ((SNSAgent)parent1).getCurrentMove() - R.nextInt(30);
				return splitCross(parent1, parent2, split);
			}
			else
				System.out.println("WARNING: Cross Behavior not defined.");
		}

		return zipperCross(parent1, parent2);
	}
	
	/**
	 * Performs crossover between two parents and generate ONE individual
	 * TODO: parameterize the 'randomness' in the crossover point
	 * @param parent1
	 * @param parent2
	 * @return
	 */
	private Evolvable cross(Evolvable parent1, Evolvable parent2)
	{
		if(((SNSAgent)parent1).getBehavior().equals("RuleBased"))
		{	
			int split = R.nextInt(7) + 1;
			if(crossBehavior.equals("zipperCross"))
				return ruleZipperCross(parent1, parent2);
			else if(crossBehavior.equals("splitCross"))
				return ruleSplitCross(parent1, parent2, split);
			else if(crossBehavior.equals("smartCross"))
				return ruleSplitCross(parent1, parent2, split);
			else
				System.out.println("WARNING: Cross Behavior not defined.");
		}
		else
		{
			int split = R.nextInt(1000) + 100;
			if(crossBehavior.equals("zipperCross"))
				return zipperCross(parent1, parent2);
			else if (crossBehavior.equals("splitCross"))
				return splitCross(parent1, parent2, split);
			else if (crossBehavior.equals("freeSplitCross")){
				return splitCross(parent1, parent2, R.nextInt(3000));
			}
			else if(crossBehavior.equals("smartCross"))
			{
				split = ((SNSAgent)parent1).getCurrentMove() - R.nextInt(30);
				return splitCross(parent1, parent2, split);
			}
			else
				System.out.println("WARNING: Cross Behavior not defined.");
		}

		return zipperCross(parent1, parent2);
	}
	
	private Evolvable ruleZipperCross(Evolvable parent1, Evolvable parent2)
	{
		SNSRule[] rulesP1 = ((SNSAgent)parent1).getRules();
		SNSRule[] rulesP2 = ((SNSAgent)parent1).getRules();
		
		int rsize = rulesP1.length;
		SNSRule[] rulesChild = new SNSRule[rsize];
		for(int i = 0; i < rsize; i++)
		{		
			if(i%2 == 0)
				rulesChild[i] = rulesP1[i];
			else
				rulesChild[i] = rulesP2[i];
		}
		return new SNSAgent(rulesChild, ((SNSAgent)parent1).behavior);
	}
	
	private Evolvable zipperCross(Evolvable parent1, Evolvable parent2)
	{
		//dna of first parent
		int[] dnaP1 = ((SNSAgent)parent1).getDna();
		//dna of second parent
		int[] dnaP2 = ((SNSAgent)parent2).getDna();
		
		//result dna
		int dnaChild[] = new int[3000];
		//simple cross
		for(int i = 0; i < 3000; i++)
		{		
			if(i%2 == 0)
				dnaChild[i] = dnaP1[i];
			else
				dnaChild[i] = dnaP2[i];
		}
		return new SNSAgent(dnaChild, ((SNSAgent)parent1).behavior);	
	}
	
	private Evolvable ruleSplitCross(Evolvable parent1, Evolvable parent2, int split)
	{
		SNSRule[] rulesP1 = ((SNSAgent)parent1).getRules();
		SNSRule[] rulesP2 = ((SNSAgent)parent1).getRules();
		
		
		
		int rsize = rulesP1.length;
		SNSRule[] rulesChild = new SNSRule[rsize];
		for(int i = 0; i < rsize; i++)
		{		
			if(i < split)
				rulesChild[i] = rulesP1[i].copy();
			else
				rulesChild[i] = rulesP2[i].copy();
		}
		return new SNSAgent(rulesChild, ((SNSAgent)parent1).behavior);
	}
	
	private Evolvable splitCross(Evolvable parent1, Evolvable parent2, int split)
	{
		//dna of first parent
		int[] dnaP1 = ((SNSAgent)parent1).getDna();
		//dna of second parent
		int[] dnaP2 = ((SNSAgent)parent2).getDna();
		
		//result dna
		int dnaChild[] = new int[3000];
		//simple cross
		for(int i = 0; i < 3000; i++)
		{		
			if(i < split)
				dnaChild[i] = dnaP1[i];
			else
				dnaChild[i] = dnaP2[i];
		}
		return new SNSAgent(dnaChild, ((SNSAgent)parent1).behavior);
		
	}
	
 	private void sortPopulationByFitness()
	{
	    for (int i = 0; i < population.length; i++)
	        for (int j = i + 1; j < population.length; j++)
	            if (fitness[i] < fitness[j])
	                swap(i, j);
	}

	private void swap(int i, int j)
	{
	    float cache = fitness[i];
	    fitness[i] = fitness[j];
	    fitness[j] = cache;
	    Evolvable gcache = population[i];
	    population[i] = population[j];
	    population[j] = gcache;
	}
}
