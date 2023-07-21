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

	/**
	 * Create a new predicate part
	 * 
	 * @param part   parted
	 * @param nested if it is nested
	 */
	public PredicatePart(String part, boolean nested) {
		Assert.notNull(part, "Predicate part cannot be null");
		this.part = part;
		this.nested = nested;
	}

	/**
	 * Get the predicate part
	 * 
	 * @return predicate part
	 */
	public String getPart() {
		return part;
	}

	/**
	 * Get if it is nested
	 * 
	 * @return if it is nested
	 */
	public boolean isNested() {
		return nested;
	}
}
