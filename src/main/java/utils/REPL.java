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
        System.out.println("Starting REPL...");
        BufferedReader bufferRead = new BufferedReader(new InputStreamReader(System.in));
        while (true)
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
                        System.out.println("Closing simulation");
                        simulation.close();
                        Application.exit();
                        break;
                    case "stats":
                        System.out.println("Number of protozoa: " + simulation.getTank().numberOfProtozoa());
                        System.out.println("Number of pellets: " + simulation.getTank().numberOfPellets());
                        System.out.println("Generation:" + simulation.getGeneration());
                        System.out.println("Time elapsed: " + simulation.getElapsedTime());
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
                }
            }
            catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
    }

    public void setTimeDilation(String[] args) throws Exception
    {
        if (args.length != 2)
            throw new Exception("This command takes 2 arguments.");

        double d = Double.parseDouble(args[1]);
        simulation.setTimeDilation(d);
    }

}