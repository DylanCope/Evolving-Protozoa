package utils;

public class Vector2f {

	private double x;
	private double y;
	
	public Vector2f(double x, double y){
		this.setX(x);
		this.setY(y);
	}
	
	public double x(){
		return getX();
	}
	
	public double y(){
		return getY();
	}
	
	public double length(){
		return Math.sqrt(getX()*getX() + getY()*getY());
	}
	
	public Vector2f add(Vector2f b){
		return new Vector2f(getX() + b.getX(), getY() + b.getY());
	}
	
	public Vector2f sub(Vector2f b){
		return new Vector2f(getX() - b.getX(), getY() - b.getY());
	}
	
	public Vector2f mul(double s) {
		return new Vector2f(s*getX(), s*getY());
	}
	
	public double dot(Vector2f b) {
		return ( getX()*b.getX() + getY()*b.getY() );
	}
	
	public Vector2f perp() {
		return new Vector2f(-getY(), getX());
	}
	
	public Vector2f unit() {
		double len = length();
		return new Vector2f(getX() / len, getY() / len);
	}
	
	public double angle() {
		return Math.atan2(getY(), getX());
	}

	public double getX() {
		return x;
	}

	public void setX(double x) {
		this.x = x;
	}

	public double getY() {
		return y;
	}

	public void setY(double y) {
		this.y = y;
	}
}
