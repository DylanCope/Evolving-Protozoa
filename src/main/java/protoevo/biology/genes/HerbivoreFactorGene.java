package protoevo.biology.genes;

import java.io.Serializable;

public class HerbivoreFactorGene extends BoundedFloatGene implements Serializable {
    private static final float minValue = 0.5f;
    private static final float maxValue = 2f;
    public HerbivoreFactorGene() {
        super(minValue, maxValue);
    }

    public HerbivoreFactorGene(Float value) {
        super(minValue, maxValue, value);
    }

    @Override
    public <G extends Gene<Float>> G createNew(Float value) {
        return (G) new HerbivoreFactorGene(value);
    }

    @Override
    public String getTraitName() {
        return "Herbivore Factor";
    }
}

