package io.github.acoboh.query.filter.jpa.exceptions.definition;

import java.io.Serial;

/**
 * Exception thrown if the element can not be filter collection type
 *
 * @author Adrián Cobo
 */
public class QFCollectionNotSupported extends QueryFilterDefinitionException {

	@Serial
	private static final long serialVersionUID = 1L;
	private static final String MESSAGE = "The filter {} on class {} is not allowed to be annotated with @QFCollectionElement. The field type is {} and must be SET or LIST";

	private final String filterName;
	private final Class<?> filterClass;
	private final Class<?> actualType;

	/**
	 * Default constructor
	 *
	 * @param filterName
	 *            filter name
	 * @param filterClass
	 *            filter class
	 * @param type
	 *            element type
	 */
	public QFCollectionNotSupported(String filterName, Class<?> filterClass, Class<?> type) {
		super(MESSAGE, filterName, filterClass, type);
		this.filterName = filterName;
		this.filterClass = filterClass;
		this.actualType = type;
	}

	/**
	 * Get the filter name
	 *
	 * @return filter name
	 */
	public String getFilterName() {
		return filterName;
	}

	/**
	 * Get the filter class
	 *
	 * @return filter class
	 */
	public Class<?> getFilterClass() {
		return filterClass;
	}

	/**
	 * Get the actual type
	 *
	 * @return actual type
	 */
	public Class<?> getActualType() {
		return actualType;
	}

}
