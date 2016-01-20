package ch.idsia.evolution.ea;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.cli.CommandLine;

public class EAParameters {
	public static final String GENERATIONS = "generations";
	public static final String POP_SIZE = "pop-size";
	public static final String TOURNAMENT_SIZE = "tournament-size";
	
	public static final String CROSSOVER_PROB = "crossover-prob";
	public static final String MUTATION_PROB = "mutation-prob";
	
	public static Map<String, EAParameter<?>> defaultParameters(){
		Map<String, EAParameter<?>> params = new HashMap<>();
		
		params.put(GENERATIONS, new EAParameter<Integer>(50));
		params.put(POP_SIZE, new EAParameter<Integer>(30));
		params.put(TOURNAMENT_SIZE, new EAParameter<Integer>(2));
		params.put(CROSSOVER_PROB, new EAParameter<Float>(.95f));
		params.put(MUTATION_PROB, new EAParameter<Float>(.001f));
		
		return params;
	}

	public static Map<String, EAParameter<?>> parametersFromCommandLine(CommandLine line) {
		
		Map<String, EAParameter<?>> params = defaultParameters();
		
		if(line.hasOption(GENERATIONS)){
			params.put(GENERATIONS, new EAParameter<Integer>(Integer.parseInt(line.getOptionValue(GENERATIONS))));
		}
		
		if(line.hasOption(POP_SIZE)){
			params.put(POP_SIZE, new EAParameter<Integer>(Integer.parseInt(line.getOptionValue(POP_SIZE))));
		}
		
		if(line.hasOption(TOURNAMENT_SIZE)){
			params.put(TOURNAMENT_SIZE, new EAParameter<Integer>(Integer.parseInt(line.getOptionValue(TOURNAMENT_SIZE))));
		}
		
		if(line.hasOption(CROSSOVER_PROB)){
			params.put(CROSSOVER_PROB, new EAParameter<Float>(Float.parseFloat(line.getOptionValue(CROSSOVER_PROB))));
		}
		
		if(line.hasOption(MUTATION_PROB)){
			params.put(MUTATION_PROB, new EAParameter<Float>(Float.parseFloat(line.getOptionValue(MUTATION_PROB))));
		}
		
		
		return params;
	}
}
