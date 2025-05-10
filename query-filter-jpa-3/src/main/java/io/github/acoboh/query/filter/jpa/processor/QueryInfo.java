package io.github.acoboh.query.filter.jpa.processor;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import jakarta.validation.constraints.NotNull;

public record QueryInfo<E>(@NotNull Root<E> root, CriteriaQuery<?> query, @NotNull CriteriaBuilder cb,
		boolean isCount) {
}
