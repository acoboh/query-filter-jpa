package io.github.acoboh.query.filter.jpa.predicate;

import jakarta.annotation.Nullable;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Predicate operations for custom predicates enumeration
 *
 * @author Adrián Cobo
 */
public enum PredicateOperation {

    /**
     * And operator
     */
    AND("AND") {
        @Override
        public Predicate getPredicate(CriteriaBuilder cb, List<Predicate> predicates) {
            return cb.and(predicates.toArray(new Predicate[0]));
        }
    },

    /**
     * Or operator
     */
    OR("OR") {
        @Override
        public Predicate getPredicate(CriteriaBuilder cb, List<Predicate> predicates) {
            return cb.or(predicates.toArray(new Predicate[0]));
        }
    };

    private static final Map<String, PredicateOperation> map;

    static {
        map = Stream.of(PredicateOperation.values()).collect(Collectors.toMap(PredicateOperation::getValue, e -> e));
    }

    private final String value;

    PredicateOperation(String value) {
        this.value = value;
    }

    /**
     * Get the enumerated from the string value
     *
     * @param value String value of operation to be found
     * @return predicate operation found. Null if the operation is not found
     */
    public static @Nullable PredicateOperation getOperator(String value) {
        return map.get(value);
    }

    /**
     * Resolve the predicate with criteria builder
     *
     * @return the predicate to be used in the query
     */
    public abstract Predicate getPredicate(CriteriaBuilder cb, List<Predicate> predicates);

    /**
     * Get the value of the predicate
     *
     * @return value of the predicate
     */
    public String getValue() {
        return value;
    }
}
