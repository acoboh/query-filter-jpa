package io.github.acoboh.query.filter.jpa.exceptions.definition;

import org.slf4j.helpers.MessageFormatter;

public class QueryFilterDefinitionException extends Exception {

	private static final long serialVersionUID = 1L;

	protected QueryFilterDefinitionException(String message, Object... args) {
		super(MessageFormatter.arrayFormat(message, args).getMessage());
	}

	protected QueryFilterDefinitionException(String message, Throwable cause, Object... args) {
		super(MessageFormatter.arrayFormat(message, args).getMessage(), cause);
	}

}
