package biology;

import com.google.common.collect.Iterators;
import core.Settings;
import utils.Vector2;

import java.awt.Color;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Iterator;


public class Retina implements Iterable<Retina.Cell>, Serializable
{
	private static final long serialVersionUID = 5214857174841633362L;

	public static class Cell implements Serializable {
		private static final long serialVersionUID = 1L;
		private final float angle;
		private final Color[] colours;
		private final float[] weights, lengths;
		private final Vector2[] rays;

		public Cell(float angle, float cellFov) {
			int nRays = 1;
			if (cellFov > Settings.minRetinaRayAngle)
				nRays = (int) (cellFov / Settings.minRetinaRayAngle);

			colours = new Color[nRays];
			weights = new float[nRays];
			lengths = new float[nRays];
			rays = new Vector2[nRays];
			float rayAngle = cellFov / nRays;
			for (int i = 0; i < nRays; i++) {
				float t = -rayAngle / 2 + angle + (nRays - 2*i) * cellFov / (2*nRays);
				rays[i] = Vector2.fromAngle(t);
			}
			this.angle = angle;
			reset();
		}

		public void reset() {
			Arrays.fill(colours, null);
			Arrays.fill(weights, 0);
			Arrays.fill(lengths, Float.MAX_VALUE);
		}

		public void set(int idx, Color c, float sqLen) {
			colours[idx] = c;
			lengths[idx] = sqLen;
			weights[idx] = 1f;
		}

		public Vector2[] getRays() {
			return rays;
		}

		public Color getColour() {
			float r = 0;
			float g = 0;
			float b = 0;
			int nEntities = 0;
			for (int i = 0; i < colours.length; i++) {
				if (colours[i] != null) {
					float w = weights[i];
					r += w * colours[i].getRed();
					g += w * colours[i].getGreen();
					b += w * colours[i].getBlue();
					nEntities++;
				}
			}

			if (nEntities == 0)
				return new Color(0, 0, 0);

			return new Color(
					(int) (r / nEntities),
					(int) (g / nEntities),
					(int) (b / nEntities)
			);
		}

		public boolean anythingVisible() {
			for (Color c : colours)
				if (c != null)
					return true;
			return false;
		}

		public float getAngle() {
			return angle;
		}

		public boolean rayIntersectedEntity(int rayIndex) {
			return colours[rayIndex] != null;
		}

		public float collisionSqLen(int rayIndex) {
			return lengths[rayIndex];
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
		float cellFov = fov / numCells;
		for (int i = 0; i < numCells; i++) {
			float angle = -cellFov / 2 + fov * (numCells - 2 * i) / (2 * numCells);
			cells[i] = new Cell(angle, cellFov);
		}
	}

	public void reset() {
		for (Retina.Cell cell : cells)
			cell.reset();
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
