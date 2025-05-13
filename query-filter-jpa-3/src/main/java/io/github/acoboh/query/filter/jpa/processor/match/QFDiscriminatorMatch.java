package io.github.acoboh.query.filter.jpa.processor.match;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.MultiValueMap;

import io.github.acoboh.query.filter.jpa.exceptions.QFDiscriminatorNotFoundException;
import io.github.acoboh.query.filter.jpa.exceptions.QFOperationNotAllowed;
import io.github.acoboh.query.filter.jpa.operations.QFOperationDiscriminatorEnum;
import io.github.acoboh.query.filter.jpa.processor.QFAttribute;
import io.github.acoboh.query.filter.jpa.processor.QFSpecificationPart;
import io.github.acoboh.query.filter.jpa.processor.QueryInfo;
import io.github.acoboh.query.filter.jpa.processor.QueryUtils;
import io.github.acoboh.query.filter.jpa.processor.definitions.QFDefinitionDiscriminator;
import io.github.acoboh.query.filter.jpa.spel.SpelResolverContext;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;

/**
 * Class with info about the discriminator matching for filtering
 *
 * @author Adrián Cobo
 */
public class QFDiscriminatorMatch implements QFSpecificationPart {

	private static final Logger LOGGER = LoggerFactory.getLogger(QFDiscriminatorMatch.class);

	private final List<String> values;

	private final QFOperationDiscriminatorEnum operation;

	private final List<Class<?>> matchingClasses;
	private final QFDefinitionDiscriminator definition;

	private final Class<?> entityClass;

	private final boolean isRoot;
	private List<QFAttribute> path;

	/**
	 * Default constructor
	 *
	 * @param values
	 *            list of values
	 * @param operation
	 *            operation to apply
	 * @param definition
	 *            definition of the field
	 * @throws io.github.acoboh.query.filter.jpa.exceptions.QFDiscriminatorNotFoundException
	 *             if any discriminator exception occurs
	 */
	public QFDiscriminatorMatch(List<String> values, QFOperationDiscriminatorEnum operation,
			QFDefinitionDiscriminator definition) throws QFDiscriminatorNotFoundException {

		if (!definition.isOperationAllowed(operation)) {
			throw new QFOperationNotAllowed(definition.getFilterName(), operation.getOperation());
		}

		this.values = values;
		this.definition = definition;
		this.operation = operation;
		this.matchingClasses = new ArrayList<>();

		for (String parsedValue : values) {

			Class<?> foundClass = definition.getDiscriminatorMap().get(parsedValue);
			if (foundClass == null) {
				LOGGER.error("The value {} is not a valid discriminator for the field", parsedValue);
				throw new QFDiscriminatorNotFoundException(parsedValue, definition.getFilterName());
			}
			matchingClasses.add(foundClass);

		}

		if (!definition.getPaths().isEmpty()) {
			path = definition.getPaths();
			if (path.isEmpty()) {
				LOGGER.error("Error. Unexpected empty path for discriminator match {}", definition.getFilterName());
			}
			entityClass = definition.getFinalClass();
			isRoot = false;

		} else {
			entityClass = definition.getEntityClass();
			isRoot = true;
		}

	}

	/**
	 * Get the original values
	 *
	 * @return original values
	 */
	@Override
	public List<String> getOriginalValuesAsString() {
		return this.values;
	}

	/**
	 * Get operation as string
	 *
	 * @return operation as string
	 */
	@Override
	public String getOperationAsString() {
		return operation.getOperation();
	}

	/**
	 * Get matching classes
	 *
	 * @return matching classes
	 */
	public List<Class<?>> getMatchingClasses() {
		return matchingClasses;
	}

	/**
	 * Get entity class
	 *
	 * @return entity class
	 */
	public Class<?> getEntityClass() {
		return entityClass;
	}

	/**
	 * Get if the field is root or nested levels
	 *
	 * @return true if root. False if the field is on nested levels
	 */
	public boolean isRoot() {
		return isRoot;
	}

	/**
	 * Get original field definition
	 *
	 * @return original field definition
	 */
	public QFDefinitionDiscriminator getDefinition() {
		return definition;
	}

	/**
	 * Get all values
	 *
	 * @return values
	 */
	public List<String> getValues() {
		return values;
	}

	/**
	 * Get list of paths for nested levels
	 *
	 * @return paths for nested levels
	 */
	public List<QFAttribute> getPath() {
		return path;
	}

	/**
	 * Get operation
	 *
	 * @return operation
	 */
	public QFOperationDiscriminatorEnum getOperation() {
		return operation;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <E> void processPart(QueryInfo<E> queryInfo, Map<String, List<Predicate>> predicatesMap,
			Map<String, Path<?>> pathsMap, MultiValueMap<String, Object> mlmap, SpelResolverContext spelResolver,
			Class<E> entityClass) {

		Expression<?> expression;
		if (isRoot) {
			expression = queryInfo.root().type();
		} else {
			expression = QueryUtils.getObject(queryInfo, path, definition.getJoinTypes(), pathsMap, false, false)
					.type();
		}

		predicatesMap.computeIfAbsent(definition.getFilterName(), t -> new ArrayList<>()).add(operation
				.generateDiscriminatorPredicate((Expression<Class<?>>) expression, queryInfo.cb(), this, mlmap));

	}

}
