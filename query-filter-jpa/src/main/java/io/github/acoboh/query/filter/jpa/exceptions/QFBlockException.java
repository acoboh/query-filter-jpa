package io.github.acoboh.query.filter.jpa.exceptions;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when the field is marked as blocked and the user is trying to filter from string filters
 *
 * @author Adrián Cobo
 * 
 */
public class QFBlockException extends QueryFilterException {

	private static final long serialVersionUID = 1L;
	private static final String MESSAGE = "Field '{}' is blocked from parsing. Operation not allowed";

	private final String field;
	private final Object[] arguments;

	/**
	 * Construct
	 *
	 * @param field name of the field
	 */
	public QFBlockException(String field) {
		super(MESSAGE, field);
		this.field = field;
		this.arguments = new Object[] { field };
	}

	/**
	 * Get name of the field
	 *
	 * @return name of the field
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
		return "qf.exceptions.blocked";
	}
}
