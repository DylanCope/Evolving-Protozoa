package utils;

import java.awt.Color;
import java.awt.Graphics;
import java.util.Collection;

import biology.Entity;
import biology.Pellet;
import biology.Protozoa;
import biology.Retina;
import core.Tank;
import core.Main;

public class Renderer 
{	
	private Vector2f tankRenderCoord;
	private double tankRenderRadius;
	private Vector2f pan;
	private double zoom;
	
	public Renderer(int screenWidth, int screenHeight)
	{
		tankRenderRadius = Main.HEIGHT / 2.0;
		tankRenderCoord = new Vector2f(screenWidth*0.5, screenHeight*0.5);
//		aspect = (double) screenHeight / (double) screenWidth;
		zoom = 1;
		pan = new Vector2f(0, 0);
	}
	
	public void protozoa(Graphics g, Protozoa p)
	{
		Vector2f pos = toRenderSpace(p.getPos());
		double r = toRenderSpace(p.getRadius());
		g.setColor(p.getColor());
		g.fillOval(
				(int)(pos.getX() - r), 
				(int)(pos.getY() - r), 
				(int)(2*r), 
				(int)(2*r));
		
		double r0 = 1;
		double r1 = 0.8;
		for (Retina.Cell cell : p.getRetina())
		{
			double x = Math.cos(cell.angle + p.getVel().angle());
			double y = Math.sin(cell.angle + p.getVel().angle());
			double len = Math.sqrt(x*x + y*y);
			double r2 = r1;// + 0.5*(1 - r1)*(1 + Math.cos(2*Math.PI*cell.angle));
			g.setColor(cell.color);
			g.drawLine(
					(int)(pos.getX() + (x*r*r0)/len), 
					(int)(pos.getY() + (y*r*r0)/len), 
					(int)(pos.getX() + (x*r*r2)/len),
					(int)(pos.getY() + (y*r*r2)/len)
					);
		}
	}
	
	public void pellet(Graphics g, Pellet p)
	{
		Vector2f pos = toRenderSpace(p.getPos());
		double r = toRenderSpace(p.getRadius());
		g.setColor(p.getColor());
		g.fillOval(
				(int)(pos.getX() - r), 
				(int)(pos.getY() - r), 
				(int)(2*r), 
				(int)(2*r));
	}
	
	public void entities(Graphics g, Collection<Entity> entities)
	{
		for (Entity e : entities) {
			if (e instanceof Protozoa)
				protozoa(g, (Protozoa) e);
			else if (e instanceof Pellet)
				pellet(g, (Pellet) e);
		}
	}
	
	public void tank(Graphics g, Tank tank)
	{
		entities(g, tank.getEntities());
		
		int nPoints = 300;
		int xPoints[] = new int[nPoints];
		int yPoints[] = new int[nPoints];
		
		int n = nPoints - 7;
		for (int i = 0; i < n; i++) 
		{
			double t = 2*Math.PI * i / (double) n;
			xPoints[i] = (int) (tankRenderCoord.getX() + tankRenderRadius * Math.cos(t));
			yPoints[i] = (int) (tankRenderCoord.getY() + tankRenderRadius * Math.sin(t));
		}
		
		xPoints[n] 	 = Main.WIDTH / 2 + (int) tankRenderRadius;
		yPoints[n]	 = Main.HEIGHT / 2;
		
		xPoints[n+1] = Main.WIDTH;
		yPoints[n+1] = Main.HEIGHT / 2;
		
		xPoints[n+2] = Main.WIDTH;
		yPoints[n+2] = 0;
		
		xPoints[n+3] = 0;
		yPoints[n+3] = 0;
		
		xPoints[n+4] = 0;
		yPoints[n+4] = Main.HEIGHT;
		
		xPoints[n+5] = Main.WIDTH;
		yPoints[n+5] = Main.HEIGHT;
		
		xPoints[n+6] = Main.WIDTH;
		yPoints[n+6] = Main.HEIGHT / 2;
		
		g.setColor(Color.BLACK);
		g.fillPolygon(xPoints, yPoints, nPoints);
	}
	
	public Vector2f toRenderSpace(Vector2f v)
	{
		return v.sub(pan).mul(zoom * tankRenderRadius).add(tankRenderCoord);
	}
	
	public double toRenderSpace(double s)
	{
		return zoom * tankRenderRadius * s;
	}
	
	public boolean inTank(Tank t, Vector2f v)
	{
		double r = zoom*tankRenderRadius;
		return v.sub(tankRenderCoord).len2() < r*r;
	}
	
}
