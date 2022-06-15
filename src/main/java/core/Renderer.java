package core;

import java.awt.*;
import java.awt.image.BufferStrategy;
import java.util.*;

import utils.Utils;
import utils.Vector2;
import utils.Window;
import biology.Entity;
import biology.Pellet;
import biology.Protozoa;
import biology.Retina;

public class Renderer extends Canvas
{	
	private static final long serialVersionUID = 1L;
	
	float time = 0;
	private final Vector2 tankRenderCoords;
	private final float tankRenderRadius;
	private Vector2 pan, panPosTemp;
	private float zoom;
	private float targetZoom;
	private final float initialZoom = 1;
	private double zoomRange = 5, zoomSlowness = 8;
	private boolean superSimpleRender = false, renderChemicals = false;
	private final float rotate = 0;
	private double lastRenderTime = 0;
	private Entity track;
	private final UI ui;
	private boolean antiAliasing = Settings.antiAliasing;

	private final HashMap<String, Integer> stats = new HashMap<>(5, 1);
	private final Simulation simulation;
	private final Window window;

	public Renderer(Simulation simulation, Window window)
	{
		this.simulation = simulation;
		this.window = window;
		window.getInput().onLeftMouseRelease = this::updatePanTemp;

		stats.put("FPS", 0);
		stats.put("Chunks Rendered", 0);
		stats.put("Protozoa Rendered", 0);
		stats.put("Pellets Rendered", 0);
		stats.put("Zoom", 0);
		
		tankRenderRadius = window.getHeight() / 2.0f;
		tankRenderCoords = new Vector2(window.getWidth()*0.5f, window.getHeight()*0.5f);
		pan = new Vector2(0, 0);
		panPosTemp = pan;

		zoom = 1f;
		targetZoom = zoom;
		zoomRange *= simulation.getTank().getRadius();

		
		ui = new UI(window, simulation, this);
		
		requestFocus();
		setFocusable(true);
		lastRenderTime = Utils.getTimeSeconds();
	}
	
	public void retina(Graphics2D g, Protozoa p)
	{
		Vector2 pos = toRenderSpace(p.getPos());
		float r = toRenderSpace(p.getRadius());
		
		Color c = p.getColor();

		float dt 	= p.getRetina().getCellAngle();
		float fov 	= p.getRetina().getFov();
		float t0 	= -p.getDir().angle() - 0.5f*fov - rotate;
		float t 	= t0;
		
		g.setColor(c.darker());
		g.fillArc(
				(int)(pos.getX() - r), 
				(int)(pos.getY() - r), 
				(int)(2*r), 
				(int)(2*r),
				(int) Math.toDegrees(t0  - 2.8*dt), 
				(int) Math.toDegrees(fov + 5.6*dt));

		if (stats.get("FPS") >= 0)
			for (Retina.Cell cell : p.getRetina())
			{
				if (cell.anythingVisible()) {
					Color col = cell.getColour();
					g.setColor(col);
				} else {
					g.setColor(Color.WHITE);
				}
				g.fillArc(
						(int)(pos.getX() - r),
						(int)(pos.getY() - r),
						(int)(2*r),
						(int)(2*r),
						(int) Math.toDegrees(t - 0.01),
						(int) Math.toDegrees(dt + 0.01));
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

		if (p == track && simulation.inDebugMode()) {
			g.setColor(Color.YELLOW.darker());
			float dirAngle = p.getDir().angle();
			for (Retina.Cell cell : p.getRetina().getCells()) {
				for (Vector2 ray : cell.getRays()) {
					Vector2 rayRotated = ray.rotate(dirAngle);
					Vector2 rayDir = rayRotated.unit().scale(toRenderSpace(Settings.protozoaInteractRange));
					Vector2 rayStart = pos.add(rayRotated.unit().scale(r));
					Vector2 rayEnd = pos.add(rayDir);
					g.drawLine(
							(int) rayStart.getX(), (int) rayStart.getY(),
							(int) rayEnd.getX(), (int) rayEnd.getY());
				}
			}
		}
	}
	
	public void protozoa(Graphics2D g, Protozoa p)
	{
		Vector2 pos = toRenderSpace(p.getPos());
		float r = toRenderSpace(p.getRadius());

		if (circleNotVisible(pos, r))
			return;
		if (!p.wasJustDamaged) {
			drawOutlinedCircle(g, pos, r, p.getColor());
		} else {
			drawOutlinedCircle(g, pos, r, p.getColor(), Color.RED);
		}

		for (Protozoa.Spike spike : p.getSpikes()) {
			if (r > 0.001 * window.getHeight()) {
				Stroke s = g.getStroke();
				g.setColor(p.getColor().darker().darker());
				g.setStroke(new BasicStroke((int) (r * 0.2)));
				Vector2 spikeStartPos = p.getDir().unit().rotate(spike.angle).setLength(r).translate(pos);
				float spikeLen = toRenderSpace(p.getSpikeLength(spike));
				Vector2 spikeEndPos = spikeStartPos.add(spikeStartPos.sub(pos).setLength(spikeLen));
				g.drawLine((int) (spikeStartPos.getX()), (int) (spikeStartPos.getY()),
						(int) (spikeEndPos.getX()), (int) (spikeEndPos.getY()));
				g.setStroke(s);
			}
		}

		stats.put("Protozoa Rendered", stats.get("Protozoa Rendered") + 1);

		if (r >= 0.005 * window.getHeight() && p.getRetina().numberOfCells() > 0)
			retina(g, p);

		if (stats.get("FPS") > 10 && r >= 0.01 * window.getHeight()) {
			Polygon nucleus = new Polygon();
			float dt = (float) (2 * Math.PI / (16.0));
			float t0 = p.getVel().angle();
			for (float t = 0; t < 2 * Math.PI; t += dt) {
				float percent = (float) (((t * t * p.id) % (1 / 7.0)) + (2 / 5.0));
				float radius = toRenderSpace(percent * p.getRadius());
				int x = (int) (radius * (0.1 + Math.cos(t + t0)) + pos.getX());
				int y = (int) (radius * (-0.1 + Math.sin(t + t0)) + pos.getY());
				nucleus.addPoint(x, y);
			}
			Color b = p.getColor().brighter();
			g.setColor(new Color(b.getRed(), b.getGreen(), b.getBlue(), 50));
			g.fillPolygon(nucleus);
		}
	}

	public boolean circleNotVisible(Vector2 pos, float r) {
		return (pos.getX() - r > window.getWidth())
			 ||(pos.getX() + r < 0)
			 ||(pos.getY() - r > window.getHeight())
			 ||(pos.getY() + r < 0);
	}

	public boolean pointNotVisible(Vector2 pos) {
		return circleNotVisible(pos, 0);
	}

	public void drawCircle(Graphics2D g, Vector2 pos, float r, Color c) {
		drawCircle(g, pos, r, c, (int) (0.2*r));
	}

	public void drawCircle(Graphics2D g, Vector2 pos, float r, Color c, int strokeSize) {
		g.setColor(c);
		Stroke s = g.getStroke();
		g.setStroke(new BasicStroke(strokeSize));
		g.drawOval(
				(int)(pos.getX() - r),
				(int)(pos.getY() - r),
				(int)(2*r),
				(int)(2*r));
		g.setStroke(s);
	}

	public void drawOutlinedCircle(Graphics2D g, Vector2 pos, float r, Color c, Color outline) {

		g.setColor(c);
		g.fillOval(
				(int)(pos.getX() - r),
				(int)(pos.getY() - r),
				(int)(2*r),
				(int)(2*r));

		if (r >= 0.01 * window.getHeight() && !superSimpleRender)
			drawCircle(g, pos, r, outline);
	}

	public void drawOutlinedCircle(Graphics2D g, Vector2 pos, float r, Color c) {
		drawOutlinedCircle(g, pos, r, c, c.darker());
	}
	
	public void pellet(Graphics2D g, Pellet p)
	{
		Vector2 pos = toRenderSpace(p.getPos());
		float r = toRenderSpace(p.getRadius());
		drawOutlinedCircle(g, pos, r, p.getColor());
		stats.put("Pellets Rendered", stats.get("Pellets Rendered") + 1);
	}

	public void renderEntity(Graphics2D g, Entity e) {
		if (e instanceof Protozoa)
			protozoa(g, (Protozoa) e);
		else if (e instanceof Pellet)
			pellet(g, (Pellet) e);
	}

	public boolean pointOnScreen(int x, int y) {
		return 0 <= x && x <= window.getWidth() &&
				0 <= y && y <= window.getHeight();
	}

	public boolean chunkInView(Chunk chunk) {
		Vector2 chunkCoords = toRenderSpace(chunk.getTankCoords());
		int originX = (int) chunkCoords.getX();
		int originY = (int) chunkCoords.getY();
		int chunkSize = toRenderSpace(simulation.getTank().getChunkManager().getChunkSize());
		return pointOnScreen(originX, originY) ||
				pointOnScreen(originX + chunkSize, originY) ||
				pointOnScreen(originX, originY + chunkSize) ||
				pointOnScreen(originX + chunkSize, originY + chunkSize);
	}

	public void renderChunk(Graphics2D g, Chunk chunk) {
		if (chunkInView(chunk)) {
			stats.put("Chunks Rendered", stats.get("Chunks Rendered") + 1);
			for (Entity e : chunk.getEntities())
				renderEntity(g, e);
		}
	}
	
	public void entities(Graphics2D g, Tank tank)
	{
		for (Chunk chunk : tank.getChunkManager().getChunks())
			renderChunk(g, chunk);

		if (simulation.inDebugMode() && track != null) {
			ChunkManager chunkManager = tank.getChunkManager();
			Iterator<Collidable> collisionEntities = chunkManager.broadCollisionDetection(
					track.getPos(), track.getRadius());
			collisionEntities.forEachRemaining(
					o -> drawCollisionBounds(g, o, Color.RED.darker())
			);

			if (track instanceof Protozoa) {
				drawCollisionBounds(g, track, Settings.protozoaInteractRange, Color.WHITE.darker());
				Iterator<Entity> interactEntities = chunkManager.broadEntityDetection(
						track.getPos(), Settings.protozoaInteractRange);
				interactEntities.forEachRemaining(
						e -> drawCollisionBounds(g, e, 1.1f * e.getRadius(), Color.WHITE.darker())
				);
			}
		}
	}

	public void rocks(Graphics2D g, Tank tank) {
		for (Rock rock : tank.getRocks()) {
			Vector2[] screenPoints = new Vector2[]{
					toRenderSpace(rock.getPoints()[0]),
					toRenderSpace(rock.getPoints()[1]),
					toRenderSpace(rock.getPoints()[2])
			};

			int[] xPoints = new int[screenPoints.length];
			for (int i = 0; i < screenPoints.length; i++)
				xPoints[i] = (int) screenPoints[i].getX();

			int[] yPoints = new int[screenPoints.length];
			for (int i = 0; i < screenPoints.length; i++)
				yPoints[i] = (int) screenPoints[i].getY();

			Color color = new Color(
					rock.getColor().getRed(),
					rock.getColor().getGreen(),
					rock.getColor().getBlue(),
					simulation.inDebugMode() ? 100 : 255
			);
			g.setColor(color);
			g.fillPolygon(xPoints, yPoints, screenPoints.length);
			g.setColor(color.darker());
			g.drawPolygon(xPoints, yPoints, screenPoints.length);

			if (simulation.inDebugMode()) {
				g.setColor(Color.YELLOW.darker());
				for (int i = 0; i < 3; i++) {
					Vector2[] edge = rock.getEdge(i);
					Vector2 edgeCentre = edge[0].add(edge[1]).scale(0.5f);
					Vector2 normalStart = edgeCentre.sub(rock.getNormals()[i].mul(0.01f));
					Vector2 normalEnd = edgeCentre.add(rock.getNormals()[i].mul(0.01f));
					Vector2 a = toRenderSpace(normalStart);
					Vector2 b = toRenderSpace(normalEnd);
					g.drawLine((int) a.getX(), (int) a.getY(), (int) b.getX(), (int) b.getY());
				}
			}
		}
	}

	public void drawCollisionBounds(Graphics2D g, Collidable collidable, Color color) {
		if (collidable instanceof Entity) {
			Entity e = (Entity) collidable;
			drawCollisionBounds(g, e, e.getRadius(), color);
		} else if (collidable instanceof Rock) {
			drawCollisionBounds(g, (Rock) collidable, color);
		}
	}

	public void drawCollisionBounds(Graphics2D g, Rock rock, Color color) {

		Vector2[] screenPoints = new Vector2[]{
			toRenderSpace(rock.getPoints()[0]),
			toRenderSpace(rock.getPoints()[1]),
			toRenderSpace(rock.getPoints()[2])
		};

		int[] xPoints = new int[screenPoints.length];
		for (int i = 0; i < screenPoints.length; i++)
			xPoints[i] = (int) screenPoints[i].getX();

		int[] yPoints = new int[screenPoints.length];
		for (int i = 0; i < screenPoints.length; i++)
			yPoints[i] = (int) screenPoints[i].getY();

		int strokeSize = 5;
		Stroke s = g.getStroke();
		g.setStroke(new BasicStroke(strokeSize));
		g.setColor(color);
		for (int i = 0; i < rock.getEdges().length; i++) {
			if (rock.getEdgeAttachedState(i))
				continue;
			Vector2[] edge = rock.getEdge(i);
			Vector2 start = toRenderSpace(edge[0]);
			Vector2 end = toRenderSpace(edge[1]);
			g.drawLine((int) start.getX(), (int) start.getY(),
					(int) end.getX(), (int) end.getY());
		}
		g.setStroke(s);
	}

	public void drawCollisionBounds(Graphics2D g, Entity e, float r, Color color) {
		Vector2 pos = toRenderSpace(e.getPos());
		r = toRenderSpace(r);
		if (!circleNotVisible(pos, r))
			drawCircle(g, pos, r, color, window.getHeight() / 500);
	}
	
	public void maskTank(Graphics g, Vector2 coords, float r, int alpha)
	{
		int nPoints = 500;
		int xPoints[] = new int[nPoints];
		int yPoints[] = new int[nPoints];
		
		int n = nPoints - 7;
		for (int i = 0; i < n; i++) 
		{
			float t = (float) (2*Math.PI * i / (float) n);
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
	
	public void background(Graphics2D graphics)
	{
		time += 0.1;
		Color backgroundColour = new Color(
				30 + (int)(5 *Math.cos(time/100.0)),
				50 + (int)(30*Math.sin(time/100.0)),
				45 + (int)(15*Math.cos(time/100.0 + 1)));
		graphics.setColor(backgroundColour);

		graphics.fillRect(0, 0, window.getWidth(), window.getHeight());

		if (renderChemicals) {
			ChemicalSolution chemicalSolution = simulation.getTank().getChemicalSolution();
			int w = toRenderSpace(chemicalSolution.getGridSize());
			for (int i = 0; i < chemicalSolution.getNXChunks(); i++) {
				for (int j = 0; j < chemicalSolution.getNYChunks(); j++) {
					Vector2 chunkCoords = toRenderSpace(chemicalSolution.toTankCoords(i, j));

					graphics.setColor(new Color(
							80, 200, 60,
							(int) (120 * chemicalSolution.getPlantPheromoneDenisty(i, j))
					));
					graphics.fillRect((int) chunkCoords.getX(), (int) chunkCoords.getY(), w, w);
				}
			}
		}

		if (simulation.inDebugMode()) {
			graphics.setColor(Color.YELLOW.darker());
			ChunkManager chunkManager = simulation.getTank().getChunkManager();
			int w = toRenderSpace(chunkManager.getChunkSize());
			for (Chunk chunk : chunkManager.getChunks()) {
				Vector2 chunkCoords = toRenderSpace(chunk.getTankCoords());
				graphics.drawRect((int) chunkCoords.getX(), (int) chunkCoords.getY(), w, w);
			}
		}
	}
	
	public void render()
	{
		stats.replaceAll((s, v) -> 0);
		stats.put("FPS", (int) (1 / (Utils.getTimeSeconds() - lastRenderTime)));
		superSimpleRender = stats.get("FPS") <= 5;
		lastRenderTime = Utils.getTimeSeconds();

		BufferStrategy bs = this.getBufferStrategy();
		
		if (bs == null) {
			this.createBufferStrategy(3);
			return;
		}
		
		Graphics2D graphics = (Graphics2D) bs.getDrawGraphics();
//		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
//		GraphicsConfiguration gc = ge.getDefaultScreenDevice().getDefaultConfiguration();
//		Graphics2D graphics = gc.createCompatibleVolatileImage(window.getWidth(), window.getHeight()).createGraphics();

		if (antiAliasing)
			graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		else
			graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);

//		graphics.setRenderingHint(
//				RenderingHints.KEY_COLOR_RENDERING,
//				RenderingHints.VALUE_COLOR_RENDER_SPEED);


		zoom = targetZoom;
		stats.put("Zoom", (int) (100 * zoom));
		synchronized (simulation.getTank()) {
			try {
				background(graphics);
				entities(graphics, simulation.getTank());
				rocks(graphics, simulation.getTank());
				maskTank(graphics,
						tankRenderCoords,
						getTracked() != null ? getTrackingScopeRadius() : tankRenderRadius,
						simulation.inDebugMode() ? 150 : 200);

				maskTank(graphics,
						toRenderSpace(new Vector2(0, 0)),
						tankRenderRadius * zoom,
						simulation.inDebugMode() ? 100 : 255);

				ui.render(graphics);

				graphics.dispose();
				bs.show();

			} catch (ConcurrentModificationException ignored) {}
		}
	}

	public float getTrackingScopeRadius() {
		return 3*tankRenderRadius/4;
	}
	
	public Vector2 toRenderSpace(Vector2 v)
	{
		if (track == null)
			return v.rotate(rotate)
					.mul(1 / simulation.getTank().getRadius())
					.add(pan.mul(1 / tankRenderRadius))
					.mul(tankRenderRadius * zoom)
					.add(tankRenderCoords);
		else {
			return v.sub(track.getPos())
					.rotate(rotate)
					.mul(tankRenderRadius * zoom / simulation.getTank().getRadius())
					.add(tankRenderCoords);
		}
	}
	
	public int toRenderSpace(float s)
	{
		return (int) (zoom * tankRenderRadius * s / simulation.getTank().getRadius());
	}

	public void setZoom(float d) {
		targetZoom = (float) (initialZoom + zoomRange * Math.tanh((d - 1f) / zoomSlowness));
		if (targetZoom < 0.5) {
			pan = new Vector2(0, 0);
			targetZoom = 0.5f;
		}
		if (targetZoom > 20)
			targetZoom = 20;
	}

	public void setPan(Vector2 delta) {
		pan = panPosTemp.add(delta);
	}

	public void updatePanTemp() {
		panPosTemp = pan;
	}
	
	public void track(Entity e) {
		if (e != null)
			pan = new Vector2(0, 0);
		else if (track != null)
			pan = track.getPos().mul(tankRenderRadius);
		track = e;
	}

	public HashMap<String, Integer> getStats() {
		return stats;
	}

	public float getZoom() {
		return zoom;
	}
	
	public Entity getTracked() {
		return track;
	}

	public void resetCamera() {
		track = null;
		pan = new Vector2(0, 0);
		targetZoom = 1;
		zoom = 1;
	}

	public void toggleChemicalGrid() {
		renderChemicals = !renderChemicals;
	}

	public void toggleAA() {
		antiAliasing = !antiAliasing;
	}
}
