package io.github.acoboh.query.filter.jpa.processor.definitions.traits;

import java.util.List;

import io.github.acoboh.query.filter.jpa.processor.QFPath;

/**
 * Sortable interface
 */
public interface IDefinitionSortable {

	/**
	 * Get the sort paths
	 * 
	 * @return sort paths
	 */
	public List<QFPath> getSortPaths();

	/**
	 * Get filter name
	 * 
	 * @return filter name
	 */
	public String getFilterName();

	/**
	 * Get if the field is sortable
	 * 
	 * @return true if sortable, false otherwise
	 */
	public boolean isSortable();

}
