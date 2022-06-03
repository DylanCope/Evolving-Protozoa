package biology;

import core.Settings;
import core.Simulation;
import neat.NetworkGenome;

import java.io.Serializable;
import java.util.stream.Stream;

/**
 * Created by dylan on 28/05/2017.
 */
public class ProtozoaGenome implements Serializable
{
//    private final NetworkGenome networkGenome;
    private final int retinaSize;
    private double radius;
    private double growthRate;
    private double splitSize;
    private double mutationChance = Settings.globalMutationChance;

    public static final int actionSpaceSize = 4;

    public ProtozoaGenome(ProtozoaGenome parentGenome) {
        retinaSize = parentGenome.retinaSize;
        radius = parentGenome.radius;
//        networkGenome = parentGenome.networkGenome;
        mutationChance = parentGenome.mutationChance;
        splitSize = parentGenome.splitSize;
        growthRate = parentGenome.growthRate;
    }

    public ProtozoaGenome()
    {
        retinaSize = Settings.defaultRetinaSize;
        radius = randomProtozoanRadius();
        splitSize = randomSplitSize();
        growthRate = randomGrowthRate();
//        networkGenome = new NetworkGenome(3 * retinaSize, actionSpaceSize);
    }

    public ProtozoaGenome(int retinaSize,
                          double radius,
                          double growthRate,
                          double splitSize)
//                          NetworkGenome networkGenome)
    {
        this.retinaSize = retinaSize;
        this.radius = radius;
//        this.networkGenome = networkGenome;
        this.growthRate = growthRate;
        this.splitSize = splitSize;
    }

    private static double randomProtozoanRadius() {
        double range = Settings.maxProtozoanBirthRadius - Settings.minProtozoanBirthRadius;
        return Settings.minProtozoanBirthRadius + range * Simulation.RANDOM.nextDouble();
    }

    private static double randomSplitSize() {
        double range = Settings.maxProtozoanSplitRadius - Settings.minProtozoanSplitRadius;
        return Settings.minProtozoanSplitRadius + range * Simulation.RANDOM.nextDouble();
    }

    private static double randomGrowthRate() {
        double range = Settings.maxProtozoanGrowthRate - Settings.minProtozoanGrowthRate;
        return Settings.minProtozoanGrowthRate + range * Simulation.RANDOM.nextDouble();
    }


    public ProtozoaGenome mutate() {
//        networkGenome.mutate();
        if (Simulation.RANDOM.nextDouble() < Settings.globalMutationChance) {
            radius = randomProtozoanRadius();
        }
        return this;
    }

    public Brain brain()
    {
//        return new NNBrain(networkGenome.phenotype());
        return Brain.RANDOM;
    }

    public Retina retina()
    {
        return new Retina(this.retinaSize);
    }

    public double getRadius()
    {
        return radius;
    }

    public double getGrowthRate() {
        return growthRate;
    }

    public double getSplitRadius() {
        return splitSize;
    }


    public Protozoa phenotype()
    {
        return new Protozoa(this);
    }

//    public Stream<ProtozoaGenome> crossover(ProtozoaGenome other) {
//        NetworkGenome childNetGenome = networkGenome.crossover(other.networkGenome);
//        int childRetinaSize = Simulation.RANDOM.nextBoolean() ? retinaSize : other.retinaSize;
//        double childRadius = Simulation.RANDOM.nextBoolean() ? radius : other.radius;
//        double childSplitSize = Simulation.RANDOM.nextBoolean() ? splitSize : other.splitSize;
//        double childGrowthRate = Simulation.RANDOM.nextBoolean() ? growthRate : other.growthRate;
//        return Stream.of(new ProtozoaGenome(
//                childRetinaSize, childRadius, childGrowthRate, childSplitSize, childNetGenome
//        ));
//    }

//    public Stream<ProtozoaGenome> reproduce(ProtozoaGenome other)
//    {
//        return crossover(other).map(ProtozoaGenome::mutate);
//    }

    public Protozoa createChild()
    {
        ProtozoaGenome childGenome = new ProtozoaGenome(this);
        return childGenome.mutate().phenotype();
    }
}
