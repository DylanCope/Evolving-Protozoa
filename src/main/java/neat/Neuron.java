package neat;

import com.google.common.collect.Streams;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by dylan on 26/05/2017.
 */
public class Neuron implements Comparable<Neuron> {

    public interface Activation extends Function<Double, Double> {

        Activation SIGMOID = z -> 1 / (1 + Math.exp(-z));
        Activation LINEAR = z -> z;
        Activation TANH = Math::tanh;
    }

    public enum Type {
        SENSOR("SENSOR"), HIDDEN("HIDDEN"), OUTPUT("OUTPUT");

        private String value;
        Type(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return value;
        }
    }

    private List<Neuron> inputs;
    private List<Double> weights;
    private Type type;
    private int id;
    private double state = 0;
    private double next_state = 0;
    private double learningRate = 0;
    private Activation activation = Activation.TANH;

    public Neuron(int id, Type type, Activation activation)
    {
        this(id, new ArrayList<>(), new ArrayList<>());
        this.type = type;
        this.activation = activation;
    }

    public Neuron(int id, List<Neuron> inputs, List<Double> weights)
    {
        this.id = id;
        this.inputs = inputs;
        this.weights = weights;
    }

    void tick()
    {
        next_state = Streams.zip(
                inputs.stream().map(Neuron::getState),
                weights.stream(),
                (x, y) -> x * y)
            .reduce(Double::sum)
            .map(activation)
            .orElse(0.0);
    }

    void update()
    {
        state = next_state;
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

    public double getState() {
        return state;
    }

    public Neuron setState(double s) {
        state = s;
        return this;
    }

    public Neuron setActivation(Neuron.Activation activation) {
        this.activation = activation;
        return this;
    }

    public void addInput(Neuron in, Double weight) {
        inputs.add(in);
        weights.add(weight);
    }

    public Neuron setInputs(List<Neuron> inputs) {
        this.inputs = inputs;
        return this;
    }

    public List<Neuron> getInputs() {
        return inputs;
    }

    public Neuron setWeights(List<Double> weights) {
        this.weights = weights;
        return this;
    }

    public List<Double> getWeights() {
        return weights;
    }

    private double getLearningRate() {
        return learningRate;
    }

    private Neuron setLearningRate(double lr) {
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
        String connections = String.format("[%s]",
                Streams.zip(inputs.stream(), weights.stream(),
                        (i, w) -> String.format("(%d, %.1f)", i.getId(), w))
                        .collect(Collectors.joining(", ")));
        return String.format("id:%d, state:%.1f, inputs:%s", id, state, connections);
    }
}
