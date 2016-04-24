package entities;

import java.awt.Color;
import java.awt.Graphics;
import java.util.Collection;
import java.util.Random;

import utils.Vector2f;

public abstract class Entity 
{

	private Vector2f pos, vel;
	private int radius;
	private Color color;
	Random random = new Random();
	
	double thinkTime = 0;
	double maxThinkTime;
	double health = 1;
	int maxVel = 100;
	
	private boolean dead = false;
	private double nutrition;
	
	public abstract void update(double delta, Collection<Entity> entities);
	
	public void render(Graphics g)
	{
		g.setColor(getColor());
		g.fillOval(
				(int)(getPos().getX() - getRadius()), 
				(int)(getPos().getY() - getRadius()), 
				getRadius()*2, 
				getRadius()*2);
	}
	
	public boolean isCollidingWith(Entity other)
	{
		double dist = getPos().sub(other.getPos()).length();
		return dist <= getRadius() + other.getRadius();
	}
	
	public boolean inEatingRange(Entity other)
	{
		double dist = getPos().sub(other.getPos()).length();
		return 0.9*dist <= getRadius() + other.getRadius();
	}
	
	public abstract boolean isEdible();
	
	public boolean move(Vector2f dr, Collection<Entity> entities)
	{
		setPos(getPos().add(dr));
		
		for (Entity e : entities) {
			if (!e.equals(this) && isCollidingWith(e)) {
				setPos(getPos().add(dr.mul(-1)));
				return false;
			}
		}
		
		return true;
	}
	
	public void nextVelocity() 
	{
		getVel().setX(random.nextInt(maxVel) - maxVel/2);
		getVel().setY(random.nextInt(maxVel) - maxVel/2);	
	}

	public Vector2f getPos() {
		return pos;
	}

	public void setPos(Vector2f pos) {
		this.pos = pos;
	}

	public int getRadius() {
		return radius;
	}

	public void setRadius(int radius) {
		this.radius = radius;
	}

	public boolean isDead() {
		return dead;
	}

	public void setDead(boolean dead) {
		this.dead = dead;
	}

	public Vector2f getVel() {
		return vel;
	}

	public void setVel(Vector2f vel) {
		this.vel = vel;
	}

	public Color getColor() {
		return color;
	}

	public void setColor(Color color) {
		this.color = color;
	}

	public double getNutrition() {
		return nutrition;
	}

	public void setNutrition(double nutrition) {
		this.nutrition = nutrition;
	}
}
