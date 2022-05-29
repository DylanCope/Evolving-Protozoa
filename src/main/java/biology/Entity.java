package biology;

import java.awt.Color;
import java.awt.Graphics;
import java.io.Serializable;
import java.util.Collection;
import java.util.stream.Stream;

import utils.Vector2;

public abstract class Entity implements Serializable
{
	private static final long serialVersionUID = -4333766895269415282L;
	private Vector2 pos;
	private double radius;
	private double direction = 0;
	private double speed = 0;
	private double interactAngle = Math.PI / 8;
	private Color colour, healthyColour;
	
	private double thinkTime = 0;
	private double maxThinkTime;
	private double timeAlive = 0;
	private double health = 1;
	
	private boolean dead = false;
	private double nutrition;

	public Entity()
	{
		healthyColour = new Color(255, 255, 255);
		colour = new Color(255, 255, 255);
	}
	
	public Stream<Entity> update(double delta, Stream<Entity> entities) {
		timeAlive += delta;
		return Stream.empty();
	}
	
	public void render(Graphics g)
	{
		g.setColor(getColor());
		g.fillOval(
				(int)(getPos().getX() - radius), 
				(int)(getPos().getY() - radius), 
				(int)(2*radius), 
				(int)(2*radius));
	}

	public boolean tick(double delta)
	{
		thinkTime += delta;

		if (thinkTime >= maxThinkTime) {
			thinkTime = 0;
			return true;
		}
		return false;
	}
	
	public boolean isCollidingWith(Entity other)
	{
		double dist = getPos().sub(other.getPos()).len();
		return dist < getRadius() + other.getRadius();
	}
	
	public void handleCollision(Entity other)
	{
		setDir(getDir().setDir(pos.sub(other.pos)));
	}
	
	protected boolean canInteractWith(Entity other)
	{
		Vector2 ds = other.getPos().sub(getPos());
//		return 0.9*ds.len() <= getRadius() + other.getRadius() &&
//				getVel().angleBetween(ds) < interactAngle;
		return 0.9*ds.len() <= getRadius() + other.getRadius();
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

		double p = 0.7;
		double colourDecay = 1 + p * (health - 1);
		colour = new Color(
				(int) (colourDecay * r),
				(int) (colourDecay * g),
				(int) (colourDecay * b)
		);
	}

	public Stream<Entity> handleDeath() {
		return Stream.empty();
	}

	public void setMaxThinkTime(double maxThinkTime) { this.maxThinkTime = maxThinkTime; }

	public Color getHealthyColour() { return healthyColour; }

	public void setHealthyColour(Color healthyColour) { this.healthyColour = healthyColour; }
	
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

	public Vector2 getDir() {
		return new Vector2(Math.cos(direction), Math.sin(direction));
	}

	public void setDir(Vector2 dir) {
		this.direction = dir.angle();
	}

	public void rotate(double theta) {
		this.direction += theta;
	}

	public Vector2 getVel() {
		return getDir().mul(speed);
	}

	public void setVel(Vector2 vel) {
		this.speed = vel.len();
		if (this.speed != 0)
			setDir(vel.mul(1 / this.speed));
	}

	public void setSpeed(double speed) { this.speed = speed; }

	public double getSpeed() { return speed; }

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
