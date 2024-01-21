package io.github.acoboh.query.filter.jpa.processor.match;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.MultiValueMap;

import io.github.acoboh.query.filter.jpa.annotations.QFDiscriminator;
import io.github.acoboh.query.filter.jpa.exceptions.QFDiscriminatorNotFoundException;
import io.github.acoboh.query.filter.jpa.processor.QFPath;
import io.github.acoboh.query.filter.jpa.processor.QFSpecificationPart;
import io.github.acoboh.query.filter.jpa.processor.QueryUtils;
import io.github.acoboh.query.filter.jpa.processor.definitions.QFDefinitionDiscriminator;
import io.github.acoboh.query.filter.jpa.spel.SpelResolverContext;

/**
 * Class with info about the discriminator matching for filtering
 *
 * @author Adrián Cobo
 * 
 */
public class QFDiscriminatorMatch implements QFSpecificationPart {

	private static final Logger LOGGER = LoggerFactory.getLogger(QFDiscriminatorMatch.class);

	private final List<String> values;

	private final List<Class<?>> matchingClasses;
	private final QFDefinitionDiscriminator definition;

	private final Class<?> entityClass;

	private final boolean isRoot;
	private List<QFPath> path;

	/**
	 * Default constructor
	 *
	 * @param values     list of values
	 * @param definition definition of the field
	 * @throws io.github.acoboh.query.filter.jpa.exceptions.QFDiscriminatorNotFoundException if any discriminator exception occurs
	 */
	public QFDiscriminatorMatch(List<String> values, QFDefinitionDiscriminator definition)
			throws QFDiscriminatorNotFoundException {

		this.values = values;
		this.definition = definition;
		this.matchingClasses = new ArrayList<>();

		for (String parsedValue : values) {

			Class<?> foundClass = Stream.of(definition.getDiscriminatorAnnotation().value())
					.filter(e -> e.name().equals(parsedValue)).map(QFDiscriminator.Value::type).findFirst()
					.orElse(null);
			if (foundClass == null) {
				throw new QFDiscriminatorNotFoundException(parsedValue, definition.getFilterName());
			}
			matchingClasses.add(foundClass);

		}

		if (!definition.getPaths().isEmpty()) {
			path = definition.getPaths();
			if (path.isEmpty()) {
				LOGGER.error("Error. Unexpected empty path for discriminator match {}", definition.getFilterName());
			}
			entityClass = path.get(path.size() - 1).getFieldClass();
			isRoot = false;

		} else {
			entityClass = definition.getEntityClass();
			isRoot = true;
		}

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
	public List<QFPath> getPath() {
		return path;
	}

	@Override
	public <E> void processPart(Root<E> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder,
			Map<String, List<Predicate>> predicatesMap, Map<String, Path<?>> pathsMap,
			MultiValueMap<String, Object> mlmap, SpelResolverContext spelResolver, Class<E> entityClass) {

		List<Predicate> orDiscriminators = new ArrayList<>();

		if (isRoot) {
			for (Class<?> clazz : matchingClasses) {
				orDiscriminators.add(criteriaBuilder.equal(root.type(), clazz));
				mlmap.add(definition.getFilterName(), clazz);
			}

		} else {

			for (Class<?> clazz : matchingClasses) {
				orDiscriminators.add(
						criteriaBuilder.equal(QueryUtils.getObject(root, path, pathsMap, false, false).type(), clazz));
				mlmap.add(definition.getFilterName(), clazz);
			}

		}

		predicatesMap.computeIfAbsent(definition.getFilterName(), k -> new ArrayList<>())
				.add(criteriaBuilder.or(orDiscriminators.toArray(new Predicate[orDiscriminators.size()])));

	}

}
