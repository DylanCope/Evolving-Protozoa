package protoevo.biology.genes;

import protoevo.core.Simulation;

public class RetinalProductionGene extends BoundedFloatGene {

    public RetinalProductionGene() {
        super(0, 1);
    }

    public RetinalProductionGene(Float value) {
        super(0, 1, value);
    }

    @Override
    public Float getNewValue() {
        if (Simulation.RANDOM.nextFloat() < 0.1f)
            return 0f;
        return super.getNewValue();
    }

    @Override
    public <G extends Gene<Float>> G createNew(Float value) {
        return (G) new RetinalProductionGene(value);
    }

    @Override
    public Float disabledValue() {
        return 0f;
    }

    @Override
    public boolean canDisable() {
        return true;
    }
}
