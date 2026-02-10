package io.github.acoboh.query.filter.jpa.converters;

import io.github.acoboh.query.filter.jpa.annotations.QFMultiParam;
import io.github.acoboh.query.filter.jpa.annotations.QFParam;
import io.github.acoboh.query.filter.jpa.processor.QFProcessor;
import io.github.acoboh.query.filter.jpa.processor.QueryFilter;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.core.MethodParameter;
import org.springframework.data.util.Pair;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class QueryFilterMethodArgumentResolver implements HandlerMethodArgumentResolver {

    private final Map<Pair<Class<?>, Class<?>>, QFProcessor<?, ?>> processorCache;

    public QueryFilterMethodArgumentResolver(List<QFProcessor<?, ?>> processors) {
        this.processorCache = new ConcurrentHashMap<>();

        for (var processor : processors) {
            processorCache.put(Pair.of(processor.getFilterClass(), processor.getEntityClass()), processor);
        }
    }

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return (parameter.hasParameterAnnotation(QFMultiParam.class))
                && QueryFilter.class.isAssignableFrom(parameter.getParameterType());
    }

    @Override
    public @Nullable Object resolveArgument(@NonNull MethodParameter parameter,
            @Nullable ModelAndViewContainer mavContainer, @NonNull NativeWebRequest webRequest,
            @Nullable WebDataBinderFactory binderFactory) throws Exception {

        QFMultiParam qfMultiParam = parameter.getParameterAnnotation(QFMultiParam.class);
        QFParam qfParam = parameter.getParameterAnnotation(QFParam.class);
        if (qfMultiParam == null) {
            return null;
        }

        Class<?> filterClass = qfMultiParam != null ? qfMultiParam.value() : qfParam.value();

        // Get the actual class used in QueryFilter<T>
        Type[] genericTypes = parameter.getGenericParameterType() instanceof ParameterizedType pType
                ? pType.getActualTypeArguments()
                : null;
        if (genericTypes == null || genericTypes.length == 0) {
            return null;
        }

        Class<?> actualClass = (Class<?>) genericTypes[0];

        // Search for QFProcessor bean
        var processor = processorCache.get(Pair.of(filterClass, actualClass));
        if (processor == null) {
            return null;
        }

        // Create filter
        Map<String, String[]> allParams = webRequest.getParameterMap();

        return processor.newQueryFilterMap(allParams, qfMultiParam.ignoreUnknown(), qfMultiParam.type());
    }
}
