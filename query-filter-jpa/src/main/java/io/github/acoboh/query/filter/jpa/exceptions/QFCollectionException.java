package io.github.acoboh.query.filter.jpa.exceptions;

import org.springframework.http.HttpStatus;

public class QFCollectionException extends QueryFilterException {

	private static final String MESSAGE = "The field '{}' can not be used as a collection. Reason {}";

	private final String field;
	private final String reason;
	private final Object[] arguments;

	public QFCollectionException(String field, String reason) {
		super(MESSAGE, reason);
		this.field = field;
		this.reason = reason;
		arguments = new Object[] { field };
	}

	public String getField() {
		return field;
	}

	public String getReason() {
		return reason;
	}

	@Override
	public HttpStatus getHttpStatus() {
		return HttpStatus.INTERNAL_SERVER_ERROR;
	}

	@Override
	public Object[] getArguments() {
		return arguments;
	}

	@Override
	public String getMessageCode() {
		return "qf.exceptions.collection";
	}

}
