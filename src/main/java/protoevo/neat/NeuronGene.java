package protoevo.neat;

import java.io.Serializable;
import java.util.Objects;

public class NeuronGene implements Comparable<NeuronGene>, Serializable
{

    private final int id;
    private final Neuron.Type type;
    private final Neuron.Activation activation;

    private final String label;

    public NeuronGene(int id, Neuron.Type type, Neuron.Activation activation)
    {
        this(id, type, activation, null);
    }

    public NeuronGene(int id, Neuron.Type type, Neuron.Activation activation, String label)
    {
        this.id = id;
        this.type = type;
        this.activation = activation;
        this.label = label;
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
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        String str = String.format("Neuron: id=%d; type=%s", id, type);
        if (label != null)
            str += ", label=" + label;
        return str;
    }

    public int getId() { return id; }
    public Neuron.Type getType() { return type; }
    public Neuron.Activation getActivation() { return activation; }

    public String getLabel() {
        return label;
    }
}
