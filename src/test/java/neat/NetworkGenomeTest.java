package neat;

import com.google.common.collect.Streams;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Created by dylan on 26/05/2017.
 */
public class NetworkGenomeTest
{
    @Test
    public void constructingGenome()
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
        net.setInput(5.0, -2.0);
        System.out.println(net);
        net.tick();
        System.out.println(net);
        List<Double> expected = Arrays.asList(0.87, 1.00, 0.00);
        List<Double> actual = net.outputs();
        Streams.zip(expected.stream(), actual.stream(), (e, a) -> {
            assertEquals(e, a, 1e-6); return null;
        });
    }

    @Test
    public void mutateNode() {
        NetworkGenome genome = new NetworkGenome(0, 2, 3);
        System.out.println(genome);
    }
}