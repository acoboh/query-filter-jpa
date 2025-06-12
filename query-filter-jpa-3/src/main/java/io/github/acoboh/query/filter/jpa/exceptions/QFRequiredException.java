package io.github.acoboh.query.filter.jpa.exceptions;

import java.io.Serial;

import org.springframework.http.HttpStatus;

public class QFRequiredException extends QueryFilterException {

	@Serial
	private static final long serialVersionUID = 1L;
	private static final String MESSAGE = "The field '{}' is required but not present in the filter";

	private final String field;

	public QFRequiredException(String field) {
		super(MESSAGE, field);
		this.field = field;
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
		return new Object[]{field};
	}

	/** {@inheritDoc} */
	@Override
	public String getMessageCode() {
		return "qf.exceptions.required";
	}
}
