package biology.genes;


import core.Settings;
import core.Simulation;
import neat.NetworkGenome;

import java.io.Serializable;

public class RetinaSizeGene extends Gene<Integer> implements Serializable {

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
    public <G extends Gene<Integer>> G mutate(Gene<?>[] genes) {
        int size = getValue();
        if (size == Settings.maxRetinaSize)
            return (G) this;

        addNetworkSensors(genes);
        return createNew(size + 1, getNumMutations() + 1);
    }

    private void addNetworkSensors(Gene<?>[] genes) {
        for (int i = 0; i < genes.length; i++) {
            Gene<?> gene = genes[i];
            if (gene instanceof NetworkGene) {
                NetworkGene networkGene = (NetworkGene) gene;
                NetworkGenome currentNetworkGenome = ((NetworkGene) gene).getValue();
                NetworkGenome newNetworkGenome = new NetworkGenome(currentNetworkGenome);
                newNetworkGenome.addSensor();
                newNetworkGenome.addSensor();
                newNetworkGenome.addSensor();
                genes[i] = networkGene.createNew(newNetworkGenome);
            }
        }
    }

    @Override
    public Integer getNewValue() {
        return 0;
    }
}