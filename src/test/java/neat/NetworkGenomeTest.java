package neat;

import org.junit.Test;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertThat;

/**
 * Created by dylan on 26/05/2017.
 */
public class NetworkGenomeTest
{
    @Test
    public void testConstructingPhenotype()
    {
        NetworkGenome genome = new NetworkGenome(0, 2, 3);
		Random r = new Random();
		double w02 = r.nextDouble(), w03 = r.nextDouble(), w04 = r.nextDouble(),
			   w12 = r.nextDouble(), w13 = r.nextDouble(), w14 = r.nextDouble();
        genome.addSynapse(0, 2, w02);
        genome.addSynapse(0, 3, w03);
        genome.addSynapse(0, 4, w04);
        genome.addSynapse(1, 2, w12);
        genome.addSynapse(1, 3, w13);
        genome.addSynapse(1, 4, w14);
        System.out.println(genome);

        NeuralNetwork net = genome.networkPhenotype();
        double x0 = r.nextDouble(), x1 = r.nextDouble();
        net.setInput(x0, x1);
        net.tick();
		System.out.println(net);

        List<Double> expected = Stream.of(
			x0*w02 + x1*w12, x0*w03 + x1*w13, x0*w04 + x1*w14
		).map(Neuron.Activation.SIGMOID).collect(Collectors.toList());
        List<Double> actual = net.getOutputs();
        assertThat(expected, contains(
        		actual.stream()
						.map(a -> closeTo(a, 1e-6))
						.collect(Collectors.toList()))
		);
    }

    @Test
    public void testMutateNode() {
        NetworkGenome genome = new NetworkGenome(0, 1, 1);
        genome.addSynapse(0, 1, 1);
        System.out.println(genome);
    }
}