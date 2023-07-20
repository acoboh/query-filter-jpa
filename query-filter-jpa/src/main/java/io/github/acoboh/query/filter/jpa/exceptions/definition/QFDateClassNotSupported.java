package io.github.acoboh.query.filter.jpa.exceptions.definition;

public class QFDateClassNotSupported extends QueryFilterDefinitionException {

	private static final long serialVersionUID = 1L;
	private static final String MESSAGE = "Class '{}' is not supported as date on field '{}'";

	private final Class<?> clazz;
	private final String field;

	public QFDateClassNotSupported(Class<?> clazz, String field) {
		super(MESSAGE, clazz, field);
		this.clazz = clazz;
		this.field = field;
	}

	public Class<?> getClazz() {
		return clazz;
	}

	public String getField() {
		return field;
	}

}
