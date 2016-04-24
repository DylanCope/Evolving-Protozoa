package core;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferStrategy;
import java.util.ArrayList;
import java.util.Random;

import utils.KeyInput;
import utils.Vector2f;
import utils.Window;
import biology.Brain;
import biology.Entity;
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

		new Window(WIDTH, HEIGHT, "Evolving Protozoa", this);
		
		tank = new Tank(new Vector2f(WIDTH, HEIGHT));

		int creatures = 20;
		int pellets = 50;
		
		ArrayList<Entity> entities = new ArrayList<Entity>();
		Random r = new Random();
		for(int i = 0; i < creatures; i++){
			Protozoa p = new Protozoa(Brain.RANDOM, 15);
			p.setPos(new Vector2f(r.nextInt(WIDTH), r.nextInt(HEIGHT)));
			entities.add(p);
		}
		for(int i = creatures; i <  creatures + pellets; i++){
			entities.add(new Pellet(r.nextInt(WIDTH), r.nextInt(HEIGHT), 5));
		}
		tank.placeRandomly(entities);
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
		
		Graphics g = bs.getDrawGraphics();
		
//		g.setColor(new Color(140, 230, 110));
//		g.setColor(Color.black);
		t += 0.001;
		g.setColor(new Color(
				10 + (int)(5*Math.cos(t)), 
				50 + (int)(30*Math.sin(t)), 
				30 + (int)(15*Math.cos(t + 1))));
		g.fillRect(0, 0, WIDTH, HEIGHT);
		
		tank.render(g);
		
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
		double amountOfTicks = 120.0;
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
