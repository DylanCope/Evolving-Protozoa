package core;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferStrategy;
import java.util.Collection;

import utils.Vector2f;
import utils.Window;
import biology.Entity;
import biology.Pellet;
import biology.Protozoa;
import biology.Retina;

public class Renderer extends Canvas
{	
	private static final long serialVersionUID = 1L;
	private Vector2f tankRenderCoords;
	private double tankRenderRadius;
	private Vector2f pan;
	private double zoom;
	private Entity track;
	
	private Simulation simulation;
	private Window window;
	
	public Renderer(Simulation simulation, Window window)
	{
		this.simulation = simulation;
		this.window = window;
		
		tankRenderRadius = window.getHeight() / 2.0;
		tankRenderCoords = new Vector2f(window.getWidth()*0.5, window.getHeight()*0.5);
//		aspect = (double) screenHeight / (double) screenWidth;
		zoom = 0.9;
		pan = new Vector2f(0, 0);
	}
	
	public void protozoa(Graphics g, Protozoa p)
	{
		Vector2f pos = toRenderSpace(p.getPos());
		double len = pos.length();
		double alpha = 1;
		if (len < 1)
			alpha = 1 - (len - 1) / (p.getRadius());
		if (alpha > 1)
			alpha = 1;
		
		double r = toRenderSpace(p.getRadius());
		Color c = p.getColor();
		g.setColor(new Color(
				c.getRed(), 
				c.getBlue(), 
				c.getGreen(), 
				(int) (255*alpha)));
		
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
			len = Math.sqrt(x*x + y*y);
			double r2 = r1;// + 0.5*(1 - r1)*(1 + Math.cos(2*Math.PI*cell.angle));
			c = cell.color;
			g.setColor(new Color(
					c.getRed(), 
					c.getBlue(), 
					c.getGreen(), 
					(int) (255*alpha)));
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
	
	public void maskTank(Graphics g, Vector2f coords, double r, int alpha)
	{
		int nPoints = 500;
		int xPoints[] = new int[nPoints];
		int yPoints[] = new int[nPoints];
		
		int n = nPoints - 7;
		for (int i = 0; i < n; i++) 
		{
			double t = 2*Math.PI * i / (double) n;
			xPoints[i] = (int) (coords.getX() + r * Math.cos(t));
			yPoints[i] = (int) (coords.getY() + r * Math.sin(t));
		}
		
		xPoints[n] 	 = (int) (coords.getX()) + (int) r;
		yPoints[n]	 = (int) (coords.getY());
		
		xPoints[n+1] = window.getWidth();
		yPoints[n+1] = (int) (coords.getY());
		
		xPoints[n+2] = window.getWidth();
		yPoints[n+2] = 0;
		
		xPoints[n+3] = 0;
		yPoints[n+3] = 0;
		
		xPoints[n+4] = 0;
		yPoints[n+4] = window.getHeight();
		
		xPoints[n+5] = window.getWidth();
		yPoints[n+5] = window.getHeight();
		
		xPoints[n+6] = window.getWidth();
		yPoints[n+6] = (int) (coords.getY());
		
		g.setColor(new Color(0, 0, 0, alpha));
		g.fillPolygon(xPoints, yPoints, nPoints);
	}
	
	double t = 0;
	public void render()
	{
		BufferStrategy bs = this.getBufferStrategy();
		
		if (bs == null) 
		{
			this.createBufferStrategy(3);
			return;
		}

		Graphics2D graphics = (Graphics2D) bs.getDrawGraphics();
		graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		t += 0.001;
		Color backgroundColour = new Color(
				10 + (int)(5*Math.cos(t)), 
				50 + (int)(30*Math.sin(t)), 
				30 + (int)(15*Math.cos(t + 1)));
		graphics.setColor(backgroundColour);

		graphics.fillRect(0, 0, window.getWidth(), window.getHeight());
		
		entities(graphics, simulation.getTank().getEntities());
		maskTank(graphics, tankRenderCoords, tankRenderRadius, 200);
		maskTank(graphics, tankRenderCoords.add(pan.mul(-1)), tankRenderRadius*zoom, 255);
		
		graphics.dispose();
		bs.show();
	}
	
	public Vector2f toRenderSpace(Vector2f v)
	{
		Vector2f x;
		if (track == null)
			x = v;
		else
			x = v.sub(track.getPos());
		return x.mul(tankRenderRadius).sub(pan).mul(zoom).add(tankRenderCoords);
	}
	
	public double toRenderSpace(double s)
	{
		return zoom * tankRenderRadius * s;
	}

	public void setZoom(double d) {
		zoom = d;
		if (zoom < 0.5) {
			pan = new Vector2f(0, 0);
			zoom = 0.5;
		}
	}

	public void pan(Vector2f delta) {
		if (track == null)
			pan = pan.add(delta.mul(1/zoom));
	}
	
	public void track(Entity e) {
		if (e != null)
			pan = new Vector2f(0, 0);
		else if (track != null)
			pan = track.getPos().mul(tankRenderRadius);
		track = e;
	}
	
}
