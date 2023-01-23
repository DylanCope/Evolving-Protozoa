package protoevo.env;

import protoevo.biology.Cell;
import protoevo.biology.PlantCell;
import protoevo.core.Settings;
import protoevo.utils.Vector2;

import java.io.Serializable;
import java.util.Arrays;
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
    private float timeSinceUpdate = 0;

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

        chemicalGrid = new Chemical[nXChunks][nYChunks];
        for (int i = 0; i < nXChunks; i++)
            for (int j = 0; j < nYChunks; j++)
                chemicalGrid[i][j] = new Chemical();
    }

    public void initialise() {
        for (int i = 1; i < nXChunks - 1; i++)
            for (int j = 1; j < nYChunks - 1; j++)
                chemicalGrid[i][j].setNeighbours(
                        chemicalGrid[i][j-1],
                        chemicalGrid[i][j+1],
                        chemicalGrid[i-1][j],
                        chemicalGrid[i+1][j]
                );
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

    public void depositChemicals(float delta, Cell e) {
        if (e instanceof PlantCell && !e.isDead()) {
            int i = toChemicalGridX(e.getPos().getX());
            int j = toChemicalGridY(e.getPos().getY());
            float k = Settings.plantPheromoneDeposit;
            chemicalGrid[i][j].currentPlantPheromoneDensity += delta * k * e.getRadius() * e.getHealth();
        }
    }

    public void update(float delta, Collection<Cell> entities) {
        timeSinceUpdate += delta;
        if (timeSinceUpdate >= Settings.chemicalsUpdateTime) {
            entities.parallelStream().forEach(e -> depositChemicals(timeSinceUpdate, e));
            Arrays.stream(chemicalGrid).parallel().forEach(
                    row -> Arrays.stream(row).forEach(chemical -> chemical.propagate(timeSinceUpdate))
            );
            Arrays.stream(chemicalGrid).parallel().forEach(
                    row -> Arrays.stream(row).forEach(Chemical::update)
            );
            timeSinceUpdate = 0;
        }
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

    public float getPlantPheromoneDensity(int i, int j) {
        return chemicalGrid[i][j].currentPlantPheromoneDensity;
    }
}
