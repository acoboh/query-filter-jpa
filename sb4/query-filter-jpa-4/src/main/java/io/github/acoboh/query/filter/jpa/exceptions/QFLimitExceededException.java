package io.github.acoboh.query.filter.jpa.exceptions;

import org.springframework.http.HttpStatus;

import java.io.Serial;

/**
 * Exception thrown when the filter input exceeds one of the configured limits
 * ({@code query-filter.limits.*})
 *
 * @author Adrián Cobo
 */
public class QFLimitExceededException extends QueryFilterException {

    private static final String MESSAGE = "The filter exceeds the limit '{}' (maximum {})";

    @Serial
    private static final long serialVersionUID = 1L;

    private final String limit;

    private final int maximum;

    private final transient Object[] arguments;

    /**
     * Default constructor
     *
     * @param limit   name of the exceeded limit (e.g. {@code max-input-length})
     * @param maximum configured maximum
     */
    public QFLimitExceededException(String limit, int maximum) {
        super(MESSAGE, limit, maximum);
        this.limit = limit;
        this.maximum = maximum;
        this.arguments = new Object[] { limit, maximum };
    }

    /**
     * Get the name of the exceeded limit
     *
     * @return limit name
     */
    public String getLimit() {
        return limit;
    }

    /**
     * Get the configured maximum
     *
     * @return maximum
     */
    public int getMaximum() {
        return maximum;
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
        return "qf.exceptions.limit";
    }

}
