package io.github.acoboh.query.filter.jpa.exceptions.definition;

import io.github.acoboh.query.filter.jpa.annotations.QFDefinitionClass;

/**
 * Exception when the field has not presented the annotation {@link QFDefinitionClass}
 *
 * @author Adrián Cobo
 */
public class QFElementException extends QueryFilterDefinitionException {

	private static final long serialVersionUID = 1L;
	private static final String MESSAGE = "The field '{}' is not present on the class '{}'";

	/**
	 * Default constructor
	 * 
	 * @param field field
	 * @param clazz class
	 */
	public QFElementException(String field, Class<?> clazz) {
		super(MESSAGE, field, clazz);
	}
}
