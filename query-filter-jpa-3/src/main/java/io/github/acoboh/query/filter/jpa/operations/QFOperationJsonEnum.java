package io.github.acoboh.query.filter.jpa.operations;

import java.util.HashMap;
import java.util.Map;

import io.github.acoboh.query.filter.jpa.exceptions.QFOperationNotFoundException;
import io.github.acoboh.query.filter.jpa.operations.resolutors.QFPredicateJsonResolutor;
import io.github.acoboh.query.filter.jpa.processor.match.QFJsonElementMatch;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;

/**
 * Enum with all JSON operations availables
 */
public enum QFOperationJsonEnum implements QFPredicateJsonResolutor {

	/**
	 * 
	 */
	EQUAL("eq") {
		@Override
		public Predicate generateJsonPredicate(Path<?> path, CriteriaBuilder cb, QFJsonElementMatch match) {

			Predicate[] predicates = new Predicate[match.getMapValues().size()];

			int i = 0;
			for (Map.Entry<String, String> nodeEntry : match.getMapValues().entrySet()) {
				predicates[i++] = cb.equal(
						cb.function(JSON_EXTRACT_FUNCTION, String.class, path, cb.literal(nodeEntry.getKey())),
						nodeEntry.getValue());
			}

			return cb.and(predicates);
		}
	},
	/**
	* 
	*/
	NOT_EQUAL("ne") {
		@Override
		public Predicate generateJsonPredicate(Path<?> path, CriteriaBuilder cb, QFJsonElementMatch match) {
			Predicate[] predicates = new Predicate[match.getMapValues().size()];

			int i = 0;
			for (Map.Entry<String, String> nodeEntry : match.getMapValues().entrySet()) {

				predicates[i++] = cb.notEqual(
						cb.function(JSON_EXTRACT_FUNCTION, String.class, path, cb.literal(nodeEntry.getKey())),
						nodeEntry.getValue());
			}

			return cb.and(predicates);
		}
	},
	/**
	* 
	*/
	LIKE("like") {
		@Override
		public Predicate generateJsonPredicate(Path<?> path, CriteriaBuilder cb, QFJsonElementMatch match) {
			Predicate[] predicates = new Predicate[match.getMapValues().size()];
			int i = 0;

			for (Map.Entry<String, String> nodeEntry : match.getMapValues().entrySet()) {

				predicates[i++] = PredicateUtils.parseLikePredicate(cb,
						cb.function(JSON_EXTRACT_FUNCTION, String.class, path, cb.literal(nodeEntry.getKey())),
						nodeEntry.getValue(), match.getDefinition().isCaseSensitive());
			}

			return cb.and(predicates);
		}
	},
	/**
	* 
	*/
	STARTS_WITH("starts") {
		@Override
		public Predicate generateJsonPredicate(Path<?> path, CriteriaBuilder cb, QFJsonElementMatch match) {
			Predicate[] predicates = new Predicate[match.getMapValues().size()];

			int i = 0;

			for (Map.Entry<String, String> nodeEntry : match.getMapValues().entrySet()) {
				predicates[i++] = PredicateUtils.parseStartsPredicate(cb,
						cb.function(JSON_EXTRACT_FUNCTION, String.class, path, cb.literal(nodeEntry.getKey())),
						nodeEntry.getValue(), match.getDefinition().isCaseSensitive());
			}

			return cb.and(predicates);
		}
	},
	/**
	* 
	*/
	ENDS_WITH("ends") {
		@Override
		public Predicate generateJsonPredicate(Path<?> path, CriteriaBuilder cb, QFJsonElementMatch match) {
			Predicate[] predicates = new Predicate[match.getMapValues().size()];

			int i = 0;
			for (Map.Entry<String, String> nodeEntry : match.getMapValues().entrySet()) {
				predicates[i++] = PredicateUtils.parseEndsPredicate(cb,
						cb.function(JSON_EXTRACT_FUNCTION, String.class, path, cb.literal(nodeEntry.getKey())),
						nodeEntry.getValue(), match.getDefinition().isCaseSensitive());
			}

			return cb.and(predicates);
		}
	};

	private static final Map<String, QFOperationJsonEnum> CONSTANTS = new HashMap<>();
	private static final String JSON_EXTRACT_FUNCTION = "jsonb_extract_path_text";

	static {
		for (QFOperationJsonEnum c : values()) {
			CONSTANTS.put(c.operation, c);
		}
	}

	private final String operation;

	QFOperationJsonEnum(String operation) {
		this.operation = operation;
	}

	@Override
	public String getOperation() {
		return operation;
	}

	/**
	 * Find operation from the parameter value
	 *
	 * @param value parameter value
	 * @return operation found
	 * @throws io.github.acoboh.query.filter.jpa.exceptions.QFOperationNotFoundException if the operation is not found
	 */
	public static QFOperationJsonEnum fromValue(String value) throws QFOperationNotFoundException {
		QFOperationJsonEnum constant = CONSTANTS.get(value);
		if (constant == null) {
			throw new QFOperationNotFoundException(value);
		}
		return constant;
	}

}
