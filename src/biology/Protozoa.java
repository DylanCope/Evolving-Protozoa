package biology;

import java.awt.Color;
import java.awt.Graphics;
import java.util.Collection;

import neuro.Genome;
import utils.Vector2;
import core.Simulation;

public class Protozoa extends Entity 
{
	private static final long serialVersionUID = 2314292760446370751L;
	public transient int id = Simulation.RANDOM.nextInt();
	double maxVel = 100;
	double fitness = 0;
	
	Genome genome;
	private Retina retina;
	Brain brain;
	
	boolean attack;
	boolean mate;
	
	public Protozoa(Brain brain, double radius)
	{
		healthyColour = new Color(200, 200, 255);
		setColor(healthyColour);
		this.brain = brain;
		retina = new Retina();
		setPos(new Vector2(0, 0));
		double t = 2 * Math.PI * Simulation.RANDOM.nextDouble();
		setVel(new Vector2(
				maxVel * Math.cos(t), 
				maxVel * Math.sin(t)));
		setVel(getVel().rotate(brain.turn(this)));
		setVel(getVel().setLength(brain.speed(this)));
		this.setRadius(radius);
		
		maxThinkTime = 0.2;
	}
	
	
	public void see(Entity e)
	{
		Vector2 dr = getPos().sub(e.getPos());
		double rx = dr.dot(getVel().unit());
		double ry = dr.dot(getVel().perp().unit());
		
		for (Retina.Cell cell : retina) 
		{
			double y = rx*Math.tan(cell.angle);
			
			boolean inView = Math.abs(y - ry) <= e.getRadius() && rx < 0;
			
			boolean isBlocked = false;
			if (cell.entity != null) 
				isBlocked = dr.len2() > cell.entity.getPos().sub(getPos()).len2();
			
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
		e.setHealth(e.health - e.getNutrition());
	}
	
	public void fight(Protozoa p)
	{
		double attack1 = 2*health   + 3*getRadius()/10.0   + 2*Simulation.RANDOM.nextDouble();
		double attack2 = 2*p.health + 3*p.getRadius()/10.0 + 2*Simulation.RANDOM.nextDouble();
		if (attack1 > 1.3*attack2)
			eat(p);
		else if (1.3*attack1 < attack2)
			p.eat(this);
	}
	
	public void think(double delta)
	{
		thinkTime += delta;
		timeAlive += delta;
		if(thinkTime >= maxThinkTime)
		{
			thinkTime = 0;
			setVel(getVel().rotate(brain.turn(this)));
			setVel(getVel().setLength(brain.speed(this)));
		}
		double deathRate = radius * delta * 2.5;
		setHealth(health * (1 - deathRate));
		
	}
	
	public void interact(Collection<Entity> entities)
	{
		for (Retina.Cell cell : retina) 
		{
			cell.color = Color.WHITE;
			cell.entity = null;
		}
		
		for (Entity e : entities) 
		{
			if (e.equals(this)) 
				continue;
			
			if (inInteractionRange(e)) 
			{
				if (e instanceof Protozoa){
//					if (attack)
						fight((Protozoa) e);
//					else if (mate)
//						mate((Protozoa) e);
				}
				else {
					eat(e);
				}
			}
			
			see(e);
		}
	}
	
	@Override
	public void update(double delta, Collection<Entity> entities)
	{
		if (isDead())
			return;

		think(delta);
		interact(entities);
		move(getVel().mul(delta), entities);
	}
	
	public void render(Graphics g)
	{
		super.render(g);
		
		double r0 = 1;
		double r1 = 0.8;
		for (Retina.Cell cell : retina)
		{
			double x = Math.cos(cell.angle + getVel().angle());
			double y = Math.sin(cell.angle + getVel().angle());
			double len = Math.sqrt(x*x + y*y);
			double r2 = r1;// + 0.5*(1 - r1)*(1 + Math.cos(2*Math.PI*cell.angle));
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
		return 20 * health * radius;
	}

	@Override
	public boolean isEdible() {
		return true;
	}

	@Override
	public void setDead(boolean dead) {
		super.setDead(dead);
//		if (dead)
//			System.out.println("(" + fitness + ", " + timeAlive + "),");
	}


	public Retina getRetina() {
		return retina;
	}


	public void setRetina(Retina retina) {
		this.retina = retina;
	}

	public double getFitness() {
		return fitness;
	}
	
}
