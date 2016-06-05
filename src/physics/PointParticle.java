package physics;

import java.io.Serializable;
import java.util.ArrayList;

import utils.Vector2;

public class PointParticle extends Vector2 implements Serializable
{
	private static final long serialVersionUID = 1L;
	protected Vector2 v, a;
	private ArrayList<Vector2> forces;
	protected double mass;
	
	public PointParticle()
	{
		super(0, 0);
		forces = new ArrayList<Vector2>();
	}
	
//	public void applyForce(Force f)
//	{
//		forces.add(f);
//	}
	
	public void applyForce(Vector2 f)
	{
		forces.add(f);
	}
	
	public void update(double delta)
	{
		Vector2 F = new Vector2(0, 0);
		for (Vector2 f : forces)
		{
//			f.update(delta);
			F = F.add(f);
		}
		a = F.mul(1 / mass);
		v = v.add(a.mul(delta));
		set(add(v.mul(delta)));
		
		forces.clear();
//		forces.removeIf(new Predicate<Force>() {
//			@Override
//			public boolean test(Force f) {
//				return f.disposable();
//			}
//		});
	}
	
}
