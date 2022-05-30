package biology;

import core.Simulation;
import utils.Vector2;

import java.awt.*;
import java.util.HashMap;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class Pellet extends Entity
{
	private static final long serialVersionUID = -5482090072120647315L;

	/**
	 * @param radius Radius of pellet
	 */
	public Pellet(double radius)
	{
		this.setRadius(radius);

		setVel(new Vector2(
				(0.5 - Simulation.RANDOM.nextDouble()) / 30.0,
				(0.5 - Simulation.RANDOM.nextDouble()) / 30.0
		));

		setNutrition(0.25 * radius);
	}

	@Override
	public Stream<Entity> update(double delta, Stream<Entity> entities)
	{
		Stream<Entity> newEntities = super.update(delta, entities);
		if (isDead())
			return newEntities;

		move(getVel().mul(delta), entities.collect(Collectors.toList()));

		return newEntities;
	}

	@Override
	public HashMap<String, Double> getStats() {
		HashMap<String, Double> stats = super.getStats();
		stats.put("Nutrition", getNutrition());
		return stats;
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
