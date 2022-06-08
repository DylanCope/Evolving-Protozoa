package core;

public final class Settings {
    // Simulation settings
    public static final long simulationSeed = 42;
    public static final float simulationUpdateDelta = 0f;// 5f / 1000f;
    public static final float maxVel = .5f;
    public static final float timeBetweenSaves = 500.0f;
    public static final float historySnapshotTime = 2.0f;
    public static final boolean writeGenomes = false;

    public static final int maxPlants = 5000;

    public static final int maxProtozoa = 3000;

    public static final int maxMeat = 800;

    // Tank settings
    public static final int numInitialProtozoa = 500;
    public static final int numInitialPlantPellets = 500;
    public static final float tankRadius = 2.0f;
    public static final int numChunkBreaks = 20;

    // Protozoa settings
    public static final float minProtozoanBirthRadius = 0.01f;
    public static final float maxProtozoanBirthRadius = 0.015f;
    public static final int defaultRetinaSize = 4;
    public static final float minHealthToSplit = 0.8f;
    public static final float maxProtozoanSplitRadius = 0.03f;
    public static final float minProtozoanSplitRadius = 0.015f;
    public static final float minProtozoanGrowthRate = .1f;
    public static final float maxProtozoanGrowthRate = .3f;
    public static final int maxTurnAngle = 25;

    public static final float globalMutationChance = 0.05f;
    public static final float protozoaInteractRange = 0.15f;

    // Plant Settings
    public static final float maxPlantRadius = 0.01f;
    public static final float minMaxPlantRadius = 0.015f;
    public static final float minPlantSplitRadius = 0.01f;
    public static final float minPlantBirthRadius = 0.005f;
    public static final float maxPlantBirthRadius = 0.03f;
    public static final float minPlantGrowth = 0.1f;
    public static final float plantGrowthRange = 0.3f;
    public static final float plantCrowdingGrowthDecay = 1.0f;
    public static final float plantCriticalCrowding = 6.0f;

    // Stats

    public static final float statsDistanceScalar = 100.0f;
    public static final float statsTimeScalar = 100.0f;
}
