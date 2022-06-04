package biology;

import java.awt.Color;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;


public class Retina implements Iterable<Retina.Cell>, Serializable
{
	private static final long serialVersionUID = 5214857174841633362L;

	public class Cell implements Serializable {
		private static final long serialVersionUID = 1L;
		public float angle;
		public Entity entity;
		public Color colour = new Color(10, 10, 10);
	}
	
	private Collection<Cell> cells;
	private float fov = (float) Math.toRadians(90);

	public Retina()
	{
		this(60);
	}

	public Retina(int numCells)
	{
		int n = numCells;
		cells = new ArrayList<>();
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

	public float getCellAngle() {
		return fov / (float) cells.size();
	}
	
	public float getFov() {
		return fov;
	}

	public int numberOfCells() { return cells.size(); }
	
}
