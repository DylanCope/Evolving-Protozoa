package neat;

import java.util.*;

public class NeuralNetwork
{
    Neuron[] outputs;
    private double[] inputs;
    Neuron[] neurons;

    public NeuralNetwork(Neuron[] outputs, Neuron[] neurons)
    {
        this.outputs = outputs;
        this.neurons = neurons;
    }

    public void setInputs(double[] inputs)
    {
        for (Neuron n : neurons)
            if (0 <= n.id && n.id < inputs.length)
                n.state = inputs[n.id];
    }

    public void tick()
    {
        for (Neuron n : neurons) n.tick();
        for (Neuron n : neurons) n.update();
    }

    public double[] outputs()
    {
        double[] outValues = new double[outputs.length];
        for (int i = 0; i < outputs.length; i++)
            outValues[i] = outputs[i].state;
        return outValues;
    }

    @Override
    public String toString()
    {
        String s = "";
        for (Neuron n : neurons)
            s += String.format("%d:%f\n", n.id, n.state);
        return s;
    }

}
