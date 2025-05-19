package io.github.acoboh.query.filter.jpa.exceptions;

import java.io.Serial;

import org.springframework.http.HttpStatus;

/**
 * Default Exception for QueryFilter
 *
 * @author Adrián Cobo
 */
public class QFParseException extends QueryFilterException {

	private static final String MESSAGE = "Field '{}' can not be parse to QueryFilter from '{}'";

	@Serial
	private static final long serialVersionUID = 1L;

	private final String input;

	private final String field;

	private final transient Object[] arguments;

	/**
	 * Default constructor
	 *
	 * @param field
	 *            field
	 * @param input
	 *            input
	 */
	public QFParseException(String field, String input) {
		super(MESSAGE, field, input);
		this.input = input;
		this.field = field;
		this.arguments = new Object[]{field, input};
	}

	/**
	 * Get input
	 *
	 * @return input
	 */
	public String getInput() {
		return input;
	}

	/**
	 * Get field
	 *
	 * @return field
	 */
	public String getField() {
		return field;
	}

	/** {@inheritDoc} */
	@Override
	public HttpStatus getHttpStatus() {
		return HttpStatus.BAD_REQUEST;
	}

	/** {@inheritDoc} */
	@Override
	public Object[] getArguments() {
		return arguments;
	}

	/** {@inheritDoc} */
	@Override
	public String getMessageCode() {
		return "qf.exceptions.parse";
	}

}
