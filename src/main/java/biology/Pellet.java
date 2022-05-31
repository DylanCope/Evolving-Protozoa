package biology;

import core.Simulation;
import utils.Vector2;

import java.util.HashMap;

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
