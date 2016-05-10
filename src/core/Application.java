package core;

import utils.TextStyle;
import javax.swing.SwingUtilities;

import utils.Window;

public class Application 
{
	public static Simulation simulation;
	public static Window window;
	
	public static final float refreshDelay = 1000 / 20f;
	
	public static void main(String[] args)
	{
		TextStyle.loadFonts();
		simulation = new Simulation(1);
		window = new Window("Evolving Protozoa", simulation);
		SwingUtilities.invokeLater(window);
		new Thread(simulation).run();
	}
	
	public static void exit()
	{
		System.exit(0);
	}
}
