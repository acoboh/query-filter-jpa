package io.github.acoboh.query.filter.jpa.exceptions;

import io.github.acoboh.query.filter.jpa.operations.QFOperationEnum;
import org.springframework.http.HttpStatus;

import java.io.Serial;

public class QFFilterNotValid extends QueryFilterException {

    @Serial
    private static final long serialVersionUID = 1L;

    private static final String MESSAGE = "Operation '{}' is correctly used on field '{}'. Check the values";

    private final QFOperationEnum operation;

    private final String field;
    private final transient Object[] arguments;

    /**
     * Default constructor
     *
     * @param operation operation not allowed
     * @param field     field
     */
    public QFFilterNotValid(QFOperationEnum operation, String field) {
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
        return "qf.exceptions.not-valid";
    }
}
