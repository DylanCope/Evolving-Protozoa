package neat;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class NeuralNetwork implements Serializable
{
    private static final long serialVersionUID = 1L;

    private final Neuron[] outputNeurons;
    private final Neuron[] inputNeurons;
    private final float[] outputs;
    private final Neuron[] neurons;
    private final int depth;
    private final int nInputs;

    public NeuralNetwork(Neuron[] neurons, int nInputs, int nOutputs) {
        this.neurons = neurons;
        this.nInputs = nInputs;

        inputNeurons = new Neuron[nInputs];
        int i = 0;
        for (Neuron neuron : neurons)
            if (neuron.getType().equals(Neuron.Type.SENSOR)) {
                inputNeurons[i] = neuron;
                i++;
            }

        outputNeurons = new Neuron[nOutputs];
        i = 0;
        for (Neuron neuron : neurons)
            if (neuron.getType().equals(Neuron.Type.OUTPUT)) {
                outputNeurons[i] = neuron;
                i++;
            }

        outputs = new float[nOutputs];
        Arrays.fill(outputs, 0f);

        depth = calculateDepth();
    }

    public int getDepth() {
        return depth;
    }

    private int calculateDepth() {
        boolean[] visited = new boolean[neurons.length];
        Arrays.fill(visited, false);
        return calculateDepth(0, outputNeurons, visited);
    }

    private int calculateDepth(int depth, Neuron[] explore, boolean[] visited) {
        int maxDepth = depth;
        for (Neuron n : explore) {
            if (visited[n.getId()] | n.getType().equals(Neuron.Type.SENSOR))
                continue;
            visited[n.getId()] = true;
            maxDepth = Math.max(maxDepth, calculateDepth(depth + 1, n.getInputs(), visited));
            n.setDepth(maxDepth);
        }

        return maxDepth;
    }

    public void setInput(float ... values) {
        for (int i = 0; i < values.length; i++)
            inputNeurons[i].setState(values[i]);
    }

    public void tick()
    {
        for (Neuron n : neurons) n.tick();
        for (Neuron n : neurons) n.update();
    }

    public float[] outputs()
    {
        for (int i = 0; i < outputNeurons.length; i++)
            outputs[i] = outputNeurons[i].getState();
        return outputs;
    }

    @Override
    public String toString()
    {
        return Stream.of(neurons)
                .map(Neuron::toString)
                .collect(Collectors.joining("\n"));
    }

    public int getInputSize() {
        return nInputs;
    }
}
