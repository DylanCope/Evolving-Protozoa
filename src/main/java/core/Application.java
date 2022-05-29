package core;

import javax.swing.SwingUtilities;

import utils.REPL;
import utils.TextStyle;
import utils.Window;

public class Application 
{
	public static Simulation simulation;
	public static Window window;
	
	public static final float refreshDelay = 1000 / 120f;
	
	public static void main(String[] args)
	{
		TextStyle.loadFonts();
		simulation = new Simulation(1);
//		simulation.loadTank("test");
		simulation.initDefaultTank();
//		FileIO.save(simulation.getTank(), "test");
		window = new Window("Evolving Protozoa", simulation);
		SwingUtilities.invokeLater(window);
		new Thread(simulation).run();
		new REPL(simulation);
	}
	
	public static void exit()
	{
		System.exit(0);
	}
}
