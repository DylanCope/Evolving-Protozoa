package protoevo.biology.genes;

import protoevo.core.Settings;

import java.io.Serializable;

public class ProtozoaSplitRadiusGene extends BoundedFloatGene implements Serializable {

    public ProtozoaSplitRadiusGene() {
        super(Settings.minProtozoanSplitRadius, Settings.maxProtozoanSplitRadius);
    }

    public ProtozoaSplitRadiusGene(Float value) {
        super(Settings.minProtozoanSplitRadius, Settings.maxProtozoanSplitRadius, value);
    }

    @Override
    public BoundedFloatGene createNew(Float value) {
        return new ProtozoaSplitRadiusGene(value);
    }
}
