package biology;

import core.Simulation;
import core.Tank;
import utils.Vector2;

import java.util.HashMap;
import java.util.Map;

public abstract class Pellet extends Entity
{
	private static final long serialVersionUID = -5482090072120647315L;

	/**
	 * @param radius Radius of pellet
	 */
	public Pellet(float radius, Tank tank)
	{
		super(tank);
		this.setRadius(radius);

		setVel(new Vector2(
				(float) ((0.5 - Simulation.RANDOM.nextDouble()) / 30.0),
				(float) ((0.5 - Simulation.RANDOM.nextDouble()) / 30.0)
		));

		setNutrition((float) (0.25 * radius));
	}

	@Override
	public Map<String, Float> getStats() {
		Map<String, Float> stats = super.getStats();
		stats.put("Nutrition", getNutrition());
		return stats;
	}


	@Override
	public boolean isEdible() {
		return true;
	}

	@Override
	public float getNutrition() {
		return 3 * getRadius();
	}

}
