package io.github.acoboh.query.filter.jpa.operations.resolutors;

import io.github.acoboh.query.filter.jpa.processor.match.QFCollectionMatch;
import org.springframework.util.MultiValueMap;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;

/**
 * Interface for predicate collection predicate resolutors
 */
public interface QFPredicateCollectionResolutor {

    /**
     * Generate predicate of collection elements
     *
     * @param path      path of criteria builder
     * @param cb        criteria builder
     * @param match     element matched
     * @param mlContext context
     * @return predicate resolver
     */
    <C extends java.util.Collection<?>> Predicate generateCollectionPredicate(Expression<C> path, CriteriaBuilder cb,
                                                                              QFCollectionMatch match, MultiValueMap<String, Object> mlContext);

    /**
     * Get the operation string value
     *
     * @return string operation
     */
    String getOperation();

}
