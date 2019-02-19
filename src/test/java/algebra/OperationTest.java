package algebra;

import org.junit.Test;

import java.util.Random;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public class OperationTest {

	private Random random = new Random();

    @Test
    public void testBinary() {
        Operation.Binary<Integer> MULTIPLY = (x, y) -> x * y;
        Integer a = random.nextInt(), b = random.nextInt();
        assertThat(MULTIPLY.operate(a, b), equalTo(a * b));
    }
}