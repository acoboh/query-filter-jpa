package io.github.acoboh.query.filter.jpa.operations.resolutors;

import io.github.acoboh.query.filter.jpa.processor.match.QFElementMatch;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import org.jspecify.annotations.Nullable;
import org.springframework.util.MultiValueMap;

import java.util.List;

/**
 * Interface to resolve all operations
 *
 * @author Adrián Cobo
 */
public interface QFPredicateResolutor {

    /**
     * Generate predicate of any query filter element
     *
     * @param path      path of criteria builder
     * @param cb        criteria builder
     * @param match     element matched
     * @param index     index of the element matched
     * @param mlContext context of spel resolving
     * @return predicate resolved
     */
    Predicate generatePredicate(Path<?> path, CriteriaBuilder cb, QFElementMatch match, int index,
            MultiValueMap<String, Object> mlContext);

    /**
     * Get the operation string value
     *
     * @return string operation
     */
    String getOperation();

    /**
     * Check if the operation is valid for the values provided
     * 
     * @param values values to check
     * @return true if the operation is valid for the values provided, false
     *         otherwise
     */
    default boolean isValid(@Nullable List<String> values, boolean arrayTyped) {
        if (values == null || values.isEmpty()) {
            return false;
        }

        if (arrayTyped) { // If array typed, we can have multiple values
            return true;
        }

        // If not array typed, we expect only one value
        return values.size() == 1;
    }

}
