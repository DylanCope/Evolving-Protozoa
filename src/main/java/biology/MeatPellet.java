package biology;

import core.Simulation;

import java.awt.*;
import java.util.stream.Stream;

public class MeatPellet extends Pellet {

    public MeatPellet(double radius) {
        super(radius);

        int r = 150 + Simulation.RANDOM.nextInt(105);
        int g = 25  + Simulation.RANDOM.nextInt(100);
        int b = 25  + Simulation.RANDOM.nextInt(100);
        setHealthyColour(new Color(r, g, b));
        setDegradedColour(new Color(158, 121, 79));
    }

    public void age(double delta) {
        double deathRate = getRadius() * delta * 100;
        setHealth(getHealth() * (1 - deathRate));
    }

    @Override
    public Stream<Entity> update(double delta, Stream<Entity> entities)
    {
        age(delta);
        return super.update(delta, entities);
    }

    @Override
    public String getPrettyName() {
        return "Meat";
    }

    @Override
    public double getNutrition() {
        return super.getNutrition() * (3 * getHealth() - 1);
    }
}
