package protoevo.env;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

import protoevo.biology.*;
import protoevo.biology.genes.Gene;
import protoevo.core.ChunkManager;
import protoevo.core.Settings;
import protoevo.core.Simulation;
import protoevo.utils.FileIO;
import protoevo.utils.Vector2;

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

		if (Settings.enableChemicalField) {
			float chemicalGridSize = 2 * radius / Settings.numChemicalBreaks;
			chemicalSolution = new ChemicalSolution(-radius, radius, -radius, radius, chemicalGridSize);
		} else {
			chemicalSolution = null;
		}

		rocks = new ArrayList<>();

		elapsedTime = 0;
		hasInitialised = false;
	}

	public void initialise() {
		if (chemicalSolution != null)
			chemicalSolution.initialise();

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
			flushEntitiesToAdd();

			if (Settings.writeGenomes && genomeFile != null)
				writeGenomeHeaders();

			hasInitialised = true;
		}
	}

	public void writeGenomeHeaders() {
		Protozoan protozoan = chunkManager.getAllCells()
				.stream()
				.filter(cell -> cell instanceof Protozoan)
				.map(cell -> (Protozoan) cell)
				.findAny()
				.orElseThrow(() -> new RuntimeException("No initial population present"));

		StringBuilder headerStr = new StringBuilder();
		headerStr.append("Generation,Time Elapsed,Parent 1 ID,Parent 2 ID,ID,");
		for (Gene<?> gene : protozoan.getGenome().getGenes())
			headerStr.append(gene.getTraitName()).append(",");

		FileIO.appendLine(genomeFile, headerStr.toString());
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
				addRandom(new Protozoan(this), findProtozoaPosition);
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

	private void flushEntitiesToAdd() {
		entitiesToAdd.forEach(chunkManager::add);
		entitiesToAdd.clear();
		chunkManager.update();
	}

	private void flushWrites() {
		List<String> genomeWritesHandled = new ArrayList<>();
		for (String line : genomesToWrite) {
			FileIO.appendLine(genomeFile, line);
			genomeWritesHandled.add(line);
		}
		genomesToWrite.removeAll(genomeWritesHandled);
	}

	public void update(float delta) 
	{
		elapsedTime += delta;
		flushEntitiesToAdd();
		flushWrites();

		Collection<Cell> cells = chunkManager.getAllCells();

		cells.parallelStream().forEach(Cell::resetPhysics);
		cells.parallelStream().forEach(cell -> updateCell(cell, delta));
		cells.parallelStream().forEach(cell -> cell.physicsUpdate(delta));
		cells.parallelStream().forEach(this::handleDeadEntities);

		updateCounts(cells);
		if (chemicalSolution != null)
			chemicalSolution.update(delta, cells);

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

	private void handleNewProtozoa(Protozoan p) {
		protozoaBorn++;
		generation = Math.max(generation, p.getGeneration());

		if (genomeFile != null && Settings.writeGenomes) {
			String genomeLine = p.getGeneration() + "," + elapsedTime + "," + p.getGenome().toString();
			genomesToWrite.add(genomeLine);
		}
	}

	public void add(Cell e) {
		if (cellCounts.getOrDefault(e.getClass(), 0)
				>= cellCapacities.getOrDefault(e.getClass(), 0))
			return;

		totalCellsAdded++;
		entitiesToAdd.add(e);

		if (e instanceof Protozoan)
			handleNewProtozoa((Protozoan) e);
	}

	public Collection<Cell> getEntities() {
		return chunkManager.getAllCells();
	}

	public Map<String, Float> getStats(boolean includeProtozoaStats) {
		Map<String, Float> stats = new TreeMap<>();
		stats.put("Protozoa", (float) numberOfProtozoa());
		stats.put("Plants", (float) cellCounts.getOrDefault(PlantCell.class, 0));
		stats.put("Meat Pellets", (float) cellCounts.getOrDefault(MeatCell.class, 0));
		stats.put("Max Generation", (float) generation);
		stats.put("Time Elapsed", elapsedTime);
		stats.put("Protozoa Born", (float) protozoaBorn);
		stats.put("Total Entities Born", (float) totalCellsAdded);
		stats.put("Crossover Events", (float) crossoverEvents);
		if (includeProtozoaStats)
			stats.putAll(getProtozoaStats());
		return stats;
	}

	public Map<String, Float> getStats() {
		return getStats(false);
	}

	public Map<String, Float> getProtozoaStats() {
		Map<String, Float> stats = new TreeMap<>();
		Collection<Protozoan> protozoa = chunkManager.getAllCells()
				.stream()
				.filter(cell -> cell instanceof Protozoan)
				.map(cell -> (Protozoan) cell)
				.collect(Collectors.toSet());

		for (Cell e : protozoa) {
			for (Map.Entry<String, Float> stat : e.getStats().entrySet()) {
				String key = "Sum " + stat.getKey();
				float currentValue = stats.getOrDefault(key, 0f);
				stats.put(key, stat.getValue() + currentValue);
			}
		}

		int numProtozoa = protozoa.size();
		for (Cell e : protozoa) {
			for (Map.Entry<String, Float> stat : e.getStats().entrySet()) {
				float sumValue = stats.getOrDefault("Sum " + stat.getKey(), 0f);
				float mean = sumValue / numProtozoa;
				stats.put("Mean " + stat.getKey(), mean);
				float currVar = stats.getOrDefault("Var " + stat.getKey(), 0f);
				float deltaVar = (float) Math.pow(stat.getValue() - mean, 2) / numProtozoa;
				stats.put("Var " + stat.getKey(), currVar + deltaVar);
			}
		}
		return stats;
	}
	
	public int numberOfProtozoa() {
		return cellCounts.getOrDefault(Protozoan.class, 0);
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
