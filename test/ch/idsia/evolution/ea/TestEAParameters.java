package ch.idsia.evolution.ea;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;

import org.junit.Test;
import org.xml.sax.SAXException;

import ch.idsia.agents.SNSLearningAgent;

public class TestEAParameters {

	@Test
	public void testIntegerParameters() {
		Set<String> intParams = EAParameters.integerParameters();
		
		assertTrue(intParams.contains(EAParameters.GENERATIONS));
		assertTrue(intParams.contains(EAParameters.POP_SIZE));
		assertTrue(intParams.contains(EAParameters.DIFFICULTY));
		assertTrue(intParams.contains(EAParameters.REPETITIONS));
		assertTrue(intParams.contains(EAParameters.TOURNAMENT_SIZE));
		assertTrue(intParams.contains(EAParameters.ELITISM));
		assertTrue(intParams.contains(EAParameters.MARIO_SEED));
		assertFalse(intParams.contains(EAParameters.BASEDIR));
	}

	@Test
	public void testFloatParameters() {
		Set<String> floatParams = EAParameters.floatParameters();
		
		assertTrue(floatParams.contains(EAParameters.CROSSOVER_PROB));
		assertTrue(floatParams.contains(EAParameters.MUTATION_PROB));
		assertFalse(floatParams.contains(EAParameters.BASEDIR));
	}

	@Test
	public void testDefaultParameters() {
		Map<String, Object> params = EAParameters.defaultParameters();
		
		assertEquals(50, (int)params.get(EAParameters.GENERATIONS));
		assertEquals(30, (int)params.get(EAParameters.POP_SIZE));
		assertEquals(1, (int)params.get(EAParameters.DIFFICULTY));
		assertEquals(5, (int)params.get(EAParameters.REPETITIONS));
		assertEquals(2, (int)params.get(EAParameters.TOURNAMENT_SIZE));
		assertEquals(1, (int)params.get(EAParameters.ELITISM));
		assertEquals(0, (int)params.get(EAParameters.MARIO_SEED));
		
		assertEquals("crossover", .95f, (float)params.get(EAParameters.CROSSOVER_PROB), .000001f);
		assertEquals("mutation", .001f, (float)params.get(EAParameters.MUTATION_PROB), .000001f);
		
		assertEquals("unidentified", (String) params.get(EAParameters.BASEDIR));
		
		@SuppressWarnings("unchecked")
		List<SNSLearningAgent> agents = (List<SNSLearningAgent>) params.get(EAParameters.AGENTS);
		
		//compares the attributes of the 4 default agents
		assertEquals("RuleBased", agents.get(0).agentType);
		assertEquals("smartCross", agents.get(0).crossType);
		assertEquals("smallElite", agents.get(0).breederType);
		
		assertEquals("UniformProb", agents.get(1).agentType);
		assertEquals("smartCross", agents.get(1).crossType);
		assertEquals("smallElite", agents.get(1).breederType);
		
		assertEquals("RJSProb", agents.get(2).agentType);
		assertEquals("smartCross", agents.get(2).crossType);
		assertEquals("smallElite", agents.get(2).breederType);
		
		assertEquals("RSJProb", agents.get(3).agentType);
		assertEquals("smartCross", agents.get(3).crossType);
		assertEquals("smallElite", agents.get(3).breederType);
		
		
	}

	@Test
	public void testParametersFromFile() throws SAXException, IOException, ParserConfigurationException {
		Map<String, Object> params = EAParameters.parametersFromFile("test/ch/idsia/evolution/ea/testparameters.xml");
		
		assertEquals(1000, (int)params.get(EAParameters.GENERATIONS));
		assertEquals(100, (int)params.get(EAParameters.POP_SIZE));
		assertEquals(3, (int)params.get(EAParameters.DIFFICULTY));
		assertEquals(30, (int)params.get(EAParameters.REPETITIONS));
		assertEquals(2, (int)params.get(EAParameters.TOURNAMENT_SIZE));
		assertEquals(5, (int)params.get(EAParameters.ELITISM));
		assertEquals(50, (int)params.get(EAParameters.MARIO_SEED));
		
		assertEquals("crossover", .60f, (float)params.get(EAParameters.CROSSOVER_PROB), .000001f);
		assertEquals("mutation", .005f, (float)params.get(EAParameters.MUTATION_PROB), .000001f);
		
		assertEquals("testparams", (String) params.get(EAParameters.BASEDIR));
		
		
		@SuppressWarnings("unchecked")
		List<SNSLearningAgent> agents = (List<SNSLearningAgent>) params.get(EAParameters.AGENTS);
		
		//compares the attributes of the agents
		assertEquals("RuleBased", agents.get(0).agentType);
		assertEquals("smartCross", agents.get(0).crossType);
		assertEquals("tournamentWithElitism", agents.get(0).breederType);
	}

}
