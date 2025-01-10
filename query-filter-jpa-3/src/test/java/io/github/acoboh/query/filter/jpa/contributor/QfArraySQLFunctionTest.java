package io.github.acoboh.query.filter.jpa.contributor;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

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

		assertEquals(" && ", ArrayFunction.OVERLAP.getOperator());
		assertEquals(" = ", ArrayFunction.EQUAL.getOperator());
		assertEquals(" <> ", ArrayFunction.NOT_EQUAL.getOperator());
		assertEquals(" < ", ArrayFunction.LESS_THAN.getOperator());
		assertEquals(" > ", ArrayFunction.GREATER_THAN.getOperator());
		assertEquals(" <= ", ArrayFunction.LESS_EQUAL_THAN.getOperator());
		assertEquals(" >= ", ArrayFunction.GREATER_EQUAL_THAN.getOperator());
		assertEquals(" @> ", ArrayFunction.CONTAINS.getOperator());
		assertEquals(" <@ ", ArrayFunction.IS_CONTAINED_BY.getOperator());

	}

}
