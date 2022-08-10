package protoevo.biology.genes;

import protoevo.core.Settings;

import java.io.Serializable;

public class ProtozoaGrowthRateGene extends BoundedFloatGene implements Serializable {

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

    @Override
    public String getTraitName() {
        return "Growth Rate";
    }
}
