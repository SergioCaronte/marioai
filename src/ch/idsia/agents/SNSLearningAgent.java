/*
 * SNSLearningAgent is an agent based on evolutionary algorithms created as
 * assignment of IA course of PPGCC
 */

package ch.idsia.agents;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import ch.idsia.agents.learning.SNSAgent;
import ch.idsia.benchmark.mario.environments.Environment;
import ch.idsia.benchmark.tasks.LearningTask;
import ch.idsia.evolution.ea.SNSEA;
import ch.idsia.tools.MarioAIOptions;

public class SNSLearningAgent implements LearningAgent 
{
	public static int generations = 1000;
	public static int populationSize = 100;
	
	// evolutionary agent
	private SNSAgent agent;
	public String agentType;
	public String crossType;
	public String breederType;
	// evolutionary algorithm
	private SNSEA ea;
	// learning task evaluates the population
	private LearningTask learningTask = null;
	// save the best agent found so far
	private Agent bestAgent;
	// save the best score found so far
	private float bestScore = 0;

	private long evaluationQuota; //common number of trials
	private long currentEvaluation; // number of exhausted trials
	private String name = getClass().getSimpleName();
	private MarioAIOptions opts;
	private String timeStamp;
	private String logName;
	private String levelDificulty;
	
	public boolean runTillConverge = false;
	public int convergedAt = 0;
	
	public SNSLearningAgent(String agentType, String crossType, String breederType) 
	{
		if(agentType.isEmpty())
			agentType = "RSJProb";
		
		this.agentType = agentType;
		this.crossType = crossType;
		this.breederType = breederType;
		/*
		 * Agent types: UniformProb, RJProb, RJSProb, RSJProb, RuleBased
		 */
		agent = new SNSAgent(agentType);
	}
	
	@Override
	public void init() 
	{
		reset();
		ea = new SNSEA(learningTask, agent, populationSize);
		/*
		 * Crossover: splitCross, zipperCross
		 */
		ea.crossBehavior = crossType;
		/*
		 * Breeding: halfElite, quarterElite, quarterEliteMigration
		 */
		ea.generateBehavior = breederType;
		
		timeStamp = new SimpleDateFormat("MMdd_HHmm").format(new Date());
		bestScore = 0;
	}
	
	public void learnTilConverge()
	{
		this.currentEvaluation++;
		String log = "Arguments ";
		 
		 levelDificulty = String.valueOf(this.opts.getLevelDifficulty());
		 log += "-ld " + levelDificulty;
		 log += " -ll " + opts.getLevelLength();
		 log += " -lt " + opts.getLevelType();
		 log += " -ls " + opts.getLevelRandSeed();
		 
		 log +="\n";
		 log +="EA Generation Behavior: " + ea.generateBehavior + "\n";
		 log +="EA Cross Behavior: " + ea.crossBehavior + "\n";
		 log +="Agent Behavior: " + agent.getBehavior() + "\n";
		 
		 int gen = 0;
		 while(true)
		 {
			ea.evaluateGeneration();
			 
			float fitn = ea.getBestFitnesses()[0];
			System.out.print(gen + " generation\r");
			//evaluate current generation
			// if we have a new champion, we save it
			if (fitn > bestScore)
			{
				//System.out.println("\t\tNew best with score " + fitn + " at generation " + gen);
				log += "New best agent with score " + fitn + " at generation " + gen + "\n";
				bestScore = fitn;
			    bestAgent = (Agent) ea.getBests()[0];
			    
				if(ea.hasGenerationWon())
				{
					convergedAt = gen;
					System.out.println(agent.behavior + " Agent converged at generation " + gen);
					log += "Agent has converged at generation " + gen + "\n";
					break;
				}
			}
			//create next generation
			ea.nextGeneration();
			gen++;
		 }
		 // log name
		 logName = "evolution/" + timeStamp + "_" + agent.getBehavior() + "_LD_" + levelDificulty + "_SCORE_" + bestScore + "_log.txt";
		 // white log info
		 writeLog(log);
		 writeResult();
		 writeCurve();
		 // save best agent
		 if(bestAgent != null)
		 {
			 ((SNSAgent)bestAgent).saveDna(timeStamp);
		 }
		
	}
		
	@Override
	public void learn() 
	{
		if(runTillConverge)
		{
			learnTilConverge();
			return;
		}
			
		this.currentEvaluation++;
		String log = "Arguments ";
		 
		 levelDificulty = String.valueOf(this.opts.getLevelDifficulty());
		 log += "-ld " + levelDificulty;
		 log += " -ll " + opts.getLevelLength();
		 log += " -lt " + opts.getLevelType();
		 log += " -ls " + opts.getLevelRandSeed();
		 
		 log +="\n";
		 log +="EA Generation Behavior: " + ea.generateBehavior + "\n";
		 log +="EA Cross Behavior: " + ea.crossBehavior + "\n";
		 log +="Agent Behavior: " + agent.getBehavior() + "\n";
		 
		 for (int gen = 0; gen < SNSLearningAgent.generations; gen++)
		 {
			ea.evaluateGeneration();
			 
			float fitn = ea.getBestFitnesses()[0];
			System.out.print(gen + " generation\r");
			//evaluate current generation
			// if we have a new champion, we save it
			if (fitn > bestScore)
			{
				//System.out.println("\t\tNew best with score " + fitn + " at generation " + gen);
				log += "New best agent with score " + fitn + " at generation " + gen + "\n";
				bestScore = fitn;
			    bestAgent = (Agent) ea.getBests()[0];
			}
			//create next generation
			ea.nextGeneration();
		 }
		 // log name
		 logName = "evolution/" + timeStamp + "_" + agent.getBehavior() + "_LD_" + levelDificulty + "_SCORE_" + bestScore + "_log.txt";
		 // white log info
		 writeLog(log);
		 writeResult();
		 // save best agent
		 if(bestAgent != null)
		 {
			 ((SNSAgent)bestAgent).saveDna(timeStamp);
		 }
	}
	
	/*
	 * Save the args passed to the program to print it out onto output log.
	 */
	public void setOpts(MarioAIOptions opts)
	{
		this.opts = opts;		
	}
	
	public void writeResult()
	{
		try {
			String filename = "experiments.csv";
			if(runTillConverge)
				filename = "experiments_convergence.csv";
			
			BufferedWriter out = new BufferedWriter(new FileWriter(filename, true));
			out.write(String.format("%s; %s; %s; %f; %s; %d; %s; %d\n", agent.getBehavior(), ea.crossBehavior, ea.generateBehavior, bestScore, levelDificulty, generations, timeStamp, convergedAt));
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void writeLog(String str)
	{
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter(logName, true));
			out.write("Evaluation Results:\n");
			out.write(str + "\n");
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void writeCurve()
	{
		String fileName = "evolution/" + timeStamp + "_" + agent.getBehavior() + "_LD_" + levelDificulty + "_SCORE_" + bestScore + "_curve.csv";
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter(fileName, true));
			int size = ea.meanScore.size();
			for(int i = 0; i < size; i++)
				out.write(String.format("%f; %f; %f;\n", ea.meanScore.get(i), ea.minScore.get(i), ea.maxScore.get(i)));
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	

	@Override
	public void setLearningTask(LearningTask t) 
	{
		learningTask = t;
		learningTask.setEvaluationQuota(this.getNeededEvaluationQuota());
	}

	@Override
	public void setEvaluationQuota(long num) 
	{
		//Unused
		evaluationQuota = num;
	}
	
	public long getNeededEvaluationQuota() 
	{
		return populationSize * generations;
	}
	
	public int getMaxGenerations()
	{
		return generations;
	}

	@Override
	public Agent getBestAgent() 
	{
		return bestAgent;
	}

	@Override
	public String getName()
	{
		return name;
	}

	@Override
	public void setName(String n)
	{
		this.name = n;
	}

	@Override
	public void giveReward(float reward) {}

	@Override
	public void newEpisode() {}
	
	// Agent heritage methods
	@Override
	public boolean[] getAction() 
	{
		return agent.getAction();
	}
	
	@Override
	public void integrateObservation(Environment environment) 
	{
		agent.integrateObservation(environment);
	}

	@Override
	public void giveIntermediateReward(float intermediateReward) 
	{
		agent.giveIntermediateReward(intermediateReward);
	}

	@Override
	public void reset() 
	{
		agent.reset();
	}

	@Override
	public void setObservationDetails(int rfWidth, int rfHeight, int egoRow, int egoCol)
	{
		agent.setObservationDetails(rfWidth, rfHeight, egoRow, egoCol);
	}
	
	public String toString(){
		return String.format("(%s, %s, %s)", agentType, crossType, breederType);
	}
}
