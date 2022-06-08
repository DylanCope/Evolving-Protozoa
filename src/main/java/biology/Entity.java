package biology;

import java.awt.Color;
import java.awt.Graphics;
import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

import com.google.common.collect.Iterators;
import core.*;
import utils.Vector2;

public abstract class Entity implements Serializable
{
	private static final long serialVersionUID = -4333766895269415282L;
	private Vector2 pos;
	private float radius;
	private float direction = 0;
	private float speed = 0;
	private float interactAngle = (float) (Math.PI / 8);
	private Color healthyColour, fullyDegradedColour;
	
	private float thinkTime = 0f;
	private float maxThinkTime;
	private float timeAlive = 0f;
	private float health = 1f;
	private float growthRate = 0.0f;
	private int generation = 1;
	
	private boolean dead = false;
	private float nutrition;
	private float crowdingFactor;
	protected boolean hasHandledDeath = false;

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

	public void handleCollisions(float delta) {
		Iterator<Entity> entities = broadCollisionDetection(getRadius());
		entities.forEachRemaining(e -> handlePotentialCollision(e, delta));
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
		setRadius(getRadius() * (1 + getGrowthRate() * delta));
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

	public boolean tick(float delta)
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
		float r = getRadius() + other.getRadius();
		return other.getPos().squareDistanceTo(getPos()) < r*r;
	}
	
	protected boolean isTouching(Entity other)
	{
		float r = getRadius() + other.getRadius();
		return 0.95 * other.getPos().distanceTo(getPos()) < r;
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
			if (e.getVel().len2()*e.getRadius() > getVel().len2()*getRadius())
				getPos().moveAway(e.getPos(), r);
			else
				e.getPos().moveAway(getPos(), r);
		}
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
	}

	public boolean isDead() {
		return dead;
	}

	public void setDead(boolean dead) {
		this.dead = dead;
	}

	public Vector2 getDir() {
		return new Vector2(
				(float) Math.cos(direction),
				(float) Math.sin(direction));
	}

	public void setDir(Vector2 dir) {
		this.direction = dir.angle();
	}

	public void rotate(float theta) {
		this.direction += theta;
		if (this.direction < 0)
			this.direction += Math.PI * 2;
		if (this.direction > Math.PI * 2)
			this.direction -= Math.PI * 2;
	}

	public Vector2 getVel() {
		return getDir().mul(speed);
	}

	public void setVel(Vector2 vel) {
		this.speed = vel.len();
		if (this.speed != 0)
			setDir(vel.mul(1 / this.speed));
	}

	public void setSpeed(float speed) { this.speed = speed; }

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

	protected <T extends Entity> void burst(Class<T> type, Function<Float, T> createChild) {
		setDead(true);
		hasHandledDeath = true;

		float angle = (float) (2 * Math.PI * Simulation.RANDOM.nextDouble());
		int nChildren = 2 + Simulation.RANDOM.nextInt(3);

		for (int i = 0; i < nChildren; i++) {
			Vector2 dir = new Vector2((float) Math.cos(angle), (float) Math.sin(angle));
			float p = (float) (0.3 + 0.7 * Simulation.RANDOM.nextDouble() / nChildren);

			int nEntities = tank.entityCounts.getOrDefault(type, 0);
			int maxEntities = tank.entityCapacities.getOrDefault(type, 0);
			if (nEntities > maxEntities)
				return;

			T e = createChild.apply(getRadius() * p);
			e.setPos(getPos().add(dir.mul(2*e.getRadius())));
			e.setGeneration(getGeneration() + 1);
			children.add(e);
			tank.add(e);
			angle += 2 * Math.PI / nChildren;
		}
	}

	public Collection<Entity> getChildren() {
		return children;
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
