package biology.genes;

import core.Settings;

public class ProtozoaRadiusGene extends BoundedFloatGene {

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
