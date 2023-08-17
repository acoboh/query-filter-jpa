package io.github.acoboh.query.filter.jpa.exceptions;

import org.slf4j.helpers.MessageFormatter;

import io.github.acoboh.query.filter.jpa.exceptions.language.ExceptionLanguageResolver;

/**
 * Just to catch a single exception on QueryFilter
 *
 * @author Adrián Cobo
 * 
 */
public abstract class QueryFilterException extends RuntimeException implements ExceptionLanguageResolver {

	private static final long serialVersionUID = 1L;

	/**
	 * Default constructor
	 *
	 * @param message message
	 * @param args    arguments
	 */
	protected QueryFilterException(String message, Object... args) {
		super(MessageFormatter.arrayFormat(message, args).getMessage());
	}

	/**
	 * Default constructor with cause
	 *
	 * @param message   message
	 * @param throwable cause
	 * @param args      arguments
	 */
	protected QueryFilterException(String message, Throwable throwable, Object... args) {
		super(MessageFormatter.arrayFormat(message, args).getMessage(), throwable);
	}
}
