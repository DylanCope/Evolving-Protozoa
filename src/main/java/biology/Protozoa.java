package biology;

import core.Settings;
import core.Simulation;
import core.Tank;
import neat.NeuralNetwork;
import utils.Vector2;

import java.awt.*;
import java.util.HashMap;
import java.util.Iterator;

public class Protozoa extends Entity 
{

	private static final long serialVersionUID = 2314292760446370751L;
	public transient int id = Simulation.RANDOM.nextInt();
	private float totalConsumption = 0;
	
	private ProtozoaGenome genome;
	private Retina retina;
	private final Brain brain;

	private float shieldFactor = 1.3f;
	private final float attackFactor = 10f;
	private final float consumeFactor = 50f;
	private float deathRate = 0;
	private int nearbyPlants = 0;

	private float splitRadius = Float.MAX_VALUE; // No splitting by default.

	public Protozoa(ProtozoaGenome genome, Tank tank) throws MiscarriageException
	{
		this(genome.brain(), genome.retina(), genome.getRadius(), genome.getColour(), tank);
		this.genome = genome;
		setGrowthRate(genome.getGrowthRate());
		splitRadius = genome.getSplitRadius();
	}

	public Protozoa(Tank tank) throws MiscarriageException {
		this(new ProtozoaGenome(), tank);
	}

	public Protozoa(Brain brain, Retina retina, float radius, Color healthyColor, Tank tank)
	{
		super(tank);
		setHealthyColour(healthyColor);

		this.brain = brain;
		this.retina = retina;
		setPos(new Vector2(0, 0));
		float t = (float) (2 * Math.PI * Simulation.RANDOM.nextDouble());
		setVel(new Vector2(
				(float) (0.1f * Math.cos(t)),
				(float) (0.1f * Math.sin(t))
		));
		this.setRadius(radius);
		setMaxThinkTime(0.2f);
	}

	public void see(Entity e)
	{
		Vector2 dr = getPos().sub(e.getPos());
		Vector2 dir = getDir();
		float rx = dr.dot(dir);
		float ry = dr.dot(dir.perp());
		
		for (Retina.Cell cell : retina) 
		{
			float y = (float) (rx*Math.tan(cell.angle));
			boolean inView = Math.abs(y - ry) <= e.getRadius() && rx < 0;
			
			boolean isBlocked = false;
			if (cell.entity != null) 
				isBlocked = dr.len2() > cell.entity.getPos().squareDistanceTo(getPos());
			
			if (inView && !isBlocked) {
				cell.entity = e;
				cell.colour = e.getColor();
			}
		}
	}
	
	public void eat(Entity e, float delta)
	{
		float consumed = consumeFactor * delta * e.getNutrition();
		totalConsumption += consumed;
		setHealth(getHealth() + Settings.eatingConversionRation * consumed);
		e.setHealth(e.getHealth() - consumed);
	}

	public void damage(float damage) {
		setHealth(getHealth() - damage);
	}
	
	public void fight(Protozoa p, float delta)
	{
		float myAttack = (float) (2*getHealth() + 0.3*getRadius() + 2*Simulation.RANDOM.nextDouble());
		float theirAttack = (float) (2*p.getHealth() + 0.3*p.getRadius() + 2*Simulation.RANDOM.nextDouble());

		if (myAttack > p.shieldFactor * theirAttack) {
			damage(delta * attackFactor * (myAttack - p.shieldFactor * theirAttack));
			if (isDead())
				handleDeath();
		}
		else if (theirAttack > shieldFactor * myAttack) {
			p.damage(delta * attackFactor * (theirAttack - shieldFactor * myAttack));
			if (p.isDead())
				p.handleDeath();
		}
	}
	
	public void think(float delta)
	{
		brain.tick(this);
		rotate(delta * 80 * brain.turn(this));
		setSpeed(Math.abs(brain.speed(this)));
	}

	private boolean shouldSplit() {
		return getRadius() > splitRadius && getHealth() > Settings.minHealthToSplit;
	}

	private Protozoa createSplitChild(float r) throws MiscarriageException {
		float stuntingFactor = r / getRadius();
		Protozoa child = genome.createChild(tank);
		child.setRadius(stuntingFactor * child.getRadius());
		return child;
	}

	public void interact(Entity other, float delta) {

		if (other == this || other.getPos().sub(getPos()).len() > Settings.protozoaInteractRange)
			return;

		if (isDead()) {
			handleDeath();
			return;
		}

		if (retina.numberOfCells() > 0)
			see(other);

		if (other instanceof PlantPellet)
			nearbyPlants++;

		if (shouldSplit()) {
			super.burst(Protozoa.class, this::createSplitChild);
			return;
		}

		if (isTouching(other)) {

			if (other instanceof Protozoa)
			{
				Protozoa p = (Protozoa) other;

				if (brain.wantToAttack(p))
					fight(p, delta);

//				else if (brain.wantToMateWith(p) && p.brain.wantToMateWith(this)) {
//					// Add some negative consequences of mating?
//					Stream<Entity> children = genome.reproduce(this, p).map(Function.identity());
//					return Streams.concat(newEntities, children);
//				}
			}
			else if (other.isEdible())
				eat(other, delta);

		}
	}

	public void handleInteractions(float delta) {
		nearbyPlants = 0;
		resetRetina();
		Iterator<Entity> entities = broadCollisionDetection(Settings.protozoaInteractRange);
		entities.forEachRemaining(e -> interact(e, delta));
	}

	public void resetRetina() {
		for (Retina.Cell cell : retina) {
			cell.colour = Color.WHITE;
			cell.entity = null;
		}
	}

	private void breakIntoPellets() {
		burst(MeatPellet.class, r -> new MeatPellet(r, tank));
	}

	public void handleDeath() {
		if (!hasHandledDeath) {
			super.handleDeath();
			breakIntoPellets();
		}
	}

	@Override
	public String getPrettyName() {
		return "Protozoan";
	}

	@Override
	public HashMap<String, Float> getStats() {
		HashMap<String, Float> stats = super.getStats();
		stats.put("Growth Rate", Settings.statsDistanceScalar * getGrowthRate());
		stats.put("Death Rate", 100 * deathRate);
		stats.put("Split Radius", Settings.statsDistanceScalar * splitRadius);
		if (genome != null) {
			stats.put("Mutations", (float) genome.getNumMutations());
			stats.put("Genetic Size", Settings.statsDistanceScalar * genome.getRadius());
		}
		if (brain instanceof NNBrain) {
			NeuralNetwork nn = ((NNBrain) brain).network;
			stats.put("Network Depth", (float) nn.getDepth());
			stats.put("Network Size", (float) nn.getSize());
		}
		return stats;
	}

	@Override
	public float getGrowthRate() {
		float growthRate = super.getGrowthRate();
		if (getRadius() > splitRadius)
			growthRate *= getHealth() * splitRadius / (5 * getRadius());
		return growthRate;
	}

	public void age(float delta) {
		deathRate = getRadius() * delta * Settings.protozoaStarvationFactor;
		deathRate *= 0.75f + 0.25f * getSpeed();
		setHealth(getHealth() * (1 - deathRate));
	}

	@Override
	public void update(float delta)
	{
		super.update(delta);

		age(delta);
		if (isDead())
			handleDeath();

		think(delta);
	}
	
	public void render(Graphics g)
	{
		super.render(g);
		
		float r0 = 1;
		float r1 = 0.8f;
		for (Retina.Cell cell : retina)
		{
			float x = (float) Math.cos(cell.angle + getVel().angle());
			float y = (float) Math.sin(cell.angle + getVel().angle());
			float len = (float) Math.sqrt(x*x + y*y);
			float r2 = r1;// + 0.5 * (1 - r1) * (1 + Math.cos(2*Math.PI*cell.angle));
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
	public float getNutrition() {
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

	public float getFitness() {
		return totalConsumption;
	}

	public ProtozoaGenome getGenome() {
		return genome;
	}

	public float getShieldFactor() {
		return shieldFactor;
	}

	public void setShieldFactor(float shieldFactor) {
		this.shieldFactor = shieldFactor;
	}

	public Brain getBrain() {
		return brain;
	}

	public int numNearbyPlants() {
		return nearbyPlants;
	}
}
