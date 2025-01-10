package io.github.acoboh.query.filter.jpa.exceptions;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when multiple sorting options are present on the same field
 *
 * @author Adrián Cobo
 */
public class QFMultipleSortException extends QueryFilterException {

    private static final long serialVersionUID = 1L;
    private static final String MESSAGE = "Multiple query filter sorting properties for the same field '{}'";

    private final String field;
    private final transient Object[] arguments;

    /**
     * Default constructor
     *
     * @param field field
     */
    public QFMultipleSortException(String field) {
        super(MESSAGE, field);
        this.field = field;
        this.arguments = new Object[]{field};
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
     * {@inheritDoc}
     */
    @Override
    public HttpStatus getHttpStatus() {
        return HttpStatus.BAD_REQUEST;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object[] getArguments() {
        return arguments;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getMessageCode() {
        return "qf.exceptions.multipleSort";
    }
}
