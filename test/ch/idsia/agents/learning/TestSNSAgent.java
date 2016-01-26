package ch.idsia.agents.learning;

import static org.junit.Assert.*;

import java.util.Arrays;

import org.junit.Test;

import util.MyRandom;

public class TestSNSAgent {

	@Test
	public void testMutate() {
		MyRandom r = new MyRandom();
		SNSAgent agent = new SNSAgent("UniformProb");
		agent.setRandomNumberGenerator(r);
		
		int[] oldDna = agent.getDna().clone();	//I'd better get a copy
		
		/*
		 * MyRandom is predictable. agent will mutate in 5 positions; 
		 * at genes indexed at 0, 1, 2, 3, 4. These positions will be filled with 11.
		 * The int array contains the sequence that generates this behavior 
		 * (-95 to reduce the # of mutated components, then their indexes come
		 * interleaved with the values to be placed)
		 */
		r.setIntSequence(new int[]{-95, 0, 11, 1, 11, 2, 11, 3, 11, 4, 11});
		agent.mutate();	
		
		int[] newDna = agent.getDna();
		
		for(int i = 0; i < SNSAgent.DNA_LENGTH; i++){
			
			if (i < 5){	//genes were mutated at odd positions up to 201
				assertEquals(11, newDna[i]);
			}
			else/*if (!(i < 202 && i % 2 == 1))*/ {
				assertEquals("@index %d. old=%d, new=%d", oldDna[i], newDna[i]);
			}
		}
		//System.out.println(Arrays.toString(oldDna));
		//System.out.println(Arrays.toString(newDna));
		
		
	}

}
