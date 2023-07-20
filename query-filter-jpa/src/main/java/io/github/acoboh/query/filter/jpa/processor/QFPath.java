package io.github.acoboh.query.filter.jpa.processor;

import java.lang.reflect.Field;

public class QFPath {

	private final Field field;
	private final String path;

	private QueryFilterElementDefType type;

	private Class<?> fieldClass;

	private boolean isFinal = true;

	public QFPath(QueryFilterElementDefType type, Field field, String path) {
		this.type = type;
		this.field = field;
		this.path = path;
		this.fieldClass = field.getType();
	}

	public enum QueryFilterElementDefType {
		PROPERTY, LIST, SET, ENUM
	}

	public QueryFilterElementDefType getType() {
		return type;
	}

	public void setType(QueryFilterElementDefType type) {
		this.type = type;
	}

	public Class<?> getFieldClass() {
		return fieldClass;
	}

	public void setFieldClass(Class<?> fieldClass) {
		this.fieldClass = fieldClass;
	}

	public boolean isFinal() {
		return isFinal;
	}

	public void setFinal(boolean isFinal) {
		this.isFinal = isFinal;
	}

	public Field getField() {
		return field;
	}

	public String getPath() {
		return path;
	}

}
