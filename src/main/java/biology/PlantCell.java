package biology;

import core.ChunkManager;
import core.Particle;
import core.Settings;
import core.Simulation;
import env.Tank;
import utils.Vector2;

import java.awt.*;
import java.util.Iterator;
import java.util.Map;

public class PlantCell extends EdibleCell {
    public static final long serialVersionUID = -3975433688803760076L;

    private final float maxRadius;
    private float crowdingFactor;
    private final float plantAttractionFactor;

    public PlantCell(float radius, Tank tank) {
        super(radius, Food.Type.Plant, tank);
        setGrowthRate((float) (Settings.minPlantGrowth + Settings.plantGrowthRange * Simulation.RANDOM.nextDouble()));

        float range = Settings.maxPlantBirthRadius - radius;
        maxRadius = (float) (radius + range * Simulation.RANDOM.nextDouble());

        setHealthyColour(new Color(
                30 + Simulation.RANDOM.nextInt(105),
                150  + Simulation.RANDOM.nextInt(100),
                10  + Simulation.RANDOM.nextInt(100))
        );
        plantAttractionFactor = 5e-8f;
    }

    @Override
    public boolean handlePotentialCollision(Particle p, float delta) {
        boolean collision = super.handlePotentialCollision(p, delta);
        if (p != this && p instanceof PlantCell) {
            PlantCell otherPlant = (PlantCell) p;
            float sqDist = otherPlant.getPos().sub(getPos()).len2();
            float r = getRadius() + otherPlant.getRadius();
            if (sqDist > 1.01f*r*r && !isAttached(otherPlant)) {
                Vector2 f = p.getPos().sub(getPos()).setLength(plantAttractionFactor / sqDist);
                accelerate(f.mul(1 / getMass()));
            }
        }
        return collision;
    }

    private static float randomPlantRadius() {
        float range = Settings.maxPlantBirthRadius - Settings.minPlantBirthRadius;
        return Settings.minPlantBirthRadius + range * Simulation.RANDOM.nextFloat();
    }

    public PlantCell(Tank tank) {
        this(randomPlantRadius(), tank);
    }

    private boolean shouldSplit() {
        return getRadius() > maxRadius &&
                getHealth() > Settings.minHealthToSplit;
    }

    public float getCrowdingFactor() {
        return crowdingFactor;
    }

    private void updateCrowding(Cell e) {
        float sqDist = e.getPos().squareDistanceTo(getPos());
        if (sqDist < Math.pow(3 * getRadius(), 2)) {
            crowdingFactor += e.getRadius() / (getRadius() + sqDist);
        }
    }

    @Override
    public void update(float delta) {
        super.update(delta);

        if (isDead())
            return;

        crowdingFactor = 0;
        ChunkManager chunkManager = getTank().getChunkManager();
        Iterator<Cell> entities = chunkManager.broadEntityDetection(getPos(), getRadius());
        entities.forEachRemaining(this::updateCrowding);

        if (getGrowthRate() < 0f)
            setHealth(getHealth() + Settings.plantRegen * delta * getGrowthRate());

        addConstructionMass(delta);
        addAvailableEnergy(delta / 3f);

        if (shouldSplit())
            burst(PlantCell.class, r -> new PlantCell(r, getTank()));
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

    public Map<String, Float> getStats() {
        Map<String, Float> stats = super.getStats();
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
