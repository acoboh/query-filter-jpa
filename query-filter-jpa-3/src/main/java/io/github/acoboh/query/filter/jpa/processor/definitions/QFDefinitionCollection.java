package io.github.acoboh.query.filter.jpa.processor.definitions;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.acoboh.query.filter.jpa.annotations.QFBlockParsing;
import io.github.acoboh.query.filter.jpa.annotations.QFCollectionElement;
import io.github.acoboh.query.filter.jpa.exceptions.definition.QFCollectionNotSupported;
import io.github.acoboh.query.filter.jpa.exceptions.definition.QueryFilterDefinitionException;
import io.github.acoboh.query.filter.jpa.operations.QFCollectionOperationEnum;
import io.github.acoboh.query.filter.jpa.processor.QFAttribute;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.metamodel.Metamodel;

/**
 * Definition for collection filter field
 */
public class QFDefinitionCollection extends QFAbstractDefinition {

	private static final Logger LOGGER = LoggerFactory.getLogger(QFDefinitionCollection.class);

	private final List<QFAttribute> attributes;
	private final List<JoinType> joinTypes;
	private final Set<QFCollectionOperationEnum> allowedOperations;

	QFDefinitionCollection(Field filterField, Class<?> filterClass, Class<?> entityClass, QFBlockParsing blockParsing,
			QFCollectionElement collectionElement, Metamodel metamodel) throws QueryFilterDefinitionException {
		super(filterField, filterClass, entityClass, blockParsing);

		var fieldClassProcessor = new FieldClassProcessor(entityClass, collectionElement.value(),
				collectionElement.subClassMapping(), collectionElement.subClassMappingPath(), metamodel);

		this.attributes = fieldClassProcessor.getAttributes();

		if (!collectionElement.name().isEmpty()) {
			super.filterName = collectionElement.name();
		}

		if (!Collection.class.isAssignableFrom(fieldClassProcessor.getFinalClass())) {
			throw new QFCollectionNotSupported(filterName, filterClass, fieldClassProcessor.getFinalClass());
		}

		if (collectionElement.joinTypes().length == 0) {
			LOGGER.debug("No join types defined for collection {}. Using default INNER", filterName);
			this.joinTypes = List.of(JoinType.INNER);
		} else {
			this.joinTypes = List.of(collectionElement.joinTypes());
		}

		allowedOperations = Set.of(collectionElement.allowedOperations());
	}

	/**
	 * Get definition paths of the collection
	 *
	 * @return paths
	 */
	public List<QFAttribute> getPaths() {
		return attributes;
	}

	/**
	 * Get join types of the collection
	 *
	 * @return join types
	 */
	public List<JoinType> getJoinTypes() {
		return joinTypes;
	}

	public boolean isOperationAllowed(QFCollectionOperationEnum operation) {
		return allowedOperations.isEmpty() || allowedOperations.contains(operation);
	}

	public Set<QFCollectionOperationEnum> getRealAllowedOperations() {
		if (allowedOperations.isEmpty()) {
			return Set.of(QFCollectionOperationEnum.values());
		}
		return allowedOperations;
	}

}
