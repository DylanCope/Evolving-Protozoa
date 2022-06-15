package biology;

import core.ChunkManager;
import core.Settings;
import core.Simulation;
import core.Tank;
import utils.Vector2;

import java.awt.*;
import java.util.HashMap;
import java.util.Iterator;

public class PlantPellet extends Pellet {

    private final float maxRadius;
    private float crowdingFactor;
    private final float plantAttractionFactor;

    public PlantPellet(float radius, Tank tank) {
        super(radius, tank);
        setGrowthRate((float) (Settings.minPlantGrowth + Settings.plantGrowthRange * Simulation.RANDOM.nextDouble()));

        float range = Settings.maxPlantBirthRadius - radius;
        maxRadius = (float) (radius + range * Simulation.RANDOM.nextDouble());

        setHealthyColour(new Color(
                30 + Simulation.RANDOM.nextInt(105),
                150  + Simulation.RANDOM.nextInt(100),
                10  + Simulation.RANDOM.nextInt(100))
        );

//        float range = Settings.maxPlantBirthRadius - Settings.minMaxPlantRadius;
//        maxRadius = (float) (1e-3 + range * Simulation.RANDOM.nextDouble());
        plantAttractionFactor = 1e-4f;
    }

    @Override
    public float handlePotentialCollision(Entity e, float delta) {
        float sqDist = super.handlePotentialCollision(e, delta);
//        if (e != this) {
//            Vector2 vecToOther = e.getPos().sub(getPos());
//            float distToOther = vecToOther.len();
//            if (distToOther - e.getRadius() <= 3*getRadius())
//                getPos().translate(vecToOther.scale(delta * 5e-4f/ vecToOther.len()));
//        }
        if (e != this && e instanceof PlantPellet) {
            Vector2 acc = e.getPos().sub(getPos()).setLength(plantAttractionFactor / sqDist);
            accelerate(acc);
        }
        return sqDist;
    }

    private static float randomPlantRadius() {
        float range = Settings.maxPlantBirthRadius - Settings.minPlantBirthRadius;
        return Settings.minPlantBirthRadius + range * Simulation.RANDOM.nextFloat();
    }

    public PlantPellet(Tank tank) {
        this(randomPlantRadius(), tank);
    }

    private boolean shouldSplit() {
        return getRadius() > maxRadius &&
//                getCrowdingFactor() < Settings.plantCriticalCrowding &&
                getHealth() > Settings.minHealthToSplit;
    }

    @Override
    public float getRadius() {
        float r = (0.3f + 0.7f * getHealth()) * super.getRadius();
        return Math.max(r, Settings.minEntityRadius);
    }

    public float getCrowdingFactor() {
        return crowdingFactor;
    }

    private void updateCrowding(Entity e) {
        float sqDist = e.getPos().squareDistanceTo(getPos());
        if (sqDist < Math.pow(3 * getRadius(), 2)) {
            crowdingFactor += e.getRadius() / (getRadius() + sqDist);
        }
    }

    @Override
    public void update(float delta) {
        super.update(delta);

        crowdingFactor = 0;
        ChunkManager chunkManager = tank.getChunkManager();
        Iterator<Entity> entities = chunkManager.broadEntityDetection(getPos(), getRadius());
        entities.forEachRemaining(this::updateCrowding);

        setHealth(getHealth() + Settings.plantRegen * delta * getGrowthRate());

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
        stats.put("Crowding Factor", crowdingFactor);
        stats.put("Split Radius", Settings.statsDistanceScalar * maxRadius);
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
