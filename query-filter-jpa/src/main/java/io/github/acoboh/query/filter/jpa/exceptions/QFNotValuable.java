package io.github.acoboh.query.filter.jpa.exceptions;

import org.springframework.http.HttpStatus;

public class QFNotValuable extends QueryFilterException {

	private static final long serialVersionUID = 1L;
	private static final String MESSAGE = "The field '{}' is not valuable";

	private final String field;
	private final Object[] arguments;

	public QFNotValuable(String field) {
		super(MESSAGE, field);
		this.field = field;
		this.arguments = new Object[] { field };
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
		return "qf.exceptions.notValuable";
	}

}
