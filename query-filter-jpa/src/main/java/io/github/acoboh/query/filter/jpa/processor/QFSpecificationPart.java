package io.github.acoboh.query.filter.jpa.processor;

import java.util.List;
import java.util.Map;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.util.MultiValueMap;

import io.github.acoboh.query.filter.jpa.processor.definitions.QFAbstractDefinition;
import io.github.acoboh.query.filter.jpa.spel.SpelResolverContext;

/**
 * Interface to create all parts of the final specification
 */
public interface QFSpecificationPart {

	/**
	 * Process the specification part
	 * 
	 * @param <E>             Entity class
	 * @param root            Root
	 * @param query           Query
	 * @param criteriaBuilder Criteria builder
	 * @param predicatesMap   Map of predicates
	 * @param pathsMap        Map of paths
	 * @param mlmap           Multi-value map for SpEL
	 * @param spelResolver    Spel Resolver class
	 * @param entityClass     Entity class
	 */
	public <E> void processPart(Root<E> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder,
			Map<String, List<Predicate>> predicatesMap, Map<String, Path<?>> pathsMap,
			MultiValueMap<String, Object> mlmap, SpelResolverContext spelResolver, Class<E> entityClass);

	/**
	 * Get definition of the filter field
	 * 
	 * @return definition
	 */
	public QFAbstractDefinition getDefinition();

}
