package io.github.acoboh.query.filter.jpa.processor;

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
import io.github.acoboh.query.filter.jpa.processor.definitions.QFDefinitionSortable;
import io.github.acoboh.query.filter.jpa.processor.definitions.traits.IDefinitionSortable;
import jakarta.annotation.Nullable;
import jakarta.persistence.EntityManager;
import jakarta.persistence.metamodel.Metamodel;
import jakarta.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.util.Pair;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Class to process all query filters.
 * <p>
 * It allows the user to create a new instance of {@linkplain QueryFilter} for
 * using as {@linkplain Specification}
 *
 * @param <F> Filter definition class
 * @param <E> Entity model class
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
    private final List<QFAbstractDefinition> definitionsOnPresent;

    private final List<QFSpecificationPart> defaultMatches;

    private final Set<String> requiredOnStringFilter;
    private final Set<String> requiredOnExecution;
    private final Set<String> requiredOnSort;

    private final List<Pair<IDefinitionSortable, Direction>> defaultSorting;

    private final ApplicationContext appContext;

    private @Nullable Map<String, PredicateProcessorResolutor> predicateMap;
    private @Nullable String predicateName;

    /**
     * Default constructor
     *
     * @param filterClass filter class
     * @param entityClass entity class
     * @param appContext  application context for spel expressions
     * @throws io.github.acoboh.query.filter.jpa.exceptions.definition.QueryFilterDefinitionException if
     *                                                                                                any.
     */
    public QFProcessor(Class<F> filterClass, Class<E> entityClass, ApplicationContextAwareSupport appContext)
            throws QueryFilterDefinitionException {
        this(filterClass, entityClass, Objects.requireNonNull(appContext.getApplicationContext()));
    }

    /**
     * Default constructor
     *
     * @param filterClass filter class
     * @param entityClass entity class
     * @param appContext  application context for spel expressions
     * @throws io.github.acoboh.query.filter.jpa.exceptions.definition.QueryFilterDefinitionException if
     *                                                                                                any
     *                                                                                                exception
     *                                                                                                on
     *                                                                                                parsing
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

        definitionsOnPresent = definitionMap.values().stream().filter(QFAbstractDefinition::isOnPresentFilterEnabled)
                .sorted(Comparator.comparingInt(QFAbstractDefinition::getOrder)).toList();

        fieldsLaunchOnPresent = new HashMap<>();
        for (var def : definitionsOnPresent) {
            String definitionName = def.getFilterName();
            if (def.getOnFilterPresentFilters() == null) {
                LOGGER.trace("Filter {} is enabled on present filter but has no related filters to launch",
                        definitionName);
                continue;
            }
            for (var related : def.getOnFilterPresentFilters()) {
                if (related.isEmpty()) {
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

    private static List<QFSpecificationPart> defaultMatches(Map<String, QFAbstractDefinition> definitionMap) {
        return definitionMap.values().stream().flatMap(e -> e.getDefaultElementMatches().stream()).toList();
    }

    private static List<Pair<IDefinitionSortable, Direction>> getDefaultSorting(QFDefinitionClass queryFilterClass,
            Map<String, QFAbstractDefinition> definitionMap, Class<?> filterClass)
            throws QueryFilterDefinitionException {

        if (queryFilterClass.defaultSort().length == 0) {
            return Collections.emptyList();
        }

        List<Pair<IDefinitionSortable, Direction>> ret = new ArrayList<>();

        for (var sort : queryFilterClass.defaultSort()) {

            var definition = definitionMap.get(sort.value());
            if (definition == null) {
                throw new QFElementException(sort.value(), filterClass);
            }

            if (!(definition instanceof IDefinitionSortable idefSortable)) {
                throw new QFNotSortableDefinitionException(definition.getFilterName(), filterClass);
            }

            ret.add(Pair.of(idefSortable, sort.direction()));

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
     * Create a new {@linkplain QueryFilter} instance with no string filter by
     * default
     * 
     * @return new {@linkplain QueryFilter} instance
     * @throws io.github.acoboh.query.filter.jpa.exceptions.QueryFilterException if
     *                                                                           any
     *                                                                           parsing
     *                                                                           exception
     *                                                                           occurs
     */
    public QueryFilter<E> newQueryFilter() {
        return new QueryFilter<>(null, QFParamType.RHS_COLON, this);
    }

    /**
     * Create a new {@linkplain QueryFilter} instance
     *
     * @param input string filter
     * @param type  standard type
     * @return new {@linkplain QueryFilter} instance
     * @throws io.github.acoboh.query.filter.jpa.exceptions.QueryFilterException if
     *                                                                           any
     *                                                                           parsing
     *                                                                           exception
     *                                                                           occurs
     */
    public QueryFilter<E> newQueryFilter(@Nullable String input, QFParamType type) throws QueryFilterException {
        return new QueryFilter<>(input, type, this);
    }

    public QueryFilter<E> newQueryFilterMap(@NotNull Map<String, String[]> input, boolean ignoreUnknown,
            QFParamType type) throws QueryFilterException {
        return new QueryFilter<>(input, ignoreUnknown, type, this);
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
    protected List<QFSpecificationPart> getDefaultMatches() {
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
    protected List<QFAbstractDefinition> getDefinitionsOnPresent() {
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
    protected @Nullable Map<String, PredicateProcessorResolutor> getPredicateMap() {
        return predicateMap;
    }

    /**
     * Get default predicate
     *
     * @return default predicate
     */
    protected @Nullable String getDefaultPredicate() {
        return predicateName;
    }
}
