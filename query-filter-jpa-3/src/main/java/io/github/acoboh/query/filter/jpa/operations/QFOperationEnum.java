package io.github.acoboh.query.filter.jpa.operations;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.util.MultiValueMap;

import io.github.acoboh.query.filter.jpa.contributor.ArrayFunction;
import io.github.acoboh.query.filter.jpa.exceptions.QFOperationNotFoundException;
import io.github.acoboh.query.filter.jpa.exceptions.QFUnsopportedSQLException;
import io.github.acoboh.query.filter.jpa.operations.resolutors.QFPredicateResolutor;
import io.github.acoboh.query.filter.jpa.processor.match.QFElementMatch;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaBuilder.In;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;

/**
 * Enumerated with all the operations
 *
 * @author Adrián Cobo
 */
public enum QFOperationEnum implements QFPredicateResolutor {

	/**
	 * Equal operation
	 */
	EQUAL("eq", true, false, ArrayFunction.EQUAL) {
		@Override
		public Predicate generatePredicate(Path<?> path, CriteriaBuilder cb, QFElementMatch match, int index,
				MultiValueMap<String, Object> mlContext) {
			if (match.getDefinition().isArrayTyped()) {
				return PredicateUtils.defaultArrayPredicate(path, cb, match, index, this.getArrayFunction(), mlContext,
						true);
			}

			mlContext.add(match.getDefinition().getFilterName(), match.getPrimaryParsedValue(index));
			return cb.equal(path, match.getPrimaryParsedValue(index));

		}

	},
	/**
	 * Not equal operation
	 */
	NOT_EQUAL("ne", true, false, ArrayFunction.NOT_EQUAL) {
		@Override
		public Predicate generatePredicate(Path<?> path, CriteriaBuilder cb, QFElementMatch match, int index,
				MultiValueMap<String, Object> mlContext) {
			if (match.getDefinition().isArrayTyped()) {
				return PredicateUtils.defaultArrayPredicate(path, cb, match, index, getArrayFunction(), mlContext,
						true);
			}
			mlContext.add(match.getDefinition().getFilterName(), match.getPrimaryParsedValue(index));
			return cb.notEqual(path, match.getPrimaryParsedValue(index));
		}

	},
	/**
	 * Greater than operation
	 */
	GREATER_THAN("gt", true, false, ArrayFunction.GREATER_THAN) {
		@Override
		@SuppressWarnings({"unchecked", "rawtypes"})
		public Predicate generatePredicate(Path<?> path, CriteriaBuilder cb, QFElementMatch match, int index,
				MultiValueMap<String, Object> mlContext) {
			if (match.getDefinition().isArrayTyped()) {
				return PredicateUtils.defaultArrayPredicate(path, cb, match, index, getArrayFunction(), mlContext,
						true);
			}

			mlContext.add(match.getDefinition().getFilterName(), match.getPrimaryParsedValue(index));
			return cb.greaterThan((Path<Comparable>) path, (Comparable) match.getPrimaryParsedValue(index));
		}

	},
	/**
	 * Greater or equal than
	 */
	GREATER_EQUAL_THAN("gte", true, false, ArrayFunction.GREATER_EQUAL_THAN) {
		@SuppressWarnings({"unchecked", "rawtypes"})
		@Override
		public Predicate generatePredicate(Path<?> path, CriteriaBuilder cb, QFElementMatch match, int index,
				MultiValueMap<String, Object> mlContext) {
			if (match.getDefinition().isArrayTyped()) {
				return PredicateUtils.defaultArrayPredicate(path, cb, match, index, getArrayFunction(), mlContext,
						true);
			}
			mlContext.add(match.getDefinition().getFilterName(), match.getPrimaryParsedValue(index));

			return cb.greaterThanOrEqualTo((Path<Comparable>) path, (Comparable) match.getPrimaryParsedValue(index));
		}

	},
	/**
	 * Less than operation
	 */
	LESS_THAN("lt", true, false, ArrayFunction.LESS_THAN) {
		@SuppressWarnings({"unchecked", "rawtypes"})
		@Override
		public Predicate generatePredicate(Path<?> path, CriteriaBuilder cb, QFElementMatch match, int index,
				MultiValueMap<String, Object> mlContext) {
			if (match.getDefinition().isArrayTyped()) {
				return PredicateUtils.defaultArrayPredicate(path, cb, match, index, getArrayFunction(), mlContext,
						true);
			}
			mlContext.add(match.getDefinition().getFilterName(), match.getPrimaryParsedValue(index));

			return cb.lessThan((Path<Comparable>) path, (Comparable) match.getPrimaryParsedValue(index));
		}

	},
	/**
	 * Less or equal than
	 */
	LESS_EQUAL_THAN("lte", true, false, ArrayFunction.LESS_EQUAL_THAN) {
		@SuppressWarnings({"unchecked", "rawtypes"})
		@Override
		public Predicate generatePredicate(Path<?> path, CriteriaBuilder cb, QFElementMatch match, int index,
				MultiValueMap<String, Object> mlContext) {
			if (match.getDefinition().isArrayTyped()) {
				return PredicateUtils.defaultArrayPredicate(path, cb, match, index, getArrayFunction(), mlContext,
						true);
			}

			mlContext.add(match.getDefinition().getFilterName(), match.getPrimaryParsedValue(index));
			return cb.lessThanOrEqualTo((Path<Comparable>) path, (Comparable) match.getPrimaryParsedValue(index));
		}

	},
	BETWEEN("btw", false, false, null) {
		@SuppressWarnings({"unchecked", "rawtypes"})
		@Override
		public Predicate generatePredicate(Path<?> path, CriteriaBuilder cb, QFElementMatch match, int index,
				MultiValueMap<String, Object> mlContext) {
			List<Object> values = match.parsedValues(index);
			mlContext.add(match.getDefinition().getFilterName(), match.parsedValues(0));
			return cb.between((Path<Comparable>) path, (Comparable) values.get(0), (Comparable) values.get(1));
		}

		@Override
		public boolean isValid(List<String> values, boolean arrayTyped) {
			return values != null && values.size() == 2;
		}
	},
	REGEX("regex", false, false, null) {
		@Override
		public Predicate generatePredicate(Path<?> path, CriteriaBuilder cb, QFElementMatch match, int index,
				MultiValueMap<String, Object> mlContext) {
			mlContext.add(match.getDefinition().getFilterName(), match.getSingleValue());
			return PredicateUtils.finalLikeSensitive(cb, path.as(String.class), match.getSingleValue(),
					match.getDefinition().isCaseSensitive(), true);
		}
	},

	/**
	 * Like operation for strings
	 */
	LIKE("like", false, false, null) {
		@Override
		public Predicate generatePredicate(Path<?> path, CriteriaBuilder cb, QFElementMatch match, int index,
				MultiValueMap<String, Object> mlContext) {
			mlContext.add(match.getDefinition().getFilterName(), match.getSingleValue());
			return PredicateUtils.parseLikePredicate(cb, path.as(String.class), match.getSingleValue(),
					match.getDefinition().isCaseSensitive(), true);
		}

	},
	NOT_LIKE("nlike", false, false, null) {
		@Override
		public Predicate generatePredicate(Path<?> path, CriteriaBuilder cb, QFElementMatch match, int index,
				MultiValueMap<String, Object> mlContext) {
			mlContext.add(match.getDefinition().getFilterName(), match.getSingleValue());
			return PredicateUtils.parseLikePredicate(cb, path.as(String.class), match.getSingleValue(),
					match.getDefinition().isCaseSensitive(), false);
		}
	},
	/**
	 * Starts with operation for strings
	 */
	STARTS_WITH("starts", false, false, null) {
		@Override
		public Predicate generatePredicate(Path<?> path, CriteriaBuilder cb, QFElementMatch match, int index,
				MultiValueMap<String, Object> mlContext) {
			mlContext.add(match.getDefinition().getFilterName(), match.getSingleValue());
			return PredicateUtils.parseStartsPredicate(cb, path.as(String.class), match.getSingleValue(),
					match.getDefinition().isCaseSensitive(), true);
		}

	},
	/**
	 * Ends with operation for strings
	 */
	ENDS_WITH("ends", false, false, null) {
		@Override
		public Predicate generatePredicate(Path<?> path, CriteriaBuilder cb, QFElementMatch match, int index,
				MultiValueMap<String, Object> mlContext) {
			mlContext.add(match.getDefinition().getFilterName(), match.getSingleValue());
			return PredicateUtils.parseEndsPredicate(cb, path.as(String.class), match.getSingleValue(),
					match.getDefinition().isCaseSensitive(), true);
		}

	},
	/**
	 * IN operation
	 */
	IN("in", true, false, ArrayFunction.CONTAINS) {
		@Override
		public Predicate generatePredicate(Path<?> path, CriteriaBuilder cb, QFElementMatch match, int index,
				MultiValueMap<String, Object> mlContext) {
			if (match.getDefinition().isArrayTyped()) {
				return PredicateUtils.defaultArrayPredicate(path, cb, match, index, getArrayFunction(), mlContext,
						true);
			}

			In<Object> in = cb.in(path);

			for (Object parsedValue : match.parsedValues(index)) {
				in.value(parsedValue);
				mlContext.add(match.getDefinition().getFilterName(), parsedValue);
			}
			return in;
		}

		@Override
		public boolean isValid(List<String> values, boolean arrayTyped) {
			return values != null && !values.isEmpty();
		}
	},
	/**
	 * Not in operation
	 */
	NOT_IN("nin", true, false, ArrayFunction.CONTAINS) {
		@Override
		public Predicate generatePredicate(Path<?> path, CriteriaBuilder cb, QFElementMatch match, int index,
				MultiValueMap<String, Object> mlContext) {
			if (match.getDefinition().isArrayTyped()) {
				return PredicateUtils.defaultArrayPredicate(path, cb, match, index, getArrayFunction(), mlContext,
						false);
			}

			In<Object> in = cb.in(path);

			for (Object parsedValue : match.parsedValues(index)) {
				in.value(parsedValue);
				mlContext.add(match.getDefinition().getFilterName(), parsedValue);
			}
			return cb.not(in);

		}

		@Override
		public boolean isValid(List<String> values, boolean arrayTyped) {
			return values != null && !values.isEmpty();
		}

	},
	/**
	 * Is null operation
	 */
	ISNULL("null", true, false, null) {
		@Override
		public Predicate generatePredicate(Path<?> path, CriteriaBuilder cb, QFElementMatch match, int index,
				MultiValueMap<String, Object> mlContext) {
			if (Boolean.TRUE.equals(match.getPrimaryParsedValue(index))) {
				return cb.isNull(path);
			} else {
				return cb.isNotNull(path);
			}
		}

	},
	/**
	 * Overlap operation for PostgreSQL Arrays
	 */
	OVERLAP("ovlp", true, true, ArrayFunction.OVERLAP) {
		@Override
		public Predicate generatePredicate(Path<?> path, CriteriaBuilder cb, QFElementMatch match, int index,
				MultiValueMap<String, Object> mlContext) {
			if (match.getDefinition().isArrayTyped()) {
				return PredicateUtils.defaultArrayPredicate(path, cb, match, index, getArrayFunction(), mlContext,
						true);
			}
			throw new QFUnsopportedSQLException(this, match.getDefinition().getFilterName());
		}

	},
	/**
	 * Not overlap operation for PostgreSQL Arrays
	 */
	NOT_OVERLAP("nOvlp", true, true, ArrayFunction.OVERLAP) {
		@Override
		public Predicate generatePredicate(Path<?> path, CriteriaBuilder cb, QFElementMatch match, int index,
				MultiValueMap<String, Object> mlContext) {
			if (match.getDefinition().isArrayTyped()) {
				return PredicateUtils.defaultArrayPredicate(path, cb, match, index, getArrayFunction(), mlContext,
						false);
			}
			throw new QFUnsopportedSQLException(this, match.getDefinition().getFilterName());
		}
	},
	/**
	 * Contained by for PostgreSQL Arrays
	 */
	CONTAINED("containedBy", true, true, ArrayFunction.IS_CONTAINED_BY) {
		@Override
		public Predicate generatePredicate(Path<?> path, CriteriaBuilder cb, QFElementMatch match, int index,
				MultiValueMap<String, Object> mlContext) {
			if (match.getDefinition().isArrayTyped()) {
				return PredicateUtils.defaultArrayPredicate(path, cb, match, index, getArrayFunction(), mlContext,
						true);
			}
			throw new QFUnsopportedSQLException(this, match.getDefinition().getFilterName());
		}
	},
	NOT_CONTAINED("notContainedBy", true, true, ArrayFunction.IS_CONTAINED_BY) {
		@Override
		public Predicate generatePredicate(Path<?> path, CriteriaBuilder cb, QFElementMatch match, int index,
				MultiValueMap<String, Object> mlContext) {
			if (match.getDefinition().isArrayTyped()) {
				return PredicateUtils.defaultArrayPredicate(path, cb, match, index, getArrayFunction(), mlContext,
						false);
			}
			throw new QFUnsopportedSQLException(this, match.getDefinition().getFilterName());
		}
	};

	private static final Map<String, QFOperationEnum> CONSTANTS = new HashMap<>();
	private static final Set<QFOperationEnum> ARRAY_TYPED_CONSTANTS = new HashSet<>();

	static {
		for (QFOperationEnum c : values()) {
			CONSTANTS.put(c.value, c);
			if (c.arrayTyped) {
				ARRAY_TYPED_CONSTANTS.add(c);
			}
		}
	}

	private final String value;

	private final boolean arrayTyped;

	private final boolean onlyArrayTyped;

	private final ArrayFunction arrayFunction;

	QFOperationEnum(String value, boolean arrayTyped, boolean onlyArrayTyped, ArrayFunction arrayFunction) {
		this.value = value;
		this.arrayTyped = arrayTyped;
		this.onlyArrayTyped = onlyArrayTyped;
		this.arrayFunction = arrayFunction;
	}

	/**
	 * Get parameter value on string filter
	 *
	 * @return value
	 */
	public String getValue() {
		return value;
	}

	/**
	 * Get if the operation can be array typed
	 *
	 * @return true if can be array typed
	 */
	public boolean isArrayTyped() {
		return arrayTyped;
	}

	/**
	 * Get the array operation of PostgreSQL
	 *
	 * @return array function for PostgreSQL
	 */
	public ArrayFunction getArrayFunction() {
		return arrayFunction;
	}

	/**
	 * Find operation from the parameter value
	 *
	 * @param value
	 *            parameter value
	 * @return operation found
	 * @throws io.github.acoboh.query.filter.jpa.exceptions.QFOperationNotFoundException
	 *             if the operation is not found
	 */
	public static QFOperationEnum fromValue(String value) throws QFOperationNotFoundException {
		QFOperationEnum constant = CONSTANTS.get(value);
		if (constant == null) {
			throw new QFOperationNotFoundException(value);
		}
		return constant;
	}

	/**
	 * Get allowed operations of any class
	 *
	 * @param clazz
	 *            class to check
	 * @param isArrayTyped
	 *            if the field is array typed
	 * @return set of operations
	 */
	public static Set<QFOperationEnum> getOperationsOfClass(Class<?> clazz, boolean isArrayTyped) {

		if (isArrayTyped) {
			return ARRAY_TYPED_CONSTANTS;
		}

		Set<QFOperationEnum> ret = new HashSet<>();

		for (QFOperationEnum op : values()) {

			if (op.onlyArrayTyped) {
				continue;
			}

			switch (op) {
				case GREATER_THAN, GREATER_EQUAL_THAN, LESS_THAN, LESS_EQUAL_THAN, BETWEEN :
					if (Comparable.class.isAssignableFrom(clazz) || clazz.isPrimitive()) {
						ret.add(op);
					}
					break;
				case ENDS_WITH, STARTS_WITH, LIKE, NOT_LIKE, REGEX :
					if (String.class.isAssignableFrom(clazz)) {
						ret.add(op);
					}
					break;
				default :
					ret.add(op);
			}

		}

		return ret;

	}

	/** {@inheritDoc} */
	@Override
	public String getOperation() {
		return value;
	}

}
