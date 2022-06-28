package neat;

import org.junit.Test;
import protoevo.neat.Neuron;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.contains;


public class NeuronTest {

    @Test
    public void testTickAndUpdate() {
        Neuron n = new Neuron(0, Collections.emptyList(), Collections.emptyList());
        n.setState(5.0);
        assertThat(n.getState(), closeTo(5.0, 1e-6));
        n.tick();
        assertThat(n.getState(), closeTo(5.0, 1e-6));
        n.update();
        assertThat(n.getState(), closeTo(0.0, 1e-6));

    }

    @Test
    public void testSynapse() {
        Neuron preSynaptic = new Neuron(0, Collections.emptyList(), Collections.emptyList());
        Neuron postSynaptic = new Neuron(1, new ArrayList<>(), new ArrayList<>());
        postSynaptic.addInput(preSynaptic, 5.0f);
        assertThat(postSynaptic.getInputs(), contains(preSynaptic));
        assertThat(postSynaptic.getWeights(), contains(closeTo(5.0, 1e-6)));

        List<Neuron.Activation> activations = Arrays.asList(
                Neuron.Activation.LINEAR, Neuron.Activation.SIGMOID, Neuron.Activation.TANH
        );
        for (Neuron.Activation activation : activations) {
            postSynaptic.setActivation(activation);
            preSynaptic.setState(2.0);
            postSynaptic.tick();
            preSynaptic.tick();
            postSynaptic.update();
            preSynaptic.update();
            double expected = activation.apply(5.0 * 2.0);
            assertThat(postSynaptic.getState(), closeTo(expected, 1e-6));
        }
    }
}