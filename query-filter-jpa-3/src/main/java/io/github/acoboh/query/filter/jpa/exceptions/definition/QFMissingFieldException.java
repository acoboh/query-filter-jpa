package io.github.acoboh.query.filter.jpa.exceptions.definition;

import java.io.Serial;

/**
 * Exception throw when the field is missing on any class
 *
 * @author Adrián Cobo
 */
public class QFMissingFieldException extends QueryFilterDefinitionException {

	@Serial
	private static final long serialVersionUID = 1L;
	private static final String MESSAGE = "The field '{}' is missing in the class '{}'";

	private final String field;
	private final Class<?> filterClass;

	/**
	 * Default constructor
	 *
	 * @param field
	 *            field to be found
	 * @param filterClass
	 *            filter class
	 */
	public QFMissingFieldException(String field, Class<?> filterClass) {
		super(MESSAGE, field, filterClass);
		this.field = field;
		this.filterClass = filterClass;
	}

	/**
	 * Field to be found
	 *
	 * @return field to be found
	 */
	public String getField() {
		return field;
	}

	/**
	 * Filter class
	 *
	 * @return filter class
	 */
	public Class<?> getFilterClass() {
		return filterClass;
	}

}
