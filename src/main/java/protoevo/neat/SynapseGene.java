package protoevo.neat;


import protoevo.core.Simulation;

import java.io.Serializable;
import java.util.Objects;

public class SynapseGene implements Comparable<SynapseGene>, Serializable
{
    private static int globalInnovation = 0;
    private final int innovation;
    private NeuronGene in, out;
    private float weight;
    private boolean disabled;

    public SynapseGene(NeuronGene in, NeuronGene out, float weight, int innovation) {
        this.in = in;
        this.out = out;
        disabled = false;
        this.weight = weight;
        this.innovation =  innovation;
    }

    public SynapseGene(NeuronGene in, NeuronGene out, float weight) {
        this(in, out, weight, globalInnovation++);
    }

    public static float randomInitialWeight() {
        return (float) (2* Simulation.RANDOM.nextDouble() - 1);
    }

    public SynapseGene(NeuronGene in, NeuronGene out) {
        this(in, out, randomInitialWeight(), globalInnovation++);
    }

    @Override
    public int compareTo(SynapseGene g) {
        return innovation - g.innovation;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof SynapseGene) {
            SynapseGene otherSynGene = ((SynapseGene) o);
            NeuronGene otherIn = otherSynGene.in;
            NeuronGene otherOut = otherSynGene.out;
            return in.equals(otherIn)
                    && out.equals(otherOut);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(in.getId(), out.getId());
    }

    @Override
    public String toString()
    {
        return String.format(
                "Synapse: innov=%d; in=%d; out=%d; w=%.5f; disabled=%b",
                innovation, in.getId(), out.getId(), weight, disabled);
    }

    public boolean isDisabled() {
        return disabled;
    }

    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }

    public int getInnovation() {
        return innovation;
    }

    public NeuronGene getIn() {
        return in;
    }

    public void setIn(NeuronGene in) {
        this.in = in;
    }

    public NeuronGene getOut() {
        return out;
    }

    public void setOut(NeuronGene out) {
        this.out = out;
    }

    public float getWeight() {
        return weight;
    }

    public void setWeight(float weight) {
        this.weight = weight;
    }
}
