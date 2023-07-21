package io.github.acoboh.query.filter.jpa.contributor;

/**
 * Custom contributor to list PostgreSQL Array operations
 *
 * @author Adrián Cobo
 * @version $Id: $Id
 */
public enum ArrayFunction {

	/**
	 * Overlap function of PostgreSQL Arrays
	 */
	OVERLAP("qfArrayOverlap", " && "),

	/**
	 * Equal function of PostgreSQL Arrays
	 */
	EQUAL("qfArrayEqual", " = "),

	/**
	 * Not equal function of PostgreSQL Arrays
	 */
	NOT_EQUAL("qfArrayNotEqual", " <> "),

	/**
	 * Less function of PostgreSQL Arrays
	 */
	LESS_THAN("qfArrayLs", " < "),

	/**
	 * Greater function of PostgreSQL Arrays
	 */
	GREATER_THAN("qfArrayGt", " > "),

	/**
	 * Less or equal than function of PostgreSQL Arrays
	 */
	LESS_EQUAL_THAN("qfArrayLte", " <= "),

	/**
	 * Greater or equal than function of PostgreSQL Arrays
	 */
	GREATER_EQUAL_THAN("qfArrayGte", " >= "),

	/**
	 * Contains function of PostgreSQL Arrays
	 */
	CONTAINS("qfArrayContains", " @> "),

	/**
	 * Is contained by of PostgreSQL Arrays
	 */
	IS_CONTAINED_BY("qfArrayIsContainedBy", " <@ ");

	private final String name;

	private final QfArraySQLFunction function;

	ArrayFunction(String name, String operator) {
		this.name = name;
		this.function = new QfArraySQLFunction(operator);
	}

	/**
	 * Name of the function
	 *
	 * @return Name of the function
	 */
	public String getName() {
		return name;
	}

	/**
	 * PostgreSQL array function
	 *
	 * @return PostgreSQL array function
	 */
	public QfArraySQLFunction getFunction() {
		return function;
	}

}
