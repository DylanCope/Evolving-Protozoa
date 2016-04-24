package biology;

import java.awt.Color;
import java.awt.Graphics;
import java.util.Collection;
import java.util.Random;

import utils.Vector2f;

public class Protozoa extends Entity 
{
	
	double thinkTime = 0;
	double maxThinkTime;
	double health = 1;
	double maxVel = 50;
	double fitness = 0;
	double timeAlive = 0;

	Random r = new Random();
	Retina retina;
	Brain brain;
	Color healthyColor = new Color(50, 50, 80);
	
	public Protozoa(Brain brain, int radius)
	{
		setColor(healthyColor);
		this.brain = brain;
		retina = new Retina();
		setPos(new Vector2f(0, 0));
		Vector2f v = new Vector2f(1, 0)
			.rotate(r.nextDouble()*2*Math.PI)
			.mul(maxVel);
		setVel(v);
		this.setRadius(radius);
		
		maxThinkTime = 0.2;
	}
	
	
	public void see(Entity e)
	{
		Vector2f dr = getPos().sub(e.getPos());
		double rx = dr.dot(getVel().unit());
		double ry = dr.dot(getVel().perp().unit());
		
		for (Retina.Cell cell : retina) {
			double y = rx*Math.tan(cell.angle);
			
			boolean inView = Math.abs(y - ry) <= e.getRadius() && rx < 0;
			
			boolean isBlocked = false;
			if (cell.entity != null) 
				isBlocked = dr.length() < cell.entity.getPos().sub(getPos()).length();
			
			if (inView && !isBlocked) {
				cell.entity = e;
				cell.color = e.getColor();
			}
		}
	}
	
	public void eat(Entity e) 
	{
		fitness += e.getNutrition();
		setHealth(health + e.getNutrition());
		e.setDead(true);
	}
	
	public void setHealth(double h)
	{
		health = h;
		if (health > 1) 
			health = 1;
		int r = healthyColor.getRed();
		r += (int) ((1 - health)*(255 - r));
		setColor(new Color(r, getColor().getGreen(), getColor().getBlue()));
	}
	
	public double getHealth() 
	{
		return health;
	}
	
	@Override
	public void update(double delta, Collection<Entity> entities)
	{
		thinkTime += delta;
		timeAlive += delta;
		if(thinkTime >= maxThinkTime)
		{
			thinkTime = 0;
			setVel(getVel().rotate(brain.turn(this)));
			move(getVel().mul(delta * brain.speed(this)), entities);
			setDead(health < 0.1);
		}
		setHealth(health * (1 - delta / 40.0));
		
		move(getVel().mul(delta), entities);
		
		for (Retina.Cell cell : retina) 
		{
			cell.color = Color.WHITE;
			cell.entity = null;
		}
		
		for (Entity e : entities) 
		{
			if (inEatingRange(e) && e.isEdible())
				eat(e);
			
			if (!e.equals(this))
				see(e);
		}
	}
	
	public void render(Graphics g)
	{
		g.setColor(getColor().brighter());
		g.fillOval(
				(int)(getPos().x()-getRadius()), 
				(int)(getPos().y()-getRadius()), 
				2*getRadius(), 2*getRadius());
		
		double r0 = 1;
		double r1 = 0.8;
		for (Retina.Cell cell : retina)
		{
			double x = Math.cos(cell.angle + getVel().angle());
			double y = Math.sin(cell.angle + getVel().angle());
			double len = Math.sqrt(x*x + y*y);
			double r2 = r1 + 0.5*(1 - r1)*(1 + Math.cos(2*Math.PI*cell.angle));
			g.setColor(cell.color);
			g.drawLine(
					(int)(getPos().getX() + (x*getRadius()*r0)/len), 
					(int)(getPos().getY() + (y*getRadius()*r0)/len), 
					(int)(getPos().getX() + (x*getRadius()*r2)/len),
					(int)(getPos().getY() + (y*getRadius()*r2)/len)
					);
		}
	}
	
	@Override
	public double getNutrition() {
		return 0.8 * health;
	}

	@Override
	public boolean isEdible() {
		return health < 0.4;
	}

	@Override
	public void setDead(boolean dead) {
		super.setDead(dead);
		if (dead)
			System.out.println("(" + fitness + ", " + timeAlive + "),");
	}
	
}
