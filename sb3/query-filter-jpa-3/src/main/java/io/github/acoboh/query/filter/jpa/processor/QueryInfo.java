package io.github.acoboh.query.filter.jpa.processor;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import jakarta.validation.constraints.NotNull;

/**
 * Query information for the {@link QFProcessor} class.
 *
 * @author Adrián Cobo
 * @since 1.0.0
 */
public record QueryInfo<E>(@NotNull Root<E> root, CriteriaQuery<?> query, @NotNull CriteriaBuilder cb,
        boolean isCount) {
}
