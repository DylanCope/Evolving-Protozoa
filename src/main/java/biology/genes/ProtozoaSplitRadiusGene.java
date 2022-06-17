package biology.genes;

import core.Settings;

import java.io.Serializable;

public class ProtozoaSplitRadiusGene extends BoundedFloatGene implements Serializable {

    public ProtozoaSplitRadiusGene() {
        super(Settings.minProtozoanSplitRadius, Settings.maxProtozoanSplitRadius);
    }

    public ProtozoaSplitRadiusGene(Float value) {
        super(Settings.minProtozoanSplitRadius, Settings.maxProtozoanSplitRadius, value);
    }

    @Override
    public <G extends Gene<Float>> G createNew(Float value) {
        return (G) new ProtozoaSplitRadiusGene(value);
    }
}
