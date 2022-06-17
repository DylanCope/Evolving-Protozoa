package neat;


import core.Simulation;

import java.io.Serializable;

public class SynapseGene implements Comparable<SynapseGene>, Serializable
{
    private static int globalInnovation = 0;
    private final int innovation;
    private NeuronGene in, out;
    private float weight;
    private boolean disabled;

    public SynapseGene(NeuronGene in, NeuronGene out, int innovation) {
        this.in = in;
        this.out = out;
        this.weight = (float) (2*Simulation.RANDOM.nextDouble() - 1);
        disabled = false;
        this.innovation =  innovation;
    }

    public SynapseGene(NeuronGene in, NeuronGene out) {
        this(in, out, globalInnovation++);
    }

    @Override
    public int compareTo(SynapseGene g) {
        if (!equals(g)) {
            return innovation - g.innovation;
        }
        return -1;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof SynapseGene) {
            SynapseGene otherSynGene = ((SynapseGene) o);
            NeuronGene otherIn = otherSynGene.in;
            NeuronGene otherOut = otherSynGene.out;
            return in.equals(otherIn)
                    && out.equals(otherOut)
                    && innovation == otherSynGene.getInnovation();
        }
        return false;
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
