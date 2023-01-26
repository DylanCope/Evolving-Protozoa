package protoevo.biology;

import protoevo.biology.genes.CAMProductionGene;
import protoevo.biology.genes.ProtozoaGenome;
import protoevo.biology.genes.RetinalProductionGene;
import protoevo.core.*;
import protoevo.env.Tank;
import protoevo.neat.NeuralNetwork;
import protoevo.utils.Vector2;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Map;

public class Protozoan extends Cell
{

	private static final long serialVersionUID = 2314292760446370751L;
	public transient int id = Simulation.RANDOM.nextInt();
	
	private final ProtozoaGenome genome;

	private ProtozoaGenome crossOverGenome;
	private Protozoan mate;
	private float timeMating = 0;

	private Retina retina;
	private final Brain brain;

	private float shieldFactor = 1.3f;
	private final float attackFactor = 10f;
	private float deathRate = 0;
	private final float herbivoreFactor;
	private final float splitRadius;

	private final Vector2 dir = new Vector2(0, 0);

	public static class Spike implements Serializable {
		private static final long serialVersionUID = 1L;
		public float length;
		public float angle;
		public float growthRate;
		public float currentLength = 0;

		public void update(float delta) {
			if (currentLength < length) {
				currentLength = Math.min(currentLength + delta * growthRate, length);
			}
		}
	}

	public static class ContactSensor implements Serializable {
		private static final long serialVersionUID = 1L;
		public float angle;
		public Collidable contact;

		public void reset() {
			contact = null;
		}

		public boolean inContact() {
			return contact != null;
		}
	}

	private final ContactSensor[] contactSensors;
	private final Spike[] spikes;
	public boolean wasJustDamaged = false;
	private float cosHalfFov;

	public Protozoan(ProtozoaGenome genome, Tank tank) throws MiscarriageException
	{
		super(tank);

		this.genome = genome;
		brain = genome.brain();
		retina = genome.retina();
		spikes = genome.getSpikes();
		herbivoreFactor = genome.getHerbivoreFactor();

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

		contactSensors = new ContactSensor[Settings.numContactSensors];
		for (int i = 0; i < Settings.numContactSensors; i++) {
			contactSensors[i] = new ContactSensor();
			contactSensors[i].angle = (float) (2 * Math.PI * i / Settings.numContactSensors);
		}

		setDigestionRate(Food.Type.Meat, 1 / herbivoreFactor);
		setDigestionRate(Food.Type.Plant, herbivoreFactor);

		setComplexMoleculeProductionRate(
				Food.ComplexMolecule.Retinal,
				genome.getGeneValue(RetinalProductionGene.class));

		if (retina.numberOfCells() > 0)
			addConstructionProject(retina.getConstructionProject());

		Map<CellAdhesion.CellAdhesionMolecule, Float> camProduction = genome.getGeneValue(CAMProductionGene.class);
		if (camProduction != null)
			for (CellAdhesion.CellAdhesionMolecule cam : camProduction.keySet())
				setCAMProductionRate(cam, camProduction.get(cam));

	}

	public Protozoan(Tank tank) throws MiscarriageException {
		this(new ProtozoaGenome(), tank);
	}

	public Vector2 getSensorPosition(ContactSensor sensor) {
		return getPos().add(dir.rotate(sensor.angle).setLength(1.01f * getRadius()));
	}

	@Override
	public boolean handlePotentialCollision(Collidable other, float delta) {
		if (other != this) {
			for (ContactSensor contactSensor : contactSensors) {
				if (other.pointInside(getSensorPosition(contactSensor))) {
					contactSensor.contact = other;
				}
			}
		}
		return super.handlePotentialCollision(other, delta);
	}

	public boolean cullFromRayCasting(Collidable o) {
		if (o instanceof Particle) {
			Particle p = (Particle) o;
			float dx = p.getPos().getX() - getPos().getX();
			float dy = p.getPos().getY() - getPos().getY();
			float d2 = dx * dx + dy * dy;
			float dirX = getDir().getX();
			float dirY = getDir().getY();
			float dirLength2 = getDir().len2();
			return (dx * dirX + dy * dirY) / Math.sqrt(d2 * dirLength2) < cosHalfFov;
		}
		return false;
	}

	private final Vector2 rayEndTmp = new Vector2(0, 0), rayStartTmp = new Vector2(0, 0);
	private final Collidable.Collision[] collisions = new Collidable.Collision[]{
			new Collidable.Collision(), new Collidable.Collision()
	};
	public void see(Collidable o)
	{
		if (cullFromRayCasting(o))
			return;

		rayStartTmp.set(getPos());
		float interactRange = getInteractRange();
		float dirAngle = getDir().angle();
		for (Retina.Cell cell : retina.getCells()) {
			Vector2[] rays = cell.getRays();
			for (int i = 0; i < rays.length; i++) {
				rayEndTmp.set(rays[i])
						.turn(dirAngle)
						.setLength(interactRange)
						.translate(rayStartTmp);
				o.rayCollisions(rayStartTmp, rayEndTmp, collisions);

				float sqLen = Float.MAX_VALUE;
				for (Collidable.Collision collision : collisions)
					if (collision.collided)
						sqLen = Math.min(sqLen, collision.point.squareDistanceTo(rayStartTmp));

				if (sqLen < cell.collisionSqLen(i))
					cell.set(i, o.getColor(), sqLen);
			}
		}
	}
	
	public void eat(EdibleCell e, float delta)
	{
		float extraction = 1f;
		if (e instanceof PlantCell) {
			if (spikes.length > 0)
				extraction *= Math.pow(Settings.spikePlantConsumptionPenalty, spikes.length);
			extraction *= herbivoreFactor;
		} else if (e instanceof MeatCell) {
			extraction /= herbivoreFactor;
		}
		extractFood(e, extraction * delta);
	}

	public void damage(float damage) {
		wasJustDamaged = true;
		setHealth(getHealth() - damage);
	}
	
	public void attack(Protozoan p, Spike spike, float delta)
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
		float spikeDecay = (float) Math.pow(Settings.spikeMovementPenaltyFactor, spikes.length);
		float sizePenalty = getRadius() / splitRadius; // smaller flagella generate less impulse
		float speed = Math.abs(brain.speed(this));
		Vector2 vel = dir.mul(sizePenalty * spikeDecay * speed);
		float work = .5f * getMass() * vel.len2();
		if (enoughEnergyAvailable(work)) {
			useEnergy(work);
			getPos().translate(vel.scale(delta));
		}
	}

	private boolean shouldSplit() {
		return getRadius() > splitRadius && getHealth() > Settings.minHealthToSplit;
	}

	private Protozoan createSplitChild(float r) throws MiscarriageException {
		float stuntingFactor = r / getRadius();
		Protozoan child = genome.createChild(getTank(), crossOverGenome);
		child.setRadius(stuntingFactor * child.getRadius());
		return child;
	}

	public void interact(Collidable other, float delta) {
		if (other == this)
			return;

		if (isDead()) {
			handleDeath();
			return;
		}

		if (retina.numberOfCells() > 0 && retina.getHealth() > 0)
			see(other);

		if (other instanceof Cell) {
			interact((Cell) other, delta);
		}
	}

	public void interact(Cell other, float delta) {

		float d = other.getPos().distanceTo(getPos());
		if (d - other.getRadius() > getInteractRange())
			return;

		if (shouldSplit()) {
			super.burst(Protozoan.class, this::createSplitChild);
			return;
		}

		float r = getRadius() + other.getRadius();

		if (other instanceof Protozoan) {
			Protozoan p = (Protozoan) other;
			for (Spike spike : spikes) {
				float spikeLen = getSpikeLength(spike);
				if (d < r + spikeLen && spikeInContact(spike, spikeLen, p))
					attack(p, spike, delta);
			}
		}

		if (0.95 * d < r) {

			if (other instanceof Protozoan)
			{
				Protozoan p = (Protozoan) other;

				if (brain.wantToMateWith(p) && p.brain.wantToMateWith(this)) {
					if (p != mate) {
						timeMating = 0;
						mate = p;
					} else {
						timeMating += delta;
						if (timeMating >= Settings.matingTime)
							crossOverGenome = p.getGenome();
					}
				}
			}
			else if (other.isEdible())
				eat((EdibleCell) other, delta);

		}
	}

	private boolean spikeInContact(Spike spike, float spikeLen, Cell other) {
		Vector2 spikeStartPos = getDir().unit().rotate(spike.angle).setLength(getRadius()).translate(getPos());
		Vector2 spikeEndPos = spikeStartPos.add(spikeStartPos.sub(getPos()).setLength(spikeLen));
		return other.getPos().sub(spikeEndPos).len2() < other.getRadius() * other.getRadius();
	}

	public float getInteractRange() {
		return retina.numberOfCells() > 0 && retina.getHealth() > 0 ?
				Settings.protozoaInteractRange : getRadius() + 0.005f;
	}

	@Override
	public void handleInteractions(float delta) {
		super.handleInteractions(delta);
		wasJustDamaged = false;
		retina.reset();
		ChunkManager chunkManager = getTank().getChunkManager();
		Iterator<Collidable> entities = chunkManager
				.broadCollisionDetection(getPos(), getInteractRange());
		entities.forEachRemaining(e -> interact(e, delta));
	}

	private void breakIntoPellets() {
		burst(MeatCell.class, r -> new MeatCell(r, getTank()));
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
	public Map<String, Float> getStats() {
		Map<String, Float> stats = super.getStats();
		stats.put("Death Rate", 100 * deathRate);
		stats.put("Split Radius", Settings.statsDistanceScalar * splitRadius);
		stats.put("Max Turning", genome.getMaxTurn());
		stats.put("Mutations", (float) genome.getNumMutations());
		stats.put("Genetic Size", Settings.statsDistanceScalar * genome.getRadius());
		stats.put("Has Mated", crossOverGenome == null ? 0f : 1f);
		if (spikes.length > 0)
			stats.put("Num Spikes", (float) spikes.length);
		if (brain instanceof NNBrain) {
			NeuralNetwork nn = ((NNBrain) brain).network;
			stats.put("Network Depth", (float) nn.getDepth());
			stats.put("Network Size", (float) nn.getSize());
		}
		if (retina.numberOfCells() > 0) {
			stats.put("Retina Cells", (float) retina.numberOfCells());
			stats.put("Retina FoV", (float) Math.toDegrees(retina.getFov()));
			stats.put("Retina Health", retina.getHealth());
		}
		stats.put("Herbivore Factor", herbivoreFactor);
		return stats;
	}

	@Override
	public float getGrowthRate() {
		float growthRate = super.getGrowthRate();
		if (getRadius() > splitRadius)
			growthRate *= getHealth() * splitRadius / (5 * getRadius());
//		for (Spike spike : spikes)
//			growthRate -= Settings.spikeGrowthPenalty * spike.growthRate;
//		growthRate -= Settings.retinaCellGrowthCost * retina.numberOfCells();
		return growthRate;
	}

	public void age(float delta) {
		deathRate = getRadius() * delta * Settings.protozoaStarvationFactor;
//		deathRate *= 0.75f + 0.25f * getSpeed();
		deathRate *= (float) Math.pow(Settings.spikeDeathRatePenalty, spikes.length);
		setHealth(getHealth() - deathRate);
	}

	@Override
	public void update(float delta)
	{
		super.update(delta);

		age(delta);
		if (isDead())
			handleDeath();

		think(delta);

		for (Spike spike : spikes)
			spike.update(delta);

		for (ContactSensor contactSensor : contactSensors)
			contactSensor.reset();

		maintainRetina(delta);
	}

	private void maintainRetina(float delta) {
		float availableRetinal = getComplexMoleculeAvailable(Food.ComplexMolecule.Retinal);
		float usedRetinal = retina.updateHealth(delta, availableRetinal);
		depleteComplexMolecule(Food.ComplexMolecule.Retinal, usedRetinal);
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
		this.cosHalfFov = (float) Math.cos(retina.getFov() / 2f);
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

	public Spike[] getSpikes() {
		return spikes;
	}

	public float getSpikeLength(Spike spike) {
		return brain.attack(this) * spike.currentLength * getRadius() / splitRadius;
	}

	public Vector2 getDir() {
		return dir;
	}

	public ContactSensor[] getContactSensors() {
		return contactSensors;
	}

	public boolean isHarbouringCrossover() {
		return crossOverGenome != null;
	}

	public Protozoan getMate() {
		return mate;
	}

}
