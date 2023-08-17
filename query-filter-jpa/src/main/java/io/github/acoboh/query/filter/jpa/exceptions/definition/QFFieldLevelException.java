package io.github.acoboh.query.filter.jpa.exceptions.definition;

/**
 * Exception thrown when there is more levels access than object access
 *
 * @author Adrián Cobo
 
 */
public class QFFieldLevelException extends QueryFilterDefinitionException {

	private static final long serialVersionUID = 1L;
	private static final String MESSAGE = "The field '{}' can't access more levels. Last level associated is '{}'";

	/**
	 * Default exception
	 *
	 * @param field     selected field
	 * @param lastLevel last reachable level
	 */
	public QFFieldLevelException(String field, String lastLevel) {
		super(MESSAGE, field, lastLevel);
	}
}
