package tests.neat;

import com.sun.istack.internal.NotNull;
import neat.NetworkGenome;
import neat.NeuralNetwork;
import neat.Neuron;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by dylan on 26/05/2017.
 */
public class NeuralNetworkTest
{
    @NotNull
    private NeuralNetwork simpleFullyConnected()
    {
        Neuron n1, n2, n3, n4, n5, n6;
        n1 = new Neuron(0);
        n2 = new Neuron(1);
        n3 = new Neuron(2, new Neuron[]{n1, n2}, new double[]{5, 1});
        n4 = new Neuron(3, new Neuron[]{n1, n2}, new double[]{-4, 6});
        n5 = new Neuron(4, new Neuron[]{n1, n2}, new double[]{9.1, 7});
        n6 = new Neuron(5, new Neuron[]{n3, n4, n5}, new double[]{1, 4, -2.3});
        n1.setActivation(Neuron.LINEAR); n2.setActivation(Neuron.LINEAR);
        n3.setActivation(Neuron.LINEAR); n4.setActivation(Neuron.LINEAR);
        n5.setActivation(Neuron.LINEAR); n6.setActivation(Neuron.LINEAR);
        Neuron[] neurons = new Neuron[]{n1, n2, n3, n4, n5, n6};
        return new NeuralNetwork(new Neuron[]{n6}, neurons);
    }

    @Test
    public void setState() throws Exception
    {
        NeuralNetwork net = simpleFullyConnected();
        net.setState(-5, 4);
        assertEquals(net.toString(), "id:0, state:-5.00, inputs:[]\n" +
                "id:1, state:4.00, inputs:[]\n" +
                "id:2, state:0.00, inputs:[(0, 5.0)(1, 1.0)]\n" +
                "id:3, state:0.00, inputs:[(0, -4.0)(1, 6.0)]\n" +
                "id:4, state:0.00, inputs:[(0, 9.1)(1, 7.0)]\n" +
                "id:5, state:0.00, inputs:[(2, 1.0)(3, 4.0)(4, -2.3)]\n");
    }

    @Test
    public void tick() throws Exception
    {
        NeuralNetwork net = simpleFullyConnected();
        net.setState(-5, 4);
        net.tick();
        assertEquals(net.toString(), "id:0, state:0.00, inputs:[]\n" +
                "id:1, state:0.00, inputs:[]\n" +
                "id:2, state:-21.00, inputs:[(0, 5.0)(1, 1.0)]\n" +
                "id:3, state:44.00, inputs:[(0, -4.0)(1, 6.0)]\n" +
                "id:4, state:-17.50, inputs:[(0, 9.1)(1, 7.0)]\n" +
                "id:5, state:0.00, inputs:[(2, 1.0)(3, 4.0)(4, -2.3)]\n");
    }

    @Test
    public void outputs() throws Exception
    {
        NeuralNetwork net = simpleFullyConnected();
        net.setState(-5, 4);
        double[] expected = {195.25};
        net.tick(); net.tick();
        assertArrayEquals(expected, net.outputs(), 1e-6);
    }
}