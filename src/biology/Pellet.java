package biology;

import java.awt.Color;
import java.util.Collection;

import utils.Vector2f;

public class Pellet extends Entity 
{
	
	public Pellet(int x, int y, int radius)
	{
		super();
		this.setRadius(radius);
		setPos(new Vector2f(x, y));
		setVel(new Vector2f(0, 0));
		setColor(new Color(150 + random.nextInt(105), 10+random.nextInt(100), 10+random.nextInt(100)));
		setNutrition(0.25);
		maxVel = 50;
		nextVelocity();
	}

	@Override
	public void update(double delta, Collection<Entity> entities) 
	{	
		move(getVel().mul(delta), entities);
	}

	@Override
	public boolean isEdible() {
		return true;
	}
	
}
