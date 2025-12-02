package io.github.acoboh.query.filter.jpa.exceptions;

import org.springframework.http.HttpStatus;

import java.io.Serial;

/**
 * Exception thrown if the field can not be used as a collection
 *
 * @author Adrián Cobo
 */
public class QFCollectionException extends QueryFilterException {

    @Serial
    private static final long serialVersionUID = 1L;

    private static final String MESSAGE = "The field '{}' can not be used as a collection. Reason {}";

    private final String field;
    private final String reason;
    private final transient Object[] arguments;

    /**
     * <p>
     * Constructor for QFCollectionException.
     * </p>
     *
     * @param field  field
     * @param reason reason
     */
    public QFCollectionException(String field, String reason) {
        super(MESSAGE, reason);
        this.field = field;
        this.reason = reason;
        arguments = new Object[] { field };
    }

    /**
     * <p>
     * Getter for the field {@code field}.
     * </p>
     *
     * @return field
     */
    public String getField() {
        return field;
    }

    /**
     * <p>
     * Getter for the field {@code reason}.
     * </p>
     *
     * @return reason
     */
    public String getReason() {
        return reason;
    }

    /** {@inheritDoc} */
    @Override
    public HttpStatus getHttpStatus() {
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }

    /** {@inheritDoc} */
    @Override
    public Object[] getArguments() {
        return arguments;
    }

    /** {@inheritDoc} */
    @Override
    public String getMessageCode() {
        return "qf.exceptions.collection";
    }

}
