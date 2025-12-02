package io.github.acoboh.query.filter.jpa.processor;

import io.github.acoboh.query.filter.jpa.processor.definitions.QFAbstractDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

class QFSpecificationsWarp {

    private static final Logger LOGGER = LoggerFactory.getLogger(QFSpecificationsWarp.class);

    private static final QFSpecificationComparator COMPARATOR = new QFSpecificationComparator();

    private final List<String> defaultFields;

    // Set of fields that will launch the default values when the filter is present
    private final Map<String, Set<String>> fieldsLaunchOnPresent;

    // Reverse map. Map key is the field with the on present annotation. value is
    // the related values
    private final Map<String, Set<String>> fieldsLaunchedOnPresent;

    private final Map<String, QFAbstractDefinition> mapFieldsOnPresent;

    private final List<QFSpecificationPart> specifications = new ArrayList<>();

    /**
     * Default constructor
     *
     * @param defaultValues default values of the query filter
     */
    public QFSpecificationsWarp(List<QFSpecificationPart> defaultValues, Map<String, Set<String>> fieldsLaunchOnPresent,
            List<QFAbstractDefinition> defaultOnPresentFields) {
        this.defaultFields = defaultValues.stream().map(e -> e.getDefinition().getFilterName())
                .collect(Collectors.toList()); // Modifiable List
        this.specifications.addAll(defaultValues);

        this.fieldsLaunchOnPresent = fieldsLaunchOnPresent;
        this.mapFieldsOnPresent = defaultOnPresentFields.stream()
                .collect(Collectors.toMap(QFAbstractDefinition::getFilterName, e -> e));

        fieldsLaunchedOnPresent = defaultOnPresentFields.stream().filter(e -> e.getOnFilterPresentFilters() != null)
                .collect(Collectors.toMap(QFAbstractDefinition::getFilterName,
                        QFAbstractDefinition::getOnFilterPresentFilters));

        for (var presentDefValue : specifications) {
            processOnFilterPresent(presentDefValue.getDefinition().getFilterName());
        }

    }

    /**
     * Add any specification to the list of specifications. If the specification
     * already exists, it will be removed from the list and added again.
     *
     * @param specification a
     *                      {@link io.github.acoboh.query.filter.jpa.processor.QFSpecificationPart}
     *                      object
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
                    var toAdd = mapFieldsOnPresent.get(fieldToAdd).getDefaultMatches();
                    LOGGER.trace("Adding default values for field {}: {}", fieldToAdd, toAdd);
                    specifications.addAll(toAdd);
                }
            }
        }
    }

    /**
     * Delete the specification from the list of specifications.
     *
     * @param field a {@link java.lang.String} object
     */
    public void deleteSpecificationField(String field) {
        defaultFields.remove(field);
        specifications.removeIf(e -> e.getDefinition().getFilterName().equals(field));

        if (fieldsLaunchOnPresent.containsKey(field) && !fieldsLaunchOnPresent.get(field).isEmpty()) {

            for (String relatedFields : fieldsLaunchOnPresent.get(field)) {
                Set<String> affectedBy = fieldsLaunchedOnPresent.get(relatedFields);
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

    public Set<String> getFilterNames() {
        return specifications.stream().map(e -> e.getDefinition().getFilterName()).collect(Collectors.toSet());
    }

    private static class QFSpecificationComparator implements Comparator<QFSpecificationPart> {

        @Override
        public int compare(QFSpecificationPart o1, QFSpecificationPart o2) {

            var def1 = o1.getDefinition();
            var def2 = o2.getDefinition();

            // First the def without default values
            if (def1.hasDefaultValues() && !def2.hasDefaultValues()) {
                return 1;
            } else if (!def1.hasDefaultValues() && def2.hasDefaultValues()) {
                return -1;
            }

            return Integer.compare(o1.getDefinition().getOrder(), o2.getDefinition().getOrder());
        }

    }

}
