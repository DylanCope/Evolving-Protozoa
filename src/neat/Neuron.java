package neat;

import java.util.ArrayList;

/**
 * Created by dylan on 26/05/2017.
 */
public class Neuron
{
    public interface Activation
    {
        double transfer(double z);
    }

    public static Activation SIGMOID = (double z) -> 1 / (1 + Math.exp(-z));
    public static Activation LINEAR = (double z) -> z;

    Neuron[] inputs;
    double[] weights;
    int id;
    double state = 0;

    private double next_state = 0;
    private Neuron.Activation activation = SIGMOID;

    public Neuron(int id)
    {
        this(id, new Neuron[]{}, new double[]{});
    }

    public void setActivation(Neuron.Activation activation) { this.activation = activation; }

    public Neuron(int id, Neuron[] inputs, double[] weights)
    {
        this.id = id;
        this.inputs = inputs;
        this.weights = weights;
    }

    void tick()
    {
        double z = 0;
        for (int i = 0; i < inputs.length; i++) {
            z += weights[i] * inputs[i].state;
        }
        next_state = activation.transfer(z);
    }

    void update()
    {
        state = next_state;
        next_state = 0;
    }
}
