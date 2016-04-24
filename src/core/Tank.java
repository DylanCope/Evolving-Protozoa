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
	private Vector2f bounds;
	
	public Tank(Vector2f bounds) 
	{
		this.bounds = bounds;
		entities = new ArrayList<Entity>();
	}
	
	public void addEntity(Entity e) {
		entities.add(e);
	}
	
	public void placeRandomly(Collection<Entity> entities)
	{
		Random r = new Random();
		int maxIterations = 100;
		for (Entity e1 : entities) 
		{	
			boolean colliding = true;
			for (int i = 0; i < maxIterations && colliding; i++) 
			{
				e1.setPos(new Vector2f(
					r.nextInt((int) bounds.getX()), 
					r.nextInt((int) bounds.getY())));
				colliding = true;
				for (Entity e2 : entities)
					if (!e1.equals(e2))
						colliding &= e1.isCollidingWith(e2);
				
				if (i == maxIterations - 1)
					System.out.println("failed");
			}
			
			this.entities.add(e1);
		}

	}
	
	public void update(double delta) 
	{
		for(Entity e : entities) {
			e.update(delta, entities);
			
			// Cause wrap around of moving entities.
			if (e.getPos().getX() - e.getRadius() > bounds.getX()) 
				e.getPos().setX(-e.getRadius());
			if (e.getPos().getX() + e.getRadius() < 0) 
				e.getPos().setX(bounds.getX() + e.getRadius());
			if (e.getPos().getY() - e.getRadius() > bounds.getY()) 
				e.getPos().setY(-e.getRadius());
			if (e.getPos().getY() + e.getRadius() < 0) 
				e.getPos().setY(bounds.getY() + e.getRadius());
		}
		
		// Remove dead entities
		entities.removeIf(new Predicate<Entity>() {

			public boolean test(Entity arg0) {
				return arg0.isDead();
			}
			
		});
	}
	
	public void render(Graphics g)
	{
		for (Entity e : entities)
			e.render(g);
	}
}
