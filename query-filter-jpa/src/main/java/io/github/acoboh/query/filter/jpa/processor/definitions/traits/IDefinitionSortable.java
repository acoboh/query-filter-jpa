package io.github.acoboh.query.filter.jpa.processor.definitions.traits;

import java.util.List;

import io.github.acoboh.query.filter.jpa.processor.QFPath;

public interface IDefinitionSortable {

	public List<QFPath> getSortPaths();

	public String getFilterName();

}
