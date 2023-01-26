package protoevo.core;

import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

public final class Settings {

    private static Settings loadSettingsYAML() {
        InputStream inputStream;
        try {
            System.out.println("Loading settings from " + Simulation.settingsPath);
            inputStream = new FileInputStream(Simulation.settingsPath);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        Yaml yaml = new Yaml(new Constructor(Settings.class));
        return yaml.load(inputStream);
    }

    private static Settings instance = null;

    public static Settings getInstance() {
        if (instance == null) {
            instance = loadSettingsYAML();
        }
        return instance;
    }

    private long simulation_seed;

    // World parameters
    private float tank_radius;
    private float fluid_resistance_multiplier;
    private int num_rock_ring_clusters;
    private float max_rock_size;
    private float min_rock_size;
    private float rock_clustering;
    private float min_rock_opening_size;

    // Initial Conditions
    private int num_initial_protozoa;
    private int num_initial_plants;
    private int num_initial_pop_centres;
    private float pop_cluster_radius;

    // Simulation parameters
    private float global_mutation_chance;
    private float plant_energy_density;
    private float meat_energy_density;
    private boolean enable_chemical_field;
    private float plant_regen;
    private float spike_damage;
    private float spike_plant_consumption_penalty;
    private float max_particle_radius;
    private float chemicals_decay;

    private float chemicals_flow;
    private float pheromones_deposit;
    private float protozoa_starvation_rate;
    private int starting_retina_size;
    private int max_retina_size;
    private float retina_grow_cost;
    private float min_health_to_split;
    private float max_protozoa_growth_rate;
    private float max_plant_growth;
    private float retina_growth_cost;
    private float cell_repair_rate;
    private float food_waste_multiplier;
    private float cam_energy_cost;


    // Performance parameters
    private int physics_substeps;
    private int spatial_hash_resolution;
    private int chemical_field_resolution;
    private int chemical_update_interval;
    private float max_interact_range;
    private int max_protozoa;
    private int max_plants;
    private int max_meat;

    // Simulation settings
    public static final long simulationSeed = getInstance().simulation_seed == 0 ? System.currentTimeMillis() : getInstance().simulation_seed;
    public static final float simulationUpdateDelta = 5f / 1000f;
    public static final float maxProtozoaSpeed = .01f;
    public static final float maxParticleSpeed = 1e-4f;
    public static final float timeBetweenSaves = 2000.0f;
    public static final float historySnapshotTime = 2.0f;
    public static final boolean writeGenomes = true;
    public static final boolean finishOnProtozoaExtinction = true;
    public static final int physicsSubSteps = getInstance().physics_substeps;
    public static final int numPossibleCAMs = 64;
    public static final float camProductionEnergyCost = getInstance().cam_energy_cost;
    public static final float startingAvailableCellEnergy = 0.01f;
    public static final float foodExtractionWasteMultiplier = getInstance().food_waste_multiplier;
    public static final float cellRepairRate = getInstance().cell_repair_rate;
    public static final float occludingBindingEnergyTransport = 0.5f;
    public static final float channelBindingEnergyTransport = 0.5f;
    public static final boolean enableAnchoringBinding = false;
    public static final boolean enableOccludingBinding = false;
    public static final boolean enableChannelFormingBinding = true;
    public static final boolean enableSignalRelayBinding = false;

    public static final int maxPlants = getInstance().max_plants;
    public static final int maxProtozoa = getInstance().max_protozoa;
    public static final int maxMeat = getInstance().max_meat;

    public static final float plantEnergyDensity = getInstance().plant_energy_density;
    public static final float meatEnergyDensity = getInstance().meat_energy_density;

    // Tank settings
    public static final int numInitialProtozoa = getInstance().num_initial_protozoa;
    public static final int numInitialPlantPellets = getInstance().num_initial_plants;
    public static final boolean initialPopulationClustering = true;
    public static final int numRingClusters = getInstance().num_rock_ring_clusters;
    public static final int numPopulationClusters = getInstance().num_initial_pop_centres;
    public static final float populationClusterRadius = getInstance().pop_cluster_radius;
    public static final float populationClusterRadiusRange = 0.f;
    public static final float tankRadius = getInstance().tank_radius;
    public static final boolean sphericalTank = false;
    public static final int numChunkBreaks = getInstance().spatial_hash_resolution;
    public static final float maxParticleRadius = getInstance().max_particle_radius;
    public static final float minParticleRadius = 0.005f;
    public static final float tankFluidResistance = 8e-4f * getInstance().fluid_resistance_multiplier;
    public static final float brownianFactor = 1000f;
    public static final float coefRestitution = 0.005f;
    public static final float maxRockSize = getInstance().max_rock_size;
    public static final float minRockSize = getInstance().min_rock_size;
    public static final float minRockSpikiness = (float) Math.toRadians(45);
    public static final float minRockOpeningSize = getInstance().min_rock_opening_size;
    public static final int rockGenerationIterations = 2000;
    public static final int rockSeedingIterations = 0;
    public static final float rockClustering = getInstance().rock_clustering;

    // Chemical settings
    public static final boolean enableChemicalField = getInstance().enable_chemical_field;
    public static final int numChemicalBreaks = getInstance().chemical_field_resolution;
    public static final float chemicalsUpdateTime = simulationUpdateDelta * getInstance().chemical_update_interval;
    public static final float chemicalsDecay = getInstance().chemicals_decay;
    public static final float chemicalsFlow = getInstance().chemicals_flow;
    public static final float plantPheromoneDeposit = getInstance().pheromones_deposit;

    // Protozoa settings
    public static final float minProtozoanBirthRadius = 0.01f;
    public static final float maxProtozoanBirthRadius = 0.015f;
    public static final float protozoaStarvationFactor = getInstance().protozoa_starvation_rate;
    public static final int defaultRetinaSize = 10; //getInstance().starting_retina_size;
    public static final int maxRetinaSize = getInstance().max_retina_size;
    public static final float retinaCellGrowthCost = getInstance().retina_growth_cost;
    public static final int numContactSensors = 0;
    public static final float minRetinaRayAngle = (float) Math.toRadians(10);
    public static final float minHealthToSplit = getInstance().min_health_to_split;
    public static final float maxProtozoanSplitRadius = 0.03f;
    public static final float minProtozoanSplitRadius = 0.015f;
    public static final float minProtozoanGrowthRate = .05f;
    public static final float maxProtozoanGrowthRate = getInstance().max_protozoa_growth_rate;
    public static final int maxTurnAngle = 25;
    public static final float spikeGrowthPenalty = .08f;
    public static final float spikeMovementPenaltyFactor = 0.97f;
    public static final float spikePlantConsumptionPenalty = getInstance().spike_plant_consumption_penalty;
    public static final float spikeDeathRatePenalty = 1.015f;
    public static final float maxSpikeGrowth = 0.1f;
    public static final float spikeDamage = getInstance().spike_damage;
    public static final float matingTime = 0.1f;
    public static final float globalMutationChance = getInstance().global_mutation_chance;
    public static final float protozoaInteractRange = getInstance().max_interact_range;
    public static final float eatingConversionRatio = 0.75f;

    // Plant Settings
    public static final float minMaxPlantRadius = 0.015f;
    public static final float minPlantSplitRadius = 0.01f;
    public static final float minPlantBirthRadius = 0.005f;
    public static final float maxPlantBirthRadius = 0.03f;

    public static final float minPlantGrowth = 0.01f;
    public static final float plantGrowthRange = getInstance().max_plant_growth - minPlantGrowth;
    public static final float plantCrowdingGrowthDecay = 1.0f;
    public static final float plantCriticalCrowding = 6.0f;
    public static final float plantRegen = getInstance().plant_regen;

    // Stats

    public static final float statsDistanceScalar = 100.0f;
    public static final float statsTimeScalar = 100.0f;
    public static final float statsMassScalar = 1000f;

    // Rendering

    public static final boolean showFPS = false;
    public static final boolean antiAliasing = true;
}
