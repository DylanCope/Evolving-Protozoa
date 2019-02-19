package algebra;

import java.util.List;
import java.util.function.Function;

@FunctionalInterface
interface Operation<T> extends Function<List<T>, T> {

    interface Unary<T> extends Operation<T> {

        @Override
        default T apply(List<T> x) {
            return this.operate(x.get(0));
        }

        T operate(T x);
    }

    interface Binary<T> extends Operation<T> {

        @Override
        default T apply(List<T> x) {
            return this.operate(x.get(0), x.get(1));
        }

        T operate(T x, T y);
    }
}