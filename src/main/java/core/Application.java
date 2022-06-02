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
		simulation.loadTank("saves/2022.06.01.11.47.48");
		try {
			if (!(args.length > 0 && args[0].equals("noui"))) {
				window = new Window("Evolving Protozoa", simulation);
				SwingUtilities.invokeLater(window);
			} else {
				simulation.shouldWaitBetweenTicks(false);
			}
			new Thread(simulation).start();
			new REPL(simulation, window);
		} catch (Exception e) {
			simulation.close();
			throw e;
		}
	}
	
	public static void exit()
	{
		System.exit(0);
	}
}
