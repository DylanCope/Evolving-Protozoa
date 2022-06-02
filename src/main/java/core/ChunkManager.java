package core;

import biology.Entity;
import com.google.common.collect.Iterators;
import utils.Vector2;

import java.io.Serializable;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ChunkManager implements Serializable {

    private final double chunkSize;
    private final double xMin;
    private final double yMin;
    private final double xMax;
    private final double yMax;
    private final int nYChunks;

    private final int nXChunks;

    private final Chunk[][] chunks;

    public ChunkManager(double xMin, double xMax,
                        double yMin, double yMax,
                        double chunkSize) {
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
        double x = pos.getX();
        double y = pos.getY();

        double chunkX = 1 + (x - xMin) / chunkSize;
        double chunkY = 1 + (y - yMin) / chunkSize;

        return new Vector2(chunkX, chunkY);
    }

    public Vector2 toTankCoords(Vector2 chunkCoords) {
        double x = (chunkCoords.getX() - 1) * chunkSize + xMin;
        double y = (chunkCoords.getY() - 1) * chunkSize + yMin;
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

    public Stream<Entity> getNearbyEntities(Entity e, int n) {
        Vector2 chunkCoords = toChunkCoords(e.getPos());
        List<Chunk> nearbyChunks = getNearbyChunks(chunkCoords, n);
        Stream<Stream<Entity>> entityStream = nearbyChunks.stream().map(Chunk::getEntities);
        return entityStream.flatMap(Function.identity()).filter(other -> !e.equals(other));
    }

    public Stream<Entity> getNearbyEntities(Entity e) {
        return getNearbyEntities(e, 1);
    }

    public Stream<Chunk> getAllChunks() {
        return Arrays.stream(chunks).flatMap(Arrays::stream);
    }

    public Stream<Entity> getAllEntities() {
        Stream<Chunk> chunkStream = getAllChunks();
        Stream<Stream<Entity>> entityStream = chunkStream.map(Chunk::getEntities);
        return entityStream.flatMap(Function.identity());
    }

    public void forEachEntity(Consumer<Entity> function) {
        getAllEntities().forEach(function);
    }

    public void update() {
        getAllChunks().forEach(Chunk::update);
    }

    public double getChunkSize() {
        return chunkSize;
    }

    public double getXMin() {
        return xMin;
    }

    public double getYMin() {
        return yMin;
    }

    public double getXMax() {
        return xMax;
    }

    public double getYMax() {
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
