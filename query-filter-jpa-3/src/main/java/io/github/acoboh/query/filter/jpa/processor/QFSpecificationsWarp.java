package io.github.acoboh.query.filter.jpa.processor;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import io.github.acoboh.query.filter.jpa.processor.match.QFElementMatch;

class QFSpecificationsWarp {

	private static final QFSpecificationComparator COMPARATOR = new QFSpecificationComparator();

	private final List<String> defaultFields;

	private final List<QFSpecificationPart> specifications = new LinkedList<>();

	/**
	 * Default constructor
	 *
	 * @param defaultValues
	 *            default values of the query filter
	 */
	public QFSpecificationsWarp(List<? extends QFSpecificationPart> defaultValues) {
		this.defaultFields = defaultValues.stream().map(e -> e.getDefinition().getFilterName())
				.collect(Collectors.toList()); // Modifiable List
		this.specifications.addAll(defaultValues);
	}

	/**
	 * Add any specification to the list of specifications. If the specification
	 * already exists, it will be removed from the list and added again.
	 *
	 * @param specification
	 *            a
	 *            {@link io.github.acoboh.query.filter.jpa.processor.QFSpecificationPart}
	 *            object
	 */
	public void addSpecification(QFSpecificationPart specification) {

		final String name = specification.getDefinition().getFilterName();

		if (defaultFields.contains(name)) {
			defaultFields.remove(name);
			specifications.removeIf(e -> e.getDefinition().getFilterName().equals(name));
		}

		specifications.add(specification);
	}

	/**
	 * Delete the specification from the list of specifications.
	 *
	 * @param field
	 *            a {@link java.lang.String} object
	 */
	public void deleteSpecificationField(String field) {
		defaultFields.remove(field);
		specifications.removeIf(e -> e.getDefinition().getFilterName().equals(field));
	}

	/**
	 * Get all the specifications of the processor.
	 *
	 * @return a {@link java.util.List} object
	 */
	public List<QFSpecificationPart> getAllParts() {
		return specifications;
	}

	/**
	 * Get all the specifications of the processor sorted by the order of the
	 * definition.
	 *
	 * @return a {@link java.util.List} object
	 */
	public List<QFSpecificationPart> getAllPartsSorted() {
		return specifications.stream().sorted(COMPARATOR).toList();
	}

	private static class QFSpecificationComparator implements Comparator<QFSpecificationPart> {

		@Override
		public int compare(QFSpecificationPart o1, QFSpecificationPart o2) {

			if (o1 instanceof QFElementMatch qf1 && o2 instanceof QFElementMatch qf2) {
				return Integer.compare(qf1.getDefinition().getOrder(), qf2.getDefinition().getOrder());
			}

			return 0;

		}

	}

}
