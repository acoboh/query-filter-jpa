package io.github.acoboh.query.filter.jpa.exceptions.definition;

import java.io.Serial;

/**
 * Exception thrown when the array level is not sub-path of the main selected path
 *
 * @author Adrián Cobo
 */
public class QFArrayException extends QueryFilterDefinitionException {

    @Serial
    private static final long serialVersionUID = 1L;
    private static final String MESSAGE = "The array level '{}' is not sub-path of the main path '{}'";

    /**
     * Default constructor for QFArrayExceptions
     *
     * @param arrayLevel actual array level
     * @param fullPath   full path
     */
    public QFArrayException(String arrayLevel, String fullPath) {
        super(MESSAGE, arrayLevel, fullPath);
    }

}
