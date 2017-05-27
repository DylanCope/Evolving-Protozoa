package core;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Random;

import javax.swing.Timer;

import utils.Vector2;
import utils.FileIO;
import biology.Brain;
import biology.Pellet;
import biology.Protozoa;
import biology.Entity;

public class Simulation implements Runnable, ActionListener
{
	private Tank tank;
	private boolean simulate;
	private final Timer timer = new Timer((int) Application.refreshDelay, this);
	private int generation = 0;
	private double elapsedTime = 0, timeDilation = 1;
	
	public static Random RANDOM;
	
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

		int creatures = 60;
		int pellets = 300;
		
		for (int i = 0; i < creatures; i++) 
		{
			double radius = (RANDOM.nextInt(5) + 5) / 500.0;
			Protozoa p = new Protozoa(Brain.RANDOM, radius);
			tank.addEntity(p);
		}
		
		for (int i = creatures; i <  creatures + pellets; i++) 
		{
			double radius = (RANDOM.nextInt(3) + 2) / 500.0;
			tank.addEntity(new Pellet(radius));
		}

		for (Entity e : tank.getEntities())
			e.move(new Vector2(0, 0), tank.getEntities());
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
		double delta = timeDilation * timer.getDelay() / 1000.0;
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
}
