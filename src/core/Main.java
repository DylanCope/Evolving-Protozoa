package core;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferStrategy;

import utils.KeyInput;
import utils.Vector2f;
import utils.Window;

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
		tank.placeRandomly(null);
	}
	
	private void tick(double delta)
	{
		input.update();
		tank.update(delta);
	}
	
	private void render()
	{
		BufferStrategy bs = this.getBufferStrategy();
		
		if (bs == null) 
		{
			this.createBufferStrategy(3);
			return;
		}
		
		Graphics g = bs.getDrawGraphics();
		
		g.setColor(new Color(140, 230, 110));
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
