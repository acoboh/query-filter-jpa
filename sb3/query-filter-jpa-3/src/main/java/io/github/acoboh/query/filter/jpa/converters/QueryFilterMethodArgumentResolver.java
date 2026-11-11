package io.github.acoboh.query.filter.jpa.converters;

import io.github.acoboh.query.filter.jpa.annotations.QFMultiParam;
import io.github.acoboh.query.filter.jpa.processor.QFProcessor;
import io.github.acoboh.query.filter.jpa.processor.QueryFilter;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.core.MethodParameter;
import org.springframework.core.ResolvableType;
import org.springframework.data.util.Pair;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Resolves controller method parameters annotated with
 * {@link io.github.acoboh.query.filter.jpa.annotations.QFMultiParam} into
 * {@link QueryFilter} instances, binding each filter field to its own top-level
 * query parameter.
 *
 * @author Adrián Cobo
 */
public class QueryFilterMethodArgumentResolver implements HandlerMethodArgumentResolver {

    private final Map<Pair<Class<?>, Class<?>>, QFProcessor<?, ?>> processorCache;

    /**
     * Default constructor
     *
     * @param processors all {@link QFProcessor} beans registered in the application
     *                   context, indexed by their filter and entity classes
     */
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

    /**
     * Resolves the {@link QueryFilter} argument.
     * <p>
     * This method never returns {@code null}: a missing or misconfigured processor
     * would otherwise execute the endpoint without any filter (fail-open). Any
     * configuration problem raises an {@link IllegalStateException}.
     */
    @Override
    public Object resolveArgument(@NonNull MethodParameter parameter, @Nullable ModelAndViewContainer mavContainer,
            @NonNull NativeWebRequest webRequest, @Nullable WebDataBinderFactory binderFactory) throws Exception {

        QFMultiParam qfMultiParam = parameter.getParameterAnnotation(QFMultiParam.class);
        if (qfMultiParam == null) {
            throw new IllegalStateException("Missing @QFMultiParam annotation on parameter " + parameter);
        }

        Class<?> filterClass = qfMultiParam.value();

        // Get the actual entity class used in QueryFilter<T>
        Class<?> entityClass = ResolvableType.forMethodParameter(parameter).getGeneric(0).resolve();
        if (entityClass == null) {
            throw new IllegalStateException("Unable to resolve the entity type of QueryFilter<?> on parameter "
                    + parameter + ". Declare it as QueryFilter<YourEntity>");
        }

        // Search for QFProcessor bean
        var processor = processorCache.get(Pair.of(filterClass, entityClass));
        if (processor == null) {
            throw new IllegalStateException("No QFProcessor registered for filter " + filterClass.getName()
                    + " and entity " + entityClass.getName() + " (parameter " + parameter
                    + "). Check @EnableQueryFilter base packages");
        }

        // Create filter
        Map<String, String[]> allParams = webRequest.getParameterMap();

        return processor.newQueryFilterMap(allParams, qfMultiParam.ignoreUnknown(), qfMultiParam.type());
    }
}
