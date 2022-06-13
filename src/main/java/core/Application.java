package core;

import javax.swing.SwingUtilities;

import utils.REPL;
import utils.TextStyle;
import utils.Window;

public class Application 
{
	public static Simulation simulation;
	public static Window window;
	
	public static final float refreshDelay = 1000 / 60f;
	
	public static void main(String[] args)
	{
		simulation = new Simulation(Settings.simulationSeed, "thalassa-charmeleon-eum");
		try {
			if (!(args.length > 0 && args[0].equals("noui"))) {
				TextStyle.loadFonts();
				window = new Window("Evolving Protozoa", simulation);
				SwingUtilities.invokeLater(window);
			}
			else {
				simulation.setUpdateDelay(0);
			}
			new Thread(new REPL(simulation, window)).start();
			simulation.simulate();
		}
		catch (Exception e) {
			simulation.close();
			throw e;
		}
	}
	
	public static void exit()
	{
		System.exit(0);
	}
}
