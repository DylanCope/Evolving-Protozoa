package core;

public final class Settings {
    // Simulation settings
    public static final double simulationUpdateDelta = 5 * 5.0 / 1000.0;
    public static final double maxVel = 1.0;
    public static final double timeBetweenSaves = 100.0;

    // Tank settings
    public static final int numInitialProtozoa = 300;
    public static final int numInitialPlantPellets = 1000;
    public static final double tankRadius = 1.0;
    public static final int numChunkBreaks = 20;

    // Protozoa settings
    public static final double minProtozoanBirthRadius = 0.01;
    public static final double maxProtozoanBirthRadius = 0.02;
    public static final int defaultRetinaSize = 8;
    public static final double minHealthToSplit = 0.8;
    public static final double maxProtozoanSplitRadius = 0.03;
    public static final double minProtozoanSplitRadius = 0.025;
    public static final double minProtozoanGrowthRate = 0.03;
    public static final double maxProtozoanGrowthRate = 0.06;

    public static final double globalMutationChance = 0.05;

    // Plant Settings
    public static final double maxPlantRadius = 0.02;
    public static final double minMaxPlantRadius = 0.015;
    public static final double minPlantSplitRadius = 0.01;
    public static final double minPlantBirthRadius = 0.01;
    public static final double maxPlantBirthRadius = 0.02;
    public static final double minPlantGrowth = 0.01;
    public static final double plantGrowthRange = 0.04;

    // Stats

    public static final double statsDistanceScalar = 100.0;
    public static final double statsTimeScalar = 100.0;
}
