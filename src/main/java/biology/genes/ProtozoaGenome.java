package biology.genes;

import biology.*;
import core.Settings;
import core.Simulation;
import core.Tank;
import neat.NetworkGenome;
import neat.SynapseGene;

import java.awt.*;
import java.io.Serializable;
import java.util.stream.Stream;

/**
 * Created by dylan on 28/05/2017.
 */
public class ProtozoaGenome implements Serializable
{
    public static final long serialVersionUID = 2421454107847378624L;
    private int numMutations = 0;
    private final Gene<?>[] genes;
    private float mutationChance = Settings.globalMutationChance;
    public static final int actionSpaceSize = 2;
    public static final int nonVisualSensorSize = 4;

    public ProtozoaGenome(ProtozoaGenome parentGenome) {
        mutationChance = parentGenome.mutationChance;
        numMutations = parentGenome.numMutations;
        genes = parentGenome.genes;
    }

    public ProtozoaGenome()
    {
        int numInputs = 3 * Settings.defaultRetinaSize + nonVisualSensorSize + 1;
        NetworkGenome networkGenome = new NetworkGenome(numInputs, actionSpaceSize);
        genes = new Gene<?>[]{
                new NetworkGene(networkGenome),
                new ProtozoaColorGene(),
                new RetinaSizeGene(),
                new ProtozoaFOVGene(),
                new ProtozoaGrowthRateGene(),
                new ProtozoaMaxTurnGene(),
                new ProtozoaRadiusGene(),
                new ProtozoaSpikesGene(),
                new ProtozoaSplitRadiusGene(),
        };
    }

    public ProtozoaGenome(Gene<?>[] genes)
    {
        this.genes = genes;
    }

    public ProtozoaGenome mutate() {
        for (int i = 0; i < genes.length; i++) {
            if (Simulation.RANDOM.nextDouble() < Settings.globalMutationChance) {
                genes[i] = genes[i].mutate(genes);
                numMutations++;
            }
        }
        return this;
    }

    private <T> T getGeneValue(Class<? extends Gene<T>> clazz) {
        T val = null;
        for (Gene<?> gene : genes) {
            if (clazz.isInstance(gene))
                val = clazz.cast(gene).getValue();
        }
        return val;
    }

    public Brain brain() throws MiscarriageException {
        float maxTurn = getMaxTurn();
        NetworkGenome networkGenome = getGeneValue(NetworkGene.class);
        try {
            return new NNBrain(networkGenome.phenotype(), maxTurn);
        } catch (IllegalArgumentException e) {
            throw new MiscarriageException();
        }
    }

    public Retina retina()
    {
        int retinaSize = getGeneValue(RetinaSizeGene.class);
        float fov = getGeneValue(ProtozoaFOVGene.class);
        return new Retina(retinaSize, fov);
    }

    private float getFloatGeneValue(Class<? extends Gene<Float>> clazz) {
        return getGeneValue(clazz);
    }

    public float getRadius()
    {
        return getFloatGeneValue(ProtozoaRadiusGene.class);
    }

    public float getGrowthRate() {
        return getFloatGeneValue(ProtozoaGrowthRateGene.class);
    }

    public float getSplitRadius() {
        return getFloatGeneValue(ProtozoaSplitRadiusGene.class);
    }


    public Protozoa phenotype(Tank tank) throws MiscarriageException
    {
        return new Protozoa(this, tank);
    }

//    public Stream<ProtozoaGenome> crossover(ProtozoaGenome other) {
//        NetworkGenome childNetGenome = networkGenome.crossover(other.networkGenome);
//        int childRetinaSize = Simulation.RANDOM.nextBoolean() ? retinaSize : other.retinaSize;
//        float childRadius = Simulation.RANDOM.nextBoolean() ? radius : other.radius;
//        float childSplitSize = Simulation.RANDOM.nextBoolean() ? splitSize : other.splitSize;
//        float childGrowthRate = Simulation.RANDOM.nextBoolean() ? growthRate : other.growthRate;
//        Color childColour = Simulation.RANDOM.nextBoolean() ? colour : other.colour;
//        float childMaxTurn = Simulation.RANDOM.nextBoolean() ? maxTurn : other.maxTurn;
//        return Stream.of(new ProtozoaGenome(childNetGenome, childMaxTurn
//        ));
//    }
//
//    public Stream<ProtozoaGenome> reproduce(ProtozoaGenome other)
//    {
//        return crossover(other).map(ProtozoaGenome::mutate);
//    }

    public Protozoa createChild(Tank tank) throws MiscarriageException {
        ProtozoaGenome childGenome = new ProtozoaGenome(this);
        return childGenome.mutate().phenotype(tank);
    }
    public Color getColour() {
        return getGeneValue(ProtozoaColorGene.class);
    }

    public int getNumMutations() {
        return numMutations + getGeneValue(NetworkGene.class).getNumMutations();
    }

    public Protozoa.Spike[] getSpikes() {
        return getGeneValue(ProtozoaSpikesGene.class);
    }

    public float getMaxTurn() {
        return getFloatGeneValue(ProtozoaMaxTurnGene.class);
    }
}
