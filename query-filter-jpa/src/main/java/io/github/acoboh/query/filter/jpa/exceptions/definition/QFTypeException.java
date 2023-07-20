package io.github.acoboh.query.filter.jpa.exceptions.definition;

/**
 * Exception when type parsing failed
 *
 * @author Adrián Cobo
 */
public class QFTypeException extends QueryFilterDefinitionException {

	private static final long serialVersionUID = 1L;
	private static final String MESSAGE = "The field '{field}' can not be parsed. {reason}";

	public QFTypeException(String field, String reason) {
		super(MESSAGE, field, reason);
	}
}
