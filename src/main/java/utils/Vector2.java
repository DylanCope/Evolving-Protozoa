package utils;

import java.io.Serializable;

public class Vector2 implements Serializable
{
	private static final long serialVersionUID = 8642244552320036511L;
	private double x;
	private double y;
	
	public Vector2(double x, double y)
	{
		this.setX(x);
		this.setY(y);
	}
	
	public double len2()
	{
		return x*x + y*y;
	}
	
	public double len(){
		return Math.sqrt(x*x + y*y);
	}

	public Vector2 copy() {
		return new Vector2(x, y);
	}

	public Vector2 translate(Vector2 dv) {
		x += dv.getX();
		y += dv.getY();
		return this;
	}

	public Vector2 scale(double s) {
		x *= s;
		y *= s;
		return this;
	}
	
	public Vector2 add(Vector2 b){
		return new Vector2(getX() + b.getX(), getY() + b.getY());
	}
	
	public Vector2 sub(Vector2 b){
		return new Vector2(getX() - b.getX(), getY() - b.getY());
	}
	
	public Vector2 mul(double s) {
		return new Vector2(s*getX(), s*getY());
	}
	
	public double dot(Vector2 b) {
		return ( getX()*b.getX() + getY()*b.getY() );
	}
	
	public Vector2 perp() {
		return new Vector2(-getY(), getX());
	}
	
	public Vector2 rotate(double angle)
	{
		double c = Math.cos(angle);
		double s = Math.sin(angle);
		return new Vector2(x*c - y*s, x*s + y*c);
	}
	
	public Vector2 unit()
	{
		double len = len();
		if (len == 0)
			return new Vector2(0, 0);
		return new Vector2(getX() / len, getY() / len);
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

	public Vector2 setLength(double targetLen)
	{
		double currentLen = len();
		x *= targetLen / currentLen;
		y *= targetLen / currentLen;
		return this;
	}
	
	public Vector2 setDir(Vector2 other)
	{
		return other.setLength(len());
	}
	
	public String toString()
	{
		return "(" + x + ", " + y + ")";
	}

	public double angleBetween(Vector2 other)
	{
		double a = len2(), b = other.len2();
		return Math.acos(dot(other) / Math.sqrt(a*b));
	}

	public boolean equals(Object o) {
		if (o instanceof Vector2) {
			Vector2 v = (Vector2) o;
			return v.getX() == getX() && v.getY() == getY();
		}
		return false;
	}
}
