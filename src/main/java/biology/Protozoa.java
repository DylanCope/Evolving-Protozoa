package biology;

import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import utils.Vector2;
import core.Simulation;

public class Protozoa extends Entity 
{

	private static final long serialVersionUID = 2314292760446370751L;
	public transient int id = Simulation.RANDOM.nextInt();
	private double totalConsumption = 0;
	
	private ProtozoaGenome genome;
	private Retina retina;
	private Brain brain;

	public Protozoa(ProtozoaGenome genome)
	{
		this(genome.brain(), genome.retina(), genome.getRadius());
		this.genome = genome;
	}

	public Protozoa(Brain brain, Retina retina, double radius)
	{
		setHealthyColour(new Color(200, 200, 255));
		setColor(getHealthyColour());
		this.brain = brain;
		this.retina = retina;
		setPos(new Vector2(0, 0));
		double t = 2 * Math.PI * Simulation.RANDOM.nextDouble();
		setVel(new Vector2(
				0.1 * Math.cos(t),
				0.1 * Math.sin(t)));
		this.setRadius(radius);
		setMaxThinkTime(0.2);
	}

	public void see(Entity e)
	{
		Vector2 dr = getPos().sub(e.getPos());
		Vector2 dir = getDir();
		double rx = dr.dot(dir);
		double ry = dr.dot(dir.perp());
		
		for (Retina.Cell cell : retina) 
		{
			double y = rx*Math.tan(cell.angle);
			boolean inView = Math.abs(y - ry) <= e.getRadius() && rx < 0;
			
			boolean isBlocked = false;
			if (cell.entity != null) 
				isBlocked = dr.len2() > cell.entity.getPos().sub(getPos()).len2();
			
			if (inView && !isBlocked) {
				cell.entity = e;
				cell.colour = e.getColor();
			}
		}
	}
	
	public void eat(Entity e) 
	{
		totalConsumption += e.getNutrition();
		setHealth(getHealth() + e.getNutrition());
		e.setHealth(e.getHealth() - e.getNutrition());
	}
	
	public void fight(Protozoa p)
	{
		double attack1 = 2*getHealth()   + 3*getRadius()/10.0   + 2*Simulation.RANDOM.nextDouble();
		double attack2 = 2*p.getHealth() + 3*p.getRadius()/10.0 + 2*Simulation.RANDOM.nextDouble();
		if (attack1 > 1.3*attack2)
			eat(p);
		else if (1.3*attack1 < attack2)
			p.eat(this);
	}
	
	public void think(double delta)
	{
		brain.tick(this);
		if(super.tick(delta))
		{
			rotate(brain.turn(this));
			setSpeed(brain.speed(this));
		}
		double deathRate = getRadius() * delta * 2.5;
		setHealth(getHealth() * (1 - deathRate));
	}

	public Stream<Entity> interactWith(Entity other) {

		if (other.equals(this))
			return Stream.empty();

		see(other);

		Collection<Entity> newEntities = new ArrayList<>();
		if (canInteractWith(other)) {
			if (other instanceof Protozoa)
			{
				Protozoa p = (Protozoa) other;
				if (brain.wantToAttack(p))
					fight(p);
				else if (brain.wantToMateWith(p) && p.brain.wantToMateWith(this))
					newEntities.add(genome.reproduce(this, p));
			}
			else {
				eat(other);
			}
		}
		return newEntities.stream();
	}

	public Stream<Entity> interact(Stream<Entity> entities) {
		for (Retina.Cell cell : retina) {
			cell.colour = Color.WHITE;
			cell.entity = null;
		}

		return entities.flatMap(this::interactWith);
	}

	@Override
	public Stream<Entity> update(double delta, Stream<Entity> entities)
	{
		if (isDead())
			return Stream.empty();

		think(delta);
		Collection<Entity> entityCollection = entities.collect(Collectors.toList());
		Stream<Entity> newEntities = interact(entityCollection.stream());
		move(getVel().mul(delta), entityCollection);

		return newEntities;
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
			g.setColor(cell.colour);
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
		return 20 * getHealth() * getRadius();
	}

	@Override
	public boolean isEdible() {
		return true;
	}

	@Override
	public void setDead(boolean dead) { super.setDead(dead); }

	public Retina getRetina() {
		return retina;
	}

	public void setRetina(Retina retina) {
		this.retina = retina;
	}

	public double getFitness() {
		return totalConsumption;
	}

	public ProtozoaGenome getGenome() { return genome; }
	
}
