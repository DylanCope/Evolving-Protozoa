package biology;

import java.awt.Color;
import java.awt.Graphics;
import java.io.Serializable;
import java.util.Collection;

import utils.Vector2;
import physics.Particle;

public abstract class Entity extends Particle implements Serializable
{
	private static final long serialVersionUID = -4333766895269415282L;
	protected Color colour, healthyColour;
	
	protected double thinkTime = 0;
	protected double maxThinkTime;
	protected double timeAlive = 0;
	protected double health = 1;
	protected int maxVel;
	
	protected boolean dead = false;
	protected double nutrition;
	
	public Entity(double radius)
	{
		super(radius);
	}
	
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
		setVel(v.setDir(sub(other)));
	}
	
	public boolean inInteractionRange(Entity other)
	{
		double dist = getPos().sub(other.getPos()).len();
		return 0.9*dist <= getRadius() + other.getRadius();
	}
	
	public abstract boolean isEdible();
	
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
		return this;
	}

	public void setPos(Vector2 pos) {
		set(pos);
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
		return v;
	}

	public void setVel(Vector2 vel) {
		v = vel;
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
