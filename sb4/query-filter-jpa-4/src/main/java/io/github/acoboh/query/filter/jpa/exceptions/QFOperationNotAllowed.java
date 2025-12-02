package io.github.acoboh.query.filter.jpa.exceptions;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown if the operation is not allowed for the field
 *
 * @author Adrián Cobo
 * @since 1.0.0
 */
public class QFOperationNotAllowed extends QueryFilterException {

    private final String field;
    private final String operation;

    private final transient Object[] arguments;

    /**
     * Default constructor
     *
     * @param field     the field of the filter
     * @param operation disallowed operation
     */
    public QFOperationNotAllowed(String field, String operation) {
        super("The operation '{}' is not allowed for the field '{}'", operation, field);
        this.field = field;
        this.operation = operation;
        this.arguments = new Object[] { operation, field };
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
    public String getOperation() {
        return operation;
    }

    /** {@inheritDoc} */
    @Override
    public HttpStatus getHttpStatus() {
        return HttpStatus.FORBIDDEN;
    }

    /** {@inheritDoc} */
    @Override
    public Object[] getArguments() {
        return arguments;
    }

    /** {@inheritDoc} */
    @Override
    public String getMessageCode() {
        return "qf.exceptions.operation-not-allowed";
    }
}
