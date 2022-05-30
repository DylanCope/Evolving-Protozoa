package core;

import java.awt.Graphics;
import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import biology.*;
import utils.Vector2;

public class Tank implements Iterable<Entity>, Serializable
{
	private static final long serialVersionUID = 2804817237950199223L;
	private final double radius = 1.0;
	private ConcurrentHashMap<Class<? extends Entity>, Integer> entityCounts;
	private final ChunkManager chunkManager;

	public Tank() 
	{
		double chunkSize = 2 * radius / 20;
		chunkManager = new ChunkManager(-radius, radius, -radius, radius, chunkSize);
		entityCounts = new ConcurrentHashMap<>();
	}
	
	public void addRandomEntity(Entity e) {
		double rad 	= radius - 2*e.getRadius();
		double t 	= 2 * Math.PI * Simulation.RANDOM.nextDouble();
		double r 	= Simulation.RANDOM.nextDouble();
		e.setPos(new Vector2(
					rad * (1 - r*r) * Math.cos(t),
					rad * (1 - r*r) * Math.sin(t)
				));
		registerNewEntity(e);
	}

	public void handleTankEdge(Entity e) {
		if (e.getPos().len() - e.getRadius() > radius)
			e.setPos(e.getPos().mul(-0.98));
	}

	public Stream<Entity> updateEntity(Entity e, double delta) {

		Stream<Entity> newEntities = e.update(delta, chunkManager.getNearbyEntities(e));

		handleTankEdge(e);

		return newEntities;
	}

	public void update(double delta) 
	{
		Collection<Entity> newEntities = chunkManager.getAllEntities()
				.flatMap(e -> updateEntity(e, delta))
				.collect(Collectors.toList());

		newEntities.forEach(this::registerNewEntity);

		chunkManager.getAllEntities().filter(Entity::isDead)
				.flatMap(this::handleEntityDeath)
				.forEach(chunkManager::add);

		chunkManager.update();
	}

	private Stream<Entity> handleEntityDeath(Entity e) {
		entityCounts.put(e.getClass(), -1 + entityCounts.get(e.getClass()));
		return e.handleDeath();
	}

	private void registerNewEntity(Entity e) {

		chunkManager.add(e);

		if (!entityCounts.containsKey(e.getClass()))
			entityCounts.put(e.getClass(), 0);
		else
			entityCounts.put(e.getClass(), 1 + entityCounts.get(e.getClass()));

	}
	
	public void render(Graphics g)
	{
		chunkManager.forEachEntity(e -> e.render(g));
	}

	public Collection<Entity> getEntities() {
		return chunkManager.getAllEntities().collect(Collectors.toList());
	}
	
	public int numberOfProtozoa() {
		if (entityCounts.containsKey(Protozoa.class))
			return entityCounts.get(Protozoa.class);
		return 0;
	}
	
	public int numberOfPellets() {
		int nPellets = 0;
		if (entityCounts.containsKey(PlantPellet.class))
			nPellets += entityCounts.get(PlantPellet.class);
		if (entityCounts.containsKey(MeatPellet.class))
			nPellets += entityCounts.get(MeatPellet.class);
		return nPellets;
	}

	public ChunkManager getChunkManager() { return chunkManager; }

	@Override
	public Iterator<Entity> iterator() {
		return chunkManager.getAllEntities().iterator();
	}

	public double getRadius() {
		return radius;
	}
}
