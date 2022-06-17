package biology.genes;

import java.io.Serializable;

public class ProtozoaFOVGene extends BoundedFloatGene implements Serializable {

    public ProtozoaFOVGene() {
        super((float) Math.toRadians(20), (float) Math.toRadians(300));
    }

    public ProtozoaFOVGene(Float value) {
        super((float) Math.toRadians(20), (float) Math.toRadians(300), value);
    }

    @Override
    public <G extends Gene<Float>> G createNew(Float value) {
        return (G) new ProtozoaFOVGene(value);
    }
}

