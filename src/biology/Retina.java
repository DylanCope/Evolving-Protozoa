package biology;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;


public class Retina implements Iterable<Retina.Cell> 
{
	public class Cell {
		public double angle;
		public Entity entity;
		public Color color = new Color(10, 10, 10);
	}
	
	private Collection<Cell> cells;
	private double fov = Math.toRadians(90);
	
	public Retina() 
	{
		int n = 60;
		cells = new ArrayList<Cell>();
		for (int i = 0; i < n; i++) {
			Cell cell = new Cell();
			cell.angle  = fov * (n - 2*i) / (2*n);
			cells.add(cell);
		}
	}

	@Override
	public Iterator<Cell> iterator() {
		return cells.iterator();
	}
	
}
