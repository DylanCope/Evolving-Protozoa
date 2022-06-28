package protoevo.biology;

import com.google.common.collect.Iterators;
import protoevo.core.Settings;
import protoevo.utils.Vector2;

import java.awt.Color;
import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


public class Retina implements Iterable<Retina.Cell>, Serializable
{
	private static final long serialVersionUID = 5214857174841633362L;

	public static class Cell implements Serializable {
		private static final long serialVersionUID = 1L;
		private final float angle;
		private final Color[] colours;
		private final float[] weights, lengths;
		private final Vector2[] rays;
		private final RetinaConstructionProject constructionProject;

		public Cell(float angle, float cellFov, RetinaConstructionProject constructionProject) {
			this.constructionProject = constructionProject;

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
			float constructionProgress = constructionProject.getProgress();
			for (int i = 0; i < colours.length; i++) {
				if (colours[i] != null) {
					float w = constructionProgress * weights[i];
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
					(int) (b / nEntities),
					(int) (255 * constructionProgress)
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

	private static class RetinaConstructionProject extends ConstructionProject {

		RetinaConstructionProject(float fov, int nCells) {
			super(getRequiredMass(fov, nCells),
				  getRequiredEnergy(fov, nCells),
				  getRequiredTime(fov, nCells),
				  getRequiredComplexMolecules(fov, nCells));
		}

		public static float getRequiredMass(float retinaFoV, int nCells) {
			float r = Settings.minParticleRadius;
			return nCells * retinaFoV * r * r * r / 10f;
		}

		public static float getRequiredEnergy(float retinaFoV, int nCells) {
			return nCells * retinaFoV / 20f;
		}

		public static float getRequiredTime(float retinaFoV, int nCells) {
			return retinaFoV * nCells / 2f;
		}

		public static Map<Food.ComplexMolecule, Float> getRequiredComplexMolecules(float retinaFoV, int nCells) {
			Map<Food.ComplexMolecule, Float> requiredMolecules = new HashMap<>();
			float r = Settings.minParticleRadius;
			requiredMolecules.put(Food.ComplexMolecule.Retinal, (float) (nCells * retinaFoV * r * r * r / (20 * Math.PI)));
			return requiredMolecules;
		}

		@Override
		public void progress(float delta) {
			super.progress(delta);
		}
	}
	
	private final Cell[] cells;
	private final float fov;
	private final RetinaConstructionProject constructionProject;
	private float health;

	public Retina(int numCells, float fov)
	{
		constructionProject = new RetinaConstructionProject(fov, numCells);
		cells = new Cell[numCells];
		this.fov = fov;
		float cellFov = fov / numCells;
		for (int i = 0; i < numCells; i++) {
			float angle = -cellFov / 2 + fov * (numCells - 2 * i) / (2 * numCells);
			cells[i] = new Cell(angle, cellFov, constructionProject);
		}
		health = 1f;
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

	public float getHealth() {
		if (!constructionProject.isFinished())
			return constructionProject.getProgress();
		return health;
	}

	public float updateHealth(float delta, float availableRetinal) {
		if (!constructionProject.isFinished())
			return 0;

		float requiredRetinal =
			constructionProject.complexMoleculesToMakeProgress(delta, Food.ComplexMolecule.Retinal) / 20f;

		if (requiredRetinal > 0) {
			float usedRetinal = Math.min(availableRetinal, requiredRetinal);
			health = Math.max(0, health - 0.01f * delta * (requiredRetinal/2f - usedRetinal) / requiredRetinal);
			health = Math.min(health, 1f);
			return usedRetinal;
		}
		return 0f;
	}

	public static float computeWeight(float sqLen) {
		float dMin = 0.9f * Settings.protozoaInteractRange;
		float wD = 0.5f;
		float x = 1 - wD;
		float k = (float) (0.5 * Math.log((1 + x) / (1 - x))) / (dMin - Settings.protozoaInteractRange);
		return (float) (1 + Math.tanh(-k*(Math.sqrt(sqLen) - dMin))) / 2f;
	}

	public ConstructionProject getConstructionProject() {
		return constructionProject;
	}

}
