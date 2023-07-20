package io.github.acoboh.query.filter.jpa.exceptions.definition;

public class QFMissingFieldException extends QueryFilterDefinitionException {

	private static final long serialVersionUID = 1L;
	private static final String MESSAGE = "The field '{}' is missing in the class '{}'";

	private final String field;
	private final Class<?> filterClass;

	public QFMissingFieldException(String field, Class<?> filterClass) {
		super(MESSAGE, field, filterClass);
		this.field = field;
		this.filterClass = filterClass;
	}

	public String getField() {
		return field;
	}

	public Class<?> getFilterClass() {
		return filterClass;
	}

}
