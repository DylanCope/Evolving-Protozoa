package protoevo.biology.genes;

import java.io.Serializable;

public class ProtozoaFOVGene extends BoundedFloatGene implements Serializable {

    public ProtozoaFOVGene() {
        super((float) Math.toRadians(20), (float) Math.toRadians(300));
    }

    public ProtozoaFOVGene(Float value) {
        super((float) Math.toRadians(20), (float) Math.toRadians(300), value);
    }

    @Override
    public String getTraitName() {
        return "Retina FoV";
    }

    @Override
    public BoundedFloatGene createNew(Float value) {
        return new ProtozoaFOVGene(value);
    }
}

