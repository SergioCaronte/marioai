package util;

import java.util.Random;

/**
 * Auxiliary class to help controlling the generation of random numbers
 * @author anderson
 *
 */
@SuppressWarnings("serial")	//avoid a warning regarding the serialization of this class
public class MyRandom extends Random{
	
	public int nextInt;
	public int[] intSequence;
	
	public MyRandom(){
		nextInt = -1;
		intSequence = null;
	}
	
	public void setIntSequence(int[] sequence){
		intSequence = sequence;
	}
	
	
	
	/**
	 * Generates a predictable sequence of integers: 
	 * intSequence[nextInt] if intSequence was specified 
	 * or netxInt modulo n (if intsequence was not specified or nextInt exceeds its size)
	 * @param n
	 * @return
	 */
 	public int nextInt(int n){
 		
 		nextInt++;
 		if (intSequence != null && nextInt < intSequence.length) return intSequence[nextInt];
 		
 		return nextInt % n;
 	}
}
