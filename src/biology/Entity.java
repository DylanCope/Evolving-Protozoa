package biology;

import java.awt.Color;
import java.awt.Graphics;
import java.io.Serializable;
import java.util.Collection;

import utils.Vector2;

public abstract class Entity implements Serializable
{
	private static final long serialVersionUID = -4333766895269415282L;
	protected Vector2 pos, vel;
	protected double radius;
	protected double interactAngle = Math.PI / 8;
	protected Color colour, healthyColour;
	
	protected double thinkTime = 0;
	protected double maxThinkTime;
	protected double timeAlive = 0;
	protected double health = 1;
	protected int maxVel;
	
	protected boolean dead = false;
	protected double nutrition;
	
	public abstract void update(double delta, Collection<Entity> entities);
	
	public void render(Graphics g)
	{
		g.setColor(getColor());
		g.fillOval(
				(int)(getPos().getX() - radius), 
				(int)(getPos().getY() - radius), 
				(int)(2*radius), 
				(int)(2*radius));
	}
	
	public boolean isCollidingWith(Entity other)
	{
		double dist = getPos().sub(other.getPos()).len();
		return dist < getRadius() + other.getRadius();
	}
	
	public void handleCollision(Entity other)
	{
		setVel(vel.setDir(pos.sub(other.pos)));
	}
	
	public boolean canInteractWith(Entity other)
	{
		Vector2 ds = other.getPos().sub(getPos());
		return 0.9*ds.len() <= getRadius() + other.getRadius() &&
				getVel().angleBetween(ds) < interactAngle;
	}
	
	public abstract boolean isEdible();
	
	public boolean move(Vector2 dr, Collection<Entity> entities)
	{
		setPos(getPos().add(dr));
		
		for (Entity e : entities) 
		{
			Vector2 dx = getPos().sub(e.getPos());
			if (!e.equals(this) && isCollidingWith(e)) 
			{
				if (e.getVel().len()*e.getRadius() > getVel().len()*getRadius())
					setPos(e.getPos().add(dx.setLength(e.getRadius() + getRadius())));
				else
					e.setPos(getPos().sub(dx.setLength(e.getRadius() + getRadius())));

				return false;
			}
		}
		
		return true;
	}
	
	public void setHealth(double h)
	{
		health = h;
		if (health > 1) 
			health = 1;
		
		if (health < 0.1) {
			setDead(true);
			return;
		}
		
		int r = healthyColour.getRed();
		int g = healthyColour.getGreen();
		int b = healthyColour.getBlue();
		
		colour = new Color(
				(int)(health * r), 
				(int)(health * g), 
				(int)(health * b));
	}
	
	public double getHealth() 
	{
		return health;
	}
	
	public Vector2 getPos() {
		return pos;
	}

	public void setPos(Vector2 pos) {
		this.pos = pos;
	}

	public double getRadius() {
		return radius;
	}

	public void setRadius(double radius) {
		this.radius = radius;
	}

	public boolean isDead() {
		return dead;
	}

	public void setDead(boolean dead) {
		this.dead = dead;
	}

	public Vector2 getVel() {
		return vel;
	}

	public void setVel(Vector2 vel) {
		this.vel = vel;
	}

	public Color getColor() {
		return colour;
	}

	public void setColor(Color color) {
		this.colour = color;
	}

	public double getNutrition() {
		return nutrition;
	}

	public void setNutrition(double nutrition) {
		this.nutrition = nutrition;
	}
}
