package neat;


import core.Simulation;

import java.io.Serializable;

public class SynapseGene implements Comparable<SynapseGene>, Serializable
{
    private int id;
    private int innovation;
    private NeuronGene in, out;
    private double weight;
    private boolean disabled;

    public SynapseGene(NeuronGene in, NeuronGene out) {
        this.in = in;
        this.out = out;
        this.weight = 2*Simulation.RANDOM.nextDouble() - 1;
        disabled = false;
        innovation = 0;
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
                "Synapse, innov:%d, in:%d, out:%d, w:%.2f, disabled:%b",
                innovation, in.getId(), out.getId(), weight, disabled);
    }

    public boolean isDisabled() {
        return disabled;
    }

    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getInnovation() {
        return innovation;
    }

    public void setInnovation(int innovation) {
        this.innovation = innovation;
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

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }
}
