package io.github.acoboh.query.filter.jpa.exceptions;

import org.springframework.http.HttpStatus;

/**
 * Exception when not valid operation of field
 *
 * @author Adrián Cobo
 */
public class QFOperationNotFoundException extends QueryFilterException {

	private static final long serialVersionUID = 1L;
	private static final String MESSAGE = "Operation {} is not found";

	private final String operation;

	private final Object[] arguments;

	public QFOperationNotFoundException(String operation) {
		super(MESSAGE, operation);
		this.operation = operation;
		this.arguments = new Object[] { operation };
	}

	public String getOperation() {
		return operation;
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
		return "qf.exceptions.operationNotFound";
	}

}
