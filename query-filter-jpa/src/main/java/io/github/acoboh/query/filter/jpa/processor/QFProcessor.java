package io.github.acoboh.query.filter.jpa.processor;

import io.github.acoboh.query.filter.jpa.annotations.QFDefinitionClass;
import io.github.acoboh.query.filter.jpa.annotations.QFDefinitionClass.QFDefaultSort;
import io.github.acoboh.query.filter.jpa.annotations.QFElement;
import io.github.acoboh.query.filter.jpa.annotations.QFPredicate;
import io.github.acoboh.query.filter.jpa.exceptions.QueryFilterException;
import io.github.acoboh.query.filter.jpa.exceptions.definition.QFClassException;
import io.github.acoboh.query.filter.jpa.exceptions.definition.QFElementException;
import io.github.acoboh.query.filter.jpa.exceptions.definition.QFNotSortableDefinitionException;
import io.github.acoboh.query.filter.jpa.exceptions.definition.QueryFilterDefinitionException;
import io.github.acoboh.query.filter.jpa.predicate.PredicateProcessorResolutor;
import io.github.acoboh.query.filter.jpa.processor.definitions.QFAbstractDefinition;
import io.github.acoboh.query.filter.jpa.processor.definitions.QFDefinitionElement;
import io.github.acoboh.query.filter.jpa.processor.definitions.traits.IDefinitionSortable;
import io.github.acoboh.query.filter.jpa.processor.match.QFElementMatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.util.Pair;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class to process all query filters.
 * <p>
 * It allows the user to create a new instance of {@linkplain QueryFilter} for using as {@linkplain Specification}
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

    private final List<QFElementMatch> defaultMatches;

    private final List<Pair<IDefinitionSortable, Direction>> defaultSorting;

    private final ApplicationContext appContext;

    private Map<String, PredicateProcessorResolutor> predicateMap;
    private String predicateName;
    private PredicateProcessorResolutor defaultPredicate;

    /**
     * Default constructor
     *
     * @param filterClass filter class
     * @param entityClass entity class
     * @param appContext  application context for spel expressions
     * @throws io.github.acoboh.query.filter.jpa.exceptions.definition.QueryFilterDefinitionException if any exception on parsing
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

        this.definitionMap = getDefinition(filterClass, queryFilterClass);
        this.defaultMatches = defaultMatches(definitionMap);
        this.defaultSorting = getDefaultSorting(queryFilterClass, definitionMap, filterClass);

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

    private static Map<String, QFAbstractDefinition> getDefinition(Class<?> filterClass,
                                                                   QFDefinitionClass queryFilterClass) throws QueryFilterDefinitionException {

        Map<String, QFAbstractDefinition> map = new HashMap<>();

        for (Field field : getAllFieldsFromClassAndSuperClass(filterClass)) {

            QFAbstractDefinition qfd = QFAbstractDefinition.buildDefinition(field, filterClass,
                    queryFilterClass.value());
            if (qfd == null) {
                continue;
            }

            map.put(qfd.getFilterName(), qfd);

        }

        return Collections.unmodifiableMap(map);
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

        for (QFAbstractDefinition abstractDef : definitionMap.values()) {
            if (abstractDef instanceof QFDefinitionElement) {
                QFDefinitionElement def = (QFDefinitionElement) abstractDef;

                for (QFElement elem : def.getElementAnnotations()) {
                    if (elem.defaultValues().length > 0) {
                        ret.add(new QFElementMatch(Arrays.asList(elem.defaultValues()), elem.defaultOperation(), def));
                    }
                }

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

        for (QFDefaultSort sort : queryFilterClass.defaultSort()) {

            QFAbstractDefinition definition = definitionMap.get(sort.value());
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

        for (QFPredicate predicate : predicates) {
            ret.put(predicate.name(), new PredicateProcessorResolutor(predicate.expression(), definitionMap,
                    predicate.includeMissing(), predicate.missingOperator()));
        }

        return Collections.unmodifiableMap(ret);
    }

    /**
     * Create a new {@linkplain QueryFilter} instance
     *
     * @param input string filter
     * @param type  standard type
     * @return new {@linkplain QueryFilter} instance
     * @throws io.github.acoboh.query.filter.jpa.exceptions.QueryFilterException if any parsing exception occurs
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
