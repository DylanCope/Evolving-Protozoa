package neat;


public class SynapseGene implements Comparable<SynapseGene>
{
    private int id;
    private int innovation;
    private NeuronGene in, out;
    private double weight;
    private boolean disabled;

    @Override
    public int compareTo(SynapseGene g) {
        return innovation - g.innovation;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof SynapseGene) {
            return ((SynapseGene) o).getId() == id;
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
