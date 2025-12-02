package io.github.acoboh.query.filter.jpa.exceptions;

import org.springframework.http.HttpStatus;

import java.io.Serial;

/**
 * Exception thrown if the field is not supported for the query filter methods
 *
 * @author Adrián Cobo
 * @since 1.0.0
 */
public class QFFieldNotSupported extends QueryFilterException {

    @Serial
    private static final long serialVersionUID = 1L;

    private final String field;
    private final transient Object[] arguments;

    /**
     * Default constructor
     *
     * @param field   field not supported
     * @param message the message to be shown
     */
    public QFFieldNotSupported(String message, String field) {
        super(message, field);
        this.field = field;
        this.arguments = new Object[] { field };
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
        return "qf.exceptions.field-not-supported";
    }
}
