package biology;

import core.Simulation;
import core.Tank;

import java.awt.*;

public class MeatPellet extends Pellet {

    public MeatPellet(float radius, Tank tank) {
        super(radius, tank);

        int r = 150 + Simulation.RANDOM.nextInt(105);
        int g = 25  + Simulation.RANDOM.nextInt(100);
        int b = 25  + Simulation.RANDOM.nextInt(100);
        setHealthyColour(new Color(r, g, b));
        setDegradedColour(new Color(158, 121, 79));
    }

    public void age(float delta) {
        float deathRate = getRadius() * delta * 100;
        setHealth(getHealth() * (1 - deathRate));
    }

    @Override
    public void update(float delta)
    {
        age(delta);
        super.update(delta);
    }

    @Override
    public String getPrettyName() {
        return "Meat";
    }

    @Override
    public float getNutrition() {
        return super.getNutrition() * (10 * getHealth() - 2);
    }
}
