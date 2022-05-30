package biology;

import core.Simulation;

import java.awt.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MeatPellet extends Pellet {

    public MeatPellet(double radius) {
        super(radius);

        setHealthyColour(new Color(
                150 + Simulation.RANDOM.nextInt(105),
                10  + Simulation.RANDOM.nextInt(100),
                10  + Simulation.RANDOM.nextInt(100)));
        setColor(getHealthyColour());

    }

    @Override
    public Color degradedHealthColour() {
        int r = getHealthyColour().getRed();
        int g = getHealthyColour().getGreen();
        int b = getHealthyColour().getBlue();
        double colourDecay = getColourDecay(0.7);
        double redColourDecay = getColourDecay(0.3);
        return new Color(
                (int) (redColourDecay * r),
                (int) (colourDecay * g),
                (int) (colourDecay * b)
        );
    }

    public void age(double delta) {
        double deathRate = getRadius() * delta * 500;
        setHealth(getHealth() * (1 - deathRate));
    }

    @Override
    public Stream<Entity> update(double delta, Stream<Entity> entities)
    {
//        age(delta);
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
