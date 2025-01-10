package io.github.acoboh.query.filter.jpa.exceptions.definition;

/**
 * Exception thrown if any element is used on default sorting options and the
 * element is not sortable
 *
 * @author Adrián Cobo
 */
public class QFNotSortableDefinitionException extends QueryFilterDefinitionException {

	private static final long serialVersionUID = 1L;

	/**
	 * Default constructor
	 *
	 * @param element
	 *            element not sortable
	 * @param filterClass
	 *            query filter class
	 */
	public QFNotSortableDefinitionException(String element, Class<?> filterClass) {
		super("The element {} is not sortable. Found on class {}", element, filterClass);
	}

}
