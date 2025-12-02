package io.github.acoboh.query.filter.jpa.processor.definitions;

import io.github.acoboh.query.filter.jpa.annotations.*;
import io.github.acoboh.query.filter.jpa.exceptions.definition.QFAnnotationsException;
import io.github.acoboh.query.filter.jpa.exceptions.definition.QueryFilterDefinitionException;
import io.github.acoboh.query.filter.jpa.processor.QFSpecificationPart;
import jakarta.annotation.Nullable;
import jakarta.persistence.metamodel.Metamodel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Abstract class for base definition
 *
 * @author Adrián Cobo
 */
public abstract class QFAbstractDefinition {

    private static final Logger LOGGER = LoggerFactory.getLogger(QFAbstractDefinition.class);

    protected final FilterFieldInfo filterInfo;

    /**
     * Filter name
     */
    protected String filterName;

    private final boolean isBlocked;
    private final boolean isRequiredStringFilter;
    private final boolean isRequiredExecution;
    private final boolean isRequiredSort;

    QFAbstractDefinition(FilterFieldInfo filterInfo) {
        this.filterInfo = filterInfo;

        this.filterName = filterInfo.field().getName(); // Default value

        this.isBlocked = filterInfo.blockParsing() != null;

        var required = filterInfo.required();

        this.isRequiredStringFilter = required != null && required.onStringFilter();
        this.isRequiredExecution = required != null && required.onExecution();
        this.isRequiredSort = required != null && required.onSort();

    }

    /**
     * Get filter name
     *
     * @return filter name
     */
    public String getFilterName() {
        return filterName;
    }

    /**
     * Get the entity class
     *
     * @return entity class
     */
    public Class<?> getEntityClass() {
        return filterInfo.entityClass();
    }

    /**
     * Get the filter class
     *
     * @return filter class
     */
    public Class<?> getFilterClass() {
        return filterInfo.filterClass();
    }

    /**
     * Get if the field is blocked
     *
     * @return true if the field is blocked
     */
    public boolean isConstructorBlocked() {
        return isBlocked;
    }

    /**
     * Get if the field is required on string filter phase
     * 
     * @return true if the field is required on string filter phase
     */
    public boolean isRequiredStringFilter() {
        return isRequiredStringFilter;
    }

    /**
     * Get if the field is required on execution phase
     *
     * @return true if the field is required on execution phase
     */
    public boolean isRequiredExecution() {
        return isRequiredExecution;
    }

    /**
     * Get if the field is required on sort phase
     *
     * @return true if the field is required on sort phase
     */
    public boolean isRequiredSort() {
        return isRequiredSort;
    }

    /**
     * Get if the on filter present annotation is enabled
     *
     * @return true if the on filter present annotation is enabled
     */
    public boolean isOnPresentFilterEnabled() {
        return filterInfo.onFilterPresent() != null;
    }

    /**
     * Get the on filter present fields
     */
    public @Nullable Set<String> getOnFilterPresentFilters() {
        return filterInfo.onFilterPresent() != null ? Set.of(filterInfo.onFilterPresent().value()) : null;
    }

    public final List<QFSpecificationPart> getDefaultElementMatches() {
        if (isOnPresentFilterEnabled()) {
            LOGGER.trace("On filter present for {} is defined. Will ignore default elements", filterName);
            return List.of();
        }
        return getDefaultMatches();
    }

    public final List<QFSpecificationPart> getDefaultMatches() {
        if (!hasDefaultValues()) {
            LOGGER.trace("No default values defined for {}. Will not create default elements", filterName);
            return List.of();
        }
        return getInnerDefaultValues();
    }

    protected abstract List<QFSpecificationPart> getInnerDefaultValues();

    public abstract int getOrder();

    public abstract boolean hasDefaultValues();

    /**
     * Create a new base definition based on annotations of the field
     *
     * @param filterField filter field
     * @param filterClass filter class
     * @param entityClass entity class
     * @return abstract definition
     * @throws io.github.acoboh.query.filter.jpa.exceptions.definition.QueryFilterDefinitionException if
     *                                                                                                any
     *                                                                                                error
     *                                                                                                happens
     *                                                                                                creating
     *                                                                                                the
     *                                                                                                definition
     * @param metamodel a {@link jakarta.persistence.metamodel.Metamodel} object
     */
    public static @Nullable QFAbstractDefinition buildDefinition(Field filterField, Class<?> filterClass,
            Class<?> entityClass, Metamodel metamodel) throws QueryFilterDefinitionException {

        boolean isQFElement = filterField.isAnnotationPresent(QFElement.class)
                || filterField.isAnnotationPresent(QFElements.class);
        boolean isQFJson = filterField.isAnnotationPresent(QFJsonElement.class);
        boolean isQFDiscriminator = filterField.isAnnotationPresent(QFDiscriminator.class);
        boolean isQFCollection = filterField.isAnnotationPresent(QFCollectionElement.class);
        boolean isQFSortable = filterField.isAnnotationPresent(QFSortable.class);

        long count = Stream.of(isQFElement, isQFJson, isQFDiscriminator, isQFCollection, isQFSortable).filter(e -> e)
                .count();

        if (count == 0) {
            LOGGER.debug("Ignored field {} because it doesn't has annotations", filterField);
            return null;
        } else if (count > 1) {
            throw new QFAnnotationsException(filterField, filterClass, isQFElement, isQFJson, isQFDiscriminator,
                    isQFCollection, isQFSortable);
        }

        QFBlockParsing blockParsing = filterField.getAnnotation(QFBlockParsing.class);
        QFRequired required = filterField.getAnnotation(QFRequired.class);
        QFOnFilterPresent onFilterPresent = filterField.getAnnotation(QFOnFilterPresent.class);

        var filterFieldInfo = new FilterFieldInfo(filterField, filterClass, entityClass, blockParsing, required,
                onFilterPresent);

        if (isQFElement) {
            // Create element definition

            QFElement[] elementAnnotations = filterField.getAnnotationsByType(QFElement.class);
            QFElements elementsAnnotation = filterField.getAnnotation(QFElements.class);
            QFDate dateAnnotation = filterField.getAnnotation(QFDate.class);

            return new QFDefinitionElement(filterFieldInfo, elementsAnnotation, elementAnnotations, dateAnnotation,
                    metamodel);

        } else if (isQFJson) {
            // Create json definition
            QFJsonElement jsonAnnotation = filterField.getAnnotation(QFJsonElement.class);
            return new QFDefinitionJson(filterFieldInfo, jsonAnnotation, metamodel);

        } else if (isQFDiscriminator) {
            // Create discriminator definition
            QFDiscriminator discriminatorAnnotation = filterField.getAnnotation(QFDiscriminator.class);
            return new QFDefinitionDiscriminator(filterFieldInfo, discriminatorAnnotation, metamodel);

        } else if (isQFCollection) {
            // Create collection definition
            QFCollectionElement collectionAnnotation = filterField.getAnnotation(QFCollectionElement.class);
            return new QFDefinitionCollection(filterFieldInfo, collectionAnnotation, metamodel);

        } else if (isQFSortable) {
            QFSortable sortableAnnotation = filterField.getAnnotation(QFSortable.class);
            return new QFDefinitionSortable(filterFieldInfo, sortableAnnotation, metamodel);
        }

        return null;
    }

}
