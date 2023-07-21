package io.github.acoboh.query.filter.jpa.exceptions;

import org.springframework.http.HttpStatus;

/**
 * 
 * @author Adrián Cobo
 *
 */
public class QFJsonParseException extends QueryFilterException {

	private static final long serialVersionUID = 1L;

	private final String field;
	private final Object[] arguments;

	/**
	 * Default constructor
	 * 
	 * @param field     field
	 * @param throwable throwable exception
	 */
	public QFJsonParseException(String field, Throwable throwable) {
		super("Error parsing json", throwable);
		this.field = field;
		this.arguments = new Object[] { field, throwable.getLocalizedMessage() };
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
		return "qf.exceptions.json";
	}

}
