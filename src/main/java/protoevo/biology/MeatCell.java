package protoevo.biology;

import protoevo.core.Simulation;
import protoevo.env.Tank;

import java.awt.*;

public class MeatCell extends EdibleCell {

    public static final long serialVersionUID = -5549426815144079228L;

    private float rotteness = 0.0f;

    public MeatCell(float radius, Tank tank) {
        super(radius, Food.Type.Meat, tank);

        int r = 150 + Simulation.RANDOM.nextInt(105);
        int g = 25  + Simulation.RANDOM.nextInt(100);
        int b = 25  + Simulation.RANDOM.nextInt(100);
        setHealthyColour(new Color(r, g, b));
        setDegradedColour(new Color(158, 121, 79));
    }

    public void age(float delta) {
        float deathRate = getRadius() * delta * 100;
        setHealth(getHealth() * (1 - deathRate));
        rotteness = rotteness * (1 - deathRate);
        if (rotteness > 1)
            rotteness = 1;
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
    public boolean cannotMakeBinding() {
        return true;
    }
}
