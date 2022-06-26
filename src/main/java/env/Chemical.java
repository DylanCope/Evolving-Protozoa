package env;

import core.Settings;

import java.io.Serializable;

public class Chemical implements Serializable {
    public static final long serialVersionUID = 1L;
    float currentPlantPheromoneDensity, nextPlantPheromoneDensity;

    public void propagate(float delta,
                          Chemical chemicalUp,
                          Chemical chemicalDown,
                          Chemical chemicalLeft,
                          Chemical chemicalRight) {
        nextPlantPheromoneDensity = currentPlantPheromoneDensity;
        nextPlantPheromoneDensity += pheromoneFlow(chemicalUp);
        nextPlantPheromoneDensity += pheromoneFlow(chemicalDown);
        nextPlantPheromoneDensity += pheromoneFlow(chemicalLeft);
        nextPlantPheromoneDensity += pheromoneFlow(chemicalRight);
        nextPlantPheromoneDensity *= 1 - delta * Settings.pheromoneDecay;
//        nextPlantPheromoneDensity = sigmoid(nextPlantPheromoneDensity);
        if (nextPlantPheromoneDensity > 1)
            nextPlantPheromoneDensity = 1;
    }

    public void update() {
        currentPlantPheromoneDensity = nextPlantPheromoneDensity;
    }

    private float sigmoid(float z) {
        return 1 / (1 + (float) Math.exp(-z));
    }

    public float pheromoneFlow(Chemical other) {
        float densityDiff = other.currentPlantPheromoneDensity - currentPlantPheromoneDensity;
        float p = Settings.pheromoneFlow * (float) Math.tanh(densityDiff);
        return p * densityDiff;
    }
}
