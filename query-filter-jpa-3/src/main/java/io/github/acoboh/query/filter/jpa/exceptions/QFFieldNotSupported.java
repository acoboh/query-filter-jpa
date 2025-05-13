package io.github.acoboh.query.filter.jpa.exceptions;

import java.io.Serial;

import org.springframework.http.HttpStatus;

public class QFFieldNotSupported extends QueryFilterException {

	@Serial
	private static final long serialVersionUID = 1L;

	private final String field;
	private final transient Object[] arguments;

	/**
	 * Default constructor
	 *
	 * @param field
	 *            field not supported
	 */
	public QFFieldNotSupported(String message, String field) {
		super(message, field);
		this.field = field;
		this.arguments = new Object[]{field};
	}

	/**
	 * Get field
	 *
	 * @return field
	 */
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
		return "qf.exceptions.field-not-supported";
	}
}
