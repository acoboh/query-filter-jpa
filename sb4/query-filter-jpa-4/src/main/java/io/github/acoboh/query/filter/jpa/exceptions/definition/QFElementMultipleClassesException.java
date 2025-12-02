package io.github.acoboh.query.filter.jpa.exceptions.definition;

import java.io.Serial;

/**
 * Class throw if multiple classes matches the same filter element
 *
 * @author Adrián Cobo
 */
public class QFElementMultipleClassesException extends QueryFilterDefinitionException {

    @Serial
    private static final long serialVersionUID = 1L;
    private static final String MESSAGE = "Multiple classes matches the same element";

    /**
     * Default constructor
     */
    public QFElementMultipleClassesException() {
        super(MESSAGE);
    }

}
