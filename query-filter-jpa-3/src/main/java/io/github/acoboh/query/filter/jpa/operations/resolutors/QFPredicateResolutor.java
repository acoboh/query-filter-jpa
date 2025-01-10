package io.github.acoboh.query.filter.jpa.operations.resolutors;

import org.springframework.util.MultiValueMap;

import io.github.acoboh.query.filter.jpa.processor.match.QFElementMatch;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;

/**
 * Interface to resolve all operations
 *
 * @author Adrián Cobo
 */
public interface QFPredicateResolutor {

	/**
	 * Generate predicate of any query filter element
	 *
	 * @param path
	 *            path of criteria builder
	 * @param cb
	 *            criteria builder
	 * @param match
	 *            element matched
	 * @param index
	 *            index of the element matched
	 * @param mlContext
	 *            context of spel resolving
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

}
