package io.github.acoboh.query.filter.jpa.exceptions;

import java.time.format.DateTimeParseException;

import org.springframework.http.HttpStatus;

public class QFDateParsingException extends QueryFilterException {

	private static final long serialVersionUID = 1L;

	private static final String MESSAGE = "The field '{}' can not be parsed with value '{}'. Check format '{}'";

	private final String field;
	private final String value;
	private final String format;
	private final Object[] arguments;

	public QFDateParsingException(String field, String value, String format, DateTimeParseException e) {
		super(MESSAGE, e, field, value, format, e);
		this.field = field;
		this.value = value;
		this.format = format;
		this.arguments = new Object[] { value, field, format };
	}

	public String getField() {
		return field;
	}

	public String getValue() {
		return value;
	}

	public String getFormat() {
		return format;
	}

	@Override
	public HttpStatus getHttpStatus() {
		return HttpStatus.BAD_REQUEST;
	}

	@Override
	public Object[] getArguments() {
		return arguments;
	}

	@Override
	public String getMessageCode() {
		return "qf.exceptions.dateParse";
	}

}
