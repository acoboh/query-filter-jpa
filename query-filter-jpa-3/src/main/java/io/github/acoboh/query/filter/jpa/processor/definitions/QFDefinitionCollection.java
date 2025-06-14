package io.github.acoboh.query.filter.jpa.processor.definitions;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.acoboh.query.filter.jpa.annotations.QFCollectionElement;
import io.github.acoboh.query.filter.jpa.exceptions.definition.QFCollectionNotSupported;
import io.github.acoboh.query.filter.jpa.exceptions.definition.QueryFilterDefinitionException;
import io.github.acoboh.query.filter.jpa.operations.QFCollectionOperationEnum;
import io.github.acoboh.query.filter.jpa.processor.QFAttribute;
import io.github.acoboh.query.filter.jpa.processor.QFSpecificationPart;
import io.github.acoboh.query.filter.jpa.processor.match.QFCollectionMatch;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.metamodel.Metamodel;

/**
 * Definition for collection filter field
 *
 * @author Adrián Cobo
 */
public class QFDefinitionCollection extends QFAbstractDefinition {

	private static final Logger LOGGER = LoggerFactory.getLogger(QFDefinitionCollection.class);

	private final List<QFAttribute> attributes;
	private final List<JoinType> joinTypes;
	private final Set<QFCollectionOperationEnum> allowedOperations;

	private final QFCollectionElement collectionElement;

	QFDefinitionCollection(FilterFieldInfo fieldInfo, QFCollectionElement collectionElement, Metamodel metamodel)
			throws QueryFilterDefinitionException {
		super(fieldInfo);

		var fieldClassProcessor = new FieldClassProcessor(fieldInfo.entityClass(), collectionElement.value(),
				collectionElement.subClassMapping(), collectionElement.subClassMappingPath(), metamodel);

		this.collectionElement = collectionElement;

		this.attributes = fieldClassProcessor.getAttributes();

		if (!collectionElement.name().isEmpty()) {
			super.filterName = collectionElement.name();
		}

		if (!Collection.class.isAssignableFrom(fieldClassProcessor.getFinalClass())) {
			throw new QFCollectionNotSupported(filterName, fieldInfo.filterClass(),
					fieldClassProcessor.getFinalClass());
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
	 * @since 1.0.0
	 */
	public List<JoinType> getJoinTypes() {
		return joinTypes;
	}

	/**
	 * Get if the operation is allowed for the collection filter
	 *
	 * @param operation
	 *            operation to check
	 * @return a boolean true if the operation is allowed, false otherwise
	 * @since 1.0.0
	 */
	public boolean isOperationAllowed(QFCollectionOperationEnum operation) {
		return allowedOperations.isEmpty() || allowedOperations.contains(operation);
	}

	/**
	 * Get the allowed operations for the collection filter
	 *
	 * @return a set with the allowed operations
	 * @since 1.0.0
	 */
	public Set<QFCollectionOperationEnum> getRealAllowedOperations() {
		if (allowedOperations.isEmpty()) {
			return Set.of(QFCollectionOperationEnum.values());
		}
		return allowedOperations;
	}

	@Override
	protected List<QFSpecificationPart> getInnerDefaultValues() {
		return List.of(
				new QFCollectionMatch(this, collectionElement.defaultOperation(), collectionElement.defaultValue()));
	}

	@Override
	public int getOrder() {
		return collectionElement.order();
	}

	@Override
	public boolean hasDefaultValues() {
		return collectionElement.defaultValue() != Integer.MIN_VALUE;
	}
}
