package io.github.acoboh.query.filter.jpa.contributor;

import org.hibernate.type.BooleanType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for array SQL functions
 *
 * @author Adrián Cobo
 */
class QfArraySQLFunctionTest {

    @DisplayName("Check functions test")
    @Test
    void checkFunctionName() {

        assertEquals(9, ArrayFunction.values().length);

        assertEquals("qfArrayOverlap", ArrayFunction.OVERLAP.getName());
        assertEquals("qfArrayEqual", ArrayFunction.EQUAL.getName());
        assertEquals("qfArrayNotEqual", ArrayFunction.NOT_EQUAL.getName());
        assertEquals("qfArrayLs", ArrayFunction.LESS_THAN.getName());
        assertEquals("qfArrayGt", ArrayFunction.GREATER_THAN.getName());
        assertEquals("qfArrayLte", ArrayFunction.LESS_EQUAL_THAN.getName());
        assertEquals("qfArrayGte", ArrayFunction.GREATER_EQUAL_THAN.getName());
        assertEquals("qfArrayContains", ArrayFunction.CONTAINS.getName());
        assertEquals("qfArrayIsContainedBy", ArrayFunction.IS_CONTAINED_BY.getName());

        assertEquals(" && ", ArrayFunction.OVERLAP.getFunction().getOperator());
        assertEquals(" = ", ArrayFunction.EQUAL.getFunction().getOperator());
        assertEquals(" <> ", ArrayFunction.NOT_EQUAL.getFunction().getOperator());
        assertEquals(" < ", ArrayFunction.LESS_THAN.getFunction().getOperator());
        assertEquals(" > ", ArrayFunction.GREATER_THAN.getFunction().getOperator());
        assertEquals(" <= ", ArrayFunction.LESS_EQUAL_THAN.getFunction().getOperator());
        assertEquals(" >= ", ArrayFunction.GREATER_EQUAL_THAN.getFunction().getOperator());
        assertEquals(" @> ", ArrayFunction.CONTAINS.getFunction().getOperator());
        assertEquals(" <@ ", ArrayFunction.IS_CONTAINED_BY.getFunction().getOperator());

    }

    @DisplayName("Check array function render")
    @Test
    void checkQfSQLFunction() {
        // Test function
        QfArraySQLFunction qf = new QfArraySQLFunction(" operator ");

        assertEquals(" operator ", qf.getOperator());

        assertTrue(qf.hasArguments());
        assertTrue(qf.hasParenthesesIfNoArguments());
        assertEquals(BooleanType.INSTANCE, qf.getReturnType(null, null));

        List<String> list = Arrays.asList("1", "2", "3");

        String render = qf.render(null, list, null);

        assertEquals("(1 operator ARRAY[2, 3]) and true ", render);
    }

}
