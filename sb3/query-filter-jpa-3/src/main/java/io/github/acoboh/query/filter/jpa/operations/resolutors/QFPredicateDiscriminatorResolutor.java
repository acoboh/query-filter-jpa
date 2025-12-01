package io.github.acoboh.query.filter.jpa.operations.resolutors;

import io.github.acoboh.query.filter.jpa.processor.match.QFDiscriminatorMatch;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;
import org.springframework.util.MultiValueMap;

/**
 * Interface for predicate discriminator predicate resolutors
 *
 * @author Adrián Cobo
 */
public interface QFPredicateDiscriminatorResolutor {

    /**
     * Generate predicate of discriminator elements
     *
     * @param expression expression of criteria
     * @param cb         criteria builder
     * @param match      element matched
     * @param mlContext  context
     * @return predicate generated
     */
    Predicate generateDiscriminatorPredicate(Expression<Class<?>> expression, CriteriaBuilder cb,
            QFDiscriminatorMatch match, MultiValueMap<String, Object> mlContext);

    /**
     * Get the operation string value
     *
     * @return string operation
     */
    String getOperation();

}
