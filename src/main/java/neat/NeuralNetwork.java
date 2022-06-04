package neat;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

public class NeuralNetwork implements Serializable
{
    private static final long serialVersionUID = 1L;

    private final List<Neuron> outputNeurons;
    private final List<Neuron> inputNeurons;
    private final Collection<Neuron> neurons;
    private final int depth;

    public NeuralNetwork(Collection<Neuron> neurons) {
        this.neurons = neurons;

        inputNeurons = neurons.stream()
                .filter(n -> n.getType().equals(Neuron.Type.SENSOR))
                .collect(Collectors.toList());
        inputNeurons.sort(Comparator.comparingInt(Neuron::getId));

        outputNeurons = neurons.stream()
                .filter(n -> n.getType().equals(Neuron.Type.OUTPUT))
                .collect(Collectors.toList());
        outputNeurons.sort(Comparator.comparingInt(Neuron::getId));

        depth = calculateDepth();
    }

    public int getDepth() {
        return depth;
    }

    private int calculateDepth() {
        return calculateDepth(0, outputNeurons);
    }

    private int calculateDepth(int depth, Collection<Neuron> explore) {
        int maxDepth = depth;
        for (Neuron n : explore)
            maxDepth = Math.max(maxDepth, calculateDepth(depth + 1, n.getInputs()));
        return maxDepth;
    }

    public void setInput(Float ... values) {
        setInput(Arrays.asList(values));
    }

    public void setInput(List<Float> inputValues)
    {
        for (int i = 0; i < inputValues.size(); i++)
            inputNeurons.get(i).setState(inputValues.get(i));
    }

    public void tick()
    {
        for (Neuron n : neurons) n.tick();
        for (Neuron n : neurons) n.update();
    }

    public List<Float> outputs()
    {
        return outputNeurons.stream()
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
