package neat;

import com.google.common.collect.Streams;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by dylan on 26/05/2017.
 */
public class Neuron implements Comparable<Neuron>, Serializable {

    public interface Activation extends Function<Float, Float>, Serializable {

        Activation SIGMOID = z -> 1 / (1 + (float) Math.exp(-z));
        Activation LINEAR = z -> z;
        Activation TANH = x -> (float) Math.tanh(x);

    }

    public enum Type implements Serializable {
        SENSOR("SENSOR"), HIDDEN("HIDDEN"), OUTPUT("OUTPUT");

        private final String value;
        Type(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return value;
        }
    }

    private static final long serialVersionUID = 1L;

    private final Neuron[] inputs;
    private final float[] weights;
    private Type type;
    private final int id;
    private float state = 0, lastState = 0, nextState = 0;
    private float learningRate = 0;
    private Activation activation;
    private int depth = 0;
    private int graphicsX = -1, graphicsY = -1;

    public Neuron(int id, Neuron[] inputs, float[] weights, Type type, Activation activation)
    {
        this.id = id;
        this.inputs = inputs;
        this.weights = weights;
        this.type = type;
        this.activation = activation;
    }

    void tick()
    {
        nextState = 0.0f;
        for (int i = 0; i < inputs.length; i++)
            nextState += inputs[i].getState() * weights[i];
        nextState = activation.apply(nextState);
    }

    void update()
    {
        lastState = state;
        state = nextState;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Neuron)
            return ((Neuron) o).getId() == id;
        return false;
    }

    public int getId() {
        return id;
    }

    public float getState() {
        return state;
    }

    public float getLastState() {
        return lastState;
    }

    public Neuron setState(float s) {
        state = s;
        return this;
    }

    public Neuron setActivation(Neuron.Activation activation) {
        this.activation = activation;
        return this;
    }

    public Neuron[] getInputs() {
        return inputs;
    }

    public float[] getWeights() {
        return weights;
    }

    private float getLearningRate() {
        return learningRate;
    }

    private Neuron setLearningRate(float lr) {
        this.learningRate = lr;
        return this;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    @Override
    public int compareTo(Neuron o) {
        return Comparator.comparingInt(Neuron::getId).compare(this, o);
    }

    @Override
    public String toString()
    {
        StringBuilder s = new StringBuilder(String.format("id:%d, state:%.1f", id, state));
        s.append(", connections: [");
        for (int i = 0; i < weights.length; i++)
            s.append(String.format("(%d, %.1f)", i, weights[i]));
        s.append("]");
        return s.toString();
    }

    public void setDepth(int depth) {
        this.depth = depth;
    }

    public int getDepth() {
        return depth;
    }

    public void setGraphicsPosition(int x, int y) {
        graphicsX = x;
        graphicsY = y;
    }

    public int getGraphicsX() {
        return graphicsX;
    }

    public int getGraphicsY() {
        return graphicsY;
    }

}
