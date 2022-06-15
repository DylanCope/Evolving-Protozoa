package biology.genes;

import core.Settings;

public class ProtozoaGrowthRateGene extends BoundedFloatGene {

    public ProtozoaGrowthRateGene() {
        super(Settings.minProtozoanGrowthRate, Settings.maxProtozoanGrowthRate);
    }

    public ProtozoaGrowthRateGene(Float value) {
        super(Settings.minProtozoanGrowthRate, Settings.maxProtozoanGrowthRate, value);
    }

    @Override
    public <G extends Gene<Float>> G createNew(Float value) {
        return (G) new ProtozoaGrowthRateGene(value);
    }
}
