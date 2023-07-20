package io.github.acoboh.query.filter.jpa.exceptions;

import org.springframework.http.HttpStatus;

import io.github.acoboh.query.filter.jpa.operations.QFOperationEnum;

public class QFUnsopportedSQLException extends QueryFilterException {

	private static final long serialVersionUID = 1L;
	private static final String MESSAGE = "The operation {} is unsupported";

	public enum TYPE {
		VALUE, JSON
	}

	public QFUnsopportedSQLException(QFOperationEnum operation) {
		super(MESSAGE, operation);
	}

	@Override
	public HttpStatus getHttpStatus() {
		return HttpStatus.INTERNAL_SERVER_ERROR;
	}

	@Override
	public Object[] getArguments() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getMessageCode() {
		// TODO Auto-generated method stub
		return null;
	}

}
