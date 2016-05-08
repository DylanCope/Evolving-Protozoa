package core;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Random;

import javax.swing.Timer;

import biology.Brain;
import biology.Pellet;
import biology.Protozoa;

public class Simulation implements Runnable, ActionListener
{
	private Tank tank;
	private boolean simulate;
	
	public static final float refreshDelay = 1000 / 30f;
	private final Timer timer = new Timer((int) refreshDelay, this);
	
	public Simulation()
	{
		tank = new Tank();

		int creatures = 80;
		int pellets = 80;
		
		Random r = new Random();
		
		for (int i = 0; i < creatures; i++) {
			double radius = (r.nextInt(5) + 5) / 500.0;
			Protozoa p = new Protozoa(Brain.RANDOM, radius);
			tank.addEntity(p);
		}
		for (int i = creatures; i <  creatures + pellets; i++) {
			double radius = (r.nextInt(3) + 2) / 500.0;
			tank.addEntity(new Pellet(radius));
		}

		simulate = true;
	}
	
	@Override
	public void run() 
	{
		timer.start();
	}

	@Override
	public void actionPerformed(ActionEvent e) 
	{
		tank.update(timer.getDelay() / 1000.0);
		
		if (simulate)
			timer.restart();
		else
			timer.stop();
	}

	public Tank getTank()
	{
		return tank;
	}
}
