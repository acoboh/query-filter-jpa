package io.github.acoboh.query.filter.jpa.converters;

import java.util.Objects;

class ProcessorInfo {

	public final Class<?> entityClass;

	private final Class<?> filterClass;

	/**
	 * <p>
	 * Constructor for ProcessorInfo.
	 * </p>
	 *
	 * @param entityClass a {@link java.lang.Class} object
	 * @param filterClass a {@link java.lang.Class} object
	 */
	public ProcessorInfo(Class<?> entityClass, Class<?> filterClass) {
		this.entityClass = entityClass;
		this.filterClass = filterClass;
	}

	/** {@inheritDoc} */
	@Override
	public int hashCode() {
		return Objects.hash(entityClass, filterClass);
	}

	/** {@inheritDoc} */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ProcessorInfo other = (ProcessorInfo) obj;
		return Objects.equals(entityClass, other.entityClass) && Objects.equals(filterClass, other.filterClass);
	}

}
