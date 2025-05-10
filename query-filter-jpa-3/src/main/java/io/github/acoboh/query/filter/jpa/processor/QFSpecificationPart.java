package io.github.acoboh.query.filter.jpa.processor;

import java.util.List;
import java.util.Map;

import org.springframework.util.MultiValueMap;

import io.github.acoboh.query.filter.jpa.processor.definitions.QFAbstractDefinition;
import io.github.acoboh.query.filter.jpa.spel.SpelResolverContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

/**
 * Interface to create all parts of the final specification
 */
public interface QFSpecificationPart {

	/**
	 * Process the specification part
	 *
	 * @param <E>
	 *            Entity class
	 * @param root
	 *            Root
	 * @param query
	 *            Query
	 * @param criteriaBuilder
	 *            Criteria builder
	 * @param predicatesMap
	 *            Map of predicates
	 * @param pathsMap
	 *            Map of paths
	 * @param mlmap
	 *            Multi-value map for SpEL
	 * @param spelResolver
	 *            Spel Resolver class
	 * @param entityClass
	 *            Entity class
	 */
	<E> void processPart(Root<E> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder,
			Map<String, List<Predicate>> predicatesMap, Map<String, Path<?>> pathsMap,
			MultiValueMap<String, Object> mlmap, SpelResolverContext spelResolver, Class<E> entityClass,
			boolean isCount);

	/**
	 * Get definition of the filter field
	 *
	 * @return definition
	 */
	QFAbstractDefinition getDefinition();

}
