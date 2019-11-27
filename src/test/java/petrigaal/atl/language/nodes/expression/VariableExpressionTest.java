package petrigaal.atl.language.nodes.expression;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class VariableExpressionTest {
    @Test
    void isEqualIfSameName() {
        VariableExpression a1 = new VariableExpression("a");
        VariableExpression a2 = new VariableExpression("a");
        assertEquals(a1, a2);
    }

    @Test
    void isNotEqualIfNameNotSame() {
        VariableExpression a1 = new VariableExpression("a");
        VariableExpression b1 = new VariableExpression("b");
        assertNotEquals(a1, b1);
    }
}