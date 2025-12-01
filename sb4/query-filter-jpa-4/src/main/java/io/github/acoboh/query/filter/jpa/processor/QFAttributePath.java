package io.github.acoboh.query.filter.jpa.processor;

import java.util.List;

/**
 * QF Attribute Path is a representation of a path to an attribute in a JPA
 * entity. It is used to store the attributes of a path and the final class of
 * the path.
 *
 * @author Adrián Cobo
 * @since 1.0.0
 */
public class QFAttributePath {

    private List<QFAttribute> attributes;
    private Class<?> finalClass;

}
