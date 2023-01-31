package protoevo.core;

import protoevo.ui.Window;
import protoevo.ui.components.TextStyle;
import protoevo.ui.simulation.SimulationController;
import protoevo.ui.simulation.SimulationRenderer;

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
		run(argsMap);
	}

	public static void run(Map<String, String> args) {
		if (args.containsKey("-save"))
			simulation = new Simulation(args.get("-save"));
		else
			simulation = new Simulation();

		try {
			if (!(Boolean.parseBoolean(args.getOrDefault("noui", "false")))) {
				TextStyle.loadFonts();
				window = new Window("Evolving Protozoa");
				simulation.getREPL().setWindow(window);
				SimulationRenderer renderer = new SimulationRenderer(simulation, window);
				SimulationController controller = new SimulationController(window, simulation, renderer);
				window.set(renderer, controller);

				SwingUtilities.invokeLater(window);
			}

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
