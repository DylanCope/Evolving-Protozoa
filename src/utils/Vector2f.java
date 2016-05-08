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
	
	public double len2()
	{
		return x*x + y*y;
	}
	
	public double length(){
		return Math.sqrt(x*x + y*y);
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
	
	public Vector2f rotate(double angle) {
		double c = Math.cos(angle);
		double s = Math.sin(angle);
		return new Vector2f(x*c - y*s, x*s + y*c);
	}
	
	public Vector2f unit() {
		double len = length();
		if (len == 0)
			return new Vector2f(0, 0);
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

	public Vector2f setLength(double len) {
		Vector2f u = unit();
		return u.mul(len);
	}
	
	public Vector2f setDir(Vector2f other)
	{
		return other.setLength(length());
	}
	
	public String toString()
	{
		return "(" + x + ", " + y + ")";
	}
}
