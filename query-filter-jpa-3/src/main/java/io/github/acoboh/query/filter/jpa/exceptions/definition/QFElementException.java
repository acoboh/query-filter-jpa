package io.github.acoboh.query.filter.jpa.exceptions.definition;

import java.io.Serial;

/**
 * Exception when the field has not presented the annotation
 * {@link io.github.acoboh.query.filter.jpa.annotations.QFDefinitionClass}
 *
 * @author Adrián Cobo
 */
public class QFElementException extends QueryFilterDefinitionException {

    @Serial
    private static final long serialVersionUID = 1L;
    private static final String MESSAGE = "The field '{}' is not present on the class '{}'";

    /**
     * Default constructor
     *
     * @param field field
     * @param clazz class
     */
    public QFElementException(String field, Class<?> clazz) {
        super(MESSAGE, field, clazz);
    }
}
