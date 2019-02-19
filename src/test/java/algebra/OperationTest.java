package algebra;

import org.junit.Test;

import static org.junit.Assert.*;

public class OperationTest {

    @Test
    public void testBinary() {
        Operation.Binary<Integer> MULTIPLY = (x, y) -> x * y;
        System.out.println(MULTIPLY.operate(5, 6));
    }
}