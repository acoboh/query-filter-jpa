package io.github.acoboh.query.filter.jpa.contributor;

/**
 * Custom contributor to list PostgreSQL Array operations
 *
 * @author Adrián Cobo
 */
public enum ArrayFunction {

	OVERLAP("qfArrayOverlap", " && "), EQUAL("qfArrayEqual", " = "), NOT_EQUAL("qfArrayNotEqual", " <> "),
	LESS_THAN("qfArrayLs", " < "), GREATER_THAN("qfArrayGt", " > "), LESS_EQUAL_THAN("qfArrayLte", " <= "),
	GREATER_EQUAL_THAN("qfArrayGte", " >= "), CONTAINS("qfArrayContains", " @> "),
	IS_CONTAINED_BY("qfArrayIsContainedBy", " <@ ");

	private final String name;

	private final QfArraySQLFunction function;

	ArrayFunction(String name, String operator) {
		this.name = name;
		this.function = new QfArraySQLFunction(operator);
	}

	public String getName() {
		return name;
	}

	public QfArraySQLFunction getFunction() {
		return function;
	}

}
