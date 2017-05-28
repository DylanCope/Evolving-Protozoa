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

    public void setState(double ... state)
    {
        for (Neuron n : neurons)
            if (0 <= n.id && n.id < state.length)
                n.state = state[n.id];
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
            s += n.toString() + "\n";
        return s;
    }

}
