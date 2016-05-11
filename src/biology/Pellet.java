package biology;

import java.awt.Color;
import java.util.Collection;

import utils.Vector2;
import core.Simulation;

public class Pellet extends Entity
{
	private static final long serialVersionUID = -5482090072120647315L;

	public Pellet(double radius)
	{
		this.setRadius(radius);
		setVel(new Vector2(
				(0.5 - Simulation.RANDOM.nextDouble()) / 30.0,
				(0.5 - Simulation.RANDOM.nextDouble()) / 30.0));
		healthyColour = new Color(
				150 + Simulation.RANDOM.nextInt(105), 
				10  + Simulation.RANDOM.nextInt(100), 
				10  + Simulation.RANDOM.nextInt(100));
		setColor(healthyColour);
		setNutrition(0.25 * radius);
		maxVel = 50;
	}

	@Override
	public void update(double delta, Collection<Entity> entities) 
	{	
		if (isDead())
			return;
		
		move(getVel().mul(delta), entities);
	}

	@Override
	public boolean isEdible() {
		return true;
	}
	
	@Override
	public double getNutrition() {
		return 10 * radius;
	}

}
