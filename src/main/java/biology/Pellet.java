package biology;

import core.Simulation;
import utils.Vector2;

import java.awt.*;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Pellet extends Entity
{
	private static final long serialVersionUID = -5482090072120647315L;

	private static final double splitRadiusFactor = 1.2;
	private static final double minSplitRadius = 0.01;
	private double initialRadius;
	private double growthRate;

	public Pellet(double radius, double growthRate)
	{
		this.setRadius(radius);
		initialRadius = radius;
		this.growthRate = growthRate;
		setVel(new Vector2(
				(0.5 - Simulation.RANDOM.nextDouble()) / 30.0,
				(0.5 - Simulation.RANDOM.nextDouble()) / 30.0));
		setColor(new Color(
				150 + Simulation.RANDOM.nextInt(105), 
				10  + Simulation.RANDOM.nextInt(100), 
				10  + Simulation.RANDOM.nextInt(100)));
		setNutrition(0.25 * radius);
	}

	public Pellet(double radius)
	{
		this(radius, 1.01);
	}

	@Override
	public Stream<Entity> update(double delta, Stream<Entity> entities)
	{
		Stream<Entity> newEntities = super.update(delta, entities);
		if (isDead())
			return newEntities;

		setRadius(getRadius() * (1 + (growthRate - 1) * delta));
		if (getRadius() > initialRadius * splitRadiusFactor & getRadius() > minSplitRadius)
			return splitPellet();

		move(getVel().mul(delta), entities.collect(Collectors.toList()));

		return newEntities;
	}

	private Stream<Entity> splitPellet() {
		setDead(true);
		Random random = new Random();
		Vector2 dir = new Vector2(2*random.nextDouble() - 1, 2*random.nextDouble() - 1);
		Pellet pellet1 = new Pellet(getRadius() / 2);
		Pellet pellet2 = new Pellet(getRadius() / 2);
		pellet1.setPos(getPos().add(dir.mul(getRadius())));
		pellet2.setPos(getPos().add(dir.mul(-getRadius())));
		return Stream.of(pellet1, pellet2);
	}

	@Override
	public boolean isEdible() {
		return true;
	}
	
	@Override
	public double getNutrition() {
		return 10 * getRadius();
	}

}
