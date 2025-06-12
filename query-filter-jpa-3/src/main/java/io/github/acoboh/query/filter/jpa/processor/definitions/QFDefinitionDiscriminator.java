package io.github.acoboh.query.filter.jpa.processor.definitions;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.acoboh.query.filter.jpa.annotations.QFBlockParsing;
import io.github.acoboh.query.filter.jpa.annotations.QFDiscriminator;
import io.github.acoboh.query.filter.jpa.annotations.QFRequired;
import io.github.acoboh.query.filter.jpa.exceptions.definition.QFDiscriminatorException;
import io.github.acoboh.query.filter.jpa.exceptions.definition.QueryFilterDefinitionException;
import io.github.acoboh.query.filter.jpa.operations.QFOperationDiscriminatorEnum;
import io.github.acoboh.query.filter.jpa.processor.QFAttribute;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.metamodel.Metamodel;

/**
 * Definition for discriminator classes
 *
 * @author Adrián Cobo
 */
public class QFDefinitionDiscriminator extends QFAbstractDefinition {

	private static final Logger LOGGER = LoggerFactory.getLogger(QFDefinitionDiscriminator.class);

	private final QFDiscriminator discriminatorAnnotation;
	private final List<QFAttribute> attributes;
	private final List<JoinType> joinTypes;
	private final Class<?> finalClass;
	private final Set<QFOperationDiscriminatorEnum> allowedOperations;

	private final Map<String, Class<?>> discriminatorMap = new HashMap<>();

	QFDefinitionDiscriminator(Field filterField, Class<?> filterClass, Class<?> entityClass,
			QFBlockParsing blockParsing, QFRequired required, QFDiscriminator discriminatorAnnotation,
			Metamodel metamodel) throws QueryFilterDefinitionException {
		super(filterField, filterClass, entityClass, blockParsing, required);

		this.discriminatorAnnotation = discriminatorAnnotation;

		if (!discriminatorAnnotation.path().isEmpty()) {
			var fieldClassProcessor = new FieldClassProcessor(entityClass, discriminatorAnnotation.path(), null, null,
					metamodel);
			this.attributes = fieldClassProcessor.getAttributes();
			this.finalClass = fieldClassProcessor.getFinalClass();
		} else {
			this.attributes = Collections.emptyList();
			this.finalClass = entityClass;
		}

		if (!discriminatorAnnotation.name().isEmpty()) {
			super.filterName = discriminatorAnnotation.name();
		}

		for (var value : discriminatorAnnotation.value()) {
			if (discriminatorMap.containsKey(value.name())) {
				throw new QFDiscriminatorException("Duplicate discriminator value name {}", value.name());
			}

			if (!finalClass.isAssignableFrom(value.type())) {
				throw new QFDiscriminatorException("Entity class '{}' is not assignable from value class '{}'",
						finalClass, value.type());
			}

			discriminatorMap.put(value.name(), value.type());
		}

		if (discriminatorAnnotation.joinTypes().length == 0) {
			LOGGER.debug("No join types defined for discriminator {}. Defaulting to INNER", filterName);
			this.joinTypes = List.of(JoinType.INNER);
		} else {
			this.joinTypes = List.of(discriminatorAnnotation.joinTypes());
		}

		allowedOperations = Set.of(discriminatorAnnotation.allowedOperations());

	}

	/**
	 * Get the discriminator annotation
	 *
	 * @return discriminator annotation
	 */
	public QFDiscriminator getDiscriminatorAnnotation() {
		return discriminatorAnnotation;
	}

	/**
	 * Get paths of the discriminator field
	 *
	 * @return paths
	 */
	public List<QFAttribute> getPaths() {
		return attributes;
	}

	/**
	 * Get final entity class
	 *
	 * @return final entity class
	 */
	public Class<?> getFinalClass() {
		return finalClass;
	}

	/**
	 * Get the discriminator map
	 *
	 * @return discriminator map
	 */
	public Map<String, Class<?>> getDiscriminatorMap() {
		return discriminatorMap;
	}

	/**
	 * Get the join types
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
	 * @param operation
	 *            to check
	 * @return a boolean true if the operation is allowed
	 * @since 1.0.0
	 */
	public boolean isOperationAllowed(QFOperationDiscriminatorEnum operation) {
		return allowedOperations.isEmpty() || allowedOperations.contains(operation);
	}

	/**
	 * Get the allowed operations
	 *
	 * @return a set of allowed operations
	 * @since 1.0.0
	 */
	public Set<QFOperationDiscriminatorEnum> getRealAllowedOperations() {
		if (allowedOperations.isEmpty()) {
			return Set.of(QFOperationDiscriminatorEnum.values());
		}
		return allowedOperations;
	}

}
