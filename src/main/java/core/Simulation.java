package core;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Random;

import javax.swing.Timer;

import biology.*;
import utils.Vector2;
import utils.FileIO;

public class Simulation implements Runnable, ActionListener
{
	private Tank tank;
	private boolean simulate;
	private final Timer timer = new Timer((int) Application.refreshDelay, this);
	private int generation = 0;
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
		tank = (Tank) FileIO.load(filename);
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

	public int getGeneration() { return generation; }

	public double getElapsedTime() { return elapsedTime; }

	public double getTimeDilation() { return timeDilation; }

	public void setTimeDilation(double td) { timeDilation = td; }

	public void close() {

	}

	public void toggleDebug() {
		debug = !debug;
	}

	public boolean inDebugMode() {
		return debug;
	}
}
