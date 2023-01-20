package protoevo.core;

import javax.swing.SwingUtilities;

import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import protoevo.utils.REPL;
import protoevo.utils.TextStyle;
import protoevo.utils.Window;

import java.io.InputStream;

public class Application 
{
	public static Simulation simulation;
	public static Window window;
	
	public static final float refreshDelay = 1000 / 120f;
	
	public static void main(String[] args)
	{
		if (args.length > 2)
			simulation = new Simulation(Settings.simulationSeed, args[2]);
		else
			simulation = new Simulation(Settings.simulationSeed);

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
