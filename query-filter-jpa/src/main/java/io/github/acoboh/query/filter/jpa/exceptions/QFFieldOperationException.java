package io.github.acoboh.query.filter.jpa.exceptions;

import org.springframework.http.HttpStatus;

import io.github.acoboh.query.filter.jpa.operations.QFOperationEnum;

/**
 * Exception thrown when the operation is not allowed on a field
 * 
 * @author Adrián Cobo
 *
 */
public class QFFieldOperationException extends QueryFilterException {

	private static final long serialVersionUID = 1L;

	private static final String MESSAGE = "Operation {} is not valid for field '{}' ";

	private final QFOperationEnum operation;

	private final String field;
	private final Object[] arguments;

	/**
	 * Default constructor
	 * 
	 * @param operation operation not allowed
	 * @param field     field
	 */
	public QFFieldOperationException(QFOperationEnum operation, String field) {
		super(MESSAGE, operation, field);
		this.operation = operation;
		this.field = field;
		this.arguments = new Object[] { operation, field };
	}

	/**
	 * Get operation
	 * 
	 * @return operation
	 */
	public QFOperationEnum getOperation() {
		return operation;
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
		return "qf.exceptions.operationFieldNotValid";
	}
}
