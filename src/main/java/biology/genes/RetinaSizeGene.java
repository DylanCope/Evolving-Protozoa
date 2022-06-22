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

        int newSize = size + 1;
        addNetworkSensors(genes, newSize);
        return createNew(newSize, getNumMutations() + 1);
    }

    private void addNetworkSensors(Gene<?>[] genes, int retinaSize) {
        for (int i = 0; i < genes.length; i++) {
            Gene<?> gene = genes[i];
            if (gene instanceof NetworkGene) {
                NetworkGene networkGene = (NetworkGene) gene;
                NetworkGenome currentNetworkGenome = ((NetworkGene) gene).getValue();
                NetworkGenome newNetworkGenome = new NetworkGenome(currentNetworkGenome);
                while (newNetworkGenome.numberOfSensors() < ProtozoaGenome.expectedNetworkInputSize(retinaSize))
                    newNetworkGenome.addSensor();

                genes[i] = networkGene.createNew(newNetworkGenome);
                return;
            }
        }
    }

    @Override
    public Integer getNewValue() {
        return 0;
    }
}