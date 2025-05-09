package io.github.acoboh.query.filter.jpa.processor.match;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.util.MultiValueMap;

import io.github.acoboh.query.filter.jpa.exceptions.QFCollectionException;
import io.github.acoboh.query.filter.jpa.operations.QFCollectionOperationEnum;
import io.github.acoboh.query.filter.jpa.processor.QFSpecificationPart;
import io.github.acoboh.query.filter.jpa.processor.QueryUtils;
import io.github.acoboh.query.filter.jpa.processor.definitions.QFDefinitionCollection;
import io.github.acoboh.query.filter.jpa.spel.SpelResolverContext;

/**
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
		super();
		this.definition = definition;
		this.operation = operation;
		this.value = value;

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

	@SuppressWarnings("unchecked")
	@Override
	public <E> void processPart(Root<E> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder,
			Map<String, List<Predicate>> predicatesMap, Map<String, Path<?>> pathsMap,
			MultiValueMap<String, Object> mlmap, SpelResolverContext spelResolver, Class<E> entityClass) {

		Path<?> expressionPath = QueryUtils.getObject(root, definition.getPaths(), pathsMap, true, false,
				criteriaBuilder);

		Expression<? extends java.util.Collection<?>> expression;

		try {
			expression = (Expression<? extends java.util.Collection<?>>) expressionPath;
		} catch (ClassCastException e) {
			throw new QFCollectionException(definition.getFilterName(), e.getMessage());
		}

		predicatesMap.computeIfAbsent(definition.getFilterName(), t -> new ArrayList<>())
				.add(operation.generateCollectionPredicate(expression, criteriaBuilder, this, mlmap));

	}

}
