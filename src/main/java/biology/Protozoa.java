package biology;

import core.Settings;
import core.Simulation;
import core.Tank;
import neat.NeuralNetwork;
import utils.Vector2;

import java.awt.*;
import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;

public class Protozoa extends Entity 
{

	private static final long serialVersionUID = 2314292760446370751L;
	public transient int id = Simulation.RANDOM.nextInt();
	private float totalConsumption = 0;
	
	private final ProtozoaGenome genome;
	private Retina retina;
	private final Brain brain;

	private float shieldFactor = 1.3f;
	private final float attackFactor = 10f;
	private final float consumeFactor = 50f;
	private float deathRate = 0;
	private int nearbyPlants = 0;

	private final float splitRadius;

	private final Vector2 dir = new Vector2(0, 0);

	public static class Spike implements Serializable {
		private static final long serialVersionUID = 1L;
		public float length;
		public float angle;
	}

	private final Spike[] spikes;
	public boolean wasJustDamaged = false;

	public Protozoa(ProtozoaGenome genome, Tank tank) throws MiscarriageException
	{
		super(tank);

		this.genome = genome;
		brain = genome.brain();
		retina = genome.retina();
		spikes = genome.getSpikes();

		setRadius(genome.getRadius());
		setHealthyColour(genome.getColour());
		setGrowthRate(genome.getGrowthRate());
		splitRadius = genome.getSplitRadius();

		setPos(new Vector2(0, 0));
		float t = (float) (2 * Math.PI * Simulation.RANDOM.nextDouble());
		dir.set(
			(float) (0.1f * Math.cos(t)),
			(float) (0.1f * Math.sin(t))
		);
	}

	public Protozoa(Tank tank) throws MiscarriageException {
		this(new ProtozoaGenome(), tank);
	}

	private int retinaCellIndex(float x, float y) {

		int cellIdx = -1;
		if (retina.getFov() <= Math.PI) {
			if (x >= 0 && y >= 0) {
				float angle = (float) Math.atan2(x, y);
				cellIdx = (int) (retina.numberOfCells() * Math.floor(2 * angle / Math.PI));
			} else if (y > x) {
				return -1;
			} else {
				return -2;
			}
		} else if (retina.getFov() > Math.PI) {
			float angle;
			if (x < 0 && y >= 0) {
				angle = (float) Math.atan2(-x, y);
			} else if (x < 0 && y < 0) {
				angle = (float) (Math.atan2(-y, -x) + Math.PI / 2);
			} else if (x > 0 && y < 0) {
				angle = (float) Math.atan2(-y, x);
			} else if (x > y){
				return -1;
			} else {
				return -2;
			}
			cellIdx = (int) (retina.numberOfCells() * Math.floor(2 * angle / (3 * Math.PI)));
		}
		if (cellIdx >= retina.numberOfCells())
			return retina.numberOfCells() - 1;
		return cellIdx;
	}

	public Retina.Cell[] getRetinaCells(Entity e, Vector2 dr) {
		Vector2 dr1 = dr.add(dr.perp().setLength(e.getRadius()));
		Vector2 dr2 = dr.add(dr.perp().setLength(-e.getRadius()));
		Vector2 dir = getDir();

		Vector2 viewMin = dir.rotate(-retina.getFov() / 2);
		Vector2 viewMax = dir.rotate(retina.getFov() / 2);
		Vector2 viewMaxPerp = viewMax.perp();
		Vector2 viewMinPerp = viewMin.perp();

		float det = viewMin.dot(viewMax.perp());
		if (det < 1e-8)
			return null;

		float x1 = -viewMaxPerp.dot(dr1) / det;
		float y1 = viewMinPerp.dot(dr1) / det;

		float x2 = -viewMaxPerp.dot(dr2) / det;
		float y2 = viewMinPerp.dot(dr2) / det;

		int idx1 = retinaCellIndex(x1, y1);
		int idx2 = retinaCellIndex(x2, y2);

		if (idx1 < 0 && idx2 < 0)
			return null;

		int idxMin, idxMax;
		if (idx1 == -1) {
			idxMin = 0; idxMax = idx2 + 1;
		}
		else if (idx1 == -2) {
			idxMin = idx2; idxMax = retina.numberOfCells();
		}
		else if (idx2 == -1) {
			idxMin = 0; idxMax = idx1 + 1;
		}
		else if (idx2 == -2) {
			idxMin = idx1; idxMax = retina.numberOfCells();
		}
		else {
			idxMin = Math.min(idx1, idx2);
			idxMax = Math.max(idx1, idx2) + 1;
		}

		return Arrays.copyOfRange(retina.getCells(), idxMin, idxMax);
	}

	public void see(Entity e)
	{
		Vector2 dr = getPos().sub(e.getPos());
		Retina.Cell[] cells = getRetinaCells(e, dr);
		if (cells == null)
			return;

		for (Retina.Cell cell : cells) {
			boolean dealtWith = false;
			float l2 = dr.len2();
			for (int i = 0; i < cell.nEntities; i++) {
				Vector2 drOther = cell.entities[i].getPos().sub(getPos());
				if (l2 > drOther.len2()) {
					if (dr.angleBetween(drOther) < Math.atan2(cell.entities[i].getRadius(), dr.len()))
						return;
				} else if (dr.angleBetween(drOther) < Math.atan2(e.getRadius(), dr.len())) {
					dealtWith = true;
					cell.set(i, e, Retina.computeWeight((float) Math.sqrt(l2)));
					break;
				}
			}

			if (!dealtWith && cell.nEntities < cell.entities.length) {
				cell.set(cell.nEntities, e, Retina.computeWeight((float) Math.sqrt(l2)));
				cell.nEntities++;
			}

			float w = cell.weights[0];
			float r = w * cell.entities[0].getColor().getRed();
			float g = w * cell.entities[0].getColor().getGreen();
			float b = w * cell.entities[0].getColor().getBlue();
			if (cell.nEntities > 1) {
				for (int i = 1; i < cell.nEntities; i++) {
					w = cell.weights[i];
					r += w * cell.entities[i].getColor().getRed();
					g += w * cell.entities[i].getColor().getGreen();
					b += w * cell.entities[i].getColor().getBlue();
				}
			}
			cell.colour = new Color(
					(int) (r / cell.nEntities),
					(int) (g / cell.nEntities),
					(int) (b / cell.nEntities)
			);
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
		wasJustDamaged = true;
		setHealth(getHealth() - damage);
	}
	
	public void attack(Protozoa p, Spike spike, float delta)
	{
		float myAttack = (float) (
				2*getHealth() +
				Settings.spikeDamage * getSpikeLength(spike) +
				2*Simulation.RANDOM.nextDouble()
		);
		float theirDefense = (float) (
				2*p.getHealth() +
				0.3*p.getRadius() +
				2*Simulation.RANDOM.nextDouble()
		);

		if (myAttack > p.shieldFactor * theirDefense)
			p.damage(delta * attackFactor * (myAttack - p.shieldFactor * theirDefense));

	}
	
	public void think(float delta)
	{
		brain.tick(this);
		dir.turn(delta * 80 * brain.turn(this));
		setVel(dir.mul(Math.abs(brain.speed(this))));
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
		float r = getRadius() + other.getRadius();
		float d = other.getPos().distanceTo(getPos());
		if (other instanceof Protozoa) {
			Protozoa p = (Protozoa) other;
			for (Spike spike : spikes) {
				float spikeLen = getSpikeLength(spike);
				if (d < r + spikeLen && spikeInContact(spike, spikeLen, p))
					attack(p, spike, delta);
			}
		}

		if (0.95 * d < r) {

			if (other instanceof Protozoa)
			{
				Protozoa p = (Protozoa) other;

//				if (brain.wantToAttack(p))
//					fight(p, delta);

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

	private boolean spikeInContact(Spike spike, float spikeLen, Entity other) {
		Vector2 spikeStartPos = getDir().unit().rotate(spike.angle).setLength(getRadius()).translate(getPos());
		Vector2 spikeEndPos = spikeStartPos.add(spikeStartPos.sub(getPos()).setLength(spikeLen));
		return other.getPos().sub(spikeEndPos).len2() < other.getRadius() * other.getRadius();
	}

	@Override
	public void handleInteractions(float delta) {
		super.handleInteractions(delta);
		nearbyPlants = 0;
		wasJustDamaged = false;
		retina.reset();
		Iterator<Entity> entities = broadCollisionDetection(Settings.protozoaInteractRange);
		entities.forEachRemaining(e -> interact(e, delta));
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
		if (retina.numberOfCells() > 0) {
			stats.put("Retina Cells", (float) retina.numberOfCells());
			stats.put("Retina FoV", (float) Math.toDegrees(retina.getFov()));
		}
		return stats;
	}

	@Override
	public float getGrowthRate() {
		float growthRate = super.getGrowthRate();
		if (getRadius() > splitRadius)
			growthRate *= getHealth() * splitRadius / (5 * getRadius());
		for (Spike spike : spikes)
			growthRate -= Settings.spikeGrowthPenalty * spike.length;
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

	public Spike[] getSpikes() {
		return spikes;
	}

	public float getSpikeLength(Spike spike) {
		return spike.length * getRadius() / splitRadius;
	}

	public Vector2 getDir() {
		return dir;
	}
}
