package core;

import biology.Entity;
import org.checkerframework.checker.units.qual.A;
import utils.Vector2;

import java.io.Serializable;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ChunkManager implements Serializable {

    private final float chunkSize;
    private final float xMin;
    private final float yMin;
    private final float xMax;
    private final float yMax;
    private final int nYChunks;

    private final int nXChunks;

    private final Chunk[][] chunks;

    public ChunkManager(float xMin, float xMax,
                        float yMin, float yMax,
                        float chunkSize) {
        this.xMin = xMin;
        this.xMax = xMax;
        this.yMin = yMin;
        this.yMax = yMax;
        this.chunkSize = chunkSize;

        this.nXChunks = 2 + (int) ((xMax - xMin) / chunkSize);
        this.nYChunks = 2 + (int) ((yMax - yMin) / chunkSize);

        this.chunks = new Chunk[nXChunks][nYChunks];
        for (int i = 0; i < nXChunks; i++)
            for (int j = 0; j < nYChunks; j++)
                this.chunks[i][j] = new Chunk(i, j, this);

    }

    public Vector2 toChunkCoords(Vector2 pos) {
        float x = pos.getX();
        float y = pos.getY();

        float chunkX = 1 + (x - xMin) / chunkSize;
        float chunkY = 1 + (y - yMin) / chunkSize;

        return new Vector2(chunkX, chunkY);
    }

    public Vector2 toTankCoords(Vector2 chunkCoords) {
        float x = (chunkCoords.getX() - 1) * chunkSize + xMin;
        float y = (chunkCoords.getY() - 1) * chunkSize + yMin;
        return new Vector2(x, y);
    }

    public Chunk getChunk(Entity e) {
        Vector2 chunkCoords = this.toChunkCoords(e.getPos());
        return getChunk(chunkCoords);
    }

    public Chunk getChunk(Vector2 chunkCoords) {
        int i = (int) chunkCoords.getX();
        int j = (int) chunkCoords.getY();
        return this.chunks[i][j];
    }

    public void add(Entity e) {
        Chunk chunk = getChunk(e);
        chunk.addEntity(e);
    }

    public void remove(Entity e) {
        Chunk chunk = getChunk(e);
        chunk.removeEntity(e);
    }

    public void removeFromChunk(Vector2 chunkCoords, Entity e) {
        Chunk chunk = getChunk(chunkCoords);
        chunk.removeEntity(e);
    }

    public List<Chunk> getNearbyChunks(Vector2 chunkCoords, int n) {
        List<Chunk> nearbyChunks = new ArrayList<>();
        int x = (int) chunkCoords.getX();
        int y = (int) chunkCoords.getY();
        for (int i = x - n; i <= x + n & i < nXChunks; i++) {
            if (i < 0)
                continue;
            int chunkYMin = y - n;
            if (chunkYMin < 0)
                chunkYMin = 0;
            int chunkYMax = (y + n + 1);
            if (chunkYMax >= nYChunks)
                chunkYMax = nYChunks - 1;
            nearbyChunks.addAll(Arrays.asList(this.chunks[i]).subList(chunkYMin, chunkYMax));
        }

        return nearbyChunks;
    }

    public Collection<Entity> getNearbyEntities(Entity e, int n) {
        Vector2 chunkCoords = toChunkCoords(e.getPos());
        List<Chunk> nearbyChunks = getNearbyChunks(chunkCoords, n);
        List<Entity> nearbyEntities = new ArrayList<>();
        for (Chunk chunk : nearbyChunks)
            for (Entity other : chunk.getEntities())
                if (other != e)
                    nearbyEntities.add(other);
        return nearbyEntities;
    }

    public Collection<Entity> getNearbyEntities(Entity e) {
        return getNearbyEntities(e, 1);
    }

    public Collection<Chunk> getAllChunks() {
        List<Chunk> allChunks = new ArrayList<>();
        for (Chunk[] chunkRow : chunks)
            Collections.addAll(allChunks, chunkRow);
        return allChunks;
    }

    public Collection<Entity> getAllEntities() {
        Collection<Chunk> chunkStream = getAllChunks();
        List<Entity> allEntities = new ArrayList<>();
        for (Chunk chunk : chunkStream)
            allEntities.addAll(chunk.getEntities());
        return allEntities;
    }

    public void forEachEntity(Consumer<Entity> function) {
        getAllEntities().forEach(function);
    }

    public void update() {
        getAllChunks().forEach(Chunk::update);
    }

    public float getChunkSize() {
        return chunkSize;
    }

    public float getXMin() {
        return xMin;
    }

    public float getYMin() {
        return yMin;
    }

    public float getXMax() {
        return xMax;
    }

    public float getYMax() {
        return yMax;
    }

    public Chunk[][] getChunks() {
        return chunks;
    }

    public int getNYChunks() {
        return nYChunks;
    }

    public int getNXChunks() {
        return nXChunks;
    }
}
