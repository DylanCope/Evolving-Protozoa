package core;

import biology.Entity;
import biology.PlantPellet;
import utils.Vector2;

import java.io.Serializable;
import java.util.Collection;

public class ChemicalSolution implements Serializable {
    public static final long serialVersionUID = 1L;

    private final float gridSize;
    private final float xMin;
    private final float yMin;
    private final float xMax;
    private final float yMax;
    private final int nYChunks;
    private final int nXChunks;
    private final Chemical[][] chemicalGrid;

    public ChemicalSolution(float xMin, float xMax,
                            float yMin, float yMax,
                            float gridSize) {
        this.xMin = xMin;
        this.xMax = xMax;
        this.yMin = yMin;
        this.yMax = yMax;
        this.gridSize = gridSize;

        this.nXChunks = 2 + (int) ((xMax - xMin) / gridSize);
        this.nYChunks = 2 + (int) ((yMax - yMin) / gridSize);

        this.chemicalGrid = new Chemical[nXChunks][nYChunks];
        for (int i = 0; i < nXChunks; i++)
            for (int j = 0; j < nYChunks; j++)
                this.chemicalGrid[i][j] = new Chemical();
    }

    public Vector2 toTankCoords(int i, int j) {
        float x = (i - 1) * gridSize + xMin;
        float y = (j - 1) * gridSize + yMin;
        return new Vector2(x, y);
    }

    public float getGridSize() {
        return gridSize;
    }

    public int toChemicalGridX(float x) {
        int i = (int) (1 + (x - xMin) / gridSize);
        if (i < 0)
            return 0;
        if (i >= nXChunks)
            return nXChunks - 1;
        return i;
    }

    public int toChemicalGridY(float y) {
        int j = (int) (1 + (y - yMin) / gridSize);
        if (j < 0)
            return 0;
        if (j >= nYChunks)
            return nYChunks - 1;
        return j;
    }

    public void depositChemicals(float delta, Entity e) {
        if (e instanceof PlantPellet) {
            int i = toChemicalGridX(e.getPos().getX());
            int j = toChemicalGridY(e.getPos().getY());
            float k = Settings.plantPheromoneDeposit;
            chemicalGrid[i][j].currentPlantPheromoneDensity += delta * k * e.getRadius() * e.getHealth();
        }
    }

    public void update(float delta, Collection<Entity> entities) {
        entities.forEach(e -> depositChemicals(delta, e));

        for (int i = 1; i < nXChunks - 1; i++)
            for (int j = 1; j < nYChunks - 1; j++)
                chemicalGrid[i][j].propagate(
                        delta,
                        chemicalGrid[i][j-1],
                        chemicalGrid[i][j+1],
                        chemicalGrid[i-1][j],
                        chemicalGrid[i+1][j]
                );
        for (int i = 0; i < nXChunks; i++)
            for (int j = 0; j < nYChunks; j++)
                chemicalGrid[i][j].update();
    }

    public float getPlantPheromoneGradientX(int i, int j) {
        if (i < 1 || i >= nXChunks - 1)
            return 0f;
        return chemicalGrid[i-1][j].currentPlantPheromoneDensity - chemicalGrid[i+1][j].currentPlantPheromoneDensity;
    }

    public float getPlantPheromoneGradientY(int i, int j) {
        if (j < 1 || j >= nYChunks - 1)
            return 0f;
        return chemicalGrid[i][j-1].currentPlantPheromoneDensity - chemicalGrid[i][j+1].currentPlantPheromoneDensity;
    }

    public int getNYChunks() {
        return nYChunks;
    }

    public int getNXChunks() {
        return nXChunks;
    }

    public float getPlantPheromoneDenisty(int i, int j) {
        return chemicalGrid[i][j].currentPlantPheromoneDensity;
    }
}
