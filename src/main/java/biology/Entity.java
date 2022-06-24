package biology;

import core.*;
import utils.Geometry;
import utils.Vector2;

import java.awt.*;
import java.io.Serializable;
import java.util.*;

public abstract class Entity extends Collidable implements Serializable
{
	private static final long serialVersionUID = -4333766895269415282L;
	private Vector2 pos;
	private Vector2 vel = new Vector2(0, 0);
	private final Vector2 acc = new Vector2(0, 0);
	private float radius;
	private Color healthyColour, fullyDegradedColour;
	
	private float timeAlive = 0f;
	private float health = 1f;
	private float growthRate = 0.0f;
	private int generation = 1;
	protected int numEntityCollisions = 0, rockCollisions = 0;
	
	private boolean dead = false;
	private float nutrition;
	protected boolean hasHandledDeath = false;

	public Tank getTank() {
		return tank;
	}

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
	}

	public void resetPhysics() {
		acc.set(0, 0);
	}

	public void physicsUpdate(float delta) {
		float subStepDelta = delta / Settings.physicsSubSteps;
		numEntityCollisions = 0;
		rockCollisions = 0;
		ChunkManager chunkManager = tank.getChunkManager();
		for (int i = 0; i < Settings.physicsSubSteps; i++) {
			Iterator<Collidable> entities = chunkManager.broadCollisionDetection(getPos(), radius);
			entities.forEachRemaining(o -> handlePotentialCollision(o, subStepDelta));
			accelerate(getDragAcceleration());
			move(subStepDelta);
		}
	}

	public void accelerate(Vector2 da) {
		acc.translate(da);
	}

	private Vector2 getBrownianAcceleration() {
		double angle = 2 * Math.PI * Simulation.RANDOM.nextDouble();
		float r = Settings.brownianFactor * Simulation.RANDOM.nextFloat();
		float x = r * (float) Math.cos(angle);
		float y = r * (float) Math.sin(angle);
		return new Vector2(x, y);
	}

	public Vector2 getDragAcceleration() {
//		 https://galileo.phys.virginia.edu/classes/152.mf1i.spring02/Stokes_Law.htm
//		float fMag = Settings.tankViscosity * vel.len2();
		float fMag = (float) (6 * Math.PI * Settings.tankViscosity * vel.len());
		return vel.mul(-fMag / getMass());
	}

	public void handleInteractions(float delta) {
		grow(delta);
	}

	public void grow(float delta) {
		float gr = getGrowthRate();
		float newR = radius * (1 + gr * delta);
		if (newR > Settings.minPlantBirthRadius || gr > 0)
			setRadius(newR);
	}

	public void setGrowthRate(float gr) {
		growthRate = gr;
	}

	public float getGrowthRate() {
		if (rockCollisions > 2)
			return 0;
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

	@Override
	public boolean pointInside(Vector2 p) {
		return Geometry.isPointInsideCircle(getPos(), getRadius(), p);
	}

	@Override
	public boolean rayIntersects(Vector2 start, Vector2 end) {
		return false;
	}

	@Override
	public Vector2[] rayCollisions(Vector2 start, Vector2 end) {
		Vector2 ray = end.sub(start).unit();
		Vector2 p = getPos().sub(start);

		float a = ray.len2();
		float b = -2 * ray.dot(p);
		float c = p.len2() - getRadius() * getRadius();

		float d = b*b - 4*a*c;
		boolean doesIntersect = d != 0;
		if (!doesIntersect)
			return null;

		float l1 = (float) ((-b + Math.sqrt(d)) / (2*a));
		float l2 = (float) ((-b - Math.sqrt(d)) / (2*a));

		if (l1 > 0 || l2 > 0) {
			return new Vector2[]{
					start.add(ray.mul(l1)),
					start.add(ray.mul(l2))
			};
		}
		return null;
	}

	public boolean isCollidingWith(Collidable other) {
		if (other instanceof Entity)
			return isCollidingWith((Entity) other);
		else if (other instanceof Rock)
			return isCollidingWith((Rock) other);
		return false;
	}

	public boolean isCollidingWith(Rock rock) {
		Vector2[][] edges = rock.getEdges();
		float r = getRadius();
		Vector2 pos = getPos();

		if (rock.pointInside(pos))
			return true;

		for (Vector2[] edge : edges) {
			if (Geometry.doesLineIntersectCircle(edge, pos, r))
				return true;
		}
		return false;
	}

	public boolean isCollidingWith(Entity other)
	{
		float r = getRadius() + other.getRadius();
		return other.getPos().squareDistanceTo(getPos()) < r*r;
	}
	
	public abstract boolean isEdible();

	@Override
	public boolean handlePotentialCollision(Collidable other, float delta) {
		if (other instanceof Entity)
			return handlePotentialCollision((Entity) other, delta);
		else if (other instanceof Rock)
			return handlePotentialCollision((Rock) other, delta);
		return false;
	}

	public boolean handlePotentialCollision(Entity e, float delta) {
		if (e == this)
			return false;

		float sqDist = e.getPos().squareDistanceTo(getPos());

		float r = getRadius() + e.getRadius();

		if (sqDist < r*r)
		{
			numEntityCollisions++;
			getPos().moveAway(e.getPos(), r);
			e.getPos().moveAway(getPos(), r);

			Vector2 v1 = elasticCollision(e);
			Vector2 v2 = e.elasticCollision(this);
			setVel(v1);
			e.setVel(v2);
		}

		return true;
	}

	public boolean handlePotentialCollision(Rock rock, float delta) {
		Vector2[][] edges = rock.getEdges();
		Vector2 pos = getPos();
		float r = getRadius();

		if (rock.pointInside(pos)) {
			setDead(true);
			return true;
		}

		for (int i = 0; i < edges.length; i++) {
			Vector2[] edge = edges[i];
			Vector2 normal = rock.getNormals()[i];
			Vector2 dir = edge[1].sub(edge[0]);
			Vector2 x = pos.sub(edge[0]);

			if (vel.dot(normal) > 0)
				continue;

			float[] coefs = Geometry.circleIntersectLineCoefficients(dir, x, r);
			if (Geometry.lineIntersectCondition(coefs)) {
				float t1 = coefs[0], t2 = coefs[1];
				float t = (t1 + t2) / 2f;

				float offset = r - x.sub(dir.mul(t)).len();

				pos.translate(normal.mul(offset));
				vel.translate(normal.mul(-2*normal.dot(vel)));
				rockCollisions++;
				return true;
			}
		}
		return false;
	}

	public Vector2 elasticCollision(Entity e) {

		// https://en.wikipedia.org/wiki/Elastic_collision

		float mr = 2 * e.getMass() / (e.getMass() + getMass());
		Vector2 dx = getPos().sub(e.getPos());
		Vector2 dv = getVel().sub(e.getVel()).scale(Settings.coefRestitution);
		float k = dv.dot(dx) / dx.len2();
		return getVel().sub(dx.scale(mr * k));
	}

	public void move(float delta)
	{
		if (vel.len2() > Settings.maxEntitySpeed * Settings.maxEntitySpeed)
			vel.setLength(Settings.maxEntitySpeed);
		Vector2 dx = vel.mul(delta).translate(acc.mul(delta * delta));
		pos.translate(dx);
		if (Float.isNaN(pos.getX()) || Float.isNaN(pos.getY()))
			setDead(true);
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

	public abstract String getPrettyName();

	public Map<String, Float> getStats() {
		TreeMap<String, Float> stats = new TreeMap<>();
		stats.put("Age", 100 * timeAlive);
		stats.put("Health", 100 * getHealth());
		stats.put("Size", Settings.statsDistanceScalar * getRadius());
		stats.put("Speed", Settings.statsDistanceScalar * getSpeed());
		stats.put("Generation", (float) getGeneration());
		return stats;
	}

	public Map<String, Float> getDebugStats() {
		TreeMap<String, Float> stats = new TreeMap<>();
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
		if (this.radius < Settings.minEntityRadius)
			this.radius = Settings.minEntityRadius;
	}

	public boolean isDead() {
		return dead;
	}

	public void setDead(boolean dead) {
		this.dead = dead;
		health = 0;
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

	public float getSpeed() { return vel.len(); }

	public Color getColor() {
		Color healthyColour = getHealthyColour();
		Color degradedColour = getFullyDegradedColour();
		return new Color(
			(int) (healthyColour.getRed() + (1 - getHealth()) * (degradedColour.getRed() - healthyColour.getRed())),
			(int) (healthyColour.getGreen() + (1 - getHealth()) * (degradedColour.getGreen() - healthyColour.getGreen())),
			(int) (healthyColour.getBlue() + (1 - getHealth()) * (degradedColour.getBlue() - healthyColour.getBlue()))
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
		return 1000f;
	}

	public Vector2[] getBoundingBox() {
		float x = pos.getX();
		float y = pos.getY();
		float r = getRadius();
		return new Vector2[]{new Vector2(x - r, y - r), new Vector2(x + r, y + r)};
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
