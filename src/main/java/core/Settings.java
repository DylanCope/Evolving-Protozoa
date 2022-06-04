package core;

public final class Settings {
    // Simulation settings
    public static final long simulationSeed = 42;
    public static final float simulationUpdateDelta = 5f / 1000f;
    public static final float maxVel = 1.0f;
    public static final float timeBetweenSaves = 500.0f;
    public static final float historySnapshotTime = 2.0f;
    public static final boolean writeGenomes = false;

    // Tank settings
    public static final int numInitialProtozoa = 300;
    public static final int numInitialPlantPellets = 600;
    public static final float tankRadius = 1.0f;
    public static final int numChunkBreaks = 20;

    // Protozoa settings
    public static final float minProtozoanBirthRadius = 0.01f;
    public static final float maxProtozoanBirthRadius = 0.02f;
    public static final int defaultRetinaSize = 8;
    public static final float minHealthToSplit = 0.8f;
    public static final float maxProtozoanSplitRadius = 0.03f;
    public static final float minProtozoanSplitRadius = 0.025f;
    public static final float minProtozoanGrowthRate = 0.03f;
    public static final float maxProtozoanGrowthRate = 0.06f;

    public static final float globalMutationChance = 0.05f;

    // Plant Settings
    public static final float maxPlantRadius = 0.02f;
    public static final float minMaxPlantRadius = 0.015f;
    public static final float minPlantSplitRadius = 0.01f;
    public static final float minPlantBirthRadius = 0.005f;
    public static final float maxPlantBirthRadius = 0.01f;
    public static final float minPlantGrowth = 0.01f;
    public static final float plantGrowthRange = 0.02f;
    public static final float plantCrowdingGrowthDecay = 1.0f;
    public static final float plantCriticalCrowding = 4.0f;

    // Stats

    public static final float statsDistanceScalar = 100.0f;
    public static final float statsTimeScalar = 100.0f;
}
