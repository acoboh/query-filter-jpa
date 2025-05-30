package io.github.acoboh.query.filter.jpa.processor.match;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.util.MultiValueMap;

import io.github.acoboh.query.filter.jpa.exceptions.QFCollectionException;
import io.github.acoboh.query.filter.jpa.exceptions.QFOperationNotAllowed;
import io.github.acoboh.query.filter.jpa.operations.QFCollectionOperationEnum;
import io.github.acoboh.query.filter.jpa.processor.QFSpecificationPart;
import io.github.acoboh.query.filter.jpa.processor.QueryInfo;
import io.github.acoboh.query.filter.jpa.processor.QueryUtils;
import io.github.acoboh.query.filter.jpa.processor.definitions.QFDefinitionCollection;
import io.github.acoboh.query.filter.jpa.spel.SpelResolverContext;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;

/**
 * QFCollectionMatch class is used to process collection elements in a query
 *
 * @author Adrián Cobo
 */
public class QFCollectionMatch implements QFSpecificationPart {

	private final QFDefinitionCollection definition;

	private final QFCollectionOperationEnum operation;

	private final int value;

	/**
	 * Default constructor
	 *
	 * @param definition
	 *            definition of element
	 * @param operation
	 *            operation of the element
	 * @param value
	 *            value of the element operation
	 */
	public QFCollectionMatch(QFDefinitionCollection definition, QFCollectionOperationEnum operation, int value) {

		if (!definition.isOperationAllowed(operation)) {
			throw new QFOperationNotAllowed(definition.getFilterName(), operation.getOperation());
		}

		this.definition = definition;
		this.operation = operation;
		this.value = value;

	}

	/** {@inheritDoc} */
	@Override
	public List<String> getOriginalValuesAsString() {
		return List.of(String.valueOf(value));
	}

	/** {@inheritDoc} */
	@Override
	public String getOperationAsString() {
		return operation.getOperation();
	}

	/**
	 * Get collection operation
	 *
	 * @return collection operation
	 */
	public QFCollectionOperationEnum getOperation() {
		return operation;
	}

	/**
	 * Get value of the collection operation
	 *
	 * @return value
	 */
	public int getValue() {
		return value;
	}

	/**
	 * Get element definition
	 *
	 * @return element definition
	 */
	public QFDefinitionCollection getDefinition() {
		return definition;
	}

	/** {@inheritDoc} */
	@SuppressWarnings("unchecked")
	@Override
	public <E> void processPart(QueryInfo<E> queryInfo, Map<String, List<Predicate>> predicatesMap,
			Map<String, Path<?>> pathsMap, MultiValueMap<String, Object> mlmap, SpelResolverContext spelResolver,
			Class<E> entityClass) {

		Path<?> expressionPath = QueryUtils.getObject(queryInfo, definition.getPaths(), definition.getJoinTypes(),
				pathsMap, true, false);

		Expression<? extends java.util.Collection<?>> expression;

		try {
			expression = (Expression<? extends java.util.Collection<?>>) expressionPath;
		} catch (ClassCastException e) {
			throw new QFCollectionException(definition.getFilterName(), e.getMessage());
		}

		predicatesMap.computeIfAbsent(definition.getFilterName(), t -> new ArrayList<>())
				.add(operation.generateCollectionPredicate(expression, queryInfo.cb(), this, mlmap));

	}

}
