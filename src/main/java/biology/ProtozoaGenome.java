package biology;

import core.Settings;
import core.Simulation;
import core.Tank;
import neat.NetworkGenome;
import neat.NeuronGene;
import neat.SynapseGene;

import java.awt.*;
import java.io.Serializable;
import java.util.stream.Stream;

/**
 * Created by dylan on 28/05/2017.
 */
public class ProtozoaGenome implements Serializable
{
    private final NetworkGenome networkGenome;
    private final int retinaSize;
    private float radius;
    private float growthRate;
    private float splitSize;
    private Color colour;
    private float mutationChance = Settings.globalMutationChance;
    private int numMutations = 0;

    public static final int actionSpaceSize = 4;
    public static final int nonVisualSensorSize = 1;

    public ProtozoaGenome(ProtozoaGenome parentGenome) {
        retinaSize = parentGenome.retinaSize;
        radius = parentGenome.radius;
        networkGenome = new NetworkGenome(parentGenome.networkGenome);
        mutationChance = parentGenome.mutationChance;
        splitSize = parentGenome.splitSize;
        growthRate = parentGenome.growthRate;
        colour = parentGenome.colour;
        numMutations = parentGenome.numMutations;
    }

    public ProtozoaGenome()
    {
        retinaSize = Settings.defaultRetinaSize;
        radius = randomProtozoanRadius();
        splitSize = randomSplitSize();
        growthRate = randomGrowthRate();
        colour = randomProtozoaColour();
        int numInputs = 3 * retinaSize + nonVisualSensorSize;
        networkGenome = new NetworkGenome(numInputs, actionSpaceSize);
    }

    public ProtozoaGenome(int retinaSize,
                          float radius,
                          float growthRate,
                          float splitSize,
                          Color colour,
                          NetworkGenome networkGenome)
    {
        this.retinaSize = retinaSize;
        this.radius = radius;
        this.networkGenome = networkGenome;
        this.growthRate = growthRate;
        this.splitSize = splitSize;
        this.colour = colour;
    }

    private static float randomProtozoanRadius() {
        float range = Settings.maxProtozoanBirthRadius - Settings.minProtozoanBirthRadius;
        return (float) (Settings.minProtozoanBirthRadius + range * Simulation.RANDOM.nextDouble());
    }

    private static float randomSplitSize() {
        float range = Settings.maxProtozoanSplitRadius - Settings.minProtozoanSplitRadius;
        return (float) (Settings.minProtozoanSplitRadius + range * Simulation.RANDOM.nextDouble());
    }

    private static float randomGrowthRate() {
        float range = Settings.maxProtozoanGrowthRate - Settings.minProtozoanGrowthRate;
        return (float) (Settings.minProtozoanGrowthRate + range * Simulation.RANDOM.nextDouble());
    }

    public ProtozoaGenome mutate() {
        networkGenome.mutate();
        if (Simulation.RANDOM.nextDouble() < Settings.globalMutationChance) {
            radius = randomProtozoanRadius();
            numMutations++;
        }
        if (Simulation.RANDOM.nextDouble() < Settings.globalMutationChance) {
            growthRate = randomGrowthRate();
            numMutations++;
        }
        if (Simulation.RANDOM.nextDouble() < Settings.globalMutationChance) {
            splitSize = randomSplitSize();
            numMutations++;
        }
        if (Simulation.RANDOM.nextDouble() < Settings.globalMutationChance) {
            colour = randomProtozoaColour();
            numMutations++;
        }
        return this;
    }

    public static Color randomProtozoaColour() {
        return new Color(
                80 + Simulation.RANDOM.nextInt(150),
                80 + Simulation.RANDOM.nextInt(150),
                80  + Simulation.RANDOM.nextInt(150)
        );
    }

    public Brain brain()
    {
        return new NNBrain(networkGenome.phenotype());
    }

    public Retina retina()
    {
        return new Retina(this.retinaSize);
    }

    public float getRadius()
    {
        return radius;
    }

    public float getGrowthRate() {
        return growthRate;
    }

    public float getSplitRadius() {
        return splitSize;
    }


    public Protozoa phenotype(Tank tank)
    {
        return new Protozoa(this, tank);
    }

    public Stream<ProtozoaGenome> crossover(ProtozoaGenome other) {
        NetworkGenome childNetGenome = networkGenome.crossover(other.networkGenome);
        int childRetinaSize = Simulation.RANDOM.nextBoolean() ? retinaSize : other.retinaSize;
        float childRadius = Simulation.RANDOM.nextBoolean() ? radius : other.radius;
        float childSplitSize = Simulation.RANDOM.nextBoolean() ? splitSize : other.splitSize;
        float childGrowthRate = Simulation.RANDOM.nextBoolean() ? growthRate : other.growthRate;
        Color childColour = Simulation.RANDOM.nextBoolean() ? colour : other.colour;
        return Stream.of(new ProtozoaGenome(
                childRetinaSize, childRadius, childGrowthRate, childSplitSize, childColour, childNetGenome
        ));
    }

    public Stream<ProtozoaGenome> reproduce(ProtozoaGenome other)
    {
        return crossover(other).map(ProtozoaGenome::mutate);
    }

    public Protozoa createChild(Tank tank)
    {
        ProtozoaGenome childGenome = new ProtozoaGenome(this);
        return childGenome.mutate().phenotype(tank);
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        s.append("retinaSize=").append(retinaSize).append(",");
        s.append("radius=").append(radius).append(",");
        s.append("growthRate=").append(growthRate).append(",");
        s.append("splitSize=").append(splitSize).append(",");
        for (SynapseGene[] geneRow : networkGenome.getSynapseGenes())
            for (SynapseGene gene : geneRow)
                s.append(gene.toString()).append(",");
        for (NeuronGene gene : networkGenome.getNeuronGenes())
            s.append(gene.toString()).append(",");
        return s.toString();
    }

    public Color getColour() {
        return colour;
    }

    public int getNumMutations() {
        return numMutations + networkGenome.getNumMutations();
    }
}
