package io.github.acoboh.query.filter.jpa.converters;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.core.ResolvableType;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.converter.GenericConverter;
import org.springframework.data.util.Pair;
import org.springframework.lang.NonNull;

import io.github.acoboh.query.filter.jpa.annotations.QFParam;
import io.github.acoboh.query.filter.jpa.processor.QFProcessor;
import io.github.acoboh.query.filter.jpa.processor.QueryFilter;

/**
 * Class with for custom converters of Spring Boot.
 * <p>
 * This class allows inject {@linkplain QueryFilter} objects on controllers
 *
 * @author Adrián Cobo
 */
public class QFCustomConverter implements GenericConverter {

	private final List<QFProcessor<?, ?>> queryFilterProcessors;

	// Filter - Entity
	private final Map<Pair<Class<?>, Class<?>>, QFProcessor<?, ?>> mapProcessors = new HashMap<>();

	/**
	 * Default constructor
	 *
	 * @param queryFilterProcessors
	 *            all query filter processors
	 */
	public QFCustomConverter(List<QFProcessor<?, ?>> queryFilterProcessors) {
		this.queryFilterProcessors = queryFilterProcessors;

		for (QFProcessor<?, ?> processor : queryFilterProcessors) {
			mapProcessors.put(Pair.of(processor.getFilterClass(), processor.getEntityClass()), processor);
		}

	}

	/** {@inheritDoc} */
	@Override
	public Set<ConvertiblePair> getConvertibleTypes() {
		Set<ConvertiblePair> set = new HashSet<>();
		for (QFProcessor<?, ?> processor : queryFilterProcessors) {
			ResolvableType type = ResolvableType.forClassWithGenerics(QueryFilter.class, processor.getEntityClass());
			set.add(new ConvertiblePair(String.class, type.toClass()));
		}

		return set;
	}

	/** {@inheritDoc} */
	@Override
	public Object convert(Object source, @NonNull TypeDescriptor sourceType, @NonNull TypeDescriptor targetType) {

		if (source.getClass() == QFProcessor.class) {
			return source;
		}

		String filter = (String) source;

		QFParam queryParam = targetType.getAnnotation(QFParam.class);
		if (queryParam == null) {
			throw new IllegalArgumentException("No QueryFilterParam found for " + targetType.getClass());
		}

		Class<?> resolvedClass = targetType.getResolvableType().getGeneric(0).resolve();
		if (resolvedClass == null) {
			throw new IllegalArgumentException("Non resolvable generic class for " + targetType);
		}

		Pair<Class<?>, Class<?>> pairKey = Pair.of(queryParam.value(), resolvedClass);

		QFProcessor<?, ?> found = mapProcessors.get(pairKey);

		if (found == null) {
			throw new IllegalArgumentException("No QueryFilterProcessor found for " + source.getClass());
		}

		return found.newQueryFilter(filter, queryParam.type());

	}

}
