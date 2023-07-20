package io.github.acoboh.query.filter.jpa.exceptions;

import org.springframework.http.HttpStatus;

import io.github.acoboh.query.filter.jpa.operations.QFOperationEnum;

public class QFFieldOperationException extends QueryFilterException {

	private static final long serialVersionUID = 1L;

	private static final String MESSAGE = "Operation {} is not valid for field '{}' ";

	private final QFOperationEnum operation;

	private final String field;
	private final Object[] arguments;

	public QFFieldOperationException(QFOperationEnum operation, String field) {
		super(MESSAGE, operation, field);
		this.operation = operation;
		this.field = field;
		this.arguments = new Object[] { operation, field };
	}

	public QFOperationEnum getOperation() {
		return operation;
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
		return "qf.exceptions.operationFieldNotValid";
	}
}
