package ch.idsia.evolution.ea;

import static org.junit.Assert.*;

import java.lang.reflect.Field;
import java.util.Random;

import org.junit.Before;
import org.junit.Test;

import util.MyRandom;
import ch.idsia.agents.Agent;
import ch.idsia.agents.SNSLearningAgent;
import ch.idsia.agents.learning.SNSAgent;
import ch.idsia.benchmark.tasks.LearningTask;
import ch.idsia.evolution.Evolvable;
import ch.idsia.tools.MarioAIOptions;

public class TestSNSEA {
	
	SNSEA ea;
	
	@Before
	public void setUp() throws Exception {
		SNSAgent a = new SNSAgent("UniformProb");
		MarioAIOptions marioOpts = new MarioAIOptions(new String[]{"-ld 1 -vis off -fps 100"});
		//marioOpts.setAgent(ag);
		LearningTask learningTask = new LearningTask(marioOpts);
		ea = new SNSEA(learningTask, a, 5);
		ea.setRandomNumberGenerator(new MyRandom());
	}
	
	@Test
	public void testElitism() throws Exception{
		
		SNSLearningAgent.elitism = 2;
		SNSLearningAgent.crossoverProb = 1.0f;
		
		//change the accessibility of the fitness field to manipulate it
		Field field = SNSEA.class.getDeclaredField("fitness");
		field.setAccessible(true);
		field.set(ea, new float[]{1000, 500, 100, 50, 10});
		
		Evolvable best1 = ea.getIndividual(0);
		Evolvable best2 = ea.getIndividual(1);
		
		ea.nextGeneration();
		
		//the first two individuals should not change
		assertEquals(best1, ea.getIndividual(0));
		assertEquals(best2, ea.getIndividual(1));
		
		//the other individuals should change (xover prob = 100%)
		//we'll test only the 3rd
		assertNotSame(best1, ea.getIndividual(2));
		assertNotSame(best2, ea.getIndividual(2));
	}

	@Test
	public void testTournament() throws Exception {
		
		//change the accessibility of the fitness field to manipulate it
		Field field = SNSEA.class.getDeclaredField("fitness");
		field.setAccessible(true);
		field.set(ea, new float[]{1000, 500, 100, 50, 10});
		
		//first tournament: 1st and 2nd as competitors, 1st is winner
		assertEquals(0, ea.tournament(2));
		
		//2nd tournament: 3rd and 4th as competitors, 3rd is the winner 
		assertEquals(2, ea.tournament(2));
		
		//3rd tournament: 5th and 1st as competitors, 1st is the winner
		assertEquals(0, ea.tournament(2));
	}
	
	@Test
	/**
	 * Tests the SNSEA.cross() when behavior is splitCross and the crossover point is passed 
	 */
	public void testSplitCross(){
		//SNSLearningAgent la = new SNSLearningAgent("UniformProb", "freeSplitCross", "singleEliteTournament");
		
		ea.crossBehavior = "splitCross";
		
		int crossoverPoint = 1400;
		
		//crosses the 1st two individuals and check if 'kids' have their chromosomes swapped
		SNSAgent parent1 = (SNSAgent) ea.getIndividual(0);
		SNSAgent parent2 = (SNSAgent) ea.getIndividual(1);
		
		SNSAgent kid1 = (SNSAgent) ea.cross(parent1, parent2, crossoverPoint);
		SNSAgent kid2 = (SNSAgent) ea.cross(parent2, parent1, crossoverPoint);
		
		//DNA of 1st child equals 1st parent and 2nd child equals 2nd parent
		for (int i = 0; i < crossoverPoint; i++){
			assertEquals(kid1.getDna()[i], parent1.getDna()[i]);
			assertEquals(kid2.getDna()[i], parent2.getDna()[i]);
		}
		
		//from xover point onwards, 1st child equals 2nd parent and 2nd child equals 1st parent
		for (int i = crossoverPoint; i < SNSAgent.DNA_LENGTH; i++){
			assertEquals(kid1.getDna()[i], parent2.getDna()[i]);
			assertEquals(kid2.getDna()[i], parent1.getDna()[i]);
		}
	}
	
}
