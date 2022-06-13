package biology;

import com.google.common.collect.Iterators;
import core.Settings;

import java.awt.Color;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Iterator;


public class Retina implements Iterable<Retina.Cell>, Serializable
{
	private static final long serialVersionUID = 5214857174841633362L;

	public static class Cell implements Serializable {
		private static final long serialVersionUID = 1L;
		public float angle;
		public Entity[] entities = new Entity[Settings.maxRetinaCellEntities];
		public float[] weights = new float[Settings.maxRetinaCellEntities];
		public Color colour = new Color(10, 10, 10);
		public int nEntities = 0;

		public void set(int idx, Entity e, float w) {
			entities[idx] = e;
			weights[idx] = w;
		}
	}
	
	private final Cell[] cells;
	private final float fov;

	public Retina(int numCells) {
		this(numCells, (float) Math.toRadians(90));
	}

	public Retina(int numCells, float fov)
	{
		cells = new Cell[numCells];
		this.fov = fov;
		for (int i = 0; i < numCells; i++) {
			Cell cell = new Cell();
			cell.angle = fov * (numCells - 2*i) / (2* numCells);
			cells[i] = cell;
		}
	}

	public void reset() {
		for (Retina.Cell cell : cells) {
			cell.colour = Color.WHITE;
			Arrays.fill(cell.entities, null);
			cell.nEntities = 0;
		}
	}

	public Cell getCell(int cellIdx) {
		return cells[cellIdx];
	}

	@Override
	public Iterator<Cell> iterator() {
		return Iterators.forArray(cells);
	}

	public float getCellAngle() {
		return fov / (float) cells.length;
	}
	
	public float getFov() {
		return fov;
	}

	public int numberOfCells() { return cells.length; }

	public Cell[] getCells() {
		return cells;
	}

	public static float computeWeight(float sqLen) {
		float dMin = 0.9f * Settings.protozoaInteractRange;
		float wD = 0.5f;
		float x = 1 - wD;
		float k = (float) (0.5 * Math.log((1 + x) / (1 - x))) / (dMin - Settings.protozoaInteractRange);
		return (float) (1 + Math.tanh(-k*(Math.sqrt(sqLen) - dMin))) / 2f;
	}
	
}
