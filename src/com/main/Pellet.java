package com.main;

import java.awt.Color;
import java.util.ArrayList;

public class Pellet extends Entity {
	
	public Pellet(int x, int y, int radius){
		super();
		this.radius = radius;
		pos = new Vector2f(x, y);
		vel = new Vector2f(0, 0);
		color = new Color(150 + r.nextInt(105), 10+r.nextInt(100), 10+r.nextInt(100));
		nutrition = 0.25;
		maxVel = 50;
		nextVelocity();
		time = System.currentTimeMillis()/1000.0;
	}

	@Override
	public void update(ArrayList<Entity> entities) {
		dt = (System.currentTimeMillis()/1000.0)-time;
		
		pos.x += vel.x*dt;
		pos.y += vel.y*dt;
		
		time = System.currentTimeMillis()/1000.0;
	}
	
}
