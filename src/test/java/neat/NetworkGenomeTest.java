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
        NetworkGenome genome = new NetworkGenome(2, 3);
        genome.addSynapse(0, 2, 0.5f);
        genome.addSynapse(0, 3, 1.2f);
        genome.addSynapse(0, 4, -1.6f);
        genome.addSynapse(1, 2, 0.3f);
        genome.addSynapse(1, 3, -0.9f);
        genome.addSynapse(1, 4, 0.2f);
        System.out.println(genome);
        NeuralNetwork net = genome.phenotype();
        net.setInput(5.0f, -2.0f);
        System.out.println(net);
        net.tick();
        System.out.println(net);
        List<Float> expected = Arrays.asList(0.87f, 1.00f, 0.00f);
        List<Float> actual = net.outputs();
        Streams.zip(expected.stream(), actual.stream(), (e, a) -> {
            assertEquals(e, a, 1e-6); return null;
        });
    }

    @Test
    public void mutateNode() {
        NetworkGenome genome = new NetworkGenome(2, 3);
        System.out.println(genome);
    }
}