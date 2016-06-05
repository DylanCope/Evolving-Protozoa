package physics;

import java.io.Serializable;

import utils.Vector2;

public class Force extends Vector2 implements Serializable
{
	private static final long serialVersionUID = 1L;

	private double interval;
	
	public Force(Vector2 f, double interval)
	{
		super(f.getX(), f.getY());
		this.interval = interval;
	}
	
	public Force(Vector2 f)
	{
		this(f, 0);
	}
	
	public Force(double x, double y)
	{
		this(new Vector2(x, y));
	}
	
	public void update(double delta) 
	{
		interval -= delta;
	}
	
	public boolean disposable()
	{
		return interval < 0;
	}
}
