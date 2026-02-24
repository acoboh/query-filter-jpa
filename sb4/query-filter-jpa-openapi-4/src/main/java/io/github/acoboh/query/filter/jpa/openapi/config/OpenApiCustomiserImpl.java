package io.github.acoboh.query.filter.jpa.openapi.config;

import io.github.acoboh.query.filter.jpa.annotations.QFDiscriminator;
import io.github.acoboh.query.filter.jpa.annotations.QFMultiParam;
import io.github.acoboh.query.filter.jpa.annotations.QFParam;
import io.github.acoboh.query.filter.jpa.operations.QFCollectionOperationEnum;
import io.github.acoboh.query.filter.jpa.operations.QFOperationDiscriminatorEnum;
import io.github.acoboh.query.filter.jpa.operations.QFOperationEnum;
import io.github.acoboh.query.filter.jpa.operations.QFOperationJsonEnum;
import io.github.acoboh.query.filter.jpa.processor.QFParamType;
import io.github.acoboh.query.filter.jpa.processor.QFProcessor;
import io.github.acoboh.query.filter.jpa.processor.definitions.*;
import io.github.acoboh.query.filter.jpa.processor.definitions.traits.IDefinitionSortable;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.ApplicationContext;
import org.springframework.core.MethodParameter;
import org.springframework.core.ResolvableType;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.lang.reflect.ParameterizedType;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 * Class used to customize the OpenAPI definition with filter elements
 *
 * @author Adrián Cobo
 */
@Component
class OpenApiCustomiserImpl implements OpenApiCustomizer {

    private static final Logger LOGGER = LoggerFactory.getLogger(OpenApiCustomiserImpl.class);

    private final ApplicationContext applicationContext;

    /**
     * Default constructor
     *
     * @param applicationContext Spring application context
     */
    OpenApiCustomiserImpl(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void customise(OpenAPI openApi) {

        RequestMappingHandlerMapping mappingHandler = applicationContext.getBean("requestMappingHandlerMapping",
                RequestMappingHandlerMapping.class);

        for (var requestMapping : mappingHandler.getHandlerMethods().entrySet()) {
            LOGGER.debug("Checking path {}", requestMapping.getKey());

            processParameter(openApi, requestMapping);

        }
    }

    private void processParameter(OpenAPI openApi, Entry<RequestMappingInfo, HandlerMethod> requestMapping) {
        for (var param : requestMapping.getValue().getMethodParameters()) {

            var qfParam = param.getParameterAnnotation(QFParam.class);
            var qfMultiParam = param.getParameterAnnotation(QFMultiParam.class);

            processQFParam(openApi, requestMapping, param, qfParam, qfMultiParam);

        }
    }

    private void processQFParam(OpenAPI openApi, Entry<RequestMappingInfo, HandlerMethod> requestMapping,
            MethodParameter param, @Nullable QFParam qfParam, @Nullable QFMultiParam qfMultiParam) {

        if (qfParam == null && qfMultiParam == null) {
            // No annotation found, skip param
            return;
        }

        var filterType = (ParameterizedType) param.getGenericParameterType();
        Class<?> classType = (Class<?>) filterType.getActualTypeArguments()[0];

        Class<?> entityClass = qfParam != null ? qfParam.value() : qfMultiParam.value();

        var resolvableBeanType = ResolvableType.forClassWithGenerics(QFProcessor.class, entityClass, classType);
        String[] names = applicationContext.getBeanNamesForType(resolvableBeanType);
        if (names.length > 1) {
            LOGGER.warn("Multiple beans found for type {}", resolvableBeanType);
        } else if (names.length == 0) {
            LOGGER.error("No bean found for type {}", resolvableBeanType);
            return;
        }

        QFProcessor<?, ?> processor = applicationContext.getBean(names[0], QFProcessor.class);

        Set<String> requestMappingPatterns;
        var rq = requestMapping.getKey().getPathPatternsCondition();
        if (rq != null) {
            requestMappingPatterns = rq.getPatternValues();
        } else { // Otherwise will be illegal state exception
            requestMappingPatterns = requestMapping.getKey().getDirectPaths();
        }

        String paramName = param.getParameterName();
        LOGGER.trace("Param name from MethodParam {}", paramName);
        if (paramName == null) {
            paramName = param.getParameter().getName();
            LOGGER.trace("Param name from parameter {}", paramName);
        }

        RequestParam requestParamAnnotation = param.getParameterAnnotation(RequestParam.class);
        if (requestParamAnnotation != null && !requestParamAnnotation.name().isEmpty()) {
            paramName = requestParamAnnotation.name();
            LOGGER.trace("Param name from RequestParam annotation {}", paramName);
        }

        if (qfParam != null) {
            processPathQFParamAnnotation(openApi, requestMapping, paramName, qfParam, processor,
                    requestMappingPatterns);
        } else {
            processPathQFMultiParamAnnotation(openApi, requestMapping, paramName, qfMultiParam, processor,
                    requestMappingPatterns);
        }
    }

    private void processPathQFParamAnnotation(OpenAPI openApi, Entry<RequestMappingInfo, HandlerMethod> requestMapping,
            String paramName, QFParam qfParamAnnotation, QFProcessor<?, ?> processor,
            Set<String> requestMappingPatterns) {
        for (String path : requestMappingPatterns) { // For multiple mapping on same method
            Optional<PathItem> optPath = openApi.getPaths().entrySet().stream().filter(e -> e.getKey().equals(path))
                    .map(Map.Entry::getValue).findFirst();

            if (optPath.isEmpty()) {
                LOGGER.error("Error processing qf annotation on {} path", path);
                continue;
            }

            Operation op = getApiOperation(optPath.get(),
                    requestMapping.getKey().getMethodsCondition().getMethods().iterator().next());

            var optParam = op.getParameters().stream().filter(e -> e.getName().equals(paramName)).findFirst();

            if (optParam.isEmpty()) {
                LOGGER.error("Error getting parameter filter on path {}", path);
                continue;
            }

            String actualDesc = optParam.get().getDescription();

            LOGGER.debug("Override description {}", actualDesc);

            optParam.get().setDescription(createDescriptionQFParam(qfParamAnnotation, processor));

            // Force string schema on swagger
            Schema<String> schema = new Schema<>();
            schema.type("string");
            optParam.get().setSchema(schema);
        }
    }

    private String createDescriptionQFParam(QFParam annotation, QFProcessor<?, ?> processor) {

        StringBuilder builder = new StringBuilder("Filter is **_").append(annotation.type().getBeautifulName());

        builder.append("_**. Available fields:  \n");

        Collection<QFAbstractDefinition> defValues = processor.getDefinitionMap().values();
        List<QFAbstractDefinition> defValuesOrdered = new ArrayList<>(defValues);
        defValuesOrdered.sort(Comparator.comparing(QFAbstractDefinition::getFilterName));

        StringBuilder prevRet = null;

        for (var def : defValuesOrdered) {
            if (prevRet != null) { // Add separation between filters
                builder.append("  \n\n");
            }
            prevRet = buildDefinitionData(def, annotation.type(), false, builder);
        }

        if (annotation.base64Encoded()) {
            builder.append("  \n\n> **_Note_**: The filter **must be base64** encoded");
        }

        return builder.toString();

    }

    private void processPathQFMultiParamAnnotation(OpenAPI openApi,
            Entry<RequestMappingInfo, HandlerMethod> requestMapping, String paramName, QFMultiParam qfParamAnnotation,
            QFProcessor<?, ?> processor, Set<String> requestMappingPatterns) {
        for (String path : requestMappingPatterns) { // For multiple mapping on same method

            var optPath = openApi.getPaths().entrySet().stream().filter(e -> e.getKey().equals(path))
                    .map(Map.Entry::getValue).findFirst();

            if (optPath.isEmpty()) {
                LOGGER.error("Error processing {} path", path);
                continue;
            }

            Operation op = getApiOperation(optPath.get(),
                    requestMapping.getKey().getMethodsCondition().getMethods().iterator().next());

            op.getParameters().removeIf(e -> e.getName().equals(paramName));
            createDescriptionQFMultiParam(qfParamAnnotation, processor, op);

        }
    }

    private void createDescriptionQFMultiParam(QFMultiParam qfParamAnnotation, QFProcessor<?, ?> processor,
            Operation op) {

        Collection<QFAbstractDefinition> defValues = processor.getDefinitionMap().values();
        List<QFAbstractDefinition> defValuesOrdered = new ArrayList<>(defValues);
        defValuesOrdered.sort(Comparator.comparing(QFAbstractDefinition::getFilterName));

        Set<String> fieldsSortables = new TreeSet<>();
        for (var def : defValuesOrdered) {
            if (def instanceof QFDefinitionSortable) {
                fieldsSortables.add(def.getFilterName());
                continue;
            }

            if (def instanceof IDefinitionSortable idef && idef.isSortable()) {
                fieldsSortables.add(def.getFilterName());
            }

            StringBuilder builder = new StringBuilder();

            var paramDef = buildDefinitionData(def, qfParamAnnotation.type(), true, builder);
            if (paramDef == null) {
                continue;
            }

            var param = new io.swagger.v3.oas.models.parameters.Parameter();
            param.setName(def.getFilterName());
            param.setIn("query");
            param.setDescription(paramDef.toString());

            var arraySchema = new ArraySchema();
            arraySchema.setItems(new StringSchema());

            param.setSchema(arraySchema);
            param.setExplode(true);
            op.addParametersItem(param);

        }

        if (!fieldsSortables.isEmpty()) {
            // Sort param
            var sortParam = new io.swagger.v3.oas.models.parameters.Parameter();
            sortParam.setName("sort");
            sortParam.setIn("query");

            StringBuilder sortDesc = new StringBuilder("Sort by fields: ");
            String fields = String.join(", ", fieldsSortables);
            sortDesc.append(fields);

            sortParam.setDescription(sortDesc.toString());

            var arraySchema = new ArraySchema();
            arraySchema.setItems(new StringSchema());

            sortParam.setSchema(arraySchema);
            sortParam.setExplode(true);
            op.addParametersItem(sortParam);
        }

        op.setDescription((op.getDescription() != null ? op.getDescription() : "") + "\n\nAll Filters are **_"
                + qfParamAnnotation.type().getBeautifulName() + "_**.");

    }

    public static @Nullable StringBuilder buildDefinitionData(QFAbstractDefinition def, QFParamType paramType,
            boolean forMulti, StringBuilder builder) {

        if (def.isConstructorBlocked()) {
            return null;
        }

        if (!forMulti) { // For single, we can just put the name of the filter
            builder.append("* **").append(def.getFilterName()).append("**");
        }

        if (!forMulti && def instanceof IDefinitionSortable idef && idef.isSortable()) {
            builder.append(" _(Sortable)_");
        }

        if (def instanceof QFDefinitionSortable) {
            return builder;
        }

        if (!forMulti) { // For single, we need to specify the operations available
            builder.append("  \n");
        }

        createOperations(def, builder);

        if (def instanceof QFDefinitionElement elem && elem.getFirstFinalClass().isEnum()) {
            // Add info about enum

            builder.append("  \nEnum values: [_");
            String enumValues = Arrays.stream(elem.getFirstFinalClass().getEnumConstants()).map(Object::toString)
                    .collect(Collectors.joining(","));
            builder.append(enumValues).append("_]");

        }

        if (def instanceof QFDefinitionDiscriminator qdefDiscriminator) {
            builder.append("  \nPossible Values: [");
            String values = Arrays.stream(qdefDiscriminator.getDiscriminatorAnnotation().value())
                    .map(QFDiscriminator.Value::name).collect(Collectors.joining(","));
            builder.append(values).append("]");
        } else if (def instanceof QFDefinitionJson) {
            builder.append(" _(JSON element)_");
        } else if (def instanceof QFDefinitionCollection) {
            builder.append(" _(Collection)_");
        }
        return builder;
    }

    private static void createOperations(QFAbstractDefinition def, StringBuilder builder) {

        Set<String> operations;

        if (def instanceof QFDefinitionElement defElement) {
            operations = defElement.getRealAllowedOperations().stream().map(QFOperationEnum::getValue)
                    .collect(Collectors.toSet());

        } else if (def instanceof QFDefinitionDiscriminator qdefDiscriminator) {
            operations = qdefDiscriminator.getRealAllowedOperations().stream()
                    .map(QFOperationDiscriminatorEnum::getOperation).collect(Collectors.toSet());

        } else if (def instanceof QFDefinitionJson qdefJson) {
            operations = qdefJson.getRealAllowedOperations().stream().map(QFOperationJsonEnum::getOperation)
                    .collect(Collectors.toSet());

        } else if (def instanceof QFDefinitionCollection defCollection) {
            operations = defCollection.getRealAllowedOperations().stream().map(QFCollectionOperationEnum::getOperation)
                    .collect(Collectors.toSet());

        } else {
            LOGGER.warn("Unknown definition type {}", def.getClass().getName());
            return;
        }

        if (!operations.isEmpty()) {
            builder.append(" Operations: [_");
            String operationsAvailable = String.join(",", operations);
            builder.append(operationsAvailable).append("_]");
        }

    }

    private Operation getApiOperation(PathItem item, RequestMethod method) {

        return switch (method) {
        case DELETE -> item.getDelete();
        case HEAD -> item.getHead();
        case OPTIONS -> item.getOptions();
        case PATCH -> item.getPatch();
        case POST -> item.getPost();
        case PUT -> item.getPut();
        case TRACE -> item.getTrace();
        case GET -> item.getGet();
        };

    }
}
