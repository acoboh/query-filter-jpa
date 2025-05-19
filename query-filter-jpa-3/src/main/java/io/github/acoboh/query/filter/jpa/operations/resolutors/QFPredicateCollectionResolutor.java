package io.github.acoboh.query.filter.jpa.operations.resolutors;

import java.util.Collection;

import org.springframework.util.MultiValueMap;

import io.github.acoboh.query.filter.jpa.processor.match.QFCollectionMatch;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;

/**
 * Interface for predicate collection predicate resolutors
 *
 * @author Adrián Cobo
 */
public interface QFPredicateCollectionResolutor {

	/**
	 * Generate predicate of collection elements
	 *
	 * @param path
	 *            path of criteria builder
	 * @param cb
	 *            criteria builder
	 * @param match
	 *            element matched
	 * @param mlContext
	 *            multi value map context
	 * @return predicate resolver
	 * @param <C>
	 *            class implementing collection
	 */
	<C extends Collection<?>> Predicate generateCollectionPredicate(Expression<C> path, CriteriaBuilder cb,
			QFCollectionMatch match, MultiValueMap<String, Object> mlContext);

	/**
	 * Get the operation string value
	 *
	 * @return string operation
	 */
	String getOperation();

}
