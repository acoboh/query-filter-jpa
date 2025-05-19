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

	public static Expression<?>[] joinExp(CriteriaBuilder cb, Expression<?> exp, List<Object> literals) {

		Expression<?>[] array = new Expression[literals.size() + 1];

		array[0] = exp;

		for (int i = 1; i < array.length; i++) {
			array[i] = cb.literal(literals.get(i - 1));
		}

		return array;
	}

	public static Predicate parseLikePredicate(CriteriaBuilder criteriaBuilder, Expression<String> exp, String value,
			boolean caseSensitive) {
		String finalValue = "%".concat(value).concat("%");
		return finalLikeSensitive(criteriaBuilder, exp, finalValue, caseSensitive);
	}

	public static Predicate parseStartsPredicate(CriteriaBuilder criteriaBuilder, Expression<String> exp, String value,
			boolean caseSensitive) {
		String finalValue = value.concat("%");
		return finalLikeSensitive(criteriaBuilder, exp, finalValue, caseSensitive);
	}

	public static Predicate parseEndsPredicate(CriteriaBuilder criteriaBuilder, Expression<String> exp, String value,
			boolean caseSensitive) {
		String finalValue = "%".concat(value);
		return finalLikeSensitive(criteriaBuilder, exp, finalValue, caseSensitive);
	}

	public static Predicate finalLikeSensitive(CriteriaBuilder criteriaBuilder, Expression<String> exp, String value,
			boolean caseSensitive) {
		if (caseSensitive) {
			LOGGER.trace("Case sensitive true on like expression");
			return criteriaBuilder.like(exp, value);
		}

		return criteriaBuilder.like(criteriaBuilder.lower(exp), value.toLowerCase());
	}

	public static Predicate defaultArrayPredicate(Path<?> path, CriteriaBuilder cb, QFElementMatch match, int index,
			ArrayFunction arrayFunction, MultiValueMap<String, Object> mlContext, boolean retValue) {

		match.parsedValues(index).forEach(e -> mlContext.add(match.getDefinition().getFilterName(), e));


		return cb.equal(
				cb.function(arrayFunction.getName(), Boolean.class, joinExp(cb, path, match.parsedValues(index))),
				retValue);
	}

}
