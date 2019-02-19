package neat;

public class NeuronGene implements Comparable<NeuronGene>
{

    private int id;
    private Neuron.Type type;
    private Neuron.Activation activation;

    public NeuronGene(int id, Neuron.Type type, Neuron.Activation activation)
    {
        this.id = id;
        this.type = type;
        this.activation = activation;
    }

    @Override
    public int compareTo(NeuronGene o) {
        return id - o.id;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof NeuronGene)
            return ((NeuronGene) o).getId() == id;
        return false;
    }

    @Override
    public String toString() {
        return String.format("Neuron, id:%d, type:%s", id, type);
    }

    public int getId() { return id; }
    public Neuron.Type getType() { return type; }
    public Neuron.Activation getActivation() { return activation; }
}
