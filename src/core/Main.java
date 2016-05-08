package core;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferStrategy;
import java.util.Random;

import utils.KeyInput;
import utils.Renderer;
import utils.Window;
import biology.Brain;
import biology.Pellet;
import biology.Protozoa;

public class Main extends Canvas implements Runnable
{
	
	private static final long serialVersionUID = 4088146271165387233L;
	
	public static int WIDTH, HEIGHT;
	private Thread thread;
	private boolean running = false;
	private KeyInput input;
	private Tank tank;
	public static Color backgroundColour;
	private Renderer renderer;

	public static void main(String args[])
	{
		new Main();
	}
	
	public Main()
	{
		input = new KeyInput();
		this.addKeyListener(input);
		Dimension d = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
		WIDTH = (int) d.getWidth();
		HEIGHT = (int) d.getHeight();
		backgroundColour = Color.BLACK;

		renderer = new Renderer(WIDTH, HEIGHT);
		tank = new Tank();

		int creatures = 20;
		int pellets = 60;
		
//		ArrayList<Entity> entities = new ArrayList<Entity>();
		Random r = new Random();
		
		for (int i = 0; i < creatures; i++) {
			double radius = (r.nextInt(5) + 5) / (double) HEIGHT;
			Protozoa p = new Protozoa(Brain.RANDOM, radius);
//			p.setPos(new Vector2f(
//					r.nextInt((int) tank.getBounds().getX()), 
//					r.nextInt((int) tank.getBounds().getY())
//				));
			tank.addEntity(p);
		}
		for (int i = creatures; i <  creatures + pellets; i++) {
			double radius = (r.nextInt(3) + 2) / (double) HEIGHT;
			tank.addEntity(new Pellet(radius));
		}
		
//		tank.placeRandomly(entities);

		new Window(WIDTH, HEIGHT, "Evolving Protozoa", this);
	}
	
	private void tick(double delta)
	{
		input.update();
		tank.update(delta);
	}
	
	double t = 0;
	private void render()
	{
		BufferStrategy bs = this.getBufferStrategy();
		
		if (bs == null) 
		{
			this.createBufferStrategy(3);
			return;
		}
		
		Graphics2D g = (Graphics2D) bs.getDrawGraphics();
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
//		g.setColor(new Color(140, 230, 110));
//		g.setColor(Color.black);
		t += 0.001;
		backgroundColour = new Color(
				10 + (int)(5*Math.cos(t)), 
				50 + (int)(30*Math.sin(t)), 
				30 + (int)(15*Math.cos(t + 1)));
		g.setColor(backgroundColour);
		g.fillRect(0, 0, WIDTH, HEIGHT);
		
//		tank.render(g);
		
		renderer.tank(g, tank);
		
		g.dispose();
		bs.show();
	}
	
	public synchronized void start()
	{
		thread = new Thread(this);
		thread.start();
		running = true;
	}
	
	public synchronized void stop()
	{
		try
		{
			thread.join();
			running = false;
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public void run()
	{
		this.requestFocus();
		
		long lastTime = System.nanoTime();
		double amountOfTicks = 60.0;
		double ns = 1000000000 / amountOfTicks;
		double delta = 0;
		long timer = System.currentTimeMillis();
		
		while (running)
		{
			long now = System.nanoTime();
			delta += (now - lastTime) / ns;
			lastTime = now;
			while (delta >= 1) {
				tick(delta / 100.0);
				delta--;
			}
			if(running)
				render();
			
			if (System.currentTimeMillis() - timer > 1000) {
				timer += 1000;
			}
		}
		stop();
	}
}
