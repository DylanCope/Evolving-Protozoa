package protoevo.core;

import protoevo.biology.Cell;
import com.google.common.collect.Iterators;
import protoevo.env.Rock;
import protoevo.utils.Vector2;

import java.io.Serializable;
import java.util.*;

public class Chunk implements Serializable {

    public static final long serialVersionUID = 4697424153087580763L;

    private final List<Cell> entities;
    private final List<Rock> rocks;
    private final int x;
    private final int y;
    private final ChunkManager chunkManager;

    public Chunk(int x, int y, ChunkManager chunkManager) {
        this.x = x;
        this.y = y;
        this.chunkManager = chunkManager;

        entities = new LinkedList<>();
        rocks = new ArrayList<>();
    }

    public Vector2 getChunkCoords() {
        return new Vector2((float) x, (float) y);
    }

    public Vector2 getTankCoords() {
        return this.chunkManager.toTankCoords(getChunkCoords());
    }

    public Collection<Cell> getCells() {
        return entities;
    }

    public void addEntity(Cell e) {
        entities.add(e);
    }

    public void addRock(Rock rock) {
        rocks.add(rock);
    }

    public boolean contains(Rock rock) {
        return rocks.contains(rock);
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

    public Iterator<Collidable> getCollidables() {
        return Iterators.concat(entities.iterator(), rocks.iterator());
    }

    public Collection<Rock> getRocks() {
        return rocks;
    }
}
