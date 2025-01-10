package io.github.acoboh.query.filter.jpa.exceptions.definition;

/**
 * Exception thrown when any error about JSON element definition happens
 *
 * @author Adrián Cobo
 */
public class QFJsonException extends QueryFilterDefinitionException {

	private static final long serialVersionUID = 1L;

	/**
	 * Custom message exception
	 *
	 * @param message
	 *            message
	 * @param args
	 *            args of message
	 */
	public QFJsonException(String message, Object... args) {
		super(message, args);
	}

}
