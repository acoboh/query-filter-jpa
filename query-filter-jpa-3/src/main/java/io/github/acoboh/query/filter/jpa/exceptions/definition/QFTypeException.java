package io.github.acoboh.query.filter.jpa.exceptions.definition;

import java.io.Serial;

/**
 * Exception when type parsing failed
 *
 * @author Adrián Cobo
 */
public class QFTypeException extends QueryFilterDefinitionException {

	@Serial
	private static final long serialVersionUID = 1L;
	private static final String MESSAGE = "The field '{}' can not be parsed. {}";

	/**
	 * Default constructor
	 *
	 * @param field
	 *            field
	 * @param reason
	 *            reason
	 */
	public QFTypeException(String field, String reason) {
		super(MESSAGE, field, reason);
	}
}
