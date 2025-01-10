package io.github.acoboh.query.filter.jpa.exceptions;

import org.springframework.http.HttpStatus;

import java.io.Serial;

/**
 * Exception thrown when the field is not sortable
 *
 * @author Adrián Cobo
 */
public class QFNotSortableException extends QueryFilterException {

    @Serial
    private static final long serialVersionUID = 1L;
    private static final String MESSAGE = "The field '{}' is not sortable";

    private final String field;
    private final transient Object[] arguments;

    /**
     * Default constructor
     *
     * @param field field
     */
    public QFNotSortableException(String field) {
        super(MESSAGE, field);
        this.field = field;
        arguments = new Object[]{field};
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
        return "qf.exceptions.notSortable";
    }

}
