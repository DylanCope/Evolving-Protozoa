package core;

import java.awt.BasicStroke;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.image.BufferStrategy;
import java.util.Collection;

import utils.TextObject;
import utils.Vector2;
import utils.Window;
import biology.Entity;
import biology.Pellet;
import biology.Protozoa;
import biology.Retina;

public class Renderer extends Canvas
{	
	private static final long serialVersionUID = 1L;
	private Vector2 tankRenderCoords;
	private double tankRenderRadius;
	private Vector2 pan;
	private double zoom, targetZoom, initialZoom = 2, rotate = 0;
	private Entity track;
	
	private TextObject title, pelletText, protozoaText;
	
	private Simulation simulation;
	private Window window;
	
	public Renderer(Simulation simulation, Window window)
	{
		this.simulation = simulation;
		this.window = window;
		
		tankRenderRadius = window.getHeight() / 2.0;
		tankRenderCoords = new Vector2(window.getWidth()*0.5, window.getHeight()*0.5);
		pan = new Vector2(0, 0);
		
		zoom = 1;
		targetZoom = 1;
		title = new TextObject("Microbial Evolution", 
				"bubble sharp", 
				window.getHeight() / 20, 
				new Vector2(10, window.getHeight() / 20));
		title.setColor(Color.WHITE);

		pelletText = new TextObject("Number of pellets: ", 
				"bubble sharp", 
				window.getHeight() / 30, 
				new Vector2(10, 3*window.getHeight() / 20));
		pelletText.setColor(Color.WHITE.darker());

		protozoaText = new TextObject("Number of protozoa: ", 
				"bubble sharp", 
				window.getHeight() / 30, 
				new Vector2(10, 4.1*window.getHeight() / 20));
		protozoaText.setColor(Color.WHITE.darker());
		
		requestFocus();
		setFocusable(true);
	}
	
	public void retina(Graphics2D g, Protozoa p)
	{
		Vector2 pos = toRenderSpace(p.getPos());
		Color c = p.getColor();
		double r = toRenderSpace(p.getRadius());

		double dt 	= p.getRetina().getCellAngle();
		double fov 	= p.getRetina().getFov();
		double t0 	= -p.getVel().angle() - 0.5*fov - rotate;
		double t 	= t0;
		
		g.setColor(c.darker());
		g.fillArc(
				(int)(pos.getX() - r), 
				(int)(pos.getY() - r), 
				(int)(2*r), 
				(int)(2*r),
				(int) Math.toDegrees(t0  - 2.8*dt), 
				(int) Math.toDegrees(fov + 5.6*dt));
		
		for (Retina.Cell cell : p.getRetina())
		{
			Color col = cell.color;
			if (cell.entity != null && 
					toRenderSpace(cell.entity.getPos()).sub(pos).len2() > tankRenderRadius*tankRenderRadius)
				col = Color.WHITE;
			g.setColor(col);
			g.fillArc(
					(int)(pos.getX() - r), 
					(int)(pos.getY() - r), 
					(int)(2*r), 
					(int)(2*r),
					(int) Math.toDegrees(t), 
					(int) Math.toDegrees(1.35*dt));
			t += dt;
		}
		
		g.setColor(c.darker());
		g.fillArc(
				(int)(pos.getX() - 0.8*r), 
				(int)(pos.getY() - 0.8*r), 
				(int)(2*0.8*r), 
				(int)(2*0.8*r),
				(int) Math.toDegrees(t0  - 3*dt), 
				(int) Math.toDegrees(fov + 6*dt));
		
		g.setColor(c);
		g.fillOval(
				(int)(pos.getX() - 0.75*r), 
				(int)(pos.getY() - 0.75*r), 
				(int)(2*0.75*r), 
				(int)(2*0.75*r));
	}
	
	public void protozoa(Graphics2D g, Protozoa p)
	{
		Vector2 pos = toRenderSpace(p.getPos());
		double r = toRenderSpace(p.getRadius());
		
		Color c = p.getColor();
		g.setColor(c);
		
		g.fillOval(
				(int)(pos.getX() - r), 
				(int)(pos.getY() - r), 
				(int)(2*r), 
				(int)(2*r));
		
		if (zoom > 1.3)
			retina(g, p);
		
		g.setColor(c.darker());
		Stroke s = g.getStroke();
		g.setStroke(new BasicStroke((int) (0.7*zoom)));
		g.drawOval(
				(int)(pos.getX() - r), 
				(int)(pos.getY() - r), 
				(int)(2*r), 
				(int)(2*r));
		g.setStroke(s);
	}
	
	public void pellet(Graphics2D g, Pellet p)
	{
		Vector2 pos = toRenderSpace(p.getPos());
		double r = toRenderSpace(p.getRadius());
		g.setColor(p.getColor());
		g.fillOval(
				(int)(pos.getX() - r), 
				(int)(pos.getY() - r), 
				(int)(2*r), 
				(int)(2*r));
		
		g.setColor(p.getColor().darker());
		Stroke s = g.getStroke();
		g.setStroke(new BasicStroke((int) (0.7*zoom)));
		g.drawOval(
				(int)(pos.getX() - r), 
				(int)(pos.getY() - r), 
				(int)(2*r), 
				(int)(2*r));
		g.setStroke(s);
	}
	
	public void entities(Graphics2D g, Collection<Entity> entities)
	{
		for (Entity e : entities) {
			if (e instanceof Protozoa)
				protozoa(g, (Protozoa) e);
			else if (e instanceof Pellet)
				pellet(g, (Pellet) e);
		}
	}
	
	public void maskTank(Graphics g, Vector2 coords, double r, int alpha)
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

		if (track != null) {
//			rotate = rotate + (- 0.5*Math.PI - track.getVel().angle() - rotate) * 0.05;
		}
		
		zoom = targetZoom;
		t += 0.001;
		Color backgroundColour = new Color(
				10 + (int)(5 *Math.cos(t)), 
				50 + (int)(30*Math.sin(t)), 
				30 + (int)(15*Math.cos(t + 1)));
		graphics.setColor(backgroundColour);

		graphics.fillRect(0, 0, window.getWidth(), window.getHeight());
		
		entities(graphics, simulation.getTank().getEntities());
		maskTank(graphics, tankRenderCoords, tankRenderRadius, 200);
		maskTank(graphics, toRenderSpace(new Vector2(0, 0)), tankRenderRadius*zoom, 255);
		
		title.render(graphics);
		pelletText.setText("Number of pellets: " + simulation.getTank().numberOfPellets());
		protozoaText.setText("Number of protozoa: " + simulation.getTank().numberOfProtozoa());
		pelletText.render(graphics);
		protozoaText.render(graphics);
		
		graphics.dispose();
		bs.show();
	}
	
	public Vector2 toRenderSpace(Vector2 v)
	{
		Vector2 x;
		if (track == null)
			x = v;
		else {
			x = v.sub(track.getPos());
		}
		return x.rotate(rotate)
				.mul(tankRenderRadius)
				.sub(pan)
				.mul(zoom)
				.add(tankRenderCoords);
	}
	
	public double toRenderSpace(double s)
	{
		return zoom * tankRenderRadius * s;
	}

	public void setZoom(double d) {
		targetZoom = Math.pow(initialZoom + d, Math.log10(2 + initialZoom + d));
		if (targetZoom < 0.5) {
			pan = new Vector2(0, 0);
			targetZoom = 0.5;
		}
		if (targetZoom > 20)
			targetZoom = 20;
	}

	public void pan(Vector2 delta) {
		if (track == null)
			pan = pan.add(delta.mul(1/zoom));
	}
	
	public void track(Entity e) {
		if (e != null)
			pan = new Vector2(0, 0);
		else if (track != null)
			pan = track.getPos().mul(tankRenderRadius);
		track = e;
	}

	public double getZoom() {
		return zoom;
	}
	
}
