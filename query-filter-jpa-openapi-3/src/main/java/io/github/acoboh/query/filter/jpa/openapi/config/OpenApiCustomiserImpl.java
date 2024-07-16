package io.github.acoboh.query.filter.jpa.openapi.config;

import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.ApplicationContext;
import org.springframework.core.ResolvableType;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import io.github.acoboh.query.filter.jpa.annotations.QFDiscriminator;
import io.github.acoboh.query.filter.jpa.annotations.QFParam;
import io.github.acoboh.query.filter.jpa.operations.QFOperationDiscriminatorEnum;
import io.github.acoboh.query.filter.jpa.operations.QFOperationEnum;
import io.github.acoboh.query.filter.jpa.operations.QFOperationJsonEnum;
import io.github.acoboh.query.filter.jpa.processor.QFProcessor;
import io.github.acoboh.query.filter.jpa.processor.definitions.QFAbstractDefinition;
import io.github.acoboh.query.filter.jpa.processor.definitions.QFDefinitionDiscriminator;
import io.github.acoboh.query.filter.jpa.processor.definitions.QFDefinitionElement;
import io.github.acoboh.query.filter.jpa.processor.definitions.QFDefinitionJson;
import io.github.acoboh.query.filter.jpa.processor.definitions.traits.IDefinitionSortable;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.media.Schema;

/**
 * Class used to customize the OpenAPI definition with filter elements
 *
 * @author Adrián Cobo
 * 
 */
@Component
class OpenApiCustomiserImpl implements OpenApiCustomizer {

	private static final Logger LOGGER = LoggerFactory.getLogger(OpenApiCustomiserImpl.class);

	private final ApplicationContext applicationContext;

	/**
	 * Default constructor
	 *
	 * @param endpoints endpoints
	 */
	OpenApiCustomiserImpl(ApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
	}

	/** {@inheritDoc} */
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
			var qfParamAnnotation = param.getParameterAnnotation(QFParam.class);
			if (qfParamAnnotation != null) {

				var filterType = (ParameterizedType) param.getGenericParameterType();
				Class<?> classType = (Class<?>) filterType.getActualTypeArguments()[0];

				var resolvableBeanType = ResolvableType.forClassWithGenerics(QFProcessor.class,
						qfParamAnnotation.value(), classType);
				String[] names = applicationContext.getBeanNamesForType(resolvableBeanType);
				if (names.length > 1) {
					LOGGER.warn("Multiple beans found for type {}", resolvableBeanType);
				} else if (names.length == 0) {
					LOGGER.error("No bean found for type {}", resolvableBeanType);
					continue;
				}

				QFProcessor<?, ?> processor = applicationContext.getBean(names[0], QFProcessor.class);

				Set<String> requestMappingPatterns;
				if (requestMapping.getKey().getPathPatternsCondition() != null) {
					requestMappingPatterns = requestMapping.getKey().getPathPatternsCondition().getPatternValues();
				} else { // Otherwise will be illegal state exception
					requestMappingPatterns = requestMapping.getKey().getPatternsCondition().getPatterns();
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

				processPath(openApi, requestMapping, paramName, qfParamAnnotation, processor, requestMappingPatterns);

			}

		}
	}

	private void processPath(OpenAPI openApi, Entry<RequestMappingInfo, HandlerMethod> requestMapping, String paramName,
			QFParam qfParamAnnotation, QFProcessor<?, ?> processor, Set<String> requestMappingPatterns) {
		for (String path : requestMappingPatterns) { // For multiple mapping on same method

			Optional<PathItem> optPath = openApi.getPaths().entrySet().stream().filter(e -> e.getKey().equals(path))
					.map(Map.Entry::getValue).findFirst();

			if (optPath.isEmpty()) {
				LOGGER.error("Error processing {} path", path);
				continue;
			}

			Operation op = getOperation(optPath.get(),
					requestMapping.getKey().getMethodsCondition().getMethods().iterator().next());

			Optional<io.swagger.v3.oas.models.parameters.Parameter> optParam = op.getParameters().stream()
					.filter(e -> e.getName().equals(paramName)).findFirst();

			if (optParam.isEmpty()) {
				LOGGER.error("Error getting parameter filter on path {}", path);
				continue;
			}

			String actualDesc = optParam.get().getDescription();

			LOGGER.debug("Override description {}", actualDesc);

			optParam.get().setDescription(createDescription(qfParamAnnotation, processor));

			// Force string schema on swagger
			Schema<String> schema = new Schema<>();
			schema.type("string");
			optParam.get().setSchema(schema);
		}
	}

	private String createDescription(QFParam annotation, QFProcessor<?, ?> processor) {

		StringBuilder builder = new StringBuilder("Filter is <b><i>").append(annotation.type().getBeatifulName());

		builder.append("</i></b>. Available fields: \n");

		Collection<QFAbstractDefinition> defValues = processor.getDefinitionMap().values();
		List<QFAbstractDefinition> defValuesOrdered = new ArrayList<>(defValues);
		defValuesOrdered.sort(Comparator.comparing(QFAbstractDefinition::getFilterName));

		for (var def : defValuesOrdered) {
			if (def.isConstructorBlocked()) {
				continue;
			}

			builder.append("<p><b>").append(def.getFilterName()).append("</b>:");

			if (def instanceof QFDefinitionElement defElement) {
				Set<QFOperationEnum> qfOperations = QFOperationEnum
						.getOperationsOfClass(defElement.getFirstFinalClass(), defElement.isArrayTyped());

				if (!qfOperations.isEmpty()) {
					builder.append(" Operations: [<i>");
					String operationsAvailable = qfOperations.stream().map(QFOperationEnum::getValue)
							.collect(Collectors.joining(","));
					builder.append(operationsAvailable).append("</i>]");
				}

			}

			if (def instanceof IDefinitionSortable idef && idef.isSortable()) {
				builder.append(" <i>(Sortable)</i>");
			}

			if (def instanceof QFDefinitionDiscriminator qdefDiscriminator) {
				builder.append(" Operations: [<i>");
				String operationsAvailable = Stream.of(QFOperationDiscriminatorEnum.values())
						.map(QFOperationDiscriminatorEnum::getOperation).collect(Collectors.joining(","));
				builder.append(operationsAvailable).append("</i>]");

				builder.append(" Possible Values: [");
				String values = Arrays.stream(qdefDiscriminator.getDiscriminatorAnnotation().value())
						.map(QFDiscriminator.Value::name).collect(Collectors.joining(","));
				builder.append(values).append("]");
			} else if (def instanceof QFDefinitionJson) {
				builder.append(" <i>(JSON element)</i> Operations: [<i>");

				String operationsAvailable = Stream.of(QFOperationJsonEnum.values())
						.map(QFOperationJsonEnum::getOperation).collect(Collectors.joining(","));
				builder.append(operationsAvailable).append("</i>]");
			}

		}

		return builder.toString();

	}

	private Operation getOperation(PathItem item, RequestMethod method) {

		return switch (method) {

		case DELETE -> item.getDelete();
		case HEAD -> item.getHead();
		case OPTIONS -> item.getOptions();
		case PATCH -> item.getPatch();
		case POST -> item.getPost();
		case PUT -> item.getPut();
		case TRACE -> item.getTrace();
		case GET -> item.getGet();
		default -> throw new IllegalArgumentException("Method not supported" + method);
		};

	}
}
