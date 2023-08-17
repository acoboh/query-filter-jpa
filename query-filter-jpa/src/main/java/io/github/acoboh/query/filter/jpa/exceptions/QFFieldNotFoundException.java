package io.github.acoboh.query.filter.jpa.exceptions;

import org.springframework.http.HttpStatus;

/**
 * Exception if the field is not found on the filter
 *
 * @author Adrián Cobo
 * 
 */
public class QFFieldNotFoundException extends QueryFilterException {

	private static final long serialVersionUID = 1L;

	private static final String MESSAGE = "Field {} not found";

	private final String field;
	private final Object[] arguments;

	/**
	 * Default constructor
	 *
	 * @param field field not found
	 */
	public QFFieldNotFoundException(String field) {
		super(MESSAGE, field);
		this.field = field;
		this.arguments = new Object[] { field };
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
		return "qf.exceptions.missingField";
	}

}
