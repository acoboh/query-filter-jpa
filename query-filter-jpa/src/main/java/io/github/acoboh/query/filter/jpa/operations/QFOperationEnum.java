package io.github.acoboh.query.filter.jpa.operations;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaBuilder.In;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.MultiValueMap;

import com.google.common.collect.Sets;

import io.github.acoboh.query.filter.jpa.contributor.ArrayFunction;
import io.github.acoboh.query.filter.jpa.exceptions.QFOperationNotFoundException;
import io.github.acoboh.query.filter.jpa.exceptions.QFUnsopportedSQLException;
import io.github.acoboh.query.filter.jpa.processor.QFElementMatch;
import io.github.acoboh.query.filter.jpa.processor.QFJsonElementMatch;

public enum QFOperationEnum implements QFPredicateResolutor {

	EQUAL("eq", true, false, ArrayFunction.EQUAL) {
		@Override
		public Predicate generatePredicate(Path<?> path, CriteriaBuilder cb, QFElementMatch match, int index,
				MultiValueMap<String, Object> mlContext) {
			if (match.getDefinition().isArrayTyped()) {
				return defaultArrayPredicate(path, cb, match, index, this.getArrayFunction(), mlContext);
			}

			mlContext.add(match.getDefinition().getFilterName(), match.getPrimaryParsedValue(index));
			return cb.equal(path, match.getPrimaryParsedValue(index));

		}

		@Override
		public Predicate generateJsonPredicate(Path<?> path, CriteriaBuilder cb, QFJsonElementMatch match) {
			Predicate finalPredicate = cb.conjunction();

			for (Map.Entry<String, String> nodeEntry : match.getMapValues().entrySet()) {

				finalPredicate.getExpressions().add(cb.equal(
						cb.function("jsonb_extract_path_text", String.class, path, cb.literal(nodeEntry.getKey())),
						nodeEntry.getValue()));
			}

			return finalPredicate;
		}

	},
	NOT_EQUAL("ne", true, false, ArrayFunction.NOT_EQUAL) {
		@Override
		public Predicate generatePredicate(Path<?> path, CriteriaBuilder cb, QFElementMatch match, int index,
				MultiValueMap<String, Object> mlContext) {
			if (match.getDefinition().isArrayTyped()) {
				return defaultArrayPredicate(path, cb, match, index, getArrayFunction(), mlContext);
			}
			mlContext.add(match.getDefinition().getFilterName(), match.getPrimaryParsedValue(index));
			return cb.notEqual(path, match.getPrimaryParsedValue(index));
		}

		@Override
		public Predicate generateJsonPredicate(Path<?> path, CriteriaBuilder cb, QFJsonElementMatch match) {
			Predicate finalPredicate = cb.conjunction();

			for (Map.Entry<String, String> nodeEntry : match.getMapValues().entrySet()) {

				finalPredicate.getExpressions().add(cb.notEqual(
						cb.function("jsonb_extract_path_text", String.class, path, cb.literal(nodeEntry.getKey())),
						nodeEntry.getValue()));
			}

			return finalPredicate;
		}
	},
	GREATER_THAN("gt", true, false, ArrayFunction.GREATER_THAN) {
		@Override
		@SuppressWarnings({ "unchecked", "rawtypes" })
		public Predicate generatePredicate(Path<?> path, CriteriaBuilder cb, QFElementMatch match, int index,
				MultiValueMap<String, Object> mlContext) {
			if (match.getDefinition().isArrayTyped()) {
				return defaultArrayPredicate(path, cb, match, index, getArrayFunction(), mlContext);
			}

			mlContext.add(match.getDefinition().getFilterName(), match.getPrimaryParsedValue(index));
			return cb.greaterThan((Path<Comparable>) path, (Comparable) match.getPrimaryParsedValue(index));
		}

		@Override
		public Predicate generateJsonPredicate(Path<?> path, CriteriaBuilder cb, QFJsonElementMatch match) {
			throw new QFUnsopportedSQLException(this);
		}
	},
	GREATER_THAN_EQUAL("gte", true, false, ArrayFunction.GREATER_EQUAL_THAN) {
		@SuppressWarnings({ "unchecked", "rawtypes" })
		@Override
		public Predicate generatePredicate(Path<?> path, CriteriaBuilder cb, QFElementMatch match, int index,
				MultiValueMap<String, Object> mlContext) {
			if (match.getDefinition().isArrayTyped()) {
				return defaultArrayPredicate(path, cb, match, index, getArrayFunction(), mlContext);
			}
			mlContext.add(match.getDefinition().getFilterName(), match.getPrimaryParsedValue(index));

			return cb.greaterThanOrEqualTo((Path<Comparable>) path, (Comparable) match.getPrimaryParsedValue(index));
		}

		@Override
		public Predicate generateJsonPredicate(Path<?> path, CriteriaBuilder cb, QFJsonElementMatch match) {
			throw new QFUnsopportedSQLException(this);
		}
	},
	LESS_THAN("lt", true, false, ArrayFunction.LESS_THAN) {
		@SuppressWarnings({ "unchecked", "rawtypes" })
		@Override
		public Predicate generatePredicate(Path<?> path, CriteriaBuilder cb, QFElementMatch match, int index,
				MultiValueMap<String, Object> mlContext) {
			if (match.getDefinition().isArrayTyped()) {
				return defaultArrayPredicate(path, cb, match, index, getArrayFunction(), mlContext);
			}
			mlContext.add(match.getDefinition().getFilterName(), match.getPrimaryParsedValue(index));

			return cb.lessThan((Path<Comparable>) path, (Comparable) match.getPrimaryParsedValue(index));
		}

		@Override
		public Predicate generateJsonPredicate(Path<?> path, CriteriaBuilder cb, QFJsonElementMatch match) {
			throw new QFUnsopportedSQLException(this);
		}
	},
	LESS_THAN_EQUAL("lte", true, false, ArrayFunction.LESS_EQUAL_THAN) {
		@SuppressWarnings({ "unchecked", "rawtypes" })
		@Override
		public Predicate generatePredicate(Path<?> path, CriteriaBuilder cb, QFElementMatch match, int index,
				MultiValueMap<String, Object> mlContext) {
			if (match.getDefinition().isArrayTyped()) {
				return defaultArrayPredicate(path, cb, match, index, getArrayFunction(), mlContext);
			}

			mlContext.add(match.getDefinition().getFilterName(), match.getPrimaryParsedValue(index));
			return cb.lessThanOrEqualTo((Path<Comparable>) path, (Comparable) match.getPrimaryParsedValue(index));
		}

		@Override
		public Predicate generateJsonPredicate(Path<?> path, CriteriaBuilder cb, QFJsonElementMatch match) {
			throw new QFUnsopportedSQLException(this);
		}
	},
	LIKE("like", false, false, null) {
		@Override
		public Predicate generatePredicate(Path<?> path, CriteriaBuilder cb, QFElementMatch match, int index,
				MultiValueMap<String, Object> mlContext) {
			mlContext.add(match.getDefinition().getFilterName(), match.getSingleValue());
			return parseLikePredicate(cb, path.as(String.class), match.getSingleValue(), match.isCaseSensitive());
		}

		@Override
		public Predicate generateJsonPredicate(Path<?> path, CriteriaBuilder cb, QFJsonElementMatch match) {
			Predicate finalPredicate = cb.conjunction();

			for (Map.Entry<String, String> nodeEntry : match.getMapValues().entrySet()) {

				finalPredicate.getExpressions()
						.add(parseLikePredicate(cb,
								cb.function("jsonb_extract_path_text", String.class, path,
										cb.literal(nodeEntry.getKey())),
								nodeEntry.getValue(), match.isCaseSensitive()));
			}

			return finalPredicate;
		}
	},
	STARTS_WITH("starts", false, false, null) {
		@Override
		public Predicate generatePredicate(Path<?> path, CriteriaBuilder cb, QFElementMatch match, int index,
				MultiValueMap<String, Object> mlContext) {
			mlContext.add(match.getDefinition().getFilterName(), match.getSingleValue());
			return parseStartsPredicate(cb, path.as(String.class), match.getSingleValue(), match.isCaseSensitive());
		}

		@Override
		public Predicate generateJsonPredicate(Path<?> path, CriteriaBuilder cb, QFJsonElementMatch match) {
			Predicate finalPredicate = cb.conjunction();

			for (Map.Entry<String, String> nodeEntry : match.getMapValues().entrySet()) {

				finalPredicate.getExpressions()
						.add(parseStartsPredicate(cb,
								cb.function("jsonb_extract_path_text", String.class, path,
										cb.literal(nodeEntry.getKey())),
								nodeEntry.getValue(), match.isCaseSensitive()));
			}

			return finalPredicate;
		}
	},
	ENDS_WITH("ends", false, false, null) {
		@Override
		public Predicate generatePredicate(Path<?> path, CriteriaBuilder cb, QFElementMatch match, int index,
				MultiValueMap<String, Object> mlContext) {
			mlContext.add(match.getDefinition().getFilterName(), match.getSingleValue());
			return parseEndsPredicate(cb, path.as(String.class), match.getSingleValue(), match.isCaseSensitive());
		}

		@Override
		public Predicate generateJsonPredicate(Path<?> path, CriteriaBuilder cb, QFJsonElementMatch match) {
			Predicate finalPredicate = cb.conjunction();

			for (Map.Entry<String, String> nodeEntry : match.getMapValues().entrySet()) {

				finalPredicate.getExpressions()
						.add(parseEndsPredicate(cb,
								cb.function("jsonb_extract_path_text", String.class, path,
										cb.literal(nodeEntry.getKey())),
								nodeEntry.getValue(), match.isCaseSensitive()));
			}

			return finalPredicate;
		}
	},
	IN("in", true, false, ArrayFunction.CONTAINS) {
		@Override
		public Predicate generatePredicate(Path<?> path, CriteriaBuilder cb, QFElementMatch match, int index,
				MultiValueMap<String, Object> mlContext) {
			if (match.getDefinition().isArrayTyped()) {
				return defaultArrayPredicate(path, cb, match, index, getArrayFunction(), mlContext);
			}

			In<Object> in = cb.in(path);

			for (Object parsedValue : match.parsedValues(index)) {
				in.value(parsedValue);
				mlContext.add(match.getDefinition().getFilterName(), parsedValue);
			}
			return in;
		}

		@Override
		public Predicate generateJsonPredicate(Path<?> path, CriteriaBuilder cb, QFJsonElementMatch match) {
			throw new QFUnsopportedSQLException(this);
		}
	},
	NOT_IN("nin", true, false, null) {
		@Override
		public Predicate generatePredicate(Path<?> path, CriteriaBuilder cb, QFElementMatch match, int index,
				MultiValueMap<String, Object> mlContext) {
			if (match.getDefinition().isArrayTyped()) {
				return cb.equal(defaultArrayPredicate(path, cb, match, index, getArrayFunction(), mlContext), false);
			}

			In<Object> in = cb.in(path);

			for (Object parsedValue : match.parsedValues(index)) {
				in.value(parsedValue);
				mlContext.add(match.getDefinition().getFilterName(), parsedValue);
			}
			return cb.not(in);

		}

		@Override
		public Predicate generateJsonPredicate(Path<?> path, CriteriaBuilder cb, QFJsonElementMatch match) {
			throw new QFUnsopportedSQLException(this);
		}
	},
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

		@Override
		public Predicate generateJsonPredicate(Path<?> path, CriteriaBuilder cb, QFJsonElementMatch match) {
			throw new QFUnsopportedSQLException(this);
		}
	},
	OVERLAP("ovlp", true, true, ArrayFunction.OVERLAP) {
		@Override
		public Predicate generatePredicate(Path<?> path, CriteriaBuilder cb, QFElementMatch match, int index,
				MultiValueMap<String, Object> mlContext) {
			if (match.getDefinition().isArrayTyped()) {
				return defaultArrayPredicate(path, cb, match, index, getArrayFunction(), mlContext);
			}
			throw new QFUnsopportedSQLException(this);
		}

		@Override
		public Predicate generateJsonPredicate(Path<?> path, CriteriaBuilder cb, QFJsonElementMatch match) {
			throw new QFUnsopportedSQLException(this);
		}

	},
	CONTAINED("containedBy", true, true, ArrayFunction.IS_CONTAINED_BY) {
		@Override
		public Predicate generatePredicate(Path<?> path, CriteriaBuilder cb, QFElementMatch match, int index,
				MultiValueMap<String, Object> mlContext) {
			if (match.getDefinition().isArrayTyped()) {
				return defaultArrayPredicate(path, cb, match, index, getArrayFunction(), mlContext);
			}
			throw new QFUnsopportedSQLException(this);
		}

		@Override
		public Predicate generateJsonPredicate(Path<?> path, CriteriaBuilder cb, QFJsonElementMatch match) {
			throw new QFUnsopportedSQLException(this);
		}
	};

	private static final Logger LOGGER = LoggerFactory.getLogger(QFOperationEnum.class);
	private static final Map<String, QFOperationEnum> CONSTANTS = new HashMap<>();

	static {
		for (QFOperationEnum c : values()) {
			CONSTANTS.put(c.value, c);
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

	public String getValue() {
		return value;
	}

	public boolean isArrayTyped() {
		return arrayTyped;
	}

	public ArrayFunction getArrayFunction() {
		return arrayFunction;
	}

	public static QFOperationEnum fromValue(String value) throws QFOperationNotFoundException {
		QFOperationEnum constant = CONSTANTS.get(value);
		if (constant == null) {
			throw new QFOperationNotFoundException(value);
		}
		return constant;
	}

	public static Set<QFOperationEnum> getOperationsOfClass(Class<?> clazz, boolean isArrayTyped) {

		if (isArrayTyped) {
			return CONSTANTS.values().stream().filter(QFOperationEnum::isArrayTyped).collect(Collectors.toSet());
		}

		Set<QFOperationEnum> ret = new HashSet<>();

		for (QFOperationEnum op : values()) {

			if (op.onlyArrayTyped) {
				continue;
			}

			switch (op) {
			case GREATER_THAN:
			case GREATER_THAN_EQUAL:
			case LESS_THAN:
			case LESS_THAN_EQUAL:
				if (Comparable.class.isAssignableFrom(clazz) || clazz.isPrimitive()) {
					ret.add(op);
				}
				break;
			case ENDS_WITH:
			case STARTS_WITH:
			case LIKE:
				if (String.class.isAssignableFrom(clazz)) {
					ret.add(op);
				}
				break;
			default:
				ret.add(op);
			}

		}

		return ret;

	}

	private static final Set<QFOperationEnum> DISCRIMINATOR_OPERATIONS = Sets.newHashSet(EQUAL, NOT_EQUAL, IN, NOT_IN);

	public static Set<QFOperationEnum> getOperrationsOfDiscriminators() {
		return DISCRIMINATOR_OPERATIONS;
	}

	private static final Set<QFOperationEnum> JSON_OPERATIONS = Sets.newHashSet(EQUAL, NOT_EQUAL, LIKE, STARTS_WITH,
			ENDS_WITH);

	public static Set<QFOperationEnum> getOperationsOfJson() {
		return JSON_OPERATIONS;
	}

	private static Predicate defaultArrayPredicate(Path<?> path, CriteriaBuilder cb, QFElementMatch match, int index,
			ArrayFunction arrayFunction, MultiValueMap<String, Object> mlContext) {

		match.parsedValues(index).forEach(e -> mlContext.add(match.getDefinition().getFilterName(), e));

		return cb.equal(
				cb.function(arrayFunction.getName(), Boolean.class, joinExp(cb, path, match.parsedValues(index))),
				true);
	}

	private static Expression<?>[] joinExp(CriteriaBuilder cb, Expression<?> exp, List<Object> literals) {

		Expression<?>[] array = new Expression[literals.size() + 1];

		array[0] = exp;

		for (int i = 1; i < array.length; i++) {
			array[i] = cb.literal(literals.get(i - 1));
		}

		return array;
	}

	private static Predicate parseLikePredicate(CriteriaBuilder criteriaBuilder, Expression<String> exp, String value,
			boolean caseSensitive) {
		String finalValue = "%".concat(value).concat("%");
		return finalLikeSensitive(criteriaBuilder, exp, finalValue, caseSensitive);
	}

	private static Predicate parseStartsPredicate(CriteriaBuilder criteriaBuilder, Expression<String> exp, String value,
			boolean caseSensitive) {
		String finalValue = value.concat("%");
		return finalLikeSensitive(criteriaBuilder, exp, finalValue, caseSensitive);
	}

	private static Predicate parseEndsPredicate(CriteriaBuilder criteriaBuilder, Expression<String> exp, String value,
			boolean caseSensitive) {
		String finalValue = "%".concat(value);
		return finalLikeSensitive(criteriaBuilder, exp, finalValue, caseSensitive);
	}

	private static Predicate finalLikeSensitive(CriteriaBuilder criteriaBuilder, Expression<String> exp, String value,
			boolean caseSensitive) {
		if (caseSensitive) {
			LOGGER.trace("Case sensitive true on like expression");
			return criteriaBuilder.like(exp, value);
		}

		return criteriaBuilder.like(criteriaBuilder.lower(exp), value.toLowerCase());
	}

	@Override
	public String getOperation() {
		return value;
	}

}
