package biology;

import core.Simulation;
import utils.Vector2;

import java.awt.*;
import java.util.Collection;
import java.util.HashMap;
import java.util.Random;
import java.util.stream.Stream;

public class PlantPellet extends Pellet {

    private static double maxRadius = 0.02;
    private static final double minMaxRadius = 0.015;
    private static final double minSplitRadius = 0.01;
    private double growthRate;

    public PlantPellet(double radius) {
        super(radius);
        growthRate = 0.01 + 0.04 * Simulation.RANDOM.nextDouble();
        maxRadius = minMaxRadius + (maxRadius - minMaxRadius) * Simulation.RANDOM.nextDouble();

        setHealthyColour(new Color(
                30 + Simulation.RANDOM.nextInt(105),
                150  + Simulation.RANDOM.nextInt(100),
                10  + Simulation.RANDOM.nextInt(100))
        );
    }

    private Stream<Entity> splitPellet() {
        setDead(true);
        return burst(PlantPellet::new);
    }

    @Override
    public Stream<Entity> update(double delta, Stream<Entity> entities) {
        Stream<Entity> newEntities = super.update(delta, entities);
        setRadius(getRadius() * (1 + getGrowthRate() * delta));
        if (getRadius() > maxRadius & getRadius() > minSplitRadius & getCrowdingFactor() < 4)
            return Stream.concat(newEntities, splitPellet());
        return newEntities;
    }

    private double getGrowthRate() {
        return growthRate * Math.exp(-getCrowdingFactor());
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
