package io.github.acoboh.query.filter.jpa.processor;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.util.Pair;

import io.github.acoboh.query.filter.jpa.annotations.QFDefinitionClass;
import io.github.acoboh.query.filter.jpa.annotations.QFPredicate;
import io.github.acoboh.query.filter.jpa.config.ApplicationContextAwareSupport;
import io.github.acoboh.query.filter.jpa.exceptions.QueryFilterException;
import io.github.acoboh.query.filter.jpa.exceptions.definition.QFClassException;
import io.github.acoboh.query.filter.jpa.exceptions.definition.QFElementException;
import io.github.acoboh.query.filter.jpa.exceptions.definition.QFNotSortableDefinitionException;
import io.github.acoboh.query.filter.jpa.exceptions.definition.QueryFilterDefinitionException;
import io.github.acoboh.query.filter.jpa.predicate.PredicateProcessorResolutor;
import io.github.acoboh.query.filter.jpa.processor.definitions.QFAbstractDefinition;
import io.github.acoboh.query.filter.jpa.processor.definitions.QFDefinitionElement;
import io.github.acoboh.query.filter.jpa.processor.definitions.QFDefinitionSortable;
import io.github.acoboh.query.filter.jpa.processor.definitions.traits.IDefinitionSortable;
import io.github.acoboh.query.filter.jpa.processor.match.QFElementMatch;
import jakarta.persistence.EntityManager;
import jakarta.persistence.metamodel.Metamodel;

/**
 * Class to process all query filters.
 * <p>
 * It allows the user to create a new instance of {@linkplain QueryFilter} for
 * using as {@linkplain Specification}
 *
 * @param <F>
 *            Filter definition class
 * @param <E>
 *            Entity model class
 * @author Adrián Cobo
 */
public class QFProcessor<F, E> {

	private static final Logger LOGGER = LoggerFactory.getLogger(QFProcessor.class);

	private final Class<F> filterClass;

	private final Class<E> entityClass;

	private final QFDefinitionClass queryFilterClass;

	private final Map<String, QFAbstractDefinition> definitionMap;

	// Map to store fields that will launch other filters by OnPresent annotation
	private final Map<String, Set<String>> fieldsLaunchOnPresent;
	private final List<QFDefinitionElement> definitionsOnPresent;

	private final List<QFElementMatch> defaultMatches;

	private final Set<String> requiredOnStringFilter;
	private final Set<String> requiredOnExecution;
	private final Set<String> requiredOnSort;

	private final List<Pair<IDefinitionSortable, Direction>> defaultSorting;

	private final ApplicationContext appContext;

	private Map<String, PredicateProcessorResolutor> predicateMap;
	private String predicateName;

	/**
	 * Default constructor
	 *
	 * @param filterClass
	 *            filter class
	 * @param entityClass
	 *            entity class
	 * @param appContext
	 *            application context for spel expressions
	 * @throws io.github.acoboh.query.filter.jpa.exceptions.definition.QueryFilterDefinitionException
	 *             if any.
	 */
	public QFProcessor(Class<F> filterClass, Class<E> entityClass, ApplicationContextAwareSupport appContext)
			throws QueryFilterDefinitionException {
		this(filterClass, entityClass, appContext.getApplicationContext());
	}

	/**
	 * Default constructor
	 *
	 * @param filterClass
	 *            filter class
	 * @param entityClass
	 *            entity class
	 * @param appContext
	 *            application context for spel expressions
	 * @throws io.github.acoboh.query.filter.jpa.exceptions.definition.QueryFilterDefinitionException
	 *             if any exception on parsing
	 */
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

		var metamodel = appContext.getBean(EntityManager.class).getMetamodel();

		this.definitionMap = getDefinition(filterClass, queryFilterClass, metamodel);
		this.defaultMatches = defaultMatches(definitionMap);
		this.defaultSorting = getDefaultSorting(queryFilterClass, definitionMap, filterClass);

		LOGGER.debug("Initializing query filter predicates...");

		QFPredicate[] predicatesAnnotations = filterClass.getAnnotationsByType(QFPredicate.class);
		if (predicatesAnnotations.length != 0) {
			LOGGER.debug("Must be processed {} predicates on class {}", predicatesAnnotations.length, queryFilterClass);
			predicateMap = resolvePredicates(predicatesAnnotations, definitionMap);
			predicateName = queryFilterClass.defaultPredicate();
			if (!predicateName.isEmpty()) {
				PredicateProcessorResolutor defaultPredicate = predicateMap.get(predicateName);
				if (defaultPredicate == null) {
					throw new IllegalStateException("Unable to found default predicate");
				}
			}
		}

		definitionsOnPresent = definitionMap.values().stream().filter(QFDefinitionElement.class::isInstance)
				.map(QFDefinitionElement.class::cast).filter(QFDefinitionElement::isOnPresentFilterEnabled)
				.sorted(Comparator.comparingInt(QFDefinitionElement::getOrder)).toList();

		fieldsLaunchOnPresent = new HashMap<>();
		for (QFDefinitionElement def : definitionsOnPresent) {
			String definitionName = def.getFilterName();
			for (var related : def.getOnFilterPresentFilters()) {
				if (related == null || related.isEmpty()) {
					continue;
				}

				fieldsLaunchOnPresent.computeIfAbsent(related, k -> new HashSet<>()).add(definitionName);
			}
		}

		// Sortable fields are bypassed on execution phase
		requiredOnExecution = definitionMap.values().stream()
				.filter(e -> e.isRequiredExecution() && !(e instanceof QFDefinitionSortable))
				.map(QFAbstractDefinition::getFilterName).collect(Collectors.toSet());

		requiredOnStringFilter = definitionMap.values().stream().filter(QFAbstractDefinition::isRequiredStringFilter)
				.map(QFAbstractDefinition::getFilterName).collect(Collectors.toSet());

		// Only applied fields that are sortable and required on sort
		requiredOnSort = definitionMap.values().stream()
				.filter(e -> e.isRequiredSort() && (e instanceof IDefinitionSortable sortable) && sortable.isSortable())
				.map(QFAbstractDefinition::getFilterName).collect(Collectors.toSet());

	}

	private static Map<String, QFAbstractDefinition> getDefinition(Class<?> filterClass,
			QFDefinitionClass queryFilterClass, Metamodel metamodel) throws QueryFilterDefinitionException {

		Map<String, QFAbstractDefinition> map = new HashMap<>();

		for (Field field : getAllFieldsFromClassAndSuperClass(filterClass)) {

			var qfd = QFAbstractDefinition.buildDefinition(field, filterClass, queryFilterClass.value(), metamodel);
			if (qfd == null) {
				continue;
			}

			map.put(qfd.getFilterName(), qfd);

		}

		return map;
	}

	private static List<Field> getAllFieldsFromClassAndSuperClass(Class<?> filterClass) {
		List<Field> fields = new ArrayList<>();

		Collections.addAll(fields, filterClass.getDeclaredFields());

		if (filterClass.getSuperclass() != null && filterClass.getSuperclass() != Object.class) {
			fields.addAll(getAllFieldsFromClassAndSuperClass(filterClass.getSuperclass()));
		}

		return fields;
	}

	private static List<QFElementMatch> defaultMatches(Map<String, QFAbstractDefinition> definitionMap) {
		List<QFElementMatch> ret = new ArrayList<>();

		for (var abstractDef : definitionMap.values()) {
			if (abstractDef instanceof QFDefinitionElement def) {
				ret.addAll(def.getDefaultElementMatches());
			}
		}

		return Collections.unmodifiableList(ret);
	}

	private static List<Pair<IDefinitionSortable, Direction>> getDefaultSorting(QFDefinitionClass queryFilterClass,
			Map<String, QFAbstractDefinition> definitionMap, Class<?> filterClass)
			throws QueryFilterDefinitionException {

		if (queryFilterClass.defaultSort() == null || queryFilterClass.defaultSort().length == 0) {
			return Collections.emptyList();
		}

		List<Pair<IDefinitionSortable, Direction>> ret = new ArrayList<>();

		for (var sort : queryFilterClass.defaultSort()) {

			var definition = definitionMap.get(sort.value());
			if (definition == null) {
				throw new QFElementException(sort.value(), filterClass);
			}

			if (!(definition instanceof IDefinitionSortable)) {
				throw new QFNotSortableDefinitionException(definition.getFilterName(), filterClass);
			}

			ret.add(Pair.of((IDefinitionSortable) definition, sort.direction()));

		}

		return Collections.unmodifiableList(ret);

	}

	private static Map<String, PredicateProcessorResolutor> resolvePredicates(QFPredicate[] predicates,
			Map<String, QFAbstractDefinition> definitionMap) {
		Map<String, PredicateProcessorResolutor> ret = new HashMap<>();

		for (var predicate : predicates) {
			ret.put(predicate.name(), new PredicateProcessorResolutor(predicate.expression(), definitionMap,
					predicate.includeMissing(), predicate.missingOperator()));
		}

		return Collections.unmodifiableMap(ret);
	}

	/**
	 * Create a new {@linkplain QueryFilter} instance
	 *
	 * @param input
	 *            string filter
	 * @param type
	 *            standard type
	 * @return new {@linkplain QueryFilter} instance
	 * @throws io.github.acoboh.query.filter.jpa.exceptions.QueryFilterException
	 *             if any parsing exception occurs
	 */
	public QueryFilter<E> newQueryFilter(String input, QFParamType type) throws QueryFilterException {
		return new QueryFilter<>(input, type, this);
	}

	/**
	 * Get all definitions of any field
	 *
	 * @return map of definitions
	 */
	public Map<String, QFAbstractDefinition> getDefinitionMap() {
		return definitionMap;
	}

	/**
	 * Get filter class
	 *
	 * @return filter class
	 */
	public Class<F> getFilterClass() {
		return filterClass;
	}

	/**
	 * Get entity class
	 *
	 * @return entity model class
	 */
	public Class<E> getEntityClass() {
		return entityClass;
	}

	/**
	 * Get definition class annotation
	 *
	 * @return definition class annotation
	 */
	protected QFDefinitionClass getDefinitionClassAnnotation() {
		return queryFilterClass;
	}

	/**
	 * Get default matches of the processor
	 *
	 * @return default matches
	 */
	protected List<QFElementMatch> getDefaultMatches() {
		return defaultMatches;
	}

	/**
	 * Get the set of fields that are required on string filter
	 * 
	 * @return set of fields that are required on string filter
	 */
	protected Set<String> getRequiredOnStringFilter() {
		return requiredOnStringFilter;
	}

	/**
	 * Get the set of fields that are required on execution phase
	 * 
	 * @return set of fields that are required on execution phase
	 */
	protected Set<String> getRequiredOnExecution() {
		return requiredOnExecution;
	}

	/**
	 * Get the set of fields that are required on sort
	 * 
	 * @return set of fields that are required on sort
	 */
	protected Set<String> getRequiredOnSort() {
		return requiredOnSort;
	}

	/**
	 * Get all the definitions that are enabled on present filter
	 * 
	 * @return list of definitions that are enabled on present filter
	 */
	protected List<QFDefinitionElement> getDefinitionsOnPresent() {
		return definitionsOnPresent;
	}

	/**
	 * Get fields that will launch other filters by OnPresent annotation
	 *
	 * @return map of fields that will launch other filters by OnPresent annotation
	 */
	protected Map<String, Set<String>> getFieldsLaunchOnPresent() {
		return fieldsLaunchOnPresent;
	}

	/**
	 * Get default sorting operations
	 *
	 * @return default sorting operations
	 */
	protected List<Pair<IDefinitionSortable, Direction>> getDefaultSorting() {
		return defaultSorting;
	}

	/**
	 * Get application context
	 *
	 * @return application context
	 */
	protected ApplicationContext getApplicationContext() {
		return appContext;
	}

	/**
	 * Get predicate map
	 *
	 * @return predicate map
	 */
	protected Map<String, PredicateProcessorResolutor> getPredicateMap() {
		return predicateMap;
	}

	/**
	 * Get default predicate
	 *
	 * @return default predicate
	 */
	protected String getDefaultPredicate() {
		return predicateName;
	}
}
