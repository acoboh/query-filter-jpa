package io.github.acoboh.query.filter.jpa.processor.definitions.traits;

import io.github.acoboh.query.filter.jpa.processor.QFAttribute;
import jakarta.persistence.criteria.JoinType;

import java.util.List;

/**
 * Sortable interface
 *
 * @author Adrián Cobo
 */
public interface IDefinitionSortable {

    /**
     * Get the sort paths
     *
     * @return sort paths
     */
    List<List<QFAttribute>> getPaths();

    /**
     * Return true if auto fetch is enabled
     *
     * @param index of path
     * @return true if enabled, false otherwise
     */
    boolean isAutoFetch(int index);

    /**
     * Get filter name
     *
     * @return filter name
     */
    String getFilterName();

    /**
     * Get if the field is sortable
     *
     * @return true if sortable, false otherwise
     */
    boolean isSortable();

    /**
     * Get full path field
     *
     * @return full path field
     */
    List<String> getPathField();

    /**
     * Get the join types
     *
     * @param index of path
     * @return join types
     * @since 1.0.0
     */
    List<JoinType> getJoinTypes(int index);

}
