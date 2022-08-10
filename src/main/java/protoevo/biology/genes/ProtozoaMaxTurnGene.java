package protoevo.biology.genes;

import java.io.Serializable;

public class ProtozoaMaxTurnGene extends BoundedFloatGene implements Serializable {

    public ProtozoaMaxTurnGene() {
        super((float) Math.toRadians(1), (float) Math.toRadians(6));
    }

    public ProtozoaMaxTurnGene(Float value) {
        super((float) Math.toRadians(1), (float) Math.toRadians(6), value);
    }

    @Override
    public <G extends Gene<Float>> G createNew(Float value) {
        return (G) new ProtozoaMaxTurnGene(value);
    }

    @Override
    public String getTraitName() {
        return "Max Turn";
    }
}

