package io.github.acoboh.query.filter.jpa.exceptions.definition;

public class QFArrayException extends QueryFilterDefinitionException {

	private static final long serialVersionUID = 1L;
	private static final String MESSAGE = "The array level '{}' is not sub-path of the main path '{}'";

	public QFArrayException(String arrayLevel, String fullPath) {
		super(MESSAGE, arrayLevel, fullPath);
	}

}
