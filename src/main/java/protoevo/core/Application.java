package protoevo.core;

import protoevo.utils.REPL;
import protoevo.utils.TextStyle;
import protoevo.utils.Window;

import javax.swing.*;
import java.util.HashMap;
import java.util.Map;

public class Application 
{
	public static Simulation simulation;
	public static Window window;

	public static Map<String, String> parseArgs(String[] args) {
		Map<String, String> argsMap = new HashMap<>();
		for (String arg: args) {
			String[] split = arg.split("=");
			if (split.length == 2) {
				if (!split[1].equals(""))
					argsMap.put(split[0], split[1]);
			}
		}
		return argsMap;
	}
	
	public static void main(String[] args)
	{
		Map<String, String> argsMap = parseArgs(args);
		if (argsMap.containsKey("-save"))
			simulation = new Simulation(argsMap.get("-save"));
		else
			simulation = new Simulation();

		try {
			if (!(args.length > 0 && args[0].equals("noui"))) {
				TextStyle.loadFonts();
				window = new Window("Evolving Protozoa", simulation);
				SwingUtilities.invokeLater(window);
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
