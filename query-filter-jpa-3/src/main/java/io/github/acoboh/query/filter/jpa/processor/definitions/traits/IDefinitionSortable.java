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
	public List<List<QFPath>> getSortPaths();

	/**
	 * Return true if auto fetch is enabled
	 * 
	 * @param index of path
	 * 
	 * @return true if enabled, false otherwise
	 */
	public boolean isAutoFetch(int index);

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
