package utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import core.Application;
import core.Simulation;

public class REPL implements Runnable
{
    private final Simulation simulation;
    private final Window window;
    private boolean running = true;

    public REPL(Simulation simulation, Window window)
    {
        this.simulation = simulation;
        this.window = window;
    }

    public void setTimeDilation(String[] args) throws Exception
    {
        if (args.length != 2)
            throw new Exception("This command takes 2 arguments.");

        float d = Float.parseFloat(args[1]);
        simulation.setTimeDilation(d);
    }

    @Override
    public void run() {
        System.out.println("Starting REPL...");
        BufferedReader bufferRead = new BufferedReader(new InputStreamReader(System.in));
        while (running)
        {
            String line;
            try
            {
                System.out.print("> ");
                line = bufferRead.readLine();
                String[] args = line.split(" ");
                String cmd = args[0];
                switch (cmd)
                {
                    case "help":
                        System.out.println("commands - help, quit, stats, settime, gettime");
                        break;
                    case "quit":
                        simulation.close();
                        Application.exit();
                        break;
                    case "stats":
                        simulation.printStats();
                        break;
                    case "settime":
                        setTimeDilation(args);
                        break;
                    case "gettime":
                        System.out.println(simulation.getTimeDilation());
                        break;
                    case "toggledebug":
                        System.out.println("Toggling debug mode.");
                        simulation.toggleDebug();
                        break;
                    case "toggleui":
                        System.out.println("Toggling UI.");
                        window.getFrame().setVisible(!window.getFrame().isVisible());
                        simulation.toggleUpdateDelay();
                        break;
                    default:
                        System.out.println("Command not recognised.");
                        break;
                }
            }
            catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
    }
}