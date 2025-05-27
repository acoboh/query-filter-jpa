package io.github.acoboh.query.filter.jpa.processor;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.acoboh.query.filter.jpa.processor.definitions.QFAbstractDefinition;
import io.github.acoboh.query.filter.jpa.processor.definitions.QFDefinitionElement;
import io.github.acoboh.query.filter.jpa.processor.match.QFElementMatch;

class QFSpecificationsWarp {

	private static final Logger LOGGER = LoggerFactory.getLogger(QFSpecificationsWarp.class);

	private static final QFSpecificationComparator COMPARATOR = new QFSpecificationComparator();

	private final List<String> defaultFields;

	// Set of fields that will launch the default values when the filter is present
	private final Map<String, Set<String>> fieldsLaunchOnPresent;

	// Reverse map. Map key is the field with the on present annotation. value is
	// the related values
	private final Map<String, Set<String>> fieldsLauchedOnPresent;

	private final Map<String, QFDefinitionElement> mapFieldsOnPresent;

	private final List<QFSpecificationPart> specifications = new LinkedList<>();

	/**
	 * Default constructor
	 *
	 * @param defaultValues
	 *            default values of the query filter
	 */
	public QFSpecificationsWarp(List<? extends QFSpecificationPart> defaultValues,
			Map<String, Set<String>> fieldsLaunchOnPresent, List<QFDefinitionElement> defaultOnPresentFields) {
		this.defaultFields = defaultValues.stream().map(e -> e.getDefinition().getFilterName())
				.collect(Collectors.toList()); // Modifiable List
		this.specifications.addAll(defaultValues);

		this.fieldsLaunchOnPresent = fieldsLaunchOnPresent;
		this.mapFieldsOnPresent = defaultOnPresentFields.stream()
				.collect(Collectors.toMap(QFAbstractDefinition::getFilterName, e -> e));

		fieldsLauchedOnPresent = defaultOnPresentFields.stream().collect(
				Collectors.toMap(QFAbstractDefinition::getFilterName, QFDefinitionElement::getOnFilterPresentFilters));

		for (var presentDefValue : specifications) {
			processOnFilterPresent(presentDefValue.getDefinition().getFilterName());
		}

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

		processOnFilterPresent(name);

		specifications.add(specification);
	}

	private void processOnFilterPresent(String name) {
		if (fieldsLaunchOnPresent.containsKey(name) && !fieldsLaunchOnPresent.get(name).isEmpty()) {
			// The field need to add the default value
			for (var fieldToAdd : fieldsLaunchOnPresent.get(name)) {

				// Check if fieldsToAdd are already present in the specifications list
				if (specifications.stream().anyMatch(e -> e.getDefinition().getFilterName().equals(fieldToAdd))) {
					LOGGER.trace("Field {} already present in the specifications, skipping default values", fieldToAdd);
					continue;
				}

				if (mapFieldsOnPresent.containsKey(fieldToAdd)) {
					var toAdd = mapFieldsOnPresent.get(fieldToAdd).getNewMatchesOnFilterPresent();
					LOGGER.trace("Adding default values for field {}: {}", fieldToAdd, toAdd);
					specifications.addAll(toAdd);
				}
			}
		}
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

		if (fieldsLaunchOnPresent.containsKey(field) && !fieldsLaunchOnPresent.get(field).isEmpty()) {

			for (String relatedFields : fieldsLaunchOnPresent.get(field)) {
				Set<String> affectedBy = fieldsLauchedOnPresent.get(relatedFields);
				if (specifications.stream().noneMatch(e -> affectedBy.contains(e.getDefinition().getFilterName()))) {
					specifications.removeIf(e -> e.getDefinition().getFilterName().equals(relatedFields));
				}
			}

		}
	}

	/**
	 * Get all the specifications of the processor.
	 *
	 * @return list of {@link QFSpecificationPart} objects
	 */
	public List<QFSpecificationPart> getAllParts() {
		return specifications;
	}

	/**
	 * Get all the specifications of the processor sorted by the order of the
	 * definition.
	 *
	 * @return list of {@link QFSpecificationPart} objects sorted by the order
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
