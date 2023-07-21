package io.github.acoboh.query.filter.jpa.exceptions.definition;

/**
 * Exception thrown when the same element contains multiple type annotations
 * 
 * @author Adrián Cobo
 *
 */
public class QFAnnotationsException extends QueryFilterDefinitionException {

	private static final long serialVersionUID = 1L;

	/**
	 * Default constructor
	 */
	public QFAnnotationsException() {
		super("Can not define different element annotations on the same field");
	}

}
