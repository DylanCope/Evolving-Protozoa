package protoevo.biology.genes;


import protoevo.biology.Retina;
import protoevo.core.Settings;
import protoevo.neat.NetworkGenome;

import javax.swing.text.html.Option;
import java.io.Serializable;
import java.util.Optional;

public class RetinaSizeGene extends Gene<Integer> implements Serializable {
    public static final long serialVersionUID = -4191267363677698742L;

    public RetinaSizeGene() {
        super();
    }

    public RetinaSizeGene(Integer value) {
        super(value);
    }

    @Override
    public RetinaSizeGene createNew(Integer value) {
        return new RetinaSizeGene(value);
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

    @Override
    public boolean canDisable() {
        return true;
    }

    @Override
    public Integer disabledValue() {
        return 0;
    }

    private Optional<Integer> findNetworkGene(Gene<?>[] genes) {
        for (int i = 0; i < genes.length; i++) {
            Gene<?> gene = genes[i];
            if (gene instanceof NetworkGene)
                return Optional.of(i);
        }
        return Optional.empty();
    }

    private void addNetworkSensors(Gene<?>[] genes, int newRetinaSize) {

        int i = findNetworkGene(genes).orElseThrow(() -> new RuntimeException("No Network Gene found"));
        NetworkGene networkGene = (NetworkGene) genes[i];
        NetworkGenome currentNetworkGenome = networkGene.getValue();
        NetworkGenome newNetworkGenome = new NetworkGenome(currentNetworkGenome);
        newNetworkGenome.ensureRetinaSensorsExist(newRetinaSize);
        genes[i] = networkGene.createNew(newNetworkGenome);

    }

    @Override
    public Integer getNewValue() {
        return Settings.defaultRetinaSize;
    }

    @Override
    public String getTraitName() {
        return "Num Retina Cells";
    }
}