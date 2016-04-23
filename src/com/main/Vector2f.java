package com.main;

public class Vector2f {

	double x, y;
	
	public Vector2f(double x, double y){
		this.x = x;
		this.y = y;
	}
	
	public double x(){
		return x;
	}
	
	public double y(){
		return y;
	}
	
	public double length(){
		return Math.sqrt(x*x + y*y);
	}
	
	public Vector2f add(Vector2f b){
		return new Vector2f(x + b.x, y + b.y);
	}
	
	public Vector2f sub(Vector2f b){
		return new Vector2f(x - b.x, y - b.y);
	}
	
	public double dot(Vector2f b){
		return ( x*b.x + y*b.y );
	}
	
	public Vector2f perp(){
		return new Vector2f(-y, x);
	}
	
	public Vector2f unit() {
		double len = length();
		return new Vector2f(x / len, y / len);
	}
	
	public double angle() {
		return Math.atan2(y, x);
	}
}
