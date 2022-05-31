package biology;

import com.google.common.collect.Streams;
import core.Settings;
import core.Simulation;
import utils.Vector2;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.Function;
import java.util.stream.Stream;

public class Protozoa extends Entity 
{

	private static final long serialVersionUID = 2314292760446370751L;
	public transient int id = Simulation.RANDOM.nextInt();
	private double totalConsumption = 0;
	
	private ProtozoaGenome genome;
	private Retina retina;
	private final Brain brain;

	private double shieldFactor = 1.3;
	private final double attackFactor = 10;
	private final double consumeFactor = 15;

	private double splitRadius = Double.MAX_VALUE; // No splitting by default.

	public Protozoa(ProtozoaGenome genome)
	{
		this(genome.brain(), genome.retina(), genome.getRadius());
		this.genome = genome;
		setGrowthRate(genome.getGrowthRate());
		splitRadius = genome.getSplitRadius();
	}

	public Protozoa() {
		this(new ProtozoaGenome());
	}

	public Protozoa(Brain brain, Retina retina, double radius)
	{
		setHealthyColour(new Color(
			100 + Simulation.RANDOM.nextInt(20),
			80 + Simulation.RANDOM.nextInt(50),
			150  + Simulation.RANDOM.nextInt(100)
		));

		this.brain = brain;
		this.retina = retina;
		setPos(new Vector2(0, 0));
		double t = 2 * Math.PI * Simulation.RANDOM.nextDouble();
		setVel(new Vector2(
				0.1 * Math.cos(t),
				0.1 * Math.sin(t)
		));
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
	
	public void eat(Entity e, double delta)
	{
		double consumed = consumeFactor * delta * e.getNutrition();
		totalConsumption += consumed;
		setHealth(getHealth() + consumed);
		e.setHealth(e.getHealth() - consumed);
	}

	public void damage(double damage) {
		setHealth(getHealth() - damage);
	}
	
	public Stream<Entity> fight(Protozoa p, double delta)
	{
		double myAttack = 2*getHealth() + 0.3*getRadius() + 2*Simulation.RANDOM.nextDouble();
		double theirAttack = 2*p.getHealth() + 0.3*p.getRadius() + 2*Simulation.RANDOM.nextDouble();

		if (myAttack > p.shieldFactor * theirAttack) {
			damage(delta * attackFactor * (myAttack - p.shieldFactor * theirAttack));
			if (isDead())
				return handleDeath();
		}
		else if (theirAttack > shieldFactor * myAttack) {
			p.damage(delta * attackFactor * (theirAttack - shieldFactor * myAttack));
			if (p.isDead())
				return p.handleDeath();
		}

		return Stream.empty();
	}
	
	public void think(double delta)
	{
		brain.tick(this);
		rotate(brain.turn(this));
		setSpeed(brain.speed(this));
	}

	private boolean shouldSplit() {
		return getRadius() > splitRadius && getHealth() > Settings.minHealthToSplit;
	}

	private Protozoa createSplitChild(double r) {
		double stuntingFactor = r / getRadius();
		Protozoa child = genome.createChild();
		child.setRadius(stuntingFactor * child.getRadius());
		return child;
	}

	public Stream<Entity> interact(Entity other, double delta) {
		if (isDead())
			return handleDeath();

		if (shouldSplit())
			return super.burst(this::createSplitChild);

		see(other);

		Stream<Entity> newEntities = super.interact(other, delta);

		if (isTouching(other)) {

			if (other instanceof Protozoa)
			{
				Protozoa p = (Protozoa) other;

				if (brain.wantToAttack(p))
					return Streams.concat(newEntities, fight(p, delta));

//				else if (brain.wantToMateWith(p) && p.brain.wantToMateWith(this)) {
//					// Add some negative consequences of mating?
//					Stream<Entity> children = genome.reproduce(this, p).map(Function.identity());
//					return Streams.concat(newEntities, children);
//				}
			}
			else if (other.isEdible())
				eat(other, delta);

		}

		return newEntities;
	}

	public void resetRetina() {
		for (Retina.Cell cell : retina) {
			cell.colour = Color.WHITE;
			cell.entity = null;
		}
	}

	private Stream<Entity> breakIntoPellets() {
		return burst(MeatPellet::new);
	}

	public Stream<Entity> handleDeath() {
		if (!hasHandledDeath) {
			return Streams.concat(super.handleDeath(), breakIntoPellets());
		}
		return Stream.empty();
	}

	@Override
	public String getPrettyName() {
		return "Protozoan";
	}

	@Override
	public HashMap<String, Double> getStats() {
		HashMap<String, Double> stats = super.getStats();
		stats.put("Fitness", getFitness());
		stats.put("Growth Rate", getGrowthRate());
		stats.put("Split Radius", splitRadius);
		return stats;
	}

	public void age(double delta) {
		double deathRate = getRadius() * getSpeed() * delta * 200;
		setHealth(getHealth() * (1 - deathRate));
	}


	@Override
	public Stream<Entity> update(double delta, Stream<Entity> entities)
	{
		age(delta);
		if (isDead())
			return handleDeath();

		resetRetina();
		think(delta);

		return super.update(delta, entities);
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
			double r2 = r1;// + 0.5 * (1 - r1) * (1 + Math.cos(2*Math.PI*cell.angle));
			g.setColor(cell.colour);
			g.drawLine(
					(int) (getPos().getX() + (x * getRadius() * r0) / len),
					(int) (getPos().getY() + (y * getRadius() * r0) / len),
					(int) (getPos().getX() + (x * getRadius() * r2) / len),
					(int) (getPos().getY() + (y * getRadius() * r2) / len)
			);
		}
	}
	
	@Override
	public double getNutrition() {
		return 20 * getHealth() * getRadius();
	}

	@Override
	public boolean isEdible() {
		return false;
	}

	public Retina getRetina() {
		return retina;
	}

	public void setRetina(Retina retina) {
		this.retina = retina;
	}

	public double getFitness() {
		return totalConsumption;
	}

	public ProtozoaGenome getGenome() {
		return genome;
	}

	public double getShieldFactor() {
		return shieldFactor;
	}

	public void setShieldFactor(double shieldFactor) {
		this.shieldFactor = shieldFactor;
	}

	public Brain getBrain() {
		return brain;
	}
}
