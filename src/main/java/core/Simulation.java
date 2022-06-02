package core;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Random;

import javax.swing.Timer;

import biology.*;
import utils.FileIO;

public class Simulation implements Runnable, ActionListener
{
	private Tank tank;
	private boolean simulate;
	private final Timer timer = new Timer((int) Application.refreshDelay, this);
	private double elapsedTime = 0, timeDilation = 1;
	
	public static Random RANDOM;
	private boolean debug = false;

	public Simulation(long seed)
	{
		RANDOM = new Random(seed);
		simulate = true;
	}
	
	public Simulation()
	{
		this(new Random().nextLong());
	}
	
	public void initDefaultTank()
	{
		tank = new Tank();

		for (int i = 0; i < Settings.numInitialProtozoa; i++)
			tank.addRandom(new Protozoa());

		for (int i = 0; i < Settings.numInitialPlantPellets; i++)
			tank.addRandom(new PlantPellet());

	}
	
	public void loadTank(String filename)
	{
		try {
			tank = (Tank) FileIO.load(filename);
		} catch (IOException | ClassNotFoundException e) {
			System.out.println("Unable to load tank at " + filename + " because: " + e.getMessage());
			initDefaultTank();
		}
	}

	public void shouldWaitBetweenTicks(boolean ticking) {
		if (ticking)
			timer.setDelay((int) Application.refreshDelay);
		else
			timer.setDelay(0);
	}
	
	@Override
	public void run() 
	{
		timer.start();
	}

	@Override
	public void actionPerformed(ActionEvent e) 
	{
//		double delta = timeDilation * timer.getDelay() / 1000.0;
		double delta = timeDilation * Settings.simulationUpdateDelta;
		elapsedTime += delta;
		tank.update(delta);
		
		if (simulate)
			timer.restart();
		else
			timer.stop();
	}

	public Tank getTank() { return tank; }

	public int getGeneration() { return tank.getGeneration(); }

	public double getElapsedTime() { return elapsedTime; }

	public double getTimeDilation() { return timeDilation; }

	public void setTimeDilation(double td) { timeDilation = td; }

	public void close() {
		System.out.println();
		System.out.println("Closing simulation.");
		String timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new java.util.Date());
		FileIO.save(tank, "saves/" + timeStamp);
	}

	public void toggleDebug() {
		debug = !debug;
	}

	public boolean inDebugMode() {
		return debug;
	}
}
