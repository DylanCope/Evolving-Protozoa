package com.main;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferStrategy;
import java.util.Random;

public class Main extends Canvas implements Runnable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 4088146271165387233L;
	
	public static final int WIDTH = 2000, HEIGHT = WIDTH / 12 * 9;
	private Thread thread;
	private boolean running = false;
	private Random r;
	private KeyInput key;
	private Tank tank;
	
	public Main(){
		key = new KeyInput();
		this.addKeyListener(key);
		
		new Window(WIDTH, HEIGHT, "JavaGame1", this);
		
		tank = new Tank();
	}
	
	private void tick(){
		key.update();
		tank.update();
	}
	
	private void render(){
		BufferStrategy bs = this.getBufferStrategy();
		if(bs == null){
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
	
	public synchronized void start(){
		thread = new Thread(this);
		thread.start();
		running = true;
	}
	
	public synchronized void stop(){
		try{
			thread.join();
			running = false;
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public void run(){
this.requestFocus();
		
		long lastTime = System.nanoTime();
		double amountOfTicks = 120.0;
		double ns = 1000000000 / amountOfTicks;
		double delta = 0;
		long timer = System.currentTimeMillis();
		int frames = 0;
		
		while(running){
			long now = System.nanoTime();
			delta += (now - lastTime) / ns;
			lastTime = now;
			while(delta >= 1){
				tick();
				delta--;
				frames++;
			}
			if(running)
				render();
			
			if(System.currentTimeMillis() - timer > 1000){
				timer += 1000;
//				System.out.println("FPS: " + frames);
				frames = 0;
			}
		}
		stop();
	}
	
	public static void main(String args[]){
		new Main();
	}
}
