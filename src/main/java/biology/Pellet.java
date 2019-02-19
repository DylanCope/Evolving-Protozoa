package biology;

import java.awt.Color;
import java.util.ArrayList;
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
		setColor(new Color(
				150 + Simulation.RANDOM.nextInt(105), 
				10  + Simulation.RANDOM.nextInt(100), 
				10  + Simulation.RANDOM.nextInt(100)));
		setNutrition(0.25 * radius);
	}

	@Override
	public Collection<Entity> update(double delta, Collection<Entity> entities)
	{	
		if (isDead())
			return new ArrayList<Entity>();
		
		move(getVel().mul(delta), entities);
		return new ArrayList<Entity>();
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
