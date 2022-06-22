package biology.genes;

import core.Simulation;

import java.io.Serializable;

public abstract class Gene<T> implements Serializable {
    public static final long serialVersionUID = -1504556284113269258L;
    private T value;
    private int numMutations = 0;
    public static int totalMutations = 0;

    public Gene() {
        value = getNewValue();
    }

    public Gene(T value) {
        this.value = value;
    }

    public abstract <G extends Gene<T>> G createNew(T value);

    public <G extends Gene<T>> G createNew(T value, int numMutations) {
        G gene = createNew(value);
        gene.setNumMutations(numMutations);
        totalMutations++;
        return gene;
    }

    public <G extends Gene<T>> G mutate(Gene<?>[] genome) {
        return this.createNew(getNewValue(), numMutations + 1);
    }

    public Gene<?> crossover(Gene<?> other) {
        if (Simulation.RANDOM.nextBoolean())
            return this;
        else
            return other;
    }

    public void setNumMutations(int numMutations) {
        this.numMutations = numMutations;
    }

    public abstract T getNewValue();

    public T getValue() {
        return value;
    }

    public void setValue(T t) {
        value = t;
    }

    public int getNumMutations() {
        return numMutations;
    }
}
