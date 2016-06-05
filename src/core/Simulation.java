package core;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Random;

import javax.swing.Timer;

import physics.Particle;
import utils.FileIO;
import biology.Brain;
import biology.Protozoa;
import biology.Pellet;

public class Simulation implements Runnable, ActionListener
{
	private Tank tank;
	private boolean simulate;
	private final Timer timer = new Timer((int) Application.refreshDelay, this);
	private int generation = 0;
	private double time = 0;
	
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
			tank.add(p);
		}
		
		for (int i = 0; i <  pellets; i++) 
		{
			double radius = (RANDOM.nextInt(3) + 2) / 500.0;
			tank.add(new Pellet(radius));
		}

//		for (Particle e : tank.getParticles())
//			e.move(new Vector2(0, 0), tank.getParticles());
	}
	
	public void nextGeneration()
	{
		generation++;
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
		double delta = timer.getDelay() / 1000.0;
		time += delta;
		tank.update(delta);
		
		if (simulate)
			timer.restart();
		else
			timer.stop();
	}

	public Tank getTank()
	{
		return tank;
	}
	
	public int getGeneration()
	{
		return generation;
	}

	public double getElapsedTime()
	{
		return time;
	}
}
