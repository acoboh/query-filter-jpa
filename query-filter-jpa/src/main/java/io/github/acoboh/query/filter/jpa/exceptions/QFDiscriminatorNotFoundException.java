package io.github.acoboh.query.filter.jpa.exceptions;

import org.springframework.http.HttpStatus;

public class QFDiscriminatorNotFoundException extends QueryFilterException {

	private static final long serialVersionUID = 1L;

	private static final String DISCRIMINATOR_NOT_FOUND = "No discriminator class found for value {} on field {}";

	private final String value;
	private final String field;
	private final Object[] arguments;

	public QFDiscriminatorNotFoundException(String value, String field) {
		super(DISCRIMINATOR_NOT_FOUND, value, field);
		this.value = value;
		this.field = field;
		this.arguments = new Object[] { value, field };
	}

	public String getValue() {
		return value;
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
		return "qf.exceptions.discriminatorTypeMissing";
	}
}
