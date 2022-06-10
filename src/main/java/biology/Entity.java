package biology;

import com.google.common.collect.Iterators;
import core.ChunkManager;
import core.Settings;
import core.Simulation;
import core.Tank;
import utils.Vector2;

import java.awt.*;
import java.io.Serializable;
import java.util.List;
import java.util.*;

public abstract class Entity implements Serializable
{
	private static final long serialVersionUID = -4333766895269415282L;
	private Vector2 pos;
	private Vector2 vel = new Vector2(0, 0);
	private final Vector2 force = new Vector2(0, 0);
	private float radius;
	private float speed = 0;
	private Color healthyColour, fullyDegradedColour;
	
	private float thinkTime = 0f;
	private float maxThinkTime;
	private float timeAlive = 0f;
	private float health = 1f;
	private float growthRate = 0.0f;
	private int generation = 1;
	protected int numCollisions = 0;
	
	private boolean dead = false;
	private float nutrition;
	private float crowdingFactor;
	protected boolean hasHandledDeath = false;

	@FunctionalInterface
	public interface EntityBuilder<T, R> {
		R apply(T t) throws MiscarriageException;
	}

	Tank tank;

	private final ArrayList<Entity> children = new ArrayList<>();

	public Entity(Tank tank)
	{
		this.tank = tank;
		healthyColour = new Color(255, 255, 255);
	}
	
	public void update(float delta) {
		timeAlive += delta;
		crowdingFactor = 0;
		grow(delta);
		move(delta);
	}

	public void physicsUpdate(float delta) {
		numCollisions = 0;
		Iterator<Entity> entities = broadCollisionDetection(radius);
		entities.forEachRemaining(e -> handlePotentialCollision(e, delta));
//		force.set(0, 0);
//		applyForce(getDragForce());
//		vel.translate(force.scale(delta / getMass()));
		vel.scale(1f - delta * Settings.tankViscosity);
	}

	public void applyForce(Vector2 f) {
		force.translate(f);
	}

	public Vector2 getDragForce() {
		// https://galileo.phys.virginia.edu/classes/152.mf1i.spring02/Stokes_Law.htm
		float fMag = (float) (6 * Math.PI * getRadius() * Settings.tankViscosity * vel.len());
		return vel.unit().scale(-fMag);
	}

	public Iterator<Entity> broadCollisionDetection(float range) {
		float x = getPos().getX();
		float y = getPos().getY();

		ChunkManager manager = tank.getChunkManager();
		int iMin = manager.toChunkX(x - range);
		int iMax = manager.toChunkX(x + range);
		int jMin = manager.toChunkY(y - range);
		int jMax = manager.toChunkY(y + range);

		List<Iterator<Entity>> entityIterators = new ArrayList<>();
		for (int i = iMin; i <= iMax; i++)
			for (int j = jMin; j <= jMax; j++)
				entityIterators.add(manager.getChunk(manager.toChunkID(i, j)).getEntities().iterator());

		return Iterators.concat(entityIterators.iterator());
	}

	public void grow(float delta) {
		setRadius(radius * (1 + getGrowthRate() * delta));
	}

	public void setGrowthRate(float gr) {
		growthRate = gr;
	}

	public float getGrowthRate() {
		return growthRate;
	}
	
	public void render(Graphics g)
	{
		g.setColor(getColor());
		g.fillOval(
			(int) (getPos().getX() - radius),
			(int) (getPos().getY() - radius),
			(int) (2*radius),
			(int) (2*radius)
		);
	}
	
	public boolean isCollidingWith(Entity other)
	{
		float r = getRadius() + other.getRadius();
		return other.getPos().squareDistanceTo(getPos()) < r*r;
	}
	
	public abstract boolean isEdible();

	public float getCrowdingFactor() {
		return crowdingFactor;
	}

	public void handlePotentialCollision(Entity e, float delta) {
		if (e == this)
			return;

		float sqDist = e.getPos().squareDistanceTo(getPos());

		if (sqDist < Math.pow(3 * getRadius(), 2)) {
			crowdingFactor += e.getRadius() / (getRadius() + sqDist);
		}

		float r = getRadius() + e.getRadius();

		if (sqDist < r*r)
		{
			getPos().moveAway(e.getPos(), r);
			e.getPos().moveAway(getPos(), r);

			Vector2 v1 = elasticCollision(e);
			Vector2 v2 = e.elasticCollision(this);
			setVel(v1);
			e.setVel(v2);
		}
	}

	public Vector2 elasticCollision(Entity e) {

		// https://en.wikipedia.org/wiki/Elastic_collision

		float mr = 2 * e.getMass() / (e.getMass() + getMass());
		Vector2 dx = getPos().sub(e.getPos());
		Vector2 dv = getVel().sub(e.getVel());
		float k = dv.dot(dx) / dx.len2();
		return getVel().sub(dx.scale(mr * k));
	}

	public void move(float delta)
	{
		getPos().translate(getVel().mul(delta));
	}

	public void setHealth(float h)
	{
		health = h;
		if (health > 1) 
			health = 1;
		
		if (health < 0.1)
			setDead(true);

	}

	public void handleDeath() {
		hasHandledDeath = true;
	}

	public void setMaxThinkTime(float maxThinkTime) {
		this.maxThinkTime = maxThinkTime;
	}

	public abstract String getPrettyName();

	public HashMap<String, Float> getStats() {
		HashMap<String, Float> stats = new HashMap<>();
		stats.put("Age", 100 * timeAlive);
		stats.put("Health", 100 * getHealth());
		stats.put("Size", Settings.statsDistanceScalar * getRadius());
		stats.put("Speed", Settings.statsDistanceScalar * getSpeed());
		stats.put("Crowding Factor", getCrowdingFactor());
		stats.put("Generation", (float) getGeneration());
		return stats;
	}

	public HashMap<String, Float> getDebugStats() {
		HashMap<String, Float> stats = new HashMap<>();
		stats.put("Position X", Settings.statsDistanceScalar * getPos().getX());
		stats.put("Position Y", Settings.statsDistanceScalar * getPos().getY());
		return stats;
	}
	
	public float getHealth() 
	{
		return health;
	}
	
	public Vector2 getPos() {
		return pos;
	}

	public void setPos(Vector2 pos) {
		this.pos = pos;
	}

	public float getRadius() {
		return radius;
	}

	public void setRadius(float radius) {
		this.radius = radius;
		if (this.radius > Settings.maxEntityRadius)
			this.radius = Settings.maxEntityRadius;
	}

	public boolean isDead() {
		return dead;
	}

	public void setDead(boolean dead) {
		this.dead = dead;
	}

	public Vector2 getDir() {
		return vel.unit();
	}

	public void rotate(float theta) {
		vel.turn(theta);
	}

	public Vector2 getVel() {
		return vel;
	}

	public void setVel(Vector2 vel) {
		this.vel = vel;
	}

	public void setSpeed(float speed) {
		vel.setLength(speed);
	}

	public float getSpeed() { return speed; }

	public Color getColor() {
		Color healthyColour = getHealthyColour();
		Color degradedColour = getFullyDegradedColour();
		return new Color(
			(int) (healthyColour.getRed() + (1 - health) * (degradedColour.getRed() - healthyColour.getRed())),
			(int) (healthyColour.getGreen() + (1 - health) * (degradedColour.getGreen() - healthyColour.getGreen())),
			(int) (healthyColour.getBlue() + (1 - health) * (degradedColour.getBlue() - healthyColour.getBlue()))
		);
	}

	public Color getHealthyColour() {
		return healthyColour;
	}

	public void setHealthyColour(Color healthyColour) {
		this.healthyColour = healthyColour;
	}

	public void setDegradedColour(Color fullyDegradedColour) {
		this.fullyDegradedColour = fullyDegradedColour;
	}

	public Color getFullyDegradedColour() {
		if (fullyDegradedColour == null) {
			Color healthyColour = getHealthyColour();
			int r = healthyColour.getRed();
			int g = healthyColour.getGreen();
			int b = healthyColour.getBlue();
			float p = 0.7f;
			return new Color((int) (r*p), (int) (g*p), (int) (b*p));
		}
		return fullyDegradedColour;
	}

	public float getNutrition() {
		return nutrition;
	}

	public void setNutrition(float nutrition) {
		this.nutrition = nutrition;
	}

	public int getGeneration() {
		return generation;
	}

	public void setGeneration(int generation) {
		this.generation = generation;
	}

	public int burstMultiplier() {
		return 20;
	}

	public <T extends Entity> void burst(Class<T> type, EntityBuilder<Float, T> createChild) {
		setDead(true);
		hasHandledDeath = true;

		float angle = (float) (2 * Math.PI * Simulation.RANDOM.nextDouble());
		int maxChildren = (int) (burstMultiplier() * getRadius() / Settings.maxEntityRadius);

		int nChildren = (maxChildren <= 1) ? 2 : 2 + Simulation.RANDOM.nextInt(maxChildren);

		for (int i = 0; i < nChildren; i++) {
			Vector2 dir = new Vector2((float) Math.cos(angle), (float) Math.sin(angle));
			float p = (float) (0.3 + 0.7 * Simulation.RANDOM.nextDouble() / nChildren);

			int nEntities = tank.entityCounts.getOrDefault(type, 0);
			int maxEntities = tank.entityCapacities.getOrDefault(type, 0);
			if (nEntities > maxEntities)
				return;
			try {
				T e = createChild.apply(getRadius() * p);
				e.setPos(getPos().add(dir.mul(2 * e.getRadius())));
				e.setGeneration(getGeneration() + 1);
				children.add(e);
				tank.add(e);
			} catch (MiscarriageException ignored) {}

			angle += 2 * Math.PI / nChildren;
		}
	}

	public Collection<Entity> getChildren() {
		return children;
	}

	public float getMass() {
		float r = getRadius();
		return (float) ((4 / 3) * Math.PI * r * r * r * getMassDensity());
	}

	public float getMassDensity() {
		return 1f;
	}

//	@Override
//	public boolean equals(Object o) {
//		if (o instanceof Entity) {
//			Entity e = (Entity) o;
//			return e.hashCode() == hashCode();
//		}
//		return false;
//	}
}
