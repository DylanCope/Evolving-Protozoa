package core;

import java.awt.Graphics;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.function.Predicate;

import utils.Vector2;
import biology.Entity;
import biology.Pellet;
import biology.Protozoa;

public class Tank implements Iterable<Entity>, Serializable
{
	private static final long serialVersionUID = 2804817237950199223L;
	private ArrayList<Entity> entities;
	private double radius = 1;
	private int protozoaNumber = 0;
	private int pelletNumber = 0;
	private double timeDilation = 1;
	
	public Tank() 
	{
		entities = new ArrayList<Entity>();
	}
	
	public void addEntity(Entity e) {
		double rad 	= radius - 2*e.getRadius();
		double t 	= 2 * Math.PI * Simulation.RANDOM.nextDouble();
		double r 	= Simulation.RANDOM.nextDouble();
		e.setPos(new Vector2(
					rad * (1 - r*r) * Math.cos(t),
					rad * (1 - r*r) * Math.sin(t)
				));
		entities.add(e);
		
		if (e instanceof Protozoa)
			protozoaNumber++;
		else if (e instanceof Pellet)
			pelletNumber++;
	}
	
	public void update(double delta) 
	{
		for(Entity e : entities) {
			e.update(delta * timeDilation, entities);
			
			if (e.getPos().len() - e.getRadius() > radius) {
				e.setPos(e.getPos().mul(-0.98));
			}
		}
		
		// Remove dead entities
		entities.removeIf(new Predicate<Entity>() {

			public boolean test(Entity e) {
				if (e.isDead()) {
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
		for (Entity e : entities)
			e.render(g);
	}

	public Collection<Entity> getEntities() {
		return entities;
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
	public Iterator<Entity> iterator() {
		return entities.iterator();
	}
}
