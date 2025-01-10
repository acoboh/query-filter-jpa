package io.github.acoboh.query.filter.jpa.exceptions.definition;

import java.io.Serial;

/**
 * Exception throw for all the discriminator exceptions
 *
 * @author Adrián Cobo
 * 
 */
public class QFDiscriminatorException extends QueryFilterDefinitionException {

	@Serial
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
