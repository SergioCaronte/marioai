/*
 * Copyright (c) 2009-2010, Sergey Karakovskiy and Julian Togelius
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *  Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *  Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *  Neither the name of the Mario AI nor the
 * names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

package ch.idsia.scenarios.champ;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import ch.idsia.agents.Agent;
import ch.idsia.agents.LearningAgent;
import ch.idsia.agents.SNSLearningAgent;
import ch.idsia.benchmark.tasks.BasicTask;
import ch.idsia.benchmark.tasks.LearningTask;
import ch.idsia.tools.EvaluationInfo;
import ch.idsia.tools.MarioAIOptions;

/**
 * Created by IntelliJ IDEA.
 * User: Sergey Karakovskiy, sergey at idsia dot ch
 * Date: Mar 17, 2010 Time: 8:34:17 AM
 * Package: ch.idsia.scenarios
 */

/**
 * Class used for agent evaluation in Learning track
 * http://www.marioai.org/learning-track
 */

public final class LearningTrackBatch
{
final static long numberOfTrials = 1000;
final static boolean scoring = false;
private static int killsSum = 0;
private static float marioStatusSum = 0;
private static int timeLeftSum = 0;
private static int marioModeSum = 0;
private static boolean detailedStats = false;

final static int populationSize = 100;

private static int evaluateSubmission(MarioAIOptions marioAIOptions, LearningAgent learningAgent)
{
	// provides the level
    LearningTask learningTask = new LearningTask(marioAIOptions); 
    // it gives LearningAgent access to evaluator via method LearningTask.evaluate(Agent)
    learningAgent.setLearningTask(learningTask);  
    learningAgent.init();
    //Passing args to be saved into log
    ((SNSLearningAgent)learningAgent).setOpts(marioAIOptions);
    // it launches the training process. numberOfTrials happen here
    learningAgent.learn(); 
    // this agent will be evaluated
    Agent agent = learningAgent.getBestAgent();

    // perform the gameplay task on the same level
    //marioAIOptions.setVisualization(true);
    //marioAIOptions.setFPS(24);
    //System.out.println("LearningTrack best agent = " + agent);
    marioAIOptions.setAgent(agent);
    BasicTask basicTask = new BasicTask(marioAIOptions);
    basicTask.setOptionsAndReset(marioAIOptions);
    //System.out.println("basicTask = " + basicTask);
    //System.out.println("agent = " + agent);

    boolean verbose = false;
    if (!basicTask.runSingleEpisode(1))  // make evaluation on the same episode once
    {
        System.out.println("MarioAI: out of computational time per action! Agent disqualified!");
    }
    EvaluationInfo evaluationInfo = basicTask.getEvaluationInfo();
    //System.out.println(evaluationInfo.toString());

    int f = evaluationInfo.computeWeightedFitness();
    ((SNSLearningAgent)learningAgent).writeLog("SCORE = " + f + ";\n Details: " + evaluationInfo.toString());
    if (verbose)
    {
        System.out.println("Intermediate SCORE = " + f + ";\n Details: " + evaluationInfo.toString());
    }

    return f;
}

private static void EvaluateBatch(String[] args)
{
	int ld = Integer.parseInt(args[1]);
	int turns = 5;
	System.out.println("AGENT EVALUATION");
	
	//MarioAIOptions[] instances = new MarioAIOptions[5];
	MarioAIOptions marioOpts;
	/*
	 * Agent types: UniformProb, RJProb, RJSProb, RSJProb, RuleBased
	 */
	SNSLearningAgent[] agents = new SNSLearningAgent[4];
	agents[0] = new SNSLearningAgent("RuleBased", "smartCross", "smallElite");
	agents[1] = new SNSLearningAgent("UniformProb", "smartCross", "smallElite");
	agents[2] = new SNSLearningAgent("RJSProb", "smartCross", "smallElite");
	agents[3] = new SNSLearningAgent("RSJProb", "smartCross", "smallElite");
	
	for(SNSLearningAgent ag : agents)
	{
		String mes = "Evaluation for agent: " + ag.agentType;
		System.out.println(mes);
		appendMessage("AgentEvaluation_LD_" + ld + "_" + agents[0].getMaxGenerations() + ".txt", mes);
		
		System.out.println("Agent " + ag.agentType +" started.");
		float totalSum = 0;
		//for(int i = 0; i < instances.length; i++)
		{
			float totalTrackSum = 0;
			for(int turn = 0; turn < turns; turn++)
			{
				System.out.println("\tInstance level "+ ld +" started. turn " + turn);
				String[] args2 = new String[1];
				marioOpts = new MarioAIOptions(args2);
				marioOpts.setAgent(ag);
				marioOpts.setArgs("-ld " + ld + " -vis off -fps 100");
				//LearningAgent learningAgent = (LearningAgent) instances[i].getAgent();
			    //System.out.println("main.learningAgent = " + learningAgent + " iteration " + i);
			    
			    float finalScore = LearningTrackBatch.evaluateSubmission(marioOpts, ag);
			    totalTrackSum += finalScore;
			    System.out.println("\tInstance finished. Final Score = " + finalScore);
			}
			float trackAverage = totalTrackSum/turns;
			mes = "Average Score " + trackAverage + " Track " + ld + "\n\n";
			appendMessage("AgentEvaluation_LD_" + ld + "_" + agents[0].getMaxGenerations() + ".txt", mes);
			totalSum += trackAverage;
		}
		//float totalAverage = totalSum;
		//mes = "Average Total Score " + totalAverage + " and Total Score " + totalSum + "\n";
		//appendMessage("AgentEvaluation_" + agents[0].getMaxGenerations() + ".txt", mes);
		
		System.out.println("Agent " + ag.agentType +" finished.");
	}
}

private static void EvaluateConverge(String[] args)
{
	int ld = Integer.parseInt(args[0]);
	
	int turns = 5;
	if(args.length > 1)
		turns = Integer.parseInt(args[1]);
	
	System.out.println("AGENT CONVERGENCE EVALUATION");
	
	MarioAIOptions marioOpts;
	/*
	 * Agent types: UniformProb, RJProb, RJSProb, RSJProb, RuleBased
	 */
	SNSLearningAgent[] agents = new SNSLearningAgent[1];
	agents[0] = new SNSLearningAgent("RSJProb", "smartCross", "Competition");
	agents[0].runTillConverge = true;
	
	for(SNSLearningAgent ag : agents)
	{
		String mes = "Evaluation for agent: " + ag.agentType +  " level difficulty: " + ld;
		System.out.println(mes);
		appendMessage("AgentEvaluation_Convergence_LD_" + ld + ".txt", mes);
		
		System.out.println("Agent " + ag.agentType +" started.");
		float totalSum = 0;
		//for(int i = 0; i < instances.length; i++)
		{
			float totalTrackSum = 0;
			for(int turn = 0; turn < turns; turn++)
			{
				System.out.println("\tInstance level "+ ld +" started. turn " + turn);
				String[] args2 = new String[1];
				marioOpts = new MarioAIOptions(args2);
				marioOpts.setAgent(ag);
				marioOpts.setArgs("-ld " + ld + " -vis off -fps 100");
				//LearningAgent learningAgent = (LearningAgent) instances[i].getAgent();
			    //System.out.println("main.learningAgent = " + learningAgent + " iteration " + i);
			    
			    float finalScore = LearningTrackBatch.evaluateSubmission(marioOpts, ag);
			    totalTrackSum += finalScore;
			    System.out.println("\tInstance finished. Final Score = " + finalScore);
			    mes = "Level " + ld + ". Turn " + turn + " converged at " + ag.convergedAt;
			    appendMessage("AgentEvaluation_Convergence_LD_" + ld + ".txt", mes);
			}
			float trackAverage = totalTrackSum/turns;
			mes = "Average Score " + trackAverage + " Track " + ld;
			appendMessage("AgentEvaluation_Convergence_LD_" + ld + ".txt", mes);
			totalSum += trackAverage;
		}
		System.out.println("Agent " + ag.agentType +" finished.");
	}
}

private static void EvaluateCross(String[] args)
{
	System.out.println("CROSSOVER EVALUATION");
	
	int turns = 5;
	
	String[] crosses = new String[2];
	crosses[0] = "splitCross";
	crosses[1] = "smartCross";
	
	for(String cross : crosses)
	{
		/*
		 * Agent types: UniformProb, RJProb, RJSProb, RSJProb, RuleBased
		 * Crossover: splitCross, zipperCross
		 * Breeding: halfElite, quarterElite, quarterEliteMigration, smallElite
		 */
		
		SNSLearningAgent[] agents = new SNSLearningAgent[4];
		agents[0] = new SNSLearningAgent("RuleBased", cross, "quarterEliteMigration");
		agents[0].runTillConverge = true;
		agents[1] = new SNSLearningAgent("UniformProb", cross, "quarterEliteMigration");
		agents[1].runTillConverge = true;
		agents[2] = new SNSLearningAgent("RJSProb", cross, "quarterEliteMigration");
		agents[2].runTillConverge = true;
		agents[3] = new SNSLearningAgent("RSJProb", cross, "quarterEliteMigration");
		agents[3].runTillConverge = true;
		
		String mes = "Evaluation for crossover method: " + cross;
		System.out.println(mes);
		appendMessage("CrossoverEvaluation_" + agents[0].getMaxGenerations() + ".txt", mes);
		
		float totalSum = 0;
		for(SNSLearningAgent ag : agents)
		{
			float partialTotalSum = 0;
			for(int turn = 0; turn < turns; turn++)
			{
				ag.reset();
				System.out.println("Agent " + ag.agentType +" started. Turn " + turn);
				MarioAIOptions instances = new MarioAIOptions(args);
				instances.setAgent(ag);
				instances.setArgs("-ld 2 -vis off -fps 100");		    
			    float finalScore = LearningTrackBatch.evaluateSubmission(instances, ag);	
			    partialTotalSum += finalScore;
				System.out.println("Agent " + ag.agentType +" finished turn " + turn + " score: " + finalScore);
				mes = "Turn " + turn + " converged at " + ag.convergedAt;
				appendMessage("CrossoverEvaluation_" + agents[0].getMaxGenerations() + ".txt", mes);
			}
			partialTotalSum = partialTotalSum/turns;
			mes = ag.agentType + " score average " + partialTotalSum;
			appendMessage("CrossoverEvaluation_" + agents[0].getMaxGenerations() + ".txt", mes);
			totalSum += partialTotalSum;
		}
		totalSum = totalSum/agents.length;
		mes = cross + " method score average " + totalSum + "\n";
		appendMessage("CrossoverEvaluation_" + agents[0].getMaxGenerations() + ".txt", mes);
	}
}

private static void EvaluateBreeder(String[] args)
{
	System.out.println("BREEDING EVALUATION");
	
	int turns = 5;
	
	String[] breeder = new String[4];
	breeder[0] = "halfElite";
	breeder[1] = "quarterElite";
	breeder[2] = "quarterEliteMigration";
	breeder[3] = "smallElite";
	
	for(String breed : breeder)
	{
		/*
		 * Agent types: UniformProb, RJProb, RJSProb, RSJProb, RuleBased
		 * Crossover: splitCross, zipperCross
		 * Breeding: halfElite, quarterElite, quarterEliteMigration, smallElite
		 */
		
		SNSLearningAgent[] agents = new SNSLearningAgent[4];
		agents[0] = new SNSLearningAgent("RuleBased", "splitCross", breed);
		agents[1] = new SNSLearningAgent("UniformProb", "splitCross", breed);
		agents[2] = new SNSLearningAgent("RJSProb", "splitCross", breed);
		agents[3] = new SNSLearningAgent("RSJProb", "splitCross", breed);
		
		String mes = "Evaluation for breeding method: " + breed;
		System.out.println(mes);
		appendMessage("BreedingEvaluation_" + agents[0].getMaxGenerations() + ".txt", mes);
		
		float totalSum = 0;
		for(SNSLearningAgent ag : agents)
		{
			float partialTotalSum = 0;
			for(int turn = 0; turn < turns; turn++)
			{
				ag.reset();
				System.out.println("Agent " + ag.agentType +" started. Turn " + turn);
				MarioAIOptions instances = new MarioAIOptions(args);
				instances.setAgent(ag);
				instances.setArgs("-ld 2 -vis off -fps 100");		    
			    float finalScore = LearningTrackBatch.evaluateSubmission(instances, ag);	
			    partialTotalSum += finalScore;
				System.out.println("Agent " + ag.agentType +" finished turn " + turn + " score: " + finalScore);
			}
			partialTotalSum = partialTotalSum/turns;
			mes = ag.agentType + " score average " + partialTotalSum;
			appendMessage("BreedingEvaluation_" + agents[0].getMaxGenerations() + ".txt", mes);
			totalSum += partialTotalSum;
		}
		totalSum = totalSum/agents.length;
		mes = breed + " method score average " + totalSum + "\n";
		appendMessage("BreedingEvaluation_" + agents[0].getMaxGenerations() + ".txt", mes);
	}
}

public static void appendMessage(String logName, String str)
{
	try {
		BufferedWriter out = new BufferedWriter(new FileWriter(logName, true));
		out.write(str + "\n");
		out.close();
	} catch (IOException e) {
		e.printStackTrace();
	}
}

public static void main(String[] args)
{
	if(args.length > 0)
	{
		System.out.println("Setting generations to " + args[0]);
		SNSLearningAgent.generations = Integer.parseInt(args[0]);
	}
	//EvaluateCross(args);
	//EvaluateBreeder(args);
	EvaluateBatch(args);
	//EvaluateConverge(args);
	
    System.exit(0);
}
}
