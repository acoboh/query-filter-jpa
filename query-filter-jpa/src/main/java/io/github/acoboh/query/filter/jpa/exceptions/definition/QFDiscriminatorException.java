package io.github.acoboh.query.filter.jpa.exceptions.definition;

/**
 * Exception throw for all the discriminator exceptions
 *
 * @author Adrián Cobo
 */
public class QFDiscriminatorException extends QueryFilterDefinitionException {

    private static final long serialVersionUID = 1L;

    /**
     * Default constructor
     *
     * @param message message
     * @param args    arguments of message
     */
    public QFDiscriminatorException(String message, Object... args) {
        super(message, args);
    }

}
