package core;

public final class Settings {
    // Simulation settings
    public static final long simulationSeed = 42;
    public static final float simulationUpdateDelta = 5f / 1000f;
    public static final float maxSpeed = .2f;
    public static final float timeBetweenSaves = 500.0f;
    public static final float historySnapshotTime = 2.0f;
    public static final boolean writeGenomes = false;
    public static final boolean finishOnProtozoaExtinction = false;

    public static final int maxPlants = 5000;
    public static final int maxProtozoa = 10000;
    public static final int maxMeat = 1000;

    // Tank settings
    public static final int numInitialProtozoa = 500;
    public static final int numInitialPlantPellets = 2000;
    public static final float tankRadius = 2.0f;
    public static final int numChunkBreaks = 20;
    public static final float maxEntityRadius = 0.15f;
    public static final float tankViscosity = 2f; // viscosity of water in Pascal seconds

    // Protozoa settings
    public static final float minProtozoanBirthRadius = 0.01f;
    public static final float maxProtozoanBirthRadius = 0.015f;
    public static final float protozoaStarvationFactor = 40f;
    public static final int defaultRetinaSize = 0;
    public static final float minHealthToSplit = 0.8f;
    public static final float maxProtozoanSplitRadius = 0.03f;
    public static final float minProtozoanSplitRadius = 0.015f;
    public static final float minProtozoanGrowthRate = .05f;
    public static final float maxProtozoanGrowthRate = .2f;
    public static final int maxTurnAngle = 25;
    public static final float spikeGrowthPenalty = .1f;
    public static final float spikeDamage = 3f;

    public static final float globalMutationChance = 0.05f;
    public static final float protozoaInteractRange = 0.15f;
    public static final float eatingConversionRation = 0.75f;

    // Plant Settings
    public static final float minMaxPlantRadius = 0.015f;
    public static final float minPlantSplitRadius = 0.01f;
    public static final float minPlantBirthRadius = 0.005f;
    public static final float maxPlantBirthRadius = 0.03f;

    public static final float minPlantGrowth = 0.01f;
    public static final float plantGrowthRange = 0.05f;
    public static final float plantCrowdingGrowthDecay = 1.0f;
    public static final float plantCriticalCrowding = 6.0f;
    public static final float plantRegen = 2f;

    // Stats

    public static final float statsDistanceScalar = 100.0f;
    public static final float statsTimeScalar = 100.0f;
}
