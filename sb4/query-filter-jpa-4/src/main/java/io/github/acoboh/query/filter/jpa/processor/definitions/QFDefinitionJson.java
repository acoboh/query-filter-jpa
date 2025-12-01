package io.github.acoboh.query.filter.jpa.processor.definitions;

import io.github.acoboh.query.filter.jpa.annotations.QFJsonElement;
import io.github.acoboh.query.filter.jpa.exceptions.definition.QFJsonException;
import io.github.acoboh.query.filter.jpa.exceptions.definition.QueryFilterDefinitionException;
import io.github.acoboh.query.filter.jpa.operations.QFOperationJsonEnum;
import io.github.acoboh.query.filter.jpa.processor.QFAttribute;
import io.github.acoboh.query.filter.jpa.processor.QFSpecificationPart;
import io.github.acoboh.query.filter.jpa.processor.match.QFJsonElementMatch;
import jakarta.persistence.Column;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.metamodel.Metamodel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Set;

/**
 * Definition for JSON filter fields
 *
 * @author Adrián Cobo
 */
public class QFDefinitionJson extends QFAbstractDefinition {

    private static final Logger LOGGER = LoggerFactory.getLogger(QFDefinitionJson.class);

    private final QFJsonElement jsonAnnotation;
    private final List<QFAttribute> attributes;
    private final List<JoinType> joinTypes;
    private final Set<QFOperationJsonEnum> allowedOperations;

    QFDefinitionJson(FilterFieldInfo fieldInfo, QFJsonElement jsonAnnotation, Metamodel metamodel)
            throws QueryFilterDefinitionException {
        super(fieldInfo);
        this.jsonAnnotation = jsonAnnotation;

        // Add json filter name
        if (!jsonAnnotation.name().isEmpty()) {
            super.filterName = jsonAnnotation.name();
        }

        var fieldClassProcessor = new FieldClassProcessor(fieldInfo.entityClass(), jsonAnnotation.value(), null, null,
                metamodel);

        this.attributes = fieldClassProcessor.getAttributes();

        Column column = getColumnAnnotation();
        if (!column.columnDefinition().toLowerCase().startsWith("jsonb")) {
            throw new QFJsonException(
                    "QFJsonElement annotations are only supported on colums of type 'jsonb'. Actual type is {}",
                    column.columnDefinition());
        }

        if (jsonAnnotation.joinTypes().length == 0) {
            LOGGER.warn("No join types defined for json element. Using default join type INNER");
            this.joinTypes = List.of(JoinType.INNER);
        } else {
            this.joinTypes = List.of(jsonAnnotation.joinTypes());
        }

        allowedOperations = Set.of(jsonAnnotation.allowedOperations());

    }

    private Column getColumnAnnotation() throws QFJsonException {
        var last = attributes.get(attributes.size() - 1);

        // Check if the last attribute is a JSONB
        // First check if java member is Field
        if (!(last.getAttribute().getJavaMember() instanceof Field javaField)) {
            throw new QFJsonException("QFJsonElement annotation are only supported on fields");
        }

        if (!javaField.isAnnotationPresent(Column.class)) {
            throw new QFJsonException("@Column annotation not found on the json element field {}", javaField);
        }

        return javaField.getAnnotation(Column.class);
    }

    /**
     * Get if the field is case-sensitive
     *
     * @return true if the field is case-sensitive
     */
    public boolean isCaseSensitive() {
        return jsonAnnotation.caseSensitive();
    }

    /**
     * Get definition paths
     *
     * @return paths
     * @since 1.0.0
     */
    public List<QFAttribute> getAttributes() {
        return attributes;
    }

    /**
     * Get join types
     *
     * @return join types
     * @since 1.0.0
     */
    public List<JoinType> getJoinTypes() {
        return joinTypes;
    }

    /**
     * Get if the operation is allowed
     *
     * @param operation to check
     * @return a boolean true if the operation is allowed
     * @since 1.0.0
     */
    public boolean isOperationAllowed(QFOperationJsonEnum operation) {
        return allowedOperations.isEmpty() || allowedOperations.contains(operation);
    }

    /**
     * Get allowed operations
     *
     * @return a set of allowed operations
     * @since 1.0.0
     */
    public Set<QFOperationJsonEnum> getRealAllowedOperations() {
        if (allowedOperations.isEmpty()) {
            return Set.of(QFOperationJsonEnum.values());
        }
        return allowedOperations;
    }

    @Override
    public List<QFSpecificationPart> getInnerDefaultValues() {
        return List.of(new QFJsonElementMatch(jsonAnnotation.defaultValue(), jsonAnnotation.defaultOperation(), this));
    }

    @Override
    public int getOrder() {
        return jsonAnnotation.order();
    }

    @Override
    public boolean hasDefaultValues() {
        return !jsonAnnotation.defaultValue().isEmpty();
    }
}
