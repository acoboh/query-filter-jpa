package io.github.acoboh.query.filter.jpa.exceptions;

import org.springframework.http.HttpStatus;

import io.github.acoboh.query.filter.jpa.operations.QFOperationEnum;

/**
 * Exception when operation is not allowed on SQL database
 *
 * @author Adrián Cobo
 * 
 */
public class QFUnsopportedSQLException extends QueryFilterException {

	private static final long serialVersionUID = 1L;
	private static final String MESSAGE = "The operation {} is unsupported";

	private final transient Object[] args;
	private final String field;
	private final QFOperationEnum operation;

	/**
	 * Default constructor
	 *
	 * @param operation operation not allowed
	 * @param field     field
	 */
	public QFUnsopportedSQLException(QFOperationEnum operation, String field) {
		super(MESSAGE, operation);
		args = new Object[] { operation, field };
		this.operation = operation;
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

	/**
	 * Get operation
	 *
	 * @return operation
	 */
	public QFOperationEnum getOperation() {
		return operation;
	}

	/** {@inheritDoc} */
	@Override
	public HttpStatus getHttpStatus() {
		return HttpStatus.INTERNAL_SERVER_ERROR;
	}

	/** {@inheritDoc} */
	@Override
	public Object[] getArguments() {
		return args;
	}

	/** {@inheritDoc} */
	@Override
	public String getMessageCode() {
		return "qf.exceptions.operationFieldNotValid";
	}

}
