package neat;

import com.sun.istack.internal.NotNull;
import org.junit.Before;
import org.junit.Test;

import java.util.*;
import java.util.stream.Collectors;

import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertThat;

/**
 * Created by dylan on 26/05/2017.
 */
public class NeuralNetworkTest
{
	private Random random = new Random();

	// Weights for feed forward network
	private double w13, w14, w15, w23, w24, w25, w36, w46, w56;
	// Weights for recurrent network
	private double w12, w21;

	@Before
	public void setUp() {
		w13 = random.nextDouble(); w14 = random.nextDouble(); w15 = random.nextDouble();
		w23 = random.nextDouble(); w24 = random.nextDouble(); w25 = random.nextDouble();
		w36 = random.nextDouble(); w46 = random.nextDouble(); w56 = random.nextDouble();

		w12 = random.nextDouble(); w21 = random.nextDouble();
	}

    @NotNull
    private NeuralNetwork getSimpleFullyConnected()
    {
        Neuron n1, n2, n3, n4, n5, n6;
        n1 = new Neuron(0, Collections.emptyList(), Collections.emptyList());
        n2 = new Neuron(1, Collections.emptyList(), Collections.emptyList());
        n1.setType(Neuron.Type.SENSOR); n2.setType(Neuron.Type.SENSOR);
        n1.setActivation(Neuron.Activation.LINEAR); n2.setActivation(Neuron.Activation.LINEAR);

        n3 = new Neuron(2, Arrays.asList(n1, n2), Arrays.asList(w13, w23));
        n4 = new Neuron(3, Arrays.asList(n1, n2), Arrays.asList(w14, w24));
        n5 = new Neuron(4, Arrays.asList(n1, n2), Arrays.asList(w15, w25));
        n3.setType(Neuron.Type.HIDDEN); n4.setType(Neuron.Type.HIDDEN); n5.setType(Neuron.Type.HIDDEN);
        n3.setActivation(Neuron.Activation.LINEAR); n4.setActivation(Neuron.Activation.LINEAR);
        n5.setActivation(Neuron.Activation.LINEAR);

        n6 = new Neuron(5, Arrays.asList(n3, n4, n5), Arrays.asList(w36, w46, w56));
        n6.setType(Neuron.Type.OUTPUT);
        n6.setActivation(Neuron.Activation.LINEAR);

        List<Neuron> neurons = Arrays.asList(n1, n2, n3, n4, n5, n6);
        return new NeuralNetwork(new HashSet<>(neurons));
    }

    @NotNull
	private NeuralNetwork getSimpleRecurrentNetwork() {

    	Neuron n1, n2;
    	n1 = new Neuron(0, Neuron.Type.SENSOR, Neuron.Activation.LINEAR);
    	n2 = new Neuron(1, Neuron.Type.SENSOR, Neuron.Activation.LINEAR);
    	n1.addInput(n2, w21);
    	n2.addInput(n1, w12);
		return new NeuralNetwork(new HashSet<>(Arrays.asList(n1, n2)));
	}

    @Test
    public void testSetInput()
    {
        NeuralNetwork net = getSimpleFullyConnected();
        double x0 = random.nextDouble(), x1 = random.nextDouble();
        net.setInput(x0, x1);
		List<Double> expectedState = Arrays.asList(x0, x1, 0.0, 0.0, 0.0, 0.0);
		List<Double> actualState = net.getFullState();
		assertThat(expectedState, contains(
				actualState.stream().map(o -> closeTo(o, 1e-6)).collect(Collectors.toList())
		));
    }

    @Test
    public void testNetworkPropagate() {
        NeuralNetwork net = getSimpleFullyConnected();
		double x0 = random.nextDouble(), x1 = random.nextDouble();
		net.setInput(x0, x1);
        net.tick();
		List<Double> expectedState = Arrays.asList(
				0.0, 0.0,
				x0*w13 + x1*w23,
				x0*w14 + x1*w24,
				x0*w15 + x1*w25,
				0.0
		);
		List<Double> actualState = net.getFullState();
		assertThat(expectedState, contains(
				actualState.stream().map(o -> closeTo(o, 1e-6)).collect(Collectors.toList())
		));
    }

    @Test
    public void testGetOutputs() {
        NeuralNetwork net = getSimpleFullyConnected();
		double x0 = random.nextDouble(), x1 = random.nextDouble();
		net.setInput(x0, x1);
        net.tick(); net.tick();
        List<Double> expectedOutputs = Collections.singletonList(
				w36 * (x0*w13 + x1*w23) +
				w46 * (x0*w14 + x1*w24) +
				w56 * (x0*w15 + x1*w25)
		);
        List<Double> actualOutputs = net.getOutputs();
        assertThat(expectedOutputs, contains(
        		actualOutputs.stream().map(o -> closeTo(o, 1e-6)).collect(Collectors.toList())
		));
    }

	@Test
	public void testRecurrentPropagate() {
    	NeuralNetwork net = getSimpleRecurrentNetwork();
		double x0 = random.nextDouble(), x1 = random.nextDouble();
		net.setInput(x0, x1);
    	net.tick();
		List<Double> expectedOutputs = Arrays.asList(w21*x1, w12*x0);
		List<Double> actualOutputs = net.getFullState();
		assertThat(expectedOutputs, contains(
				actualOutputs.stream().map(o -> closeTo(o, 1e-6)).collect(Collectors.toList())
		));
	}
}