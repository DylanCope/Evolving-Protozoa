package core;

import java.awt.Graphics;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Random;
import java.util.function.Predicate;

import utils.Vector2f;
import biology.Entity;

public class Tank 
{
	private ArrayList<Entity> entities;
	private double radius = 1;
	private Random r = new Random();
	
	public Tank() 
	{
		entities = new ArrayList<Entity>();
	}
	
	public void addEntity(Entity e) {
		double rad = radius - 2*e.getRadius();
		double t = 2*Math.PI*r.nextDouble();
		e.setPos(new Vector2f(
					rad * r.nextDouble() * Math.cos(t),
					rad * r.nextDouble() * Math.sin(t)
				));
		entities.add(e);
	}
	
	public void update(double delta) 
	{
		for(Entity e : entities) {
			e.update(delta, entities);
			
			if (e.getPos().length() - e.getRadius() > radius) {
				e.setPos(e.getPos().mul(-0.98));
			}
		}
		
		// Remove dead entities
		entities.removeIf(new Predicate<Entity>() {

			public boolean test(Entity e) {
				return e.isDead();
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
}
