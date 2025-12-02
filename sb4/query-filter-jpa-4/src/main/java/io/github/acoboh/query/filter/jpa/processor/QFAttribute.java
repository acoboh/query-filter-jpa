package io.github.acoboh.query.filter.jpa.processor;

import jakarta.annotation.Nullable;
import jakarta.persistence.metamodel.Attribute;

/**
 * QFAttribute class is used to process attributes in a query
 *
 * @author Adrián Cobo
 * @since 1.0.0
 */
public class QFAttribute {

    private final Attribute<?, ?> attribute;
    private final @Nullable Class<?> treatClass;
    private final String pathName;
    private final boolean isEnum;

    /**
     * <p>
     * Constructor for QFAttribute.
     * </p>
     *
     * @param attribute  attribute of the metamodel
     * @param treatClass the class to treat as
     */
    public QFAttribute(Attribute<?, ?> attribute, @Nullable Class<?> treatClass) {
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

    /**
     * Get the attribute of the metamodel
     *
     * @return a {@link jakarta.persistence.metamodel.Attribute} object
     */
    public Attribute<?, ?> getAttribute() {
        return attribute;
    }

    /**
     * Get the class to treat as
     *
     * @return a {@link java.lang.Class} object
     */
    public @Nullable Class<?> getTreatClass() {
        return treatClass;
    }

    /**
     * Get the path name
     *
     * @return a {@link java.lang.String} object
     */
    public String getPathName() {
        return pathName;
    }

    /**
     * Get if the attribute is an enum
     *
     * @return a {@link java.lang.Boolean} object
     */
    public boolean isEnum() {
        return isEnum;
    }

}
