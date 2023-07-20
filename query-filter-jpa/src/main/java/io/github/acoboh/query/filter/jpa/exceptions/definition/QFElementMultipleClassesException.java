package io.github.acoboh.query.filter.jpa.exceptions.definition;

public class QFElementMultipleClassesException extends QueryFilterDefinitionException {

	private static final long serialVersionUID = 1L;
	private static final String MESSAGE = "Multiple classes matches the same element";

	public QFElementMultipleClassesException() {
		super(MESSAGE);
	}

}
