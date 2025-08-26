import entity.Result;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ServiceTest {
    private static Result result;

    @BeforeAll
    static void initAll() {
        result = new Result();
    }

    @Test
    public void testInCircleTrue() {
        assertTrue(result.checkHit(1, -1, 2), "Точка (1, -1) должна попасть в фигуры с r = 2");
    }

    @Test
    public void testInCircleFalse() {
        assertFalse(result.checkHit(2, -2, 2), "Точка (2, -2) не должна попасть в фигуры с r = 2");
    }

    @Test
    public void testInRectangleTrue() {
        assertTrue(result.checkHit(-1, 1, 3), "Точка (-1, 1) должна попасть в фигуры с r = 3");
    }

    @Test
    public void testInRectangleFalse() {
        assertTrue(result.checkHit(-5, 4, 3), "Точка (-5, 4) не должна попасть в фигуры с r = 3");
    }

    @Test
    public void testInTriangleFalse() {
        assertFalse(result.checkHit(-3, -3, 3), "Точка (-3, 3) не должна попасть в фигуры с r = 3");
    }

    @Test
    public void testInTriangleTrue() {
        assertFalse(result.checkHit(-3, 0, 3), "Точка (-3, 0) должна попасть в фигуры с r = 3");
    }

}
