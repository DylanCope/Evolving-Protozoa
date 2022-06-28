package protoevo.biology.genes;

import protoevo.core.Settings;

import java.io.Serializable;

public class ProtozoaRadiusGene extends BoundedFloatGene implements Serializable {

    public ProtozoaRadiusGene() {
        super(Settings.minProtozoanBirthRadius, Settings.maxProtozoanBirthRadius);
    }

    public ProtozoaRadiusGene(Float value) {
        super(Settings.minProtozoanBirthRadius, Settings.maxProtozoanBirthRadius, value);
    }

    @Override
    public <G extends Gene<Float>> G createNew(Float value) {
        return (G) new ProtozoaRadiusGene(value);
    }
}
