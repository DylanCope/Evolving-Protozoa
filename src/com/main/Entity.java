package com.main;

import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.Random;

public abstract class Entity {

	Vector2f pos, vel;
	int radius;
	Color color;
	Random r = new Random();
	
	double time;
	double dt;
	double thinkTime = 0;
	double maxThinkTime;
	double health = 1;
	int maxVel = 100;
	
	boolean dead = false;
	double nutrition;
	
	public abstract void update(ArrayList<Entity> entities);
	
	public void render(Graphics g){
		g.setColor(color);
		g.fillOval((int)(pos.x - radius), (int)(pos.y - radius), radius*2, radius*2);
	}
	
	public void nextVelocity() {
		vel.x = r.nextInt(maxVel)-maxVel/2;
		vel.y = r.nextInt(maxVel)-maxVel/2;	
	}
}
