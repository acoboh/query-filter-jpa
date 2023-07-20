package io.github.acoboh.query.filter.jpa.predicate;

import org.springframework.util.Assert;

/**
 * Predicate part
 *
 * @author Adrián Cobo
 */
public class PredicatePart {

	private final String part;

	private final boolean nested;

	public PredicatePart(String part, boolean nested) {
		Assert.notNull(part, "Predicate part cannot be null");
		this.part = part;
		this.nested = nested;
	}

	public String getPart() {
		return part;
	}

	public boolean isNested() {
		return nested;
	}
}
