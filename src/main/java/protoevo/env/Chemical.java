package protoevo.env;

import protoevo.core.Settings;

import java.io.Serializable;

public class Chemical implements Serializable {
    public static final long serialVersionUID = 1L;
    float currentPlantPheromoneDensity, nextPlantPheromoneDensity;
    private transient Chemical[] neighbours;

    public void propagate(float delta) {
        nextPlantPheromoneDensity = currentPlantPheromoneDensity;
        if (neighbours != null && neighbours.length > 0) {
            float incoming = 0f;
            for (Chemical chemical : neighbours)
                incoming += chemical.currentPlantPheromoneDensity;
            incoming /= neighbours.length;
            nextPlantPheromoneDensity += delta * incoming;
        }
        nextPlantPheromoneDensity *= 1 - delta * Settings.chemicalsDecay;
    }

    public void update() {
        currentPlantPheromoneDensity = Math.max(Math.min(nextPlantPheromoneDensity, 1f), 0f);
        if (Float.isNaN(currentPlantPheromoneDensity))
            currentPlantPheromoneDensity = 0f;
    }

    private float sigmoid(float z) {
        return 1 / (1 + (float) Math.exp(-z));
    }

    public float pheromoneFlow(Chemical other) {
        float densityDiff = other.currentPlantPheromoneDensity - currentPlantPheromoneDensity;
        float p = Settings.chemicalsFlow * (float) Math.tanh(densityDiff);
        return p * densityDiff;
    }

    public void setNeighbours(Chemical...neighbours) {
        this.neighbours = neighbours;
    }
}
