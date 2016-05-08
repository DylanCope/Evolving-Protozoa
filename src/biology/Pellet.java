package biology;

import java.awt.Color;
import java.util.Collection;

import core.Simulation;
import utils.Vector2f;

public class Pellet extends Entity 
{
	
	public Pellet(double radius)
	{
		this.setRadius(radius);
		setVel(new Vector2f(
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
