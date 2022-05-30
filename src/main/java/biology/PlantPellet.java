package biology;

import core.Simulation;
import utils.Vector2;

import java.awt.*;
import java.util.Random;
import java.util.stream.Stream;

public class PlantPellet extends Pellet {

    private static final double splitRadiusFactor = 1.2;
    private static final double minSplitRadius = 0.01;
    private double initialRadius;
    private double growthRate;

    private int plantPelletsNearby = 0;

    public PlantPellet(double radius) {
        super(radius);
        initialRadius = getRadius();
        growthRate = 0.3;

        setHealthyColour(new Color(
                30 + Simulation.RANDOM.nextInt(105),
                150  + Simulation.RANDOM.nextInt(100),
                10  + Simulation.RANDOM.nextInt(100)));
        setColor(getHealthyColour());
    }

    private Stream<Entity> splitPellet() {
        setDead(true);
        Random random = new Random();
        Vector2 dir = new Vector2(2*random.nextDouble() - 1, 2*random.nextDouble() - 1);
        Pellet pellet1 = new PlantPellet(getRadius() / 2);
        Pellet pellet2 = new PlantPellet(getRadius() / 2);
        pellet1.setPos(getPos().add(dir.mul(getRadius())));
        pellet2.setPos(getPos().add(dir.mul(-getRadius())));
        return Stream.of(pellet1, pellet2);
    }

    @Override
    public Stream<Entity> update(double delta, Stream<Entity> entities) {
        setRadius(getRadius() * (1 + growthRate * delta));
        if (getRadius() > initialRadius * splitRadiusFactor & getRadius() > minSplitRadius)
            return splitPellet();
        return super.update(delta, entities);
    }

    @Override
    public String getPrettyName() {
        return "Plant";
    }
}
