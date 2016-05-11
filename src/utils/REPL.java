package utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import core.Application;
import core.Simulation;

public class REPL 
{
	private Simulation simulation;

	public REPL(Simulation simulation)
	{
		this.simulation = simulation;
	    BufferedReader bufferRead = new BufferedReader(new InputStreamReader(System.in));
		while (true)
		{
			String line;
			try {
				line = bufferRead.readLine();
				String[] args = line.split(" ");
				String cmd = args[0];
				switch (cmd)
				{
				case "quit":
					Application.exit();
					break;
				case "stats":
					System.out.println("Number of protozoa: " + simulation.getTank().numberOfProtozoa());
					System.out.println("Number of pellets: " + simulation.getTank().numberOfPellets());
					System.out.println("Generation:" + simulation.getGeneration());
					System.out.println("Time elapsed: " + simulation.getElapsedTime());
					break;
				case "settime":
					setTime(args);
					break;
				case "gettime":
					System.out.println(simulation.getTank().getTimeDilation());
				}
			} 
			catch (Exception e) {
				System.out.println(e.getMessage());
			}
		}
	}
	
	public void setTime(String[] args) throws Exception
	{
		if (args.length != 2)
			throw new Exception("This command takes 2 arguments.");
		
		double d = Double.parseDouble(args[1]);
		simulation.getTank().setTimeDilation(d);
	}
	
}
