package io.github.acoboh.query.filter.jpa.processor;

import java.lang.reflect.Field;

/**
 * Class with info of the instrospect data for any entity model
 * <p>
 * Store info about the actual property like if it is a property or a one to many relation
 *
 * @author Adrián Cobo
 * 
 */
public class QFPath {

	/**
	 * Element type of the path
	 * 
	 * @author Adrián Cobo
	 *
	 */
	public enum QFElementDefType {

		/**
		 * Type is property
		 */
		PROPERTY,

		/**
		 * Type is list
		 */
		LIST,

		/**
		 * Type is a set
		 */
		SET,

		/**
		 * Type is an enumeration
		 */
		ENUM
	}

	private final Field field;
	private final String path;

	private QFElementDefType type;

	private Class<?> fieldClass;

	private boolean isFinal;

	/**
	 * Default constructor
	 *
	 * @param type       element type
	 * @param field      field
	 * @param path       path
	 * @param fieldClass field class
	 * @param isFinal    if the field is final
	 */
	public QFPath(Field field, String path, QFElementDefType type, Class<?> fieldClass, boolean isFinal) {
		this.type = type;
		this.field = field;
		this.path = path;
		this.fieldClass = fieldClass;
		this.isFinal = isFinal;
	}

	/**
	 * Get type of the path
	 *
	 * @return type of path
	 */
	public QFElementDefType getType() {
		return type;
	}

	/**
	 * Set new type
	 *
	 * @param type type
	 */
	public void setType(QFElementDefType type) {
		this.type = type;
	}

	/**
	 * Get field class
	 *
	 * @return field class
	 */
	public Class<?> getFieldClass() {
		return fieldClass;
	}

	/**
	 * Set class of field
	 *
	 * @param fieldClass new class of field
	 */
	public void setFieldClass(Class<?> fieldClass) {
		this.fieldClass = fieldClass;
	}

	/**
	 * Get if the property is the final of the full path
	 *
	 * @return true if it is final path
	 */
	public boolean isFinal() {
		return isFinal;
	}

	/**
	 * Set new value of final
	 *
	 * @param isFinal new value of final
	 */
	public void setFinal(boolean isFinal) {
		this.isFinal = isFinal;
	}

	/**
	 * Get field
	 *
	 * @return field
	 */
	public Field getField() {
		return field;
	}

	/**
	 * Get string path
	 *
	 * @return string path
	 */
	public String getPath() {
		return path;
	}

}
