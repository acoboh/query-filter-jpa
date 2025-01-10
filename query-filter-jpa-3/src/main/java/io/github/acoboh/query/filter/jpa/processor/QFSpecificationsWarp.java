package io.github.acoboh.query.filter.jpa.processor;

import io.github.acoboh.query.filter.jpa.processor.match.QFElementMatch;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

class QFSpecificationsWarp {

    private static final QFSpecificationComparator COMPARATOR = new QFSpecificationComparator();

    private final List<String> defaultFields;

    private final List<QFSpecificationPart> specifications = new LinkedList<>();

    public QFSpecificationsWarp(List<? extends QFSpecificationPart> defaultValues) {
        this.defaultFields = defaultValues.stream().map(e -> e.getDefinition().getFilterName())
                .collect(Collectors.toList()); // Modifiable List
        this.specifications.addAll(defaultValues);
    }

    public void addSpecification(QFSpecificationPart specification) {

        final String name = specification.getDefinition().getFilterName();

        if (defaultFields.contains(name)) {
            defaultFields.remove(name);
            specifications.removeIf(e -> e.getDefinition().getFilterName().equals(name));
        }

        specifications.add(specification);
    }

    public void deleteSpecificationField(String field) {
        defaultFields.remove(field);
        specifications.removeIf(e -> e.getDefinition().getFilterName().equals(field));
    }

    public List<QFSpecificationPart> getAllParts() {
        return specifications;
    }

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
