package ch.idsia.agents.controllers;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ch.idsia.agents.Agent;
import ch.idsia.agents.learning.SNSAgent;

import ch.idsia.benchmark.mario.environments.Environment;

public class ReplaySNSAgent extends SNSAgent implements Agent
{	
	private int cur = 0;
	private int[] commands;
	private String behavior = "UniformProb";
	
	public ReplaySNSAgent()
	{
	    super("Useless");
	    reset();
	    
	    commands = new int[3000]; 
	}
	
	public void loadReplayer(String fileName)
	{
		try {
			BufferedReader file = new BufferedReader(new FileReader(fileName));
			
			Pattern pattern = Pattern.compile("_[a-zA-z]*_");
			Matcher matcher = pattern.matcher(fileName);
			if (matcher.find())
			{
			    behavior = matcher.group(0);
			    behavior = (String)behavior.subSequence(1, behavior.length()-1);
			    System.out.println(behavior);
			}
			
			for(int i = 0; i < 3000; i++)
			{
				try {
					commands[i] =  Integer.parseInt(file.readLine());
				} catch (NumberFormatException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public boolean[] getAction()
	{	
		if(behavior.equals("UniformProb"))
			return getUniformProb(commands[cur++ %3000]);
		else if(behavior.equals("RJProb"))
			return getRJProb(commands[cur++ %3000]);
		else if(behavior.equals("RJSProb"))
			return getRJSProb(commands[cur++ %3000]);
		else if(behavior.equals("RSJProb"))
			return getRSJProb(commands[cur++ %3000]);
		else if(behavior.equals("RuleBased"))
			return getUniformProb(commands[cur++ %3000]);
		return getUniformProb(commands[cur++ %3000]);
	}
	
	public void reset()
	{
		cur = 0;
	    action = new boolean[Environment.numberOfKeys];
	    //action[Mario.KEY_RIGHT] = true;
	}
}
