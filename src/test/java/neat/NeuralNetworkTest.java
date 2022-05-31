package neat;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Created by dylan on 26/05/2017.
 */
public class NeuralNetworkTest
{
    private NeuralNetwork simpleFullyConnected()
    {
        Neuron n1, n2, n3, n4, n5, n6;
        n1 = new Neuron(0, Collections.emptyList(), Collections.emptyList());
        n2 = new Neuron(1, Collections.emptyList(), Collections.emptyList());
        n1.setType(Neuron.Type.SENSOR); n2.setType(Neuron.Type.SENSOR);
        n1.setActivation(Neuron.Activation.LINEAR); n2.setActivation(Neuron.Activation.LINEAR);

        n3 = new Neuron(2, Arrays.asList(n1, n2), Arrays.asList(5.0,  1.0));
        n4 = new Neuron(3, Arrays.asList(n1, n2), Arrays.asList(-4.0, 6.0));
        n5 = new Neuron(4, Arrays.asList(n1, n2), Arrays.asList(9.1,  7.0));
        n3.setType(Neuron.Type.HIDDEN); n4.setType(Neuron.Type.HIDDEN); n5.setType(Neuron.Type.HIDDEN);
        n3.setActivation(Neuron.Activation.LINEAR); n4.setActivation(Neuron.Activation.LINEAR);
        n5.setActivation(Neuron.Activation.LINEAR);

        n6 = new Neuron(5, Arrays.asList(n3, n4, n5), Arrays.asList(1.0, 4.0, -2.3));
        n6.setType(Neuron.Type.OUTPUT);
        n6.setActivation(Neuron.Activation.LINEAR);

        List<Neuron> neurons = Arrays.asList(n1, n2, n3, n4, n5, n6);
        return new NeuralNetwork(new HashSet<>(neurons));
    }

    @Test
    public void setInput()
    {
        NeuralNetwork net = simpleFullyConnected();
        net.setInput(-5.0, 4.0);
        assertEquals(
                "id:0, state:-5.0, inputs:[]\n" +
                "id:1, state:4.0, inputs:[]\n" +
                "id:2, state:0.0, inputs:[(0, 5.0), (1, 1.0)]\n" +
                "id:3, state:0.0, inputs:[(0, -4.0), (1, 6.0)]\n" +
                "id:4, state:0.0, inputs:[(0, 9.1), (1, 7.0)]\n" +
                "id:5, state:0.0, inputs:[(2, 1.0), (3, 4.0), (4, -2.3)]",
                net.toString());
    }

    @Test
    public void tick() {
        NeuralNetwork net = simpleFullyConnected();
        net.setInput(-5.0, 4.0);
        net.tick();
        assertEquals(
                "id:0, state:0.0, inputs:[]\n" +
                "id:1, state:0.0, inputs:[]\n" +
                "id:2, state:-21.0, inputs:[(0, 5.0), (1, 1.0)]\n" +
                "id:3, state:44.0, inputs:[(0, -4.0), (1, 6.0)]\n" +
                "id:4, state:-17.5, inputs:[(0, 9.1), (1, 7.0)]\n" +
                "id:5, state:0.0, inputs:[(2, 1.0), (3, 4.0), (4, -2.3)]",
                net.toString());
    }

    @Test
    public void outputs() throws Exception
    {
        NeuralNetwork net = simpleFullyConnected();
        net.setInput(-5.0, 4.0);
        net.tick(); net.tick();
        double expectedOutput = 195.25;
        double actualOutput = net.outputs().get(0);
        assertEquals(expectedOutput, actualOutput, 1e-6);
    }
}