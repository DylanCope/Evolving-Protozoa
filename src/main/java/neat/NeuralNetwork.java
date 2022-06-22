package neat;

import biology.MiscarriageException;

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
    private boolean computedGraphics = false;
    private int nodeSpacing;

    public NeuralNetwork(Neuron[] neurons) {
        this.neurons = neurons;

        int nSensors = 0;
        int nOutputs = 0;
        for (Neuron neuron : neurons) {
            if (neuron == null)
                throw new IllegalArgumentException("Cannot handle null neurons.");
            else if (neuron.getType().equals(Neuron.Type.SENSOR))
                nSensors++;
            else if (neuron.getType().equals(Neuron.Type.OUTPUT))
                nOutputs++;
        }
        this.nInputs = nSensors;

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
        int depth = calculateDepth(outputNeurons, visited);

        for (Neuron n : outputNeurons)
            n.setDepth(depth);

        for (Neuron n : inputNeurons)
            n.setDepth(0);

        for (Neuron n : neurons)
            if (n.getDepth() == -1)
                n.setDepth(depth);

        return depth;
    }

    private int calculateDepth(Neuron[] explore, boolean[] visited) {

        List<Neuron> unexplored = Arrays.stream(explore)
                .filter(n -> !visited[n.getId()])
                .collect(Collectors.toList());

        for (Neuron n : explore)
            visited[n.getId()] = true;

        int maxDepth = 0;

        for (Neuron n : unexplored) {
            int neuronDepth = 1 + calculateDepth(n.getInputs(), visited);
            n.setDepth(neuronDepth);
            maxDepth = Math.max(maxDepth, neuronDepth);
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

    public int getSize() {
        return neurons.length;
    }

    public Neuron[] getNeurons() {
        return neurons;
    }

    public boolean hasComputedGraphicsPositions() {
        return computedGraphics;
    }

    public void setComputedGraphicsPositions(boolean computedGraphics) {
        this.computedGraphics = computedGraphics;
    }

    public void setGraphicsNodeSpacing(int nodeSpacing) {
        this.nodeSpacing = nodeSpacing;
    }

    public int getGraphicsNodeSpacing() {
        return nodeSpacing;
    }
}
