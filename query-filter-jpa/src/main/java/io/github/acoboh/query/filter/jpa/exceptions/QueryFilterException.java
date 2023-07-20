package io.github.acoboh.query.filter.jpa.exceptions;

import org.slf4j.helpers.MessageFormatter;

import io.github.acoboh.query.filter.jpa.exceptions.language.ExceptionLanguageResolver;

/**
 * Just to catch a single exception on QueryFilter
 *
 * @author Adrián Cobo
 */
public abstract class QueryFilterException extends RuntimeException implements ExceptionLanguageResolver {

	private static final long serialVersionUID = 1L;

	protected QueryFilterException(String message, Object... args) {
		super(MessageFormatter.arrayFormat(message, args).getMessage());
	}

	protected QueryFilterException(String message, Throwable throwable, Object... args) {
		super(MessageFormatter.arrayFormat(message, args).getMessage(), throwable);
	}
}
