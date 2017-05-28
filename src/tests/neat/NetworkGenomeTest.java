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
public class NetworkGenomeTest
{
    @Test
    public void constructingGenome() throws Exception
    {
        NetworkGenome genome = new NetworkGenome(0, 2, 3);
        genome.addSynapse(0, 2, 0.5);
        genome.addSynapse(0, 3, 1.2);
        genome.addSynapse(0, 4, -1.6);
        genome.addSynapse(1, 2, 0.3);
        genome.addSynapse(1, 3, -0.9);
        genome.addSynapse(1, 4, 0.2);
        System.out.println(genome);
        NeuralNetwork net = genome.networkPhenotype();
        net.setState(5, -2);
        System.out.println(net);
        net.tick();
        System.out.println(net);
        double[] expected = {0.87, 1.00, 0.00};
        assertArrayEquals(expected, net.outputs(), 1e-2);
    }

    @Test
    public void mutateNode() throws Exception
    {
        NetworkGenome genome = new NetworkGenome(0, 2, 3);
        System.out.println(genome);
    }
}