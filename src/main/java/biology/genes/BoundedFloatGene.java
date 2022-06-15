package biology.genes;

import core.Simulation;

public abstract class BoundedFloatGene extends Gene<Float>  {
    float minValue, maxValue;

    public BoundedFloatGene(float minValue, float maxValue) {
        this.minValue = minValue;
        this.maxValue = maxValue;
        setValue(getNewValue());
    }

    public BoundedFloatGene(float minValue, float maxValue, float value) {
        this.minValue = minValue;
        this.maxValue = maxValue;
        setValue(value);
    }

    @Override
    public Float getNewValue() {
        return minValue + (maxValue - minValue) * Simulation.RANDOM.nextFloat();
    }
}
