package io.github.acoboh.query.filter.jpa.converters;

import io.github.acoboh.query.filter.jpa.annotations.QFParam;
import io.github.acoboh.query.filter.jpa.processor.QFProcessor;
import io.github.acoboh.query.filter.jpa.processor.QueryFilter;
import jakarta.validation.constraints.NotNull;
import org.jspecify.annotations.Nullable;
import org.springframework.core.ResolvableType;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.converter.GenericConverter;
import org.springframework.data.util.Pair;

import java.nio.charset.Charset;
import java.util.*;

/**
 * Class with for custom converters of Spring Boot.
 * <p>
 * This class allows to inject {@linkplain QueryFilter} objects on controllers
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
     * @param queryFilterProcessors all query filter processors
     */
    public QFCustomConverter(List<QFProcessor<?, ?>> queryFilterProcessors) {
        this.queryFilterProcessors = queryFilterProcessors;

        for (QFProcessor<?, ?> processor : queryFilterProcessors) {
            mapProcessors.put(Pair.of(processor.getFilterClass(), processor.getEntityClass()), processor);
        }

    }

    /** {@inheritDoc} */
    @Override
    public @Nullable Set<@NotNull ConvertiblePair> getConvertibleTypes() {
        Set<ConvertiblePair> set = new HashSet<>();
        for (QFProcessor<?, ?> processor : queryFilterProcessors) {
            ResolvableType type = ResolvableType.forClassWithGenerics(QueryFilter.class, processor.getEntityClass());
            set.add(new ConvertiblePair(String.class, type.toClass()));
        }

        return set;
    }

    /** {@inheritDoc} */
    @Override
    public Object convert(@Nullable Object source, TypeDescriptor sourceType, TypeDescriptor targetType) {

        if (source != null && source.getClass() == QFProcessor.class) {
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
            throw new IllegalArgumentException("No QueryFilterProcessor found for " + pairKey);
        }

        if (queryParam.base64Encoded()) {
            filter = new String(Base64.getDecoder().decode(filter), Charset.defaultCharset());
        }

        return found.newQueryFilter(filter, queryParam.type());

    }

}
