package protoevo.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import protoevo.core.Application;
import protoevo.core.Simulation;
import protoevo.ui.Window;

public class REPL implements Runnable
{
    private Simulation simulation;
    private Window window;
    private boolean running = true;
    private BufferedReader bufferRead;
    private InputStreamReader input;

    public REPL(Simulation simulation)
    {
        this.simulation = simulation;
    }

    public void setWindow(Window window) {
        this.window = window;
    }

    public void setTimeDilation(String[] args) throws Exception
    {
        if (args.length != 2)
            throw new Exception("This command takes 2 arguments.");

        float d = Float.parseFloat(args[1]);
        simulation.setTimeDilation(d);
    }

    public void close() {
        running = false;
        try {
            input.close();
            bufferRead.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String interruptibleReadLine(BufferedReader reader)
            throws InterruptedException, IOException {
        Pattern line = Pattern.compile("^(.*)\\R");
        Matcher matcher;
        boolean interrupted = false;

        StringBuilder result = new StringBuilder();
        int chr = -1;
        do {
            if (reader.ready()) chr = reader.read();
            if (chr > -1) result.append((char) chr);
            matcher = line.matcher(result.toString());
            interrupted = Thread.interrupted(); // resets flag, call only once
        } while (!interrupted && !matcher.matches());
        if (interrupted) throw new InterruptedException();
        return (matcher.matches() ? matcher.group(1) : "");
    }

    @Override
    public void run() {
        System.out.println("Starting REPL...\nType 'help' for a list of commands.");
        input = new InputStreamReader(System.in);
        bufferRead = new BufferedReader(input);
        while (running)
        {
            String line;
            try
            {
                System.out.print("> ");
                line = bufferRead.readLine();

                if (line.equals("\n")) {
                    System.out.println();
                    continue;
                }

                String[] args = line.split(" ");
                String cmd = args[0];
                switch (cmd)
                {
                    case "help":
                        System.out.println("commands - help, toggleui, quit, stats, settime, gettime");
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
                        if (window != null) {
                            System.out.println("Toggling UI.");
                            window.getFrame().setVisible(!window.getFrame().isVisible());
                            simulation.toggleUpdateDelay();
                        } else {
                            System.out.println("No UI to toggle.");
                        }
                        break;
                    case "pause":
                        simulation.togglePause();
                        System.out.println("Toggling pause.");
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