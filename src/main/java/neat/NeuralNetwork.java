package neat;

import com.google.common.collect.Streams;

import java.util.*;
import java.util.stream.Collectors;

public class NeuralNetwork
{
    private List<Neuron> outputs;
    private List<Neuron> inputs;
    private Set<Neuron> neurons;

    public NeuralNetwork(Collection<Neuron> neurons) {
        this.neurons = new HashSet<>(neurons);

        inputs = neurons.stream()
                .filter(n -> n.getType().equals(Neuron.Type.SENSOR))
				.sorted()
                .collect(Collectors.toList());

        outputs = neurons.stream()
                .filter(n -> n.getType().equals(Neuron.Type.OUTPUT))
				.sorted()
                .collect(Collectors.toList());
    }

    public void setInput(Double ... values) {
        setInput(Arrays.asList(values));
    }

    public void setInput(List<Double> input)
    {
        inputs = Streams.zip(inputs.stream(), input.stream(), Neuron::setState)
                        .collect(Collectors.toList());
	}

    public void tick()
    {
        for (Neuron n : neurons) n.tick();
        for (Neuron n : neurons) n.update();
    }

    public List<Double> getOutputs()
    {
        return outputs.stream()
                .map(Neuron::getState)
                .collect(Collectors.toList());
    }

    public List<Double> getFullState() {
    	return neurons.stream()
				.sorted()
				.map(Neuron::getState)
				.collect(Collectors.toList());
	}

    @Override
    public String toString()
    {
        return neurons.stream()
                .sorted()
                .map(Neuron::toString)
                .collect(Collectors.joining("\n"));
    }
}
