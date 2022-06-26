package env;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import biology.*;
import core.ChunkManager;
import core.Settings;
import core.Simulation;
import utils.FileIO;
import utils.Vector2;

public class Tank implements Iterable<Cell>, Serializable
{
	private static final long serialVersionUID = 2804817237950199223L;
	private final float radius = Settings.tankRadius;
	private float elapsedTime;
	public final ConcurrentHashMap<Class<? extends Cell>, Integer> cellCounts =
			new ConcurrentHashMap<>(3, 1);
	public final ConcurrentHashMap<Class<? extends Cell>, Integer> cellCapacities =
			new ConcurrentHashMap<>(3, 1);
	private final ChunkManager chunkManager;
	private final ChemicalSolution chemicalSolution;
	private final List<Rock> rocks;
	private long generation = 1, protozoaBorn = 0, totalCellsAdded = 0, crossoverEvents = 0;

	private String genomeFile = null;
	private final List<String> genomesToWrite = new ArrayList<>();

	private final List<Cell> entitiesToAdd = new ArrayList<>();
	private boolean hasInitialised;

	public Tank() 
	{
		float chunkSize = 2 * radius / Settings.numChunkBreaks;
		chunkManager = new ChunkManager(-radius, radius, -radius, radius, chunkSize);

		float chemicalGridSize = 2 * radius / Settings.numChemicalBreaks;
		chemicalSolution = new ChemicalSolution(-radius, radius, -radius, radius, chemicalGridSize);

		rocks = new ArrayList<>();

		elapsedTime = 0;
		hasInitialised = false;
	}

	public void initialise() {
		if (!hasInitialised) {

			Vector2[] clusterCentres = null;
			if (Settings.initialPopulationClustering) {
				clusterCentres = new Vector2[Settings.numRingClusters];
				for (int i = 0; i < clusterCentres.length; i++) {
					clusterCentres[i] = randomPosition(Settings.populationClusterRadius);
					RockGeneration.generateRingOfRocks(this, clusterCentres[i], Settings.populationClusterRadius*5);
				}
			}
			RockGeneration.generateRocks(this);

			rocks.forEach(chunkManager::allocateToChunk);
			if (clusterCentres != null)
				initialisePopulation(Arrays.copyOfRange(clusterCentres, 0, Settings.numPopulationClusters));
			else
				initialisePopulation();
			hasInitialised = true;
		}
	}

	public boolean hasBeenInitialised() {
		return hasInitialised;
	}

	public void initialisePopulation(Vector2[] clusterCentres) {
		Function<Float, Vector2> findPlantPosition = this::randomPosition;
		Function<Float, Vector2> findProtozoaPosition = this::randomPosition;
		if (clusterCentres != null) {
			findPlantPosition = r -> randomPosition(1.5f*r, clusterCentres);
			findProtozoaPosition = r -> randomPosition(r, clusterCentres);
		}

		for (int i = 0; i < Settings.numInitialPlantPellets; i++)
			addRandom(new PlantCell(this), findPlantPosition);

		for (int i = 0; i < Settings.numInitialProtozoa; i++) {
			try {
				addRandom(new Protozoa(this), findProtozoaPosition);
			} catch (MiscarriageException ignored) {}
		}
	}

	public void initialisePopulation() {
		Vector2[] clusterCentres = new Vector2[Settings.numPopulationClusters];
		for (int i = 0; i < clusterCentres.length; i++)
			clusterCentres[i] = randomPosition(Settings.populationClusterRadius);
		initialisePopulation(clusterCentres);
	}

	public Vector2 randomPosition(float entityRadius, Vector2[] clusterCentres) {
		int clusterIdx = Simulation.RANDOM.nextInt(clusterCentres.length);
		Vector2 clusterCentre = clusterCentres[clusterIdx];
		return randomPosition(entityRadius, clusterCentre, Settings.populationClusterRadius);
	}

	public Vector2 randomPosition(float entityRadius, Vector2 centre, float clusterRadius) {
		float rad = clusterRadius - 4*entityRadius;
		float t = (float) (2 * Math.PI * Simulation.RANDOM.nextDouble());
		float r = 2*entityRadius + rad * Simulation.RANDOM.nextFloat();
		return new Vector2(
				(float) (r * Math.cos(t)),
				(float) (r * Math.sin(t))
		).add(centre);
	}

	public Vector2 randomPosition(float entityRadius) {
		return randomPosition(entityRadius, Vector2.ZERO, radius);
	}

	public void handleTankEdge(Cell e) {
		float rPos = e.getPos().len();
		if (Settings.sphericalTank && rPos - e.getRadius() > radius)
			e.getPos().setLength(-0.98f * radius);
		else if (rPos + e.getRadius() > radius) {
			e.getPos().setLength(radius - e.getRadius());
			Vector2 normal = e.getPos().unit().scale(-1);
			e.getVel().translate(normal.mul(-2*normal.dot(e.getVel())));
		}

	}

	public void updateCell(Cell e, float delta) {
		e.handleInteractions(delta);
		e.update(delta);
		handleTankEdge(e);
	}

	public void update(float delta) 
	{
		elapsedTime += delta;

		entitiesToAdd.forEach(chunkManager::add);
		entitiesToAdd.clear();
		chunkManager.update();

		Collection<Cell> cells = chunkManager.getAllCells();

		cells.parallelStream().forEach(Cell::resetPhysics);
		cells.parallelStream().forEach(cell -> updateCell(cell, delta));
		cells.parallelStream().forEach(cell -> cell.physicsUpdate(delta));
		cells.parallelStream().forEach(this::handleDeadEntities);

		updateCounts(cells);
//		chemicalSolution.update(delta, entities);

	}

	private void updateCounts(Collection<Cell> entities) {
		cellCounts.clear();
		for (Cell e : entities)
			cellCounts.put(e.getClass(), 1 + cellCounts.getOrDefault(e.getClass(), 0));
	}

	private void handleDeadEntities(Cell e) {
		if (!e.isDead())
			return;
		e.handleDeath();
	}

	private void handleNewProtozoa(Protozoa p) {
		protozoaBorn++;
		generation = Math.max(generation, p.getGeneration());

		if (genomeFile != null && Settings.writeGenomes) {
			String genomeStr = p.getGenome().toString();
			String genomeLine = "generation=" + p.getGeneration() + "," + genomeStr;
			genomesToWrite.add(genomeLine);
			List<String> genomeWritesHandled = new ArrayList<>();
			for (String line : genomesToWrite) {
				FileIO.appendLine(genomeFile, line);
				genomeWritesHandled.add(line);
			}

			genomesToWrite.removeAll(genomeWritesHandled);
		}
	}

	public void add(Cell e) {
		if (cellCounts.getOrDefault(e.getClass(), 0)
				>= cellCapacities.getOrDefault(e.getClass(), 0))
			return;

		totalCellsAdded++;
		entitiesToAdd.add(e);

		if (e instanceof Protozoa)
			handleNewProtozoa((Protozoa) e);
	}

	public Collection<Cell> getEntities() {
		return chunkManager.getAllCells();
	}

	public Map<String, Float> getStats() {
		Map<String, Float> stats = new TreeMap<>();
		stats.put("Protozoa", (float) numberOfProtozoa());
		stats.put("Plants", (float) cellCounts.getOrDefault(PlantCell.class, 0));
		stats.put("Meat Pellets", (float) cellCounts.getOrDefault(MeatCell.class, 0));
		stats.put("Max Generation", (float) generation);
		stats.put("Time Elapsed", elapsedTime);
		stats.put("Protozoa Born", (float) protozoaBorn);
		stats.put("Total Entities Born", (float) totalCellsAdded);
		stats.put("Crossover Events", (float) crossoverEvents);

		Collection<Cell> entities = chunkManager.getAllCells();
		float n = (float) numberOfProtozoa();
		for (Cell e : entities) {
			if (e instanceof Protozoa) {
				for (Map.Entry<String, Float> stat : e.getStats().entrySet()) {
					String key = "Mean " + stat.getKey();
					float currentValue = stats.getOrDefault(key, 0.0f);
					stats.put(key, stat.getValue() / n + currentValue);
				}
			}
		}

		return stats;
	}
	
	public int numberOfProtozoa() {
		return cellCounts.getOrDefault(Protozoa.class, 0);
	}
	
	public int numberOfPellets() {
		int nPellets = cellCounts.getOrDefault(PlantCell.class, 0);
		nPellets += cellCounts.getOrDefault(MeatCell.class, 0);
		return nPellets;
	}

	public ChunkManager getChunkManager() {
		return chunkManager;
	}

	@Override
	public Iterator<Cell> iterator() {
		return chunkManager.getAllCells().iterator();
	}

	public float getRadius() {
		return radius;
	}

	public long getGeneration() {
		return generation;
	}

	public boolean isCollidingWithAnything(Cell e) {
		if (chunkManager.getAllCells().stream().anyMatch(e::isCollidingWith))
			return true;
		return rocks.stream().anyMatch(e::isCollidingWith);
	}

    public void addRandom(Cell e, Function<Float, Vector2> findPosition) {
		for (int i = 0; i < 5; i++) {
			e.setPos(findPosition.apply(e.getRadius()));
			if (!isCollidingWithAnything(e)) {
				add(e);
				return;
			}
		}
    }

	public float getElapsedTime() {
		return elapsedTime;
	}

	public void setGenomeFile(String genomeFile) {
		this.genomeFile = genomeFile;
	}

	public ChemicalSolution getChemicalSolution() {
		return chemicalSolution;
	}

	public List<Rock> getRocks() {
		return rocks;
	}

	public void registerCrossoverEvent() {
		crossoverEvents++;
	}
}
