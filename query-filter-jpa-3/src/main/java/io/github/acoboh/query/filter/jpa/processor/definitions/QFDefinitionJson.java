package io.github.acoboh.query.filter.jpa.processor.definitions;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.acoboh.query.filter.jpa.annotations.QFBlockParsing;
import io.github.acoboh.query.filter.jpa.annotations.QFJsonElement;
import io.github.acoboh.query.filter.jpa.exceptions.definition.QFJsonException;
import io.github.acoboh.query.filter.jpa.exceptions.definition.QueryFilterDefinitionException;
import io.github.acoboh.query.filter.jpa.operations.QFOperationJsonEnum;
import io.github.acoboh.query.filter.jpa.processor.QFAttribute;
import jakarta.persistence.Column;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.metamodel.Metamodel;

/**
 * Definition for JSON filter fields
 */
public class QFDefinitionJson extends QFAbstractDefinition {

	private static final Logger LOGGER = LoggerFactory.getLogger(QFDefinitionJson.class);

	private final QFJsonElement jsonAnnotation;
	private final List<QFAttribute> attributes;
	private final List<JoinType> joinTypes;
	private final Set<QFOperationJsonEnum> allowedOperations;

	QFDefinitionJson(Field filterField, Class<?> filterClass, Class<?> entityClass, QFBlockParsing blockParsing,
			QFJsonElement jsonAnnotation, Metamodel metamodel) throws QueryFilterDefinitionException {
		super(filterField, filterClass, entityClass, blockParsing);
		this.jsonAnnotation = jsonAnnotation;

		// Add json filter name
		if (!jsonAnnotation.name().isEmpty()) {
			super.filterName = jsonAnnotation.name();
		}

		var fieldClassProcessor = new FieldClassProcessor(entityClass, jsonAnnotation.value(), null, null, metamodel);

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
	 * Get if the field is case sensitive
	 *
	 * @return true if the field is case sensitive
	 */
	public boolean isCaseSensitive() {
		return jsonAnnotation.caseSensitive();
	}

	/**
	 * Get definition paths
	 *
	 * @return paths
	 */
	public List<QFAttribute> getAttributes() {
		return attributes;
	}

	/**
	 * Get join types
	 *
	 * @return join types
	 */
	public List<JoinType> getJoinTypes() {
		return joinTypes;
	}

	public boolean isOperationAllowed(QFOperationJsonEnum operation) {
		return allowedOperations.isEmpty() || allowedOperations.contains(operation);
	}

	public Set<QFOperationJsonEnum> getRealAllowedOperations() {
		if (allowedOperations.isEmpty()) {
			return Set.of(QFOperationJsonEnum.values());
		}
		return allowedOperations;
	}

}
