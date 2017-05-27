package tests.neat;

import com.sun.istack.internal.NotNull;
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
    public void setInputs() throws Exception
    {
        NeuralNetwork net = simpleFullyConnected();
        net.setInputs(new double[]{-5, 4});
        assertEquals(net.toString(), "0:-5.000000\n" +
                "1:4.000000\n" +
                "2:0.000000\n" +
                "3:0.000000\n" +
                "4:0.000000\n" +
                "5:0.000000\n");
    }

    @Test
    public void tick() throws Exception
    {
        NeuralNetwork net = simpleFullyConnected();
        net.setInputs(new double[]{-5, 4});
        net.tick();
        assertEquals(net.toString(), "0:0.000000\n" +
                "1:0.000000\n" +
                "2:-21.000000\n" +
                "3:44.000000\n" +
                "4:-17.500000\n" +
                "5:0.000000\n");
    }

    @Test
    public void outputs() throws Exception
    {
        NeuralNetwork net = simpleFullyConnected();
        net.setInputs(new double[]{-5, 4});
        double[] expected = {195.25};
        net.tick(); net.tick();
        assertArrayEquals(expected, net.outputs(), 1e-6);
    }
}