package io.github.acoboh.query.filter.jpa.exceptions;

import org.springframework.http.HttpStatus;

/**
 * Default Exception for QueryFilter
 *
 * @author Adrián Cobo
 */
public class QFParseException extends QueryFilterException {

	private static final String MESSAGE = "Field '{}' can not be parse to QueryFilter from '{}'";

	private static final long serialVersionUID = 1L;

	private final String input;

	private final String field;

	private final Object[] arguments;

	public QFParseException(String field, String input) {
		super(MESSAGE, field, input);
		this.input = input;
		this.field = field;
		this.arguments = new Object[] { field, input };
	}

	public String getInput() {
		return input;
	}

	public String getField() {
		return field;
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
		return "qf.exceptions.parse";
	}

}
