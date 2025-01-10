package io.github.acoboh.query.filter.jpa.exceptions;

import java.io.Serial;
import java.time.format.DateTimeParseException;

import org.springframework.http.HttpStatus;

/**
 * Exception throw when any date formating error occurs
 *
 * @author Adrián Cobo
 */
public class QFDateParsingException extends QueryFilterException {

	@Serial
	private static final long serialVersionUID = 1L;

	private static final String MESSAGE = "The field '{}' can not be parsed with value '{}'. Check format '{}'";

	private final String field;
	private final String value;
	private final String format;
	private final transient Object[] arguments;

	/**
	 * Default constructor
	 *
	 * @param field
	 *            field to be parsed
	 * @param value
	 *            value to be parsed
	 * @param format
	 *            original format
	 * @param e
	 *            original date time parsing exception
	 */
	public QFDateParsingException(String field, String value, String format, DateTimeParseException e) {
		super(MESSAGE, e, field, value, format, e);
		this.field = field;
		this.value = value;
		this.format = format;
		this.arguments = new Object[]{value, field, format};
	}

	/**
	 * Get affected field
	 *
	 * @return get field
	 */
	public String getField() {
		return field;
	}

	/**
	 * Get original value
	 *
	 * @return original value
	 */
	public String getValue() {
		return value;
	}

	/**
	 * Get the default format
	 *
	 * @return default format
	 */
	public String getFormat() {
		return format;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public HttpStatus getHttpStatus() {
		return HttpStatus.BAD_REQUEST;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object[] getArguments() {
		return arguments;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getMessageCode() {
		return "qf.exceptions.dateParse";
	}

}
