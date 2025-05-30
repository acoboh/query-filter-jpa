package io.github.acoboh.query.filter.jpa.exceptions.definition;

import java.io.Serial;

import org.slf4j.helpers.MessageFormatter;

/**
 * Base exception class of all query filter definition exceptions
 *
 * @author Adrián Cobo
 */
public class QueryFilterDefinitionException extends Exception {

	@Serial
	private static final long serialVersionUID = 1L;

	/**
	 * Default constructors
	 *
	 * @param message
	 *            message
	 * @param args
	 *            arguments
	 */
	public QueryFilterDefinitionException(String message, Object... args) {
		super(MessageFormatter.arrayFormat(message, args).getMessage());
	}

	/**
	 * Default constructor with cause
	 *
	 * @param message
	 *            message
	 * @param cause
	 *            cause
	 * @param args
	 *            arguments
	 */
	public QueryFilterDefinitionException(String message, Throwable cause, Object... args) {
		super(MessageFormatter.arrayFormat(message, args).getMessage(), cause);
	}

}
