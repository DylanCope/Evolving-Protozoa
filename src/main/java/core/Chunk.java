package core;

import biology.Entity;
import utils.Vector2;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Chunk implements Serializable {

    private final HashSet<Entity> entities;
    private final int x;
    private final int y;
    private final ChunkManager chunkManager;

    public Chunk(int x, int y, ChunkManager chunkManager) {
        this.entities = new HashSet<>();
        this.x = x;
        this.y = y;
        this.chunkManager = chunkManager;
    }

    public void update() {
        entities.removeIf(this::shouldRemove);
    }

    public boolean inChunkBounds(Entity e) {
        Vector2 chunkCoords = chunkManager.toChunkCoords(e.getPos());
        int entityX = (int) chunkCoords.getX();
        int entityY = (int) chunkCoords.getY();
        return entityX == x & entityY == y;
    }

    public Vector2 getChunkCoords() {
        return new Vector2((float) x, (float) y);
    }

    public Vector2 getTankCoords() {
        return this.chunkManager.toTankCoords(getChunkCoords());
    }

    private boolean shouldRemove(Entity e) {
        if (e.isDead())
            return true;

        if (!inChunkBounds(e)) {
            chunkManager.add(e);
            return true;
        }

        return false;
    }

    public void removeEntity(Entity e) {
        entities.remove(e);
    }

    public Collection<Entity> getEntities() {
        return entities;
    }

    public void addEntity(Entity e) {
        entities.add(e);
    }
}
