package biology;

import java.awt.Color;
import java.awt.Graphics;
import java.util.Collection;

import utils.Vector2f;

public abstract class Entity 
{

	protected Vector2f pos, vel;
	protected double radius;
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
		double dist = getPos().sub(other.getPos()).length();
		return dist < getRadius() + other.getRadius();
	}
	
	public void handleCollision(Entity other)
	{
		setVel(vel.setDir(pos.sub(other.pos)));
	}
	
	public boolean inInteractionRange(Entity other)
	{
		double dist = getPos().sub(other.getPos()).length();
		return 0.9*dist <= getRadius() + other.getRadius();
	}
	
	public abstract boolean isEdible();
	
	public boolean move(Vector2f dr, Collection<Entity> entities)
	{
		setPos(getPos().add(dr));
		
		for (Entity e : entities) 
		{
			Vector2f dx = getPos().sub(e.getPos());
			if (!e.equals(this) && isCollidingWith(e)) 
			{
				if (e.getVel().length()*e.getRadius() > getVel().length()*getRadius())
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
	
	public Vector2f getPos() {
		return pos;
	}

	public void setPos(Vector2f pos) {
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

	public Vector2f getVel() {
		return vel;
	}

	public void setVel(Vector2f vel) {
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
