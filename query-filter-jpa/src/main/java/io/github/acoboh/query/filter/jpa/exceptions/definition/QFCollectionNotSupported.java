package io.github.acoboh.query.filter.jpa.exceptions.definition;

import io.github.acoboh.query.filter.jpa.processor.QFPath.QueryFilterElementDefType;

public class QFCollectionNotSupported extends QueryFilterDefinitionException {

	private static final long serialVersionUID = 1L;
	private static final String MESSAGE = "The filter {} on class {} is not allowed to be annotated with @QFCollectionElement. The field type is {} and must be SET or LIST";

	private final String filterName;
	private final Class<?> filterClass;
	private final QueryFilterElementDefType actualType;

	public QFCollectionNotSupported(String filterName, Class<?> filterClass, QueryFilterElementDefType type) {
		super(MESSAGE, filterName, filterClass, type);
		this.filterName = filterName;
		this.filterClass = filterClass;
		this.actualType = type;
	}

	public String getFilterName() {
		return filterName;
	}

	public Class<?> getFilterClass() {
		return filterClass;
	}

	public QueryFilterElementDefType getActualType() {
		return actualType;
	}

}
