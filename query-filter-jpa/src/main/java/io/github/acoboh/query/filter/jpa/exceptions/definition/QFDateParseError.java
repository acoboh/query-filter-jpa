package io.github.acoboh.query.filter.jpa.exceptions.definition;

import java.time.format.DateTimeParseException;

public class QFDateParseError extends QueryFilterDefinitionException {

	private static final long serialVersionUID = 1L;

	private static final String MESSAGE = "The format '{}' is not valid for class '{}'. Please check logs";

	private final String format;
	private final Class<?> dateClass;

	public QFDateParseError(String format, Class<?> dateClass, DateTimeParseException e) {
		super(MESSAGE, e, format, dateClass);
		this.format = format;
		this.dateClass = dateClass;
	}

	public String getFormat() {
		return format;
	}

	public Class<?> getDateClass() {
		return dateClass;
	}

}
