package io.github.acoboh.query.filter.jpa.operations.resolutors;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;

import org.springframework.util.MultiValueMap;

import io.github.acoboh.query.filter.jpa.processor.match.QFElementMatch;

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
