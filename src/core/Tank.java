package core;

import java.awt.Graphics;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.function.Predicate;

import physics.Particle;
import utils.Vector2;
import biology.Entity;
import biology.Pellet;
import biology.Protozoa;

public class Tank implements Iterable<Particle>, Serializable
{
	private static final long serialVersionUID = 2804817237950199223L;
	private ArrayList<Particle> particles;
	private double radius = 1;
	private int protozoaNumber = 0;
	private int pelletNumber = 0;
	private double timeDilation = 1;
	
	public Tank() 
	{
		particles = new ArrayList<Particle>();
	}
	
	public void add(Particle e) {
		double rad 	= radius - 2*e.getRadius();
		double t 	= 2 * Math.PI * Simulation.RANDOM.nextDouble();
		double r 	= Simulation.RANDOM.nextDouble();
		e.set(new Vector2(
					rad * (1 - r*r) * Math.cos(t),
					rad * (1 - r*r) * Math.sin(t)
				));
		particles.add(e);
		
		if (e instanceof Protozoa)
			protozoaNumber++;
		else if (e instanceof Pellet)
			pelletNumber++;
	}
	
	public void update(double delta) 
	{
		double dt = delta * timeDilation;

		int n = particles.size();
		for (int i = 0; i < n; i++)
		{
			Particle p1 = particles.get(i);
			for (int j = i + 1; j < n; j++)
			{
				Particle p2 = particles.get(j);
				
				p1.handleCollision(p2, dt);
			}
			p1.update(dt);
			
			if (p1.len() - p1.getRadius() > radius) {
				p1.set(p1.mul(-0.98));
			}
		}
		
		// Remove dead particles
		particles.removeIf(new Predicate<Particle>() {

			public boolean test(Particle e) 
			{	
				if (e instanceof Entity && ((Entity) e).isDead()) {
					if (e instanceof Protozoa)
						protozoaNumber--;
					else if (e instanceof Pellet)
						pelletNumber--;
					return true;
				}
				return false;
			}
			
		});
	}
	
	
	public void render(Graphics g)
	{
//		for (Particle e : particles)
//			e.render(g);
	}

	public Collection<Particle> getParticles() {
		return particles;
	}
	
	public int numberOfProtozoa() {
		return protozoaNumber;
	}
	
	public int numberOfPellets() {
		return pelletNumber;
	}

	public void setTimeDilation(double d) {
		timeDilation = d;
	}

	public double getTimeDilation() {
		return timeDilation;
	}
	
	@Override
	public Iterator<Particle> iterator() {
		return particles.iterator();
	}
}
