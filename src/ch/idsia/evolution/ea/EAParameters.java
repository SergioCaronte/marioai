package ch.idsia.evolution.ea;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.cli.CommandLine;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import ch.idsia.agents.SNSLearningAgent;

public class EAParameters {
	public static final String GENERATIONS = "generations";
	public static final String POP_SIZE = "pop-size";
	public static final String TOURNAMENT_SIZE = "tournament-size";
	
	public static final String CROSSOVER_PROB = "crossover-prob";
	public static final String MUTATION_PROB = "mutation-prob";
	
	public static final String AGENTS = "agents";
	
	public static final String AGENT_TYPE = "type";
	public static final String AGENT_CROSSOVER = "crossover";
	public static final String AGENT_ELITISM = "elitism";
	
	public static final String DIFFICULTY = "difficulty";
	
	private static Set<String> integerParams = null;
	private static Set<String> floatParams = null;
	
	/**
	 * Returns a Set with the integer parameters of the experiment
	 * @return
	 */
	public static Set<String> integerParameters(){
		if (integerParams == null){
			integerParams = new HashSet<>();
			integerParams.add(GENERATIONS);
			integerParams.add(POP_SIZE);
			integerParams.add(TOURNAMENT_SIZE);
			integerParams.add(DIFFICULTY);
		}
		return integerParams;
	}
	
	/**
	 * Returns a Set with the float parameters of the experiment
	 * @return
	 */
	public static Set<String> floatParameters(){
		if (floatParams == null){
			floatParams = new HashSet<>();
			floatParams.add(CROSSOVER_PROB);
			floatParams.add(MUTATION_PROB);
		}
		return floatParams;
	}
	
	
	/**
	 * Returns the default parameters
	 * @return
	 */
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

	/**
	 * Extracts the parameters from command line and returns them
	 * @param line
	 * @return
	 */
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
	
	/**
	 * Reads the parameters of a xml file
	 * @param path the path to the xml file
	 * @return
	 * @throws SAXException
	 * @throws IOException
	 * @throws ParserConfigurationException
	 */
	public static Map<String, Object> parametersFromFile(String path) throws SAXException, IOException, ParserConfigurationException{
		Map<String, Object> params = defaultParameters();
		DocumentBuilder dBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		Document doc = dBuilder.parse(new File(path));
		
		//traverses all 1st level nodes of xml file (root children) 
		NodeList nList = doc.getDocumentElement().getChildNodes();
		
		for (int i = 0; i < nList.getLength(); i++){
			Node n = nList.item(i);

			//if node is 'parameters', traverses all its child nodes, setting experiment parameters accordingly
			if (n.getNodeName() == "parameters"){
				for(Node param = n.getFirstChild(); param != null; param = param.getNextSibling()){
					
					if (param.getNodeType() != Node.ELEMENT_NODE) continue;	//prevents ClassCastException
					
					//inserts the parameter on the map according to its type
					Element e = (Element) param;
					if (integerParameters().contains(param.getNodeName())){
						params.put(param.getNodeName(), Integer.parseInt(e.getAttribute("value")));
					}
					else if (floatParameters().contains(param.getNodeName())){
						params.put(param.getNodeName(), Float.parseFloat(e.getAttribute("value")));
					}
					else {	//parameter is a string (probably)
						params.put(param.getNodeName(), e.getAttribute("value"));
					}
				}
			}
			
			//if node is 'agents', traverses all its child nodes, creating one agent per node
			if (n.getNodeName() == "agents"){
				List<SNSLearningAgent> theAgents = new ArrayList<>();
				for(Node agent = n.getFirstChild(); agent != null; agent = agent.getNextSibling()){
					
					if (agent.getNodeType() != Node.ELEMENT_NODE) continue;	//prevents ClassCastException
					
					Element e = (Element) agent;
					
					//adds a new agent with the properties described in the node attributes
					theAgents.add(new SNSLearningAgent(
						e.getAttribute(AGENT_TYPE), 
						e.getAttribute(AGENT_CROSSOVER), 
						e.getAttribute(AGENT_ELITISM))
					);
					
				}
				params.put(EAParameters.AGENTS, theAgents);
			}
			
		}
		
		return params;
	}
}
