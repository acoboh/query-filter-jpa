package io.github.acoboh.query.filter.jpa.exceptions;

import org.springframework.http.HttpStatus;

/**
 * Exception throw the selected discriminator value is not present
 *
 * @author Adrián Cobo
 * @version $Id: $Id
 */
public class QFDiscriminatorNotFoundException extends QueryFilterException {

	private static final long serialVersionUID = 1L;

	private static final String DISCRIMINATOR_NOT_FOUND = "No discriminator class found for value {} on field {}";

	private final String value;
	private final String field;
	private final Object[] arguments;

	/**
	 * Default constructor
	 *
	 * @param value selected value
	 * @param field selected field
	 */
	public QFDiscriminatorNotFoundException(String value, String field) {
		super(DISCRIMINATOR_NOT_FOUND, value, field);
		this.value = value;
		this.field = field;
		this.arguments = new Object[] { value, field };
	}

	/**
	 * Get filtered value
	 *
	 * @return filtered value
	 */
	public String getValue() {
		return value;
	}

	/**
	 * Get filter field
	 *
	 * @return filter field
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
		return "qf.exceptions.discriminatorTypeMissing";
	}
}
