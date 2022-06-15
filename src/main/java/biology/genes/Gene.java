package biology.genes;

public abstract class Gene<T> {
    private T value;

    public Gene() {
        value = getNewValue();
    }

    public Gene(T value) {
        this.value = value;
    }

    public abstract <G extends Gene<T>> G createNew(T value);

    public <G extends Gene<T>> G mutate(Gene<?>[] genome) {
        return this.createNew(getNewValue());
    }

    public abstract T getNewValue();

    public T getValue() {
        return value;
    }

    public void setValue(T t) {
        value = t;
    }
}
