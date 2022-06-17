package neat;

import java.io.Serializable;

public class NeuronGene implements Comparable<NeuronGene>, Serializable
{

    private final int id;
    private final Neuron.Type type;
    private final Neuron.Activation activation;

    public NeuronGene(int id, Neuron.Type type, Neuron.Activation activation)
    {
        this.id = id;
        this.type = type;
        this.activation = activation;
    }

    public NeuronGene(NeuronGene g) {
        id = g.id;
        type = g.type;
        activation = g.activation;
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
        return String.format("Neuron: id=%d; type=%s", id, type);
    }

    public int getId() { return id; }
    public Neuron.Type getType() { return type; }
    public Neuron.Activation getActivation() { return activation; }
}
