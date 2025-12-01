package io.github.acoboh.query.filter.jpa.processor;

import io.github.acoboh.query.filter.jpa.processor.definitions.QFAbstractDefinition;
import io.github.acoboh.query.filter.jpa.spel.SpelResolverContext;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import org.springframework.util.MultiValueMap;

import java.util.List;
import java.util.Map;

/**
 * Interface to create all parts of the final specification
 *
 * @author Adrián Cobo
 */
public interface QFSpecificationPart {

    /**
     * Process the specification part
     *
     * @param <E>           Entity class
     * @param predicatesMap Map of predicates
     * @param pathsMap      Map of paths
     * @param mlmap         Multi-value map for SpEL
     * @param spelResolver  Spel Resolver class
     * @param entityClass   Entity class
     * @param queryInfo     a
     *                      {@link io.github.acoboh.query.filter.jpa.processor.QueryInfo}
     *                      object
     */
    <E> void processPart(QueryInfo<E> queryInfo, Map<String, List<Predicate>> predicatesMap,
            Map<String, Path<?>> pathsMap, MultiValueMap<String, Object> mlmap, SpelResolverContext spelResolver,
            Class<E> entityClass);

    /**
     * Get definition of the filter field
     *
     * @return definition
     */
    QFAbstractDefinition getDefinition();

    /**
     * Get the field operation as string
     *
     * @return operation as string
     * @since 1.0.0
     */
    String getOperationAsString();

    /**
     * Get original values as string
     *
     * @return original values
     * @since 1.0.0
     */
    List<String> getOriginalValuesAsString();

}
