package io.github.acoboh.query.filter.jpa.processor;

import jakarta.persistence.metamodel.Attribute;

public class QFAttribute {

	private final Attribute<?, ?> attribute;
	private final Class<?> treatClass;
	private final String pathName;
	private final Boolean isEnum;

	public QFAttribute(Attribute<?, ?> attribute, Class<?> treatClass) {
		this.attribute = attribute;
		this.treatClass = treatClass;

		if (treatClass != null && !Void.class.equals(treatClass)) {
			this.pathName = attribute.getName() + "-asTreat-" + treatClass.getSimpleName();
		} else {
			this.pathName = attribute.getName();
		}

		Class<?> javaType = attribute.getJavaType();
		this.isEnum = (javaType.isEnum() || Enum.class.isAssignableFrom(javaType));
	}

	public Attribute<?, ?> getAttribute() {
		return attribute;
	}

	public Class<?> getTreatClass() {
		return treatClass;
	}

	public String getPathName() {
		return pathName;
	}

	public Boolean isEnum() {
		return isEnum;
	}

}
