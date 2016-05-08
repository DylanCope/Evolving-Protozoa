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
	private double radius = 1; //Main.HEIGHT;
	private Random r = new Random();
	
	public Tank() 
	{
//		this.setBounds(bounds);
		entities = new ArrayList<Entity>();
	}
	
	public void addEntity(Entity e) {
		e.setPos(new Vector2f(
					radius * r.nextDouble() * Math.cos(2*Math.PI*r.nextDouble()),
					radius * r.nextDouble() * Math.sin(2*Math.PI*r.nextDouble())
				));
		entities.add(e);
	}
	
//	public void placeRandomly(Collection<Entity> entities)
//	{
//		
//		Random r = new Random();
//		int maxIterations = 100;
//		for (Entity e1 : entities) 
//		{	
//			boolean colliding = true;
//			for (int i = 0; i < maxIterations && colliding; i++) 
//			{
//				e1.setPos(new Vector2f(
//					r.nextInt((int) getBounds().getX()), 
//					r.nextInt((int) getBounds().getY())
//				));
//				colliding = true;
//				for (Entity e2 : entities)
//					if (!e1.equals(e2))
//						colliding &= e1.isCollidingWith(e2);
//				
//				if (i == maxIterations - 1)
//					System.out.println("failed");
//			}
//			
//			this.entities.add(e1);
//		}
//
//	}
	
	public void update(double delta) 
	{
		for(Entity e : entities) {
			e.update(delta, entities);
			
			// Cause wrap around of moving entities.
//			if (e.getPos().getX() - e.getRadius() > getBounds().getX()) 
//				e.getPos().setX(-e.getRadius());
//			if (e.getPos().getX() + e.getRadius() < 0) 
//				e.getPos().setX(getBounds().getX() + e.getRadius());
//			if (e.getPos().getY() - e.getRadius() > getBounds().getY()) 
//				e.getPos().setY(-e.getRadius());
//			if (e.getPos().getY() + e.getRadius() < 0) 
//				e.getPos().setY(getBounds().getY() + e.getRadius());
			
			if (e.getPos().length() - e.getRadius() > radius) {
				e.setPos(e.getPos().mul(-1));
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

//	public Vector2f getBounds() {
//		return bounds;
//	}
//
//	public void setBounds(Vector2f bounds) {
//		this.bounds = bounds;
//	}
}
