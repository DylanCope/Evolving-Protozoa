package physics;

import java.util.Collection;

import utils.Vector2;

public class Particle extends PointParticle
{	
	private static final long serialVersionUID = 1L;
	protected double radius;
	protected double dragCoef = 1;
	
	public Particle(double radius)
	{
		super();
		this.radius = radius;
		mass = area();
	}
	
	public double area()
	{
		return Math.PI * radius * radius;
	}
	
	public boolean isCollidingWith(Particle p)
	{
		double dist = sub(p).len();
		return dist < radius + p.getRadius();
	}

	
	public boolean move(Vector2 dr, Collection<Particle> particles)
	{
		set(add(dr));
		
		for (Particle e : particles) 
		{
			Vector2 dx = sub(e);
			if (!e.equals(this) && isCollidingWith(e)) 
			{
				if (e.v.len()*e.radius > v.len()*radius)
					set(e.add(dx.setLength(e.radius + radius)));
				else
					e.set(sub(dx.setLength(e.radius + radius)));

				return false;
			}
		}
		
		return true;
	}
	
	private double[] calculateQuadraticCoefs(Particle other)
	{
//	    See working for collision of circles moving between frames
	    Vector2 dS = sub(other);
	    Vector2 dV = v.sub(other.v);
	    double sR = radius + other.radius;

//	    at^2 + bt + c = 0
	    double a = dV.len2();
	    double b = 2 * dS.dot(dV);
	    double c = dS.len2() - sR*sR;

	    return new double[]{a, b, c};
	}    		

	private double[] calculateRoots(double[] coefs)
	{
		double a = coefs[0];
		double b = coefs[1];
		double c = coefs[2];
		
	    double t1 = (-b + Math.sqrt(b*b - 4*a*c)) / (2*a);
	    double t2 = (-b - Math.sqrt(b*b - 4*a*c)) / (2*a);

	    return new double[]{t1, t2};
	}
	
	private Vector2 momentumCalculation(Particle p, double cR) 
	{
	    double u1x = v.getX();
	    double u1y = v.getY();
	    
	    double u2x = v.getX();
	    double u2y = p.v.getY();
	    
	    double m1 = mass;
	    double m2 = p.mass;
	    double s = m1 + m2;

	    double vx = (m1*u1x + m2*u2x + m2*cR*(u1x - u2x)) / s;
	    double vy = (m1*u1y + m2*u2y + m2*cR*(u1y - u2y)) / s;

	    return new Vector2(vx, vy);
	}

	private void applyCollisionForces(Particle p, double cR, double delta)
	{
	    Vector2 v1 = momentumCalculation(p, cR);
	    Vector2 v2 = p.momentumCalculation(this, cR);

	    Vector2 f1 = v1.sub(v).mul(mass / delta);
	    Vector2 f2 = v2.sub(p.v).mul(p.mass / delta);

	    applyForce(f2);
	    p.applyForce(f1);
	}

	private double computeRestitutionCoef(Particle p)
	{
		return 1;
	}
	
//	Calculates and sets the new positions and force on the particles
//	if they have collided
	public void handleCollision(Particle p, double delta) 
	{
		double cR = computeRestitutionCoef(p);
//	    Apply relevant mathematics regarding collision
	    double[] coefs = calculateQuadraticCoefs(p);
		double a = coefs[0];
		double b = coefs[1];
		double c = coefs[2];

	    if (b*b - 4*a*c >= 0 && a != 0) 
	    {
	        double[] roots = calculateRoots(coefs);
	        double t = Math.min(roots[0], roots[1]);

	        if (0 < t && t <= delta)
	        {
//	            Apply s = s0 + vt to move particles to where they collide
	            p.set(p.add(v.mul(t)));
	            set(add(p.v.mul(t)));

	            applyCollisionForces(p, cR, delta);
	            p.applyCollisionForces(this, cR, delta);
	        }
	    }
	}
	public double getRadius()
	{
		return radius;
	}
	
}
