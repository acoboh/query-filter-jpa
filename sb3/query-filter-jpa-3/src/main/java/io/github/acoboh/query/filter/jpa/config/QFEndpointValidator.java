package io.github.acoboh.query.filter.jpa.config;

import io.github.acoboh.query.filter.jpa.annotations.QFMultiParam;
import io.github.acoboh.query.filter.jpa.annotations.QFParam;
import io.github.acoboh.query.filter.jpa.processor.QFProcessor;
import io.github.acoboh.query.filter.jpa.properties.QueryFilterProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.core.MethodParameter;
import org.springframework.core.ResolvableType;
import org.springframework.data.util.Pair;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.AbstractHandlerMethodMapping;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Validates at startup that every controller parameter annotated with
 * {@link QFParam} or {@link QFMultiParam} has a registered {@link QFProcessor}.
 * <p>
 * Without this check, a missing processor is only detected at request time (or,
 * worse, an endpoint could run without its filter). Disable with
 * {@code query-filter.validate-endpoints=false}.
 *
 * @author Adrián Cobo
 */
class QFEndpointValidator implements SmartInitializingSingleton {

    private static final Logger LOGGER = LoggerFactory.getLogger(QFEndpointValidator.class);

    private final ListableBeanFactory beanFactory;
    private final QueryFilterProperties properties;

    QFEndpointValidator(ListableBeanFactory beanFactory, QueryFilterProperties properties) {
        this.beanFactory = beanFactory;
        this.properties = properties;
    }

    @Override
    @SuppressWarnings("rawtypes")
    public void afterSingletonsInstantiated() {
        if (!properties.isValidateEndpoints()) {
            LOGGER.warn("query-filter.validate-endpoints=false: endpoints are not validated at startup");
            return;
        }

        Set<Pair<Class<?>, Class<?>>> registered = new HashSet<>();
        for (QFProcessor<?, ?> processor : beanFactory.getBeansOfType(QFProcessor.class).values()) {
            registered.add(Pair.of(processor.getFilterClass(), processor.getEntityClass()));
        }

        List<String> problems = new ArrayList<>();
        for (AbstractHandlerMethodMapping mapping : beanFactory.getBeansOfType(AbstractHandlerMethodMapping.class)
                .values()) {
            for (Object handler : mapping.getHandlerMethods().values()) {
                if (handler instanceof HandlerMethod handlerMethod) {
                    for (MethodParameter parameter : handlerMethod.getMethodParameters()) {
                        validate(parameter, registered, problems);
                    }
                }
            }
        }

        if (!problems.isEmpty()) {
            throw new IllegalStateException("Invalid query filter endpoints:\n - " + String.join("\n - ", problems)
                    + "\nCheck the @EnableQueryFilter base packages (or set query-filter.validate-endpoints=false)");
        }
    }

    private static void validate(MethodParameter parameter, Set<Pair<Class<?>, Class<?>>> registered,
            List<String> problems) {
        Class<?> filterClass;
        QFParam qfParam = parameter.getParameterAnnotation(QFParam.class);
        QFMultiParam qfMultiParam = parameter.getParameterAnnotation(QFMultiParam.class);
        if (qfParam != null) {
            filterClass = qfParam.value();
        } else if (qfMultiParam != null) {
            filterClass = qfMultiParam.value();
        } else {
            return;
        }

        Class<?> entityClass = ResolvableType.forMethodParameter(parameter).getGeneric(0).resolve();
        if (entityClass == null) {
            problems.add(parameter.getExecutable() + ": cannot resolve the entity type of QueryFilter<?>");
        } else if (!registered.contains(Pair.of(filterClass, entityClass))) {
            problems.add(parameter.getExecutable() + ": no QFProcessor for filter " + filterClass.getName()
                    + " and entity " + entityClass.getName());
        }
    }
}
