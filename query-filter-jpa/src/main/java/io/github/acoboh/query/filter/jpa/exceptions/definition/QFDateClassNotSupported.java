package io.github.acoboh.query.filter.jpa.exceptions.definition;

/**
 * Exception thrown when the date class is not supported
 *
 * @author Adrián Cobo
 
 */
public class QFDateClassNotSupported extends QueryFilterDefinitionException {

	private static final long serialVersionUID = 1L;
	private static final String MESSAGE = "Class '{}' is not supported as date on field '{}'";

	private final Class<?> clazz;
	private final String field;

	/**
	 * Default constructor
	 *
	 * @param clazz class not supported
	 * @param field field which is not supported
	 */
	public QFDateClassNotSupported(Class<?> clazz, String field) {
		super(MESSAGE, clazz, field);
		this.clazz = clazz;
		this.field = field;
	}

	/**
	 * Class not supported
	 *
	 * @return class not supported
	 */
	public Class<?> getClazz() {
		return clazz;
	}

	/**
	 * Get field with date annotation
	 *
	 * @return field with date annotation
	 */
	public String getField() {
		return field;
	}

}
