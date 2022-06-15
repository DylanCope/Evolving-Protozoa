package biology.genes;


import core.Settings;
import core.Simulation;
import neat.NetworkGenome;

public class RetinaSizeGene extends Gene<Integer> {

    public RetinaSizeGene() {
        super();
    }

    public RetinaSizeGene(Integer value) {
        super(value);
    }

    @Override
    public <G extends Gene<Integer>> G createNew(Integer value) {
        return (G) new RetinaSizeGene(value);
    }

    @Override
    public <G extends Gene<Integer>> G mutate(Gene<?>[] genome) {
        int size = getValue();
        if (size >= Settings.maxRetinaSize)
            return (G) this;

        if (size == 0 || Simulation.RANDOM.nextBoolean()) {
            size++;
            NetworkGenome networkGenome = getNetworkGenome(genome);
            networkGenome.addSensor();
            networkGenome.addSensor();
            networkGenome.addSensor();
        }
        return createNew(size);
    }

    private NetworkGenome getNetworkGenome(Gene<?>[] genes) {
        for (Gene<?> gene : genes) {
            if (gene instanceof NetworkGene)
                return ((NetworkGene) gene).getValue();
        }
        return null;
    }

    @Override
    public Integer getNewValue() {
        return 0;
    }
}