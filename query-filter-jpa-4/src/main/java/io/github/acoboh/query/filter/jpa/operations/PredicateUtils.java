package io.github.acoboh.query.filter.jpa.operations;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.MultiValueMap;

import io.github.acoboh.query.filter.jpa.contributor.ArrayFunction;
import io.github.acoboh.query.filter.jpa.processor.match.QFElementMatch;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;

class PredicateUtils {

	private static final Logger LOGGER = LoggerFactory.getLogger(PredicateUtils.class);

	private PredicateUtils() {

	}

	/**
	 * Join expressions on single array. Used for array functions to unify the
	 * parameters
	 *
	 * @param cb
	 *            Criteria builder of the query
	 * @param exp
	 *            expression to join
	 * 
	 * @param literals
	 *            list of literals to join
	 * 
	 * @return an array of {@link jakarta.persistence.criteria.Expression} objects
	 */
	public static Expression<?>[] joinExp(CriteriaBuilder cb, Expression<?> exp, List<Object> literals) {

		Expression<?>[] array = new Expression[literals.size() + 1];

		array[0] = exp;

		for (int i = 1; i < array.length; i++) {
			array[i] = cb.literal(literals.get(i - 1));
		}

		return array;
	}

	/**
	 * Creates a predicate for the given expression and value using the like
	 * operator.
	 *
	 * @param criteriaBuilder
	 *            criteriaBuilder of the query
	 * @param exp
	 *            expression to apply the predicate
	 * @param value
	 *            the value to match against
	 * @param caseSensitive
	 *            true if the predicate should be case-sensitive, false otherwise
	 * @return a {@link jakarta.persistence.criteria.Predicate} which is the result
	 *         of the like operation
	 */
	public static Predicate parseLikePredicate(CriteriaBuilder criteriaBuilder, Expression<String> exp, String value,
			boolean caseSensitive, boolean like) {
		String finalValue = "%".concat(value).concat("%");
		return finalLikeSensitive(criteriaBuilder, exp, finalValue, caseSensitive, like);
	}

	/**
	 * Creates a predicate for the given expression and value using the like
	 * operator.
	 * <p>
	 * This method is used to create a 'starts with' predicate. The value is
	 * concatenated with a '%' character to match any string that starts with the
	 * given value.
	 * </p>
	 *
	 * @param criteriaBuilder
	 *            criteriaBuilder of the query
	 * @param exp
	 *            expression to apply the predicate
	 * @param value
	 *            the value to match against
	 * @param caseSensitive
	 *            true if the predicate should be case-sensitive, false
	 * @return a {@link jakarta.persistence.criteria.Predicate} which is the result
	 *         of the like operation
	 */
	public static Predicate parseStartsPredicate(CriteriaBuilder criteriaBuilder, Expression<String> exp, String value,
			boolean caseSensitive, boolean like) {
		String finalValue = value.concat("%");
		return finalLikeSensitive(criteriaBuilder, exp, finalValue, caseSensitive, like);
	}

	/**
	 * Creates a predicate for the given expression and value using the like
	 * operator.
	 * <p>
	 * This method is used to create an 'ends with' predicate. The value is
	 * concatenated with a '%' character to match any string that ends with the
	 * given value.
	 * </p>
	 *
	 * @param criteriaBuilder
	 *            criteriaBuilder of the query
	 * @param exp
	 *            expression to apply the predicate
	 * @param value
	 *            the value to match against
	 * @param caseSensitive
	 *            true if the predicate should be case-sensitive, false otherwise
	 * @return a {@link jakarta.persistence.criteria.Predicate} which is the result
	 *         of the like operation
	 */
	public static Predicate parseEndsPredicate(CriteriaBuilder criteriaBuilder, Expression<String> exp, String value,
			boolean caseSensitive, boolean like) {
		String finalValue = "%".concat(value);
		return finalLikeSensitive(criteriaBuilder, exp, finalValue, caseSensitive, like);
	}

	/**
	 * Creates a predicate for the given expression and value using the like
	 * operator.
	 * 
	 *
	 * @param criteriaBuilder
	 *            criteriaBuilder of the query
	 * @param exp
	 *            expression to apply the predicate
	 * @param value
	 *            value to match against
	 * @param caseSensitive
	 *            true if the predicate should be case-sensitive, false otherwise
	 * @return a {@link jakarta.persistence.criteria.Predicate} object
	 */
	public static Predicate finalLikeSensitive(CriteriaBuilder criteriaBuilder, Expression<String> exp, String value,
			boolean caseSensitive, boolean like) {

		if (caseSensitive) {
			LOGGER.trace("Case sensitive true on like expression");
			if (like) {
				LOGGER.trace("Like is true on like expression");
				return criteriaBuilder.like(exp, value);
			}
			return criteriaBuilder.notLike(exp, value);
		}

		if (like) {
			LOGGER.trace("Case sensitive false on like expression");
			return criteriaBuilder.like(criteriaBuilder.lower(exp), value.toLowerCase());
		}

		LOGGER.trace("Case sensitive false on not like expression");
		return criteriaBuilder.notLike(criteriaBuilder.lower(exp), value.toLowerCase());
	}

	/**
	 * Create a predicate for array functions
	 *
	 * @param path
	 *            path to the field
	 * @param cb
	 *            criteria builder of the query
	 * @param match
	 *            the match object
	 * @param index
	 *            the index of the parsed value
	 * @param arrayFunction
	 *            the array function to use
	 * @param mlContext
	 *            multi value map context of all the parsed values
	 * @param retValue
	 *            true if the function should return true, false otherwise
	 * @return a {@link jakarta.persistence.criteria.Predicate} object
	 */
	public static Predicate defaultArrayPredicate(Path<?> path, CriteriaBuilder cb, QFElementMatch match, int index,
			ArrayFunction arrayFunction, MultiValueMap<String, Object> mlContext, boolean retValue) {

		match.parsedValues(index).forEach(e -> mlContext.add(match.getDefinition().getFilterName(), e));

		return cb.equal(
				cb.function(arrayFunction.getName(), Boolean.class, joinExp(cb, path, match.parsedValues(index))),
				retValue);
	}

}
