package com.main;

import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.Random;

public class Creature extends Entity {
	
	public class RetinaCell {
		double dist;
		Color color = new Color(10, 10, 10);
		double t;
	}
	
	double time;
	double dt;
	double thinkTime = 0;
	double maxThinkTime;
	double health = 1;
	Random r = new Random();
	int maxVel = 300;
	
	double fov = Math.toRadians(90);
	int retinaSize = 61;
	RetinaCell retina[] = new RetinaCell[retinaSize];
	
	public void see(Entity e){
		Vector2f r = e.pos;
		for (int i = 0; i < retina.length; i++){
			double rx = pos.sub(r).dot(vel.unit());
			double ry = pos.sub(r).dot(vel.perp().unit());
			double y = rx*Math.tan(retina[i].t);
			if (Math.abs(y - ry) <= e.radius && rx < 0) {
				retina[i].dist = (rx*rx + ry*ry);
				retina[i].color = e.color;
			}
		}
	}
	
	public void eat(Entity e) {
		health += e.nutrition;
		if (health > 1) health = 1;
		e.dead = true;
	}
	
	public Creature(double x, double y, int radius){
		color = new Color(50, 50, 80);
		pos = new Vector2f(x, y);
		vel = new Vector2f(0, 0);
		vel.x = r.nextInt(maxVel)-maxVel/2;
		vel.y = r.nextInt(maxVel)-maxVel/2;
		this.radius = radius;
		time = System.currentTimeMillis()/1000.0;
		maxThinkTime = (r.nextInt(10)/10.0)+1;
		for(int i = 0; i < retina.length; i++){
			retina[i] = new RetinaCell();
			retina[i].t  = fov * (retinaSize - 2*i) / (2*retinaSize);
		}
	}
	
	@Override
	public void update(ArrayList<Entity> entities){
		dt = (System.currentTimeMillis()/1000.0)-time;
		
		thinkTime += dt;
		if(thinkTime >= maxThinkTime){
			thinkTime = 0;
			nextVelocity();
			health *= 0.95;
			dead = health < 0.1;
		}
		
		pos.x += vel.x*dt;
		pos.y += vel.y*dt;
		
		time = System.currentTimeMillis()/1000.0;
		
		for (Entity e : entities) {
			if (e instanceof Pellet) {
				double dist = pos.sub(e.pos).length();
				if (dist <= radius + e.radius)
					eat((Pellet) e);
			}
			if (!e.equals(this))
				see(e);
		}
	}
	
	public void nextVelocity() {
		vel.x = r.nextInt(maxVel)-maxVel/2;
		vel.y = r.nextInt(maxVel)-maxVel/2;	
	}
	
	public void render(Graphics g){
		Color c = new Color(color.getRed(), color.getGreen(), color.getBlue(), (int) (255 * health));
		g.setColor(c);
		g.fillOval((int)(pos.x()-radius), (int)(pos.y()-radius), 2*radius, 2*radius);
		
		double r0 = 1;
		double r1 = 0.8;
		for (int i = 0; i < retina.length; i++){
			double x = Math.cos(retina[i].t + vel.angle());
			double y = Math.sin(retina[i].t + vel.angle());
			double len = Math.sqrt(x*x + y*y);
			double r2 = r1 + 0.5*(1 - r1)*(1 + Math.cos(2*Math.PI*retina[i].t));
			g.setColor(retina[i].color);
			g.drawLine(
					(int)(pos.x + (x*radius*r0)/len), 
					(int)(pos.y + (y*radius*r0)/len), 
					(int)(pos.x + (x*radius*r2)/len),
					(int)(pos.y + (y*radius*r2)/len)
					);
		}
//		g.setColor(new Color(255, 255, 255));
//		g.drawLine((int)pos.x(), (int)pos.y(), 
//				(int)(pos.x + (vel.x()*radius*1.5)/vel.length()),
//				(int)(pos.y + (vel.y()*radius*1.5)/vel.length())
//				);
		
		for (RetinaCell cell : retina)
			cell.color = Color.WHITE;
	}

}
