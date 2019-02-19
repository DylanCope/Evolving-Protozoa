package algebra;

public interface Set<T> {

    public boolean includes(T x);
    public Cardinal cardinality();

}
