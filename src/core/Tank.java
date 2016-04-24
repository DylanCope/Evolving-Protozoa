package core;

import java.awt.Graphics;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Random;
import java.util.function.Predicate;

import utils.Vector2f;
import entities.Entity;
import entities.Pellet;
import entities.Protozoa;

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
		int creatures = 20;
		int pellets = 30;
		Random r = new Random();
		for(int i = 0; i < creatures; i++){
			this.entities.add(new Protozoa(
					r.nextInt((int) bounds.getX()), 
					r.nextInt((int) bounds.getY()), 
					25));
		}
		for(int i = creatures; i <  creatures + pellets; i++){
			this.entities.add(new Pellet(
					r.nextInt((int) bounds.getX()), 
					r.nextInt((int) bounds.getY()), 
					5));
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
