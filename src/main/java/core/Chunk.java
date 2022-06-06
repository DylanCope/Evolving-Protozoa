package core;

import biology.Entity;
import utils.Vector2;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Chunk implements Serializable {

    private final List<Entity> entities;
    private final int x;
    private final int y;
    private final ChunkManager chunkManager;

    public Chunk(int x, int y, ChunkManager chunkManager) {
        this.entities = new ArrayList<>();
        this.x = x;
        this.y = y;
        this.chunkManager = chunkManager;
    }

    public Vector2 getChunkCoords() {
        return new Vector2((float) x, (float) y);
    }

    public Vector2 getTankCoords() {
        return this.chunkManager.toTankCoords(getChunkCoords());
    }

    public Collection<Entity> getEntities() {
        return entities;
    }

    public void addEntity(Entity e) {
        entities.add(e);
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Chunk) {
            Chunk otherChunk = (Chunk) o;
            return otherChunk.x == x && otherChunk.y == y;
        }
        return false;
    }

    public void clear() {
        entities.clear();
    }
}
