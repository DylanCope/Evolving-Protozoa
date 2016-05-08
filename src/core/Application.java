package core;

import javax.swing.SwingUtilities;

import utils.Window;

public class Application 
{
	public static Simulation simulation;
	public static Window window;
	
	public static final float refreshDelay = 1000 / 40f;
	
	public static void main(String[] args)
	{
		simulation = new Simulation(1);
		window = new Window("Evolving Protozoa", simulation);
		SwingUtilities.invokeLater(window);
		new Thread(simulation).run();
	}
}
