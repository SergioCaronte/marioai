package ch.idsia.evolution.ea;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.cli.CommandLine;

import ch.idsia.agents.SNSLearningAgent;

public class EAParameters {
	public static final String GENERATIONS = "generations";
	public static final String POP_SIZE = "pop-size";
	public static final String TOURNAMENT_SIZE = "tournament-size";
	
	public static final String CROSSOVER_PROB = "crossover-prob";
	public static final String MUTATION_PROB = "mutation-prob";
	
	public static final String AGENTS = "agents";
	
	public static final String DIFFICULTY = "difficulty";
	
	public static Map<String, Object> defaultParameters(){
		Map<String, Object> params = new HashMap<>();
		
		params.put(GENERATIONS, 50);
		params.put(POP_SIZE, 30);
		params.put(TOURNAMENT_SIZE, 2);
		params.put(CROSSOVER_PROB, .95f);
		params.put(MUTATION_PROB, .001f);
		
		//constructs the list of default agents
		List<SNSLearningAgent> agents = new ArrayList<>();
		agents.add(new SNSLearningAgent("RuleBased", "smartCross", "smallElite"));
		agents.add(new SNSLearningAgent("UniformProb", "smartCross", "smallElite"));
		agents.add(new SNSLearningAgent("RJSProb", "smartCross", "smallElite"));
		agents.add(new SNSLearningAgent("RSJProb", "smartCross", "smallElite"));
		
		//finally inserts the agents in the parameters
		params.put(AGENTS, agents);
		
		params.put(DIFFICULTY, 1);
		
		return params;
	}

	public static Map<String, Object> parametersFromCommandLine(CommandLine line) {
		
		Map<String, Object> params = defaultParameters();
		
		if(line.hasOption(GENERATIONS)){
			params.put(GENERATIONS, Integer.parseInt(line.getOptionValue(GENERATIONS)));
		}
		
		if(line.hasOption(POP_SIZE)){
			params.put(POP_SIZE, Integer.parseInt(line.getOptionValue(POP_SIZE)));
		}
		
		if(line.hasOption(TOURNAMENT_SIZE)){
			params.put(TOURNAMENT_SIZE, Integer.parseInt(line.getOptionValue(TOURNAMENT_SIZE)));
		}
		
		if(line.hasOption(CROSSOVER_PROB)){
			params.put(CROSSOVER_PROB, Float.parseFloat(line.getOptionValue(CROSSOVER_PROB)));
		}
		
		if(line.hasOption(MUTATION_PROB)){
			params.put(MUTATION_PROB, Float.parseFloat(line.getOptionValue(MUTATION_PROB)));
		}
		
		
		return params;
	}
}
