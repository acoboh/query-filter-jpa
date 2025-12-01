package io.github.acoboh.query.filter.jpa.predicate;

import org.springframework.util.Assert;

/**
 * Predicate part
 *
 * @author Adrián Cobo
 */
public record PredicatePart(String part, boolean nested) {

	/**
	 * Create a new predicate part
	 *
	 * @param part
	 *            parted
	 * @param nested
	 *            if it is nested
	 */
	public PredicatePart {
		Assert.notNull(part, "Predicate part cannot be null");
	}

}
