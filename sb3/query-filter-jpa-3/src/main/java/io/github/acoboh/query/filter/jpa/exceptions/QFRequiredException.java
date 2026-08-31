package io.github.acoboh.query.filter.jpa.exceptions;

import org.springframework.http.HttpStatus;

import java.io.Serial;

/**
 * Exception thrown when a field annotated with {@code required} on
 * {@link io.github.acoboh.query.filter.jpa.annotations.QFRequired} is missing
 * from the request filter.
 *
 * @author Adrián Cobo
 */
public class QFRequiredException extends QueryFilterException {

    @Serial
    private static final long serialVersionUID = 1L;
    private static final String MESSAGE = "The field '{}' is required but not present in the filter";

    private final String field;

    /**
     * Default constructor
     *
     * @param field name of the required field missing from the filter
     */
    public QFRequiredException(String field) {
        super(MESSAGE, field);
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

    /** {@inheritDoc} */
    @Override
    public HttpStatus getHttpStatus() {
        return HttpStatus.BAD_REQUEST;
    }

    /** {@inheritDoc} */
    @Override
    public Object[] getArguments() {
        return new Object[] { field };
    }

    /** {@inheritDoc} */
    @Override
    public String getMessageCode() {
        return "qf.exceptions.required";
    }
}
