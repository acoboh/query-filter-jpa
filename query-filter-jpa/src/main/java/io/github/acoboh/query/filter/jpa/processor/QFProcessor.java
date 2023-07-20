package io.github.acoboh.query.filter.jpa.processor;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import io.github.acoboh.query.filter.jpa.annotations.QFDefinitionClass;
import io.github.acoboh.query.filter.jpa.annotations.QFElement;
import io.github.acoboh.query.filter.jpa.annotations.QFPredicate;
import io.github.acoboh.query.filter.jpa.exceptions.QueryFilterException;
import io.github.acoboh.query.filter.jpa.exceptions.definition.QFClassException;
import io.github.acoboh.query.filter.jpa.exceptions.definition.QueryFilterDefinitionException;
import io.github.acoboh.query.filter.jpa.predicate.PredicateProcessorResolutor;
import io.github.acoboh.query.filter.jpa.spel.SpelResolverInterface;

public class QFProcessor<F, E> {

	private static final Logger LOGGER = LoggerFactory.getLogger(QFProcessor.class);

	private final Class<F> filterClass;

	private final Class<E> entityClass;

	private final QFDefinitionClass queryFilterClass;

	private final Map<String, QFDefinition> definitionMap;

	private final List<QFElementMatch> defaultMatches;

	private final ApplicationContext appContext;

	private Map<String, PredicateProcessorResolutor> predicateMap;
	private String predicateName;
	private PredicateProcessorResolutor defaultPredicate;

	public QFProcessor(Class<F> filterClass, Class<E> entityClass, ApplicationContext appContext)
			throws QueryFilterDefinitionException {

		this.filterClass = filterClass;
		this.entityClass = entityClass;
		this.appContext = appContext;

		if (!filterClass.isAnnotationPresent(QFDefinitionClass.class)) {
			throw new QFClassException(QFDefinitionClass.class, filterClass.getName());
		}

		this.queryFilterClass = filterClass.getAnnotation(QFDefinitionClass.class);
		if (this.queryFilterClass.value() != entityClass) {
			throw new QFClassException(queryFilterClass.value(), filterClass, entityClass);
		}

		this.definitionMap = getDefinition(filterClass, queryFilterClass);
		this.defaultMatches = defaultMatches(definitionMap);

		LOGGER.debug("Initializing query filter predicates...");

		QFPredicate[] predicatesAnnotations = filterClass.getAnnotationsByType(QFPredicate.class);
		if (predicatesAnnotations.length != 0) {
			LOGGER.debug("Must be processed {} predicates on class {}", predicatesAnnotations.length, queryFilterClass);
			predicateMap = resolvePredicates(predicatesAnnotations, definitionMap);
			predicateName = queryFilterClass.defaultPredicate();
			if (!predicateName.isEmpty()) {
				defaultPredicate = predicateMap.get(predicateName);
				if (defaultPredicate == null) {
					throw new IllegalStateException("Unable to found default predicate");
				}
			}
		}
	}

	private static Map<String, QFDefinition> getDefinition(Class<?> filterClass, QFDefinitionClass queryFilterClass)
			throws QueryFilterDefinitionException {

		Map<String, QFDefinition> map = new HashMap<>();

		for (Field field : filterClass.getDeclaredFields()) {

			QFDefinition qfd = new QFDefinition(field, filterClass, queryFilterClass.value());
			map.put(qfd.getFilterName(), qfd);

		}

		return map;
	}

	private static List<QFElementMatch> defaultMatches(Map<String, QFDefinition> definitionMap) {
		List<QFElementMatch> ret = new ArrayList<>();

		for (QFDefinition def : definitionMap.values()) {
			if (def.isElementFilter()) {

				for (QFElement elem : def.getElementAnnotations()) {
					if (elem.defaultValues().length > 0) {
						ret.add(new QFElementMatch(Arrays.asList(elem.defaultValues()), elem.defaultOperation(), def));
					}
				}
			}
		}

		return ret;
	}

	private static Map<String, PredicateProcessorResolutor> resolvePredicates(QFPredicate[] predicates,
			Map<String, QFDefinition> definitionMap) {
		Map<String, PredicateProcessorResolutor> ret = new HashMap<>();

		for (QFPredicate predicate : predicates) {
			ret.put(predicate.name(), new PredicateProcessorResolutor(predicate.expression(), definitionMap,
					predicate.includeMissing(), predicate.missingOperator()));
		}

		return ret;
	}

	public QueryFilter<E> newQueryFilter(String input, QFParamType type) throws QueryFilterException {
		return new QueryFilter<>(input, type, entityClass, filterClass, definitionMap, queryFilterClass, defaultMatches,
				appContext.getBean(SpelResolverInterface.class), predicateMap, predicateName, defaultPredicate);
	}

	public Map<String, QFDefinition> getDefinitionMap() {
		return definitionMap;
	}

	public Class<F> getFilterClass() {
		return filterClass;
	}

	public Class<E> getEntityClass() {
		return entityClass;
	}

}