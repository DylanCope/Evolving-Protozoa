package biology.genes;

import biology.*;
import core.Settings;
import core.Simulation;
import env.Tank;
import neat.NetworkGenome;
import neat.NeuralNetwork;

import java.awt.*;
import java.io.Serializable;
import java.util.Arrays;

/**
 * Created by dylan on 28/05/2017.
 */
public class ProtozoaGenome implements Serializable
{
    public static final long serialVersionUID = 2421454107847378624L;
    private final Gene<?>[] genes;
    private float mutationChance = Settings.globalMutationChance;
    public static final int actionSpaceSize = 3;
    public static final int nonVisualSensorSize = 4;

    public ProtozoaGenome(ProtozoaGenome parentGenome) {
        mutationChance = parentGenome.mutationChance;
        genes = Arrays.copyOf(parentGenome.genes, parentGenome.genes.length);
    }

    public ProtozoaGenome()
    {
        int numInputs = expectedNetworkInputSize(Settings.defaultRetinaSize);

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
                new HerbivoreFactorGene(),
        };
    }

    public static int expectedNetworkInputSize(int retinaSize) {
        return 3 * retinaSize
                + nonVisualSensorSize
                + 1 + Settings.numContactSensors;
    }

    public ProtozoaGenome(Gene<?>[] genes) {
        this.genes = genes;
        NetworkGenome networkGenome = getGeneValue(NetworkGene.class);
        int retinaSize = getGeneValue(RetinaSizeGene.class);
        while (networkGenome.numberOfSensors() < expectedNetworkInputSize(retinaSize))
            networkGenome.addSensor();
    }

    public ProtozoaGenome mutate() {
        Gene<?>[] newGenes = Arrays.copyOf(genes, genes.length);
        for (int i = 0; i < genes.length; i++) {
            if (Simulation.RANDOM.nextDouble() < Settings.globalMutationChance) {
                newGenes[i] = genes[i].mutate(newGenes);
            } else {
                newGenes[i] = genes[i];
            }
        }
        return new ProtozoaGenome(newGenes);
    }

    public ProtozoaGenome crossover(ProtozoaGenome other) {
        Gene<?>[] newGenes = Arrays.copyOf(genes, genes.length);
        for (int i = 0; i < genes.length; i++)
            newGenes[i] = genes[i].crossover(other.genes[i]);
        return new ProtozoaGenome(newGenes);
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
            NeuralNetwork nn = networkGenome.phenotype();
            if (nn.getInputSize() < expectedNetworkInputSize(retina().numberOfCells()))
                throw new MiscarriageException();
            return new NNBrain(nn, maxTurn);
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

    public Protozoa createChild(Tank tank, ProtozoaGenome otherGenome) throws MiscarriageException {
        if (otherGenome == null)
            return createChild(tank);
        tank.registerCrossoverEvent();
        ProtozoaGenome childGenome = crossover(otherGenome);
        return childGenome.mutate().phenotype(tank);
    }

    public Color getColour() {
        return getGeneValue(ProtozoaColorGene.class);
    }

    public int getNumMutations() {
        int numMutations = 0;
        for (Gene<?> gene : genes)
            numMutations += gene.getNumMutations();
        return numMutations;
    }

    public Protozoa.Spike[] getSpikes() {
        return getGeneValue(ProtozoaSpikesGene.class);
    }

    public float getMaxTurn() {
        return getFloatGeneValue(ProtozoaMaxTurnGene.class);
    }

    public float getHerbivoreFactor() {
        return getGeneValue(HerbivoreFactorGene.class);
    }
}
