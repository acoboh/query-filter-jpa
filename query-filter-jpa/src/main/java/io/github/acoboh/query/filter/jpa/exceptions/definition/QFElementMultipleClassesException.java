package io.github.acoboh.query.filter.jpa.exceptions.definition;

/**
 * Class throw if multiple classes matches the same filter element
 *
 * @author Adrián Cobo
 * @version $Id: $Id
 */
public class QFElementMultipleClassesException extends QueryFilterDefinitionException {

	private static final long serialVersionUID = 1L;
	private static final String MESSAGE = "Multiple classes matches the same element";

	/**
	 * Default constructor
	 */
	public QFElementMultipleClassesException() {
		super(MESSAGE);
	}

}
