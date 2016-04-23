package com.main;

import java.awt.Graphics;
import java.util.ArrayList;
import java.util.Random;
import java.util.function.Predicate;

public class Tank {

	int creatures = 5, pellets = 30;
	Creature[] c = new Creature[creatures];
	Random r = new Random();
	Pellet[] p = new Pellet[pellets];
	ArrayList<Entity> entities = new ArrayList<Entity>();
	int w = 1366, h = 768;
	
	public Tank(){
		entities = new ArrayList<Entity>();
		for(int i = 0; i < creatures; i++){
			entities.add(new Creature(r.nextInt(w), r.nextInt(h), 25));
		}
		for(int i = creatures; i <  creatures + pellets; i++){
			entities.add(new Pellet(r.nextInt(w), r.nextInt(h), 5));
		}
	}
	
	public void update(){
		for(Entity e : entities) {
			e.update(entities);
			if (e.pos.x - e.radius > w) e.pos.x = -e.radius;
			if (e.pos.x + e.radius < 0) e.pos.x = w + e.radius;
			if (e.pos.y - e.radius > h) e.pos.y = -e.radius;
			if (e.pos.y + e.radius < 0) e.pos.y = h + e.radius;
		}
		entities.removeIf(new Predicate<Entity>() {

			public boolean test(Entity arg0) {
				return arg0.dead;
			}
			
		});
	}
	
//	Math.abs(c[i].pos.y - x * Math.tan()) < c[i].radius;
	
	public void render(Graphics g){
//		for(int i = 0; i < p.length; i++){
//			p[i].render(g);
//		}
//		for(int i = 0; i < c.length; i++){
//			c[i].render(g);
//		}
		for (Entity e : entities)
			e.render(g);
	}
}
