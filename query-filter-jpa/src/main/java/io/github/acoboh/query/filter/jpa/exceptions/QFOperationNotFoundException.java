package io.github.acoboh.query.filter.jpa.exceptions;

import org.springframework.http.HttpStatus;

/**
 * Exception when the operation is not found
 *
 * @author Adrián Cobo
 
 */
public class QFOperationNotFoundException extends QueryFilterException {

	private static final long serialVersionUID = 1L;
	private static final String MESSAGE = "Operation {} is not found";

	private final String operation;

	private final Object[] arguments;

	/**
	 * Default constructor
	 *
	 * @param operation operation
	 */
	public QFOperationNotFoundException(String operation) {
		super(MESSAGE, operation);
		this.operation = operation;
		this.arguments = new Object[] { operation };
	}

	/**
	 * Get operation
	 *
	 * @return operation
	 */
	public String getOperation() {
		return operation;
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
		return "qf.exceptions.operationNotFound";
	}

}
