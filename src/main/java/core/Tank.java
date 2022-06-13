package core;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import biology.*;
import utils.FileIO;
import utils.Vector2;

public class Tank implements Iterable<Entity>, Serializable
{
	private static final long serialVersionUID = 2804817237950199223L;
	private final float radius = Settings.tankRadius;
	private float elapsedTime;
	public final ConcurrentHashMap<Class<? extends Entity>, Integer> entityCounts = new ConcurrentHashMap<>(3, 1);
	public final ConcurrentHashMap<Class<? extends Entity>, Integer> entityCapacities = new ConcurrentHashMap<>(3, 1);
	private final ChunkManager chunkManager;
	private int generation = 1;
	private int protozoaBorn = 0;
	private int totalEntitiesAdded = 0;

	private String genomeFile = null;
	private final List<String> genomesToWrite = new ArrayList<>();

	private final List<Entity> entitiesToAdd = new ArrayList<>();

	public Tank() 
	{
		float chunkSize = 2 * radius / Settings.numChunkBreaks;
		chunkManager = new ChunkManager(-radius, radius, -radius, radius, chunkSize);
		elapsedTime = 0;
	}
	
	public Vector2 randomPosition(float entityRadius) {
		float rad = radius - 4*entityRadius;
		float t = (float) (2 * Math.PI * Simulation.RANDOM.nextDouble());
		float r = 2*entityRadius + rad * Simulation.RANDOM.nextFloat();
		return new Vector2(
				(float) (r * Math.cos(t)),
				(float) (r * Math.sin(t))
		);
	}

	public void handleTankEdge(Entity e) {
		if (e.getPos().len() - e.getRadius() > radius)
			e.getPos().setLength(-0.98f * radius);
	}

	public void updateEntity(Entity e, float delta) {
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

		Collection<Entity> entities = chunkManager.getAllEntities();

//		Arrays.stream(chunkManager.getChunks())
//				.parallel()
//				.forEach(chunk -> chunk.getEntities().forEach(e -> updateEntity(e, delta)));
//
//		Arrays.stream(chunkManager.getChunks())
//				.parallel()
//				.forEach(chunk -> chunk.getEntities().forEach(e -> e.handleCollisions(delta)));
		entities.parallelStream().forEach(Entity::resetPhysics);
		entities.parallelStream().forEach(e -> updateEntity(e, delta));
		entities.parallelStream().forEach(e -> e.physicsUpdate(delta));
		entities.parallelStream().forEach(this::handleDeadEntities);

	}

	private void handleDeadEntities(Entity e) {
		if (!e.isDead())
			return;

		entityCounts.put(e.getClass(), -1 + entityCounts.get(e.getClass()));
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

	public void add(Entity e) {
		if (entityCounts.getOrDefault(e.getClass(), 0)
				>= entityCapacities.getOrDefault(e.getClass(), 0))
			return;

		totalEntitiesAdded++;
		entitiesToAdd.add(e);

		if (e instanceof Protozoa)
			handleNewProtozoa((Protozoa) e);

		entityCounts.put(e.getClass(), 1 + entityCounts.getOrDefault(e.getClass(), 0));
	}

	public Collection<Entity> getEntities() {
		return chunkManager.getAllEntities();
	}

	public Map<String, Float> getStats() {
		Map<String, Float> stats = new HashMap<>();
		stats.put("Number of Protozoa", (float) numberOfProtozoa());
		stats.put("Number of Plant Pellets", (float) entityCounts.getOrDefault(PlantPellet.class, 0));
		stats.put("Number of Meat Pellets", (float) entityCounts.getOrDefault(MeatPellet.class, 0));
		stats.put("Max Generation", (float) generation);
		stats.put("Time Elapsed", elapsedTime);
		stats.put("Protozoa Born", (float) protozoaBorn);
		stats.put("Total Entities Born", (float) totalEntitiesAdded);

		Collection<Entity> entities = chunkManager.getAllEntities();
		float n = (float) entities.size();
		for (Entity e : entities) {
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
		return entityCounts.getOrDefault(Protozoa.class, 0);
	}
	
	public int numberOfPellets() {
		int nPellets = entityCounts.getOrDefault(PlantPellet.class, 0);
		nPellets += entityCounts.getOrDefault(MeatPellet.class, 0);
		return nPellets;
	}

	public ChunkManager getChunkManager() {
		return chunkManager;
	}

	@Override
	public Iterator<Entity> iterator() {
		return chunkManager.getAllEntities().iterator();
	}

	public float getRadius() {
		return radius;
	}

	public int getGeneration() {
		return generation;
	}

    public void addRandom(Entity e) {
		e.setPos(randomPosition(e.getRadius()));
		for (int i = 0; i < 5 && chunkManager.getAllEntities().stream().anyMatch(e::isCollidingWith); i++)
			e.setPos(randomPosition(e.getRadius()));
		if (chunkManager.getAllEntities().stream().noneMatch(e::isCollidingWith))
			add(e);
    }

	public float getElapsedTime() {
		return elapsedTime;
	}

	public void setGenomeFile(String genomeFile) {
		this.genomeFile = genomeFile;
	}
}
