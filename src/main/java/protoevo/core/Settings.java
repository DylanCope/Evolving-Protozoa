package protoevo.core;

public final class Settings {
    // Simulation settings
    public static final long simulationSeed = 42;
    public static final float simulationUpdateDelta = 5f / 1000f;
    public static final float maxProtozoaSpeed = .01f;
    public static final float maxParticleSpeed = 1e-4f;
    public static final float timeBetweenSaves = 500.0f;
    public static final float historySnapshotTime = 2.0f;
    public static final boolean writeGenomes = false;
    public static final boolean finishOnProtozoaExtinction = true;
    public static final int physicsSubSteps = 3;
    public static final int numPossibleCAMs = 64;
    public static final float camProductionEnergyCost = 0.05f;
    public static final float startingAvailableCellEnergy = 0.01f;
    public static final float foodExtractionWasteMultiplier = 1.5f;
    public static final float cellRepairRate = 0.5f;
    public static final float occludingBindingEnergyTransport = 0.5f;
    public static final float channelBindingEnergyTransport = 0.5f;
    public static final boolean enableAnchoringBinding = false;
    public static final boolean enableOccludingBinding = false;
    public static final boolean enableChannelFormingBinding = true;
    public static final boolean enableSignalRelayBinding = false;

    public static final int maxPlants = 7000;
    public static final int maxProtozoa = 1500;
    public static final int maxMeat = 1000;

    public static final float plantEnergyDensity = 1f;
    public static final float meatEnergyDensity = 10f;

    // Tank settings
    public static final int numInitialProtozoa = 100;
    public static final int numInitialPlantPellets = 1000;
    public static final boolean initialPopulationClustering = true;
    public static final int numRingClusters = 4;
    public static final int numPopulationClusters = 4;
    public static final float populationClusterRadius = 0.3f;
    public static final float tankRadius = 3.0f;
    public static final boolean sphericalTank = false;
    public static final int numChunkBreaks = 100;
    public static final float maxParticleRadius = 0.15f;
    public static final float minParticleRadius = 0.005f;
    public static final float tankFluidResistance = 8e-4f;
    public static final float brownianFactor = 1000f;
    public static final float coefRestitution = 0.005f;
    public static final float maxRockSize = 0.15f;
    public static final float minRockSize = 0.05f;
    public static final float minRockSpikiness = (float) Math.toRadians(45);
    public static final float minRockOpeningSize = 0.08f;
    public static final int rockGenerationIterations = 2000;
    public static final int rockSeedingIterations = 0;
    public static final float rockClustering = 0.99f;

    // Chemical settings
    public static final boolean enableChemicalField = true;
    public static final int numChemicalBreaks = numChunkBreaks * 4;
    public static final float pheromoneUpdateTime = simulationUpdateDelta * 10f;
    public static final float pheromoneDecay = 1f;
    public static final float pheromoneFlow = 0.05f;
    public static final float plantPheromoneDeposit = 50f;

    // Protozoa settings
    public static final float minProtozoanBirthRadius = 0.01f;
    public static final float maxProtozoanBirthRadius = 0.015f;
    public static final float protozoaStarvationFactor = 35f;
    public static final int defaultRetinaSize = 0;
    public static final int maxRetinaSize = 16;
    public static final float retinaCellGrowthCost = .03f;
    public static final int numContactSensors = 0;
    public static final float minRetinaRayAngle = (float) Math.toRadians(10);
    public static final float minHealthToSplit = 0.5f;
    public static final float maxProtozoanSplitRadius = 0.03f;
    public static final float minProtozoanSplitRadius = 0.015f;
    public static final float minProtozoanGrowthRate = .05f;
    public static final float maxProtozoanGrowthRate = .1f;
    public static final int maxTurnAngle = 25;
    public static final float spikeGrowthPenalty = .08f;
    public static final float spikeMovementPenalty = 0.97f;
    public static final float spikePlantConsumptionPenalty = 0.8f;
    public static final float spikeDeathRatePenalty = 1.015f;
    public static final float maxSpikeGrowth = 0.1f;
    public static final float spikeDamage = 3f;
    public static final float matingTime = 0.1f;
    public static final float globalMutationChance = 0.05f;
    public static final float protozoaInteractRange = 0.15f;
    public static final float eatingConversionRatio = 0.75f;

    // Plant Settings
    public static final float minMaxPlantRadius = 0.015f;
    public static final float minPlantSplitRadius = 0.01f;
    public static final float minPlantBirthRadius = 0.005f;
    public static final float maxPlantBirthRadius = 0.03f;

    public static final float minPlantGrowth = 0.01f;
    public static final float plantGrowthRange = 0.02f;
    public static final float plantCrowdingGrowthDecay = 1.0f;
    public static final float plantCriticalCrowding = 6.0f;
    public static final float plantRegen = 2f;

    // Stats

    public static final float statsDistanceScalar = 100.0f;
    public static final float statsTimeScalar = 100.0f;
    public static final float statsMassScalar = 1000f;

    // Rendering

    public static final boolean showFPS = false;
    public static final boolean antiAliasing = true;
}