package biology;

import core.Settings;
import core.Simulation;
import core.Tank;
import utils.Vector2;

import java.awt.*;
import java.util.HashMap;

public class PlantPellet extends Pellet {

    private final float maxRadius;

    public PlantPellet(float radius, Tank tank) {
        super(radius, tank);
        setGrowthRate((float) (Settings.minPlantGrowth + Settings.plantGrowthRange * Simulation.RANDOM.nextDouble()));

        float range = Settings.maxPlantBirthRadius - Settings.minMaxPlantRadius;
        maxRadius = (float) (Settings.minMaxPlantRadius + range * Simulation.RANDOM.nextDouble());

        setHealthyColour(new Color(
                30 + Simulation.RANDOM.nextInt(105),
                150  + Simulation.RANDOM.nextInt(100),
                10  + Simulation.RANDOM.nextInt(100))
        );
    }

    @Override
    public void handlePotentialCollision(Entity e, float delta) {
        super.handlePotentialCollision(e, delta);
        if (e != this) {
            Vector2 vecToOther = e.getPos().sub(getPos());
            float distToOther = vecToOther.len();
            if (distToOther - e.getRadius() <= 3*getRadius())
                getPos().translate(vecToOther.scale(delta * 5e-4f/ vecToOther.len()));
        }
    }

    private static float randomPlantRadius() {
        float range = Settings.maxPlantBirthRadius - Settings.minPlantBirthRadius;
        return (float) (Settings.minPlantBirthRadius + range * Simulation.RANDOM.nextDouble());
    }

    public PlantPellet(Tank tank) {
        this(randomPlantRadius(), tank);
    }

    private boolean shouldSplit() {
        return getRadius() > maxRadius &&
                getCrowdingFactor() < Settings.plantCriticalCrowding &&
                getHealth() > Settings.minHealthToSplit &&
                numCollisions < 2;
    }

    @Override
    public void update(float delta) {
        super.update(delta);
        if (getHealth() < 1.0)
            setHealth(getHealth() + Settings.plantRegen * delta * (1 - getHealth()) * getGrowthRate());

        if (shouldSplit())
            burst(PlantPellet.class, r -> new PlantPellet(r, tank));
    }

    /**
     * <a href="https://www.desmos.com/calculator/hmhjwdk0jc">Desmos Graph</a>
     * @return The growth rate based on the crowding and current radius.
     */
    @Override
    public float getGrowthRate() {
        float x = (-getCrowdingFactor() + Settings.plantCriticalCrowding) / Settings.plantCrowdingGrowthDecay;
        x = (float) (Math.tanh(x));// * Math.tanh(-0.01 + 50 * getCrowdingFactor() / Settings.plantCriticalCrowding));
        x = x < 0 ? (float) (1 - Math.exp(-Settings.plantCrowdingGrowthDecay * x)) : x;
        float growthRate = super.getGrowthRate() * x;
        if (getRadius() > maxRadius)
            growthRate *= Math.exp(maxRadius - getRadius());
        growthRate = growthRate > 0 ? growthRate * getHealth() : growthRate;
        return growthRate;
    }

    public HashMap<String, Float> getStats() {
        HashMap<String, Float> stats = super.getStats();
        stats.put("Growth Rate", Settings.statsDistanceScalar * getGrowthRate());
        return stats;
    }

    @Override
    public String getPrettyName() {
        return "Plant";
    }

    public int burstMultiplier() {
        return 200;
    }
}
