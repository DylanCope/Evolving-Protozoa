package biology;

import core.Settings;
import core.Simulation;

import java.awt.*;
import java.util.HashMap;
import java.util.stream.Stream;

public class PlantPellet extends Pellet {

    private final double maxRadius;

    public PlantPellet(double radius) {
        super(radius);
        setGrowthRate(Settings.minPlantGrowth + Settings.plantGrowthRange * Simulation.RANDOM.nextDouble());

        double range = Settings.maxPlantRadius - Settings.minMaxPlantRadius;
        maxRadius = Settings.minMaxPlantRadius + range * Simulation.RANDOM.nextDouble();

        setHealthyColour(new Color(
                30 + Simulation.RANDOM.nextInt(105),
                150  + Simulation.RANDOM.nextInt(100),
                10  + Simulation.RANDOM.nextInt(100))
        );
    }

    private static double randomPlantRadius() {
        double range = Settings.maxPlantBirthRadius - Settings.minPlantBirthRadius;
        return Settings.minPlantBirthRadius + range * Simulation.RANDOM.nextDouble();
    }

    public PlantPellet() {
        this(randomPlantRadius());
    }

    private boolean shouldSplit() {
        return getRadius() > maxRadius &&
                getCrowdingFactor() < 4 &&
                getHealth() > Settings.minHealthToSplit;
    }

    @Override
    public Stream<Entity> update(double delta, Stream<Entity> entities) {
        Stream<Entity> newEntities = super.update(delta, entities);
        if (getHealth() < 1.0)
            setHealth(getHealth() + (1 - getHealth()) * getGrowthRate());

        if (shouldSplit())
            return burst(PlantPellet::new);

        return newEntities;
    }
    @Override
    public double getGrowthRate() {
        double growthRate = super.getGrowthRate() * Math.exp(-getCrowdingFactor());
        if (getRadius() > maxRadius)
            growthRate *= getHealth() * maxRadius / (2 * getRadius());
        return growthRate;
    }

    public HashMap<String, Double> getStats() {
        HashMap<String, Double> stats = super.getStats();
        stats.put("Growth Rate", getGrowthRate());
        return stats;
    }

    @Override
    public String getPrettyName() {
        return "Plant";
    }
}
