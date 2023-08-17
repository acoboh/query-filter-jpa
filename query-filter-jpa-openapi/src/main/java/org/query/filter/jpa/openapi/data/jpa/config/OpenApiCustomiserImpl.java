package org.query.filter.jpa.openapi.data.jpa.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springdoc.core.customizers.OpenApiCustomiser;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.RequestMethod;

import io.github.acoboh.query.filter.jpa.annotations.QFDiscriminator;
import io.github.acoboh.query.filter.jpa.operations.QFOperationEnum;
import io.github.acoboh.query.filter.jpa.processor.QFDefinition;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;

/**
 * Class used to customize the OpenAPI definition with filter elements
 *
 * @author Adrián Cobo
 
 */
public class OpenApiCustomiserImpl implements OpenApiCustomiser {

	private static final Logger LOGER = LoggerFactory.getLogger(OpenApiCustomiserImpl.class);

	private List<QFEndpoint> endpoints;

	/**
	 * Default constructor
	 *
	 * @param endpoints endpoints
	 */
	public OpenApiCustomiserImpl(List<QFEndpoint> endpoints) {
		Assert.notNull(endpoints, "Query filter endpoints must not be null");
		this.endpoints = endpoints;
	}

	/** {@inheritDoc} */
	@Override
	public void customise(OpenAPI openApi) {
		for (QFEndpoint endpoint : endpoints) {

			Optional<PathItem> optPath = openApi.getPaths().entrySet().stream()
					.filter(e -> e.getKey().equals(endpoint.getEndpoint())).map(Map.Entry::getValue).findFirst();

			if (!optPath.isPresent()) {
				LOGER.error("Error processing {} path", endpoint.getEndpoint());
				continue;
			}

			Operation op = getOperation(optPath.get(), endpoint.getRequestMethod());

			Optional<Parameter> optParam = op.getParameters().stream()
					.filter(e -> e.getName().equals(endpoint.getParameterName())).findFirst();

			if (!optParam.isPresent()) {
				LOGER.error("Error getting parameter {} on path {}", endpoint.getParameter().getName(),
						endpoint.getEndpoint());
				continue;
			}

			String actualDesc = optParam.get().getDescription();
			LOGER.debug("Override description {}", actualDesc);
			optParam.get().setDescription(createDescription(endpoint));
			Parameter param = optParam.get();
			Schema<String> schema = new Schema<>();
			schema.type("string");
			param.setSchema(schema);

		}

	}

	private String createDescription(QFEndpoint endpoint) {

		StringBuilder builder = new StringBuilder("Filter is <b><i>").append(endpoint.getParamType().getBeatifulName());

		builder.append("</i></b>. Available fields: \n");

		Collection<QFDefinition> defValues = endpoint.getProcessor().getDefinitionMap().values();
		List<QFDefinition> defValuesOrdered = new ArrayList<>(defValues);
		defValuesOrdered.sort(Comparator.comparing(QFDefinition::getFilterName));

		for (QFDefinition def : defValuesOrdered) {
			if (def.isConstructorBlocked()) {
				continue;
			}

			builder.append("<p><b>").append(def.getFilterName()).append("</b>:");

			if (def.isElementFilter()) {

				Set<QFOperationEnum> qfOperations = QFOperationEnum.getOperationsOfClass(def.getFinalClass(),
						def.isArrayTyped());

				if (!qfOperations.isEmpty()) {
					builder.append(" Operations: [<i>");
					String operationsAvailable = qfOperations.stream().map(QFOperationEnum::getValue)
							.collect(Collectors.joining(","));
					builder.append(operationsAvailable).append("</i>]");
				}

			}

			if (def.isSortable()) {
				builder.append(" <i>(Sortable)</i>");
			}

			if (def.isDiscriminatorFilter()) {
				Set<QFOperationEnum> qfOperations = QFOperationEnum.getOperationsOfDiscriminators();
				builder.append(" Operations: [<i>");
				String operationsAvailable = qfOperations.stream().map(QFOperationEnum::getValue)
						.collect(Collectors.joining(","));
				builder.append(operationsAvailable).append("</i>]");

				builder.append(" Possible Values: [");
				String values = Arrays.stream(def.getDiscriminatorAnnotation().value()).map(QFDiscriminator.Value::name)
						.collect(Collectors.joining(","));
				builder.append(values).append("]");

			}

			if (def.isJsonElementFilter()) {
				Set<QFOperationEnum> qfOperations = QFOperationEnum.getOperationsOfJson();
				builder.append(" <i>(JSON element)</i> Operations: [<i>");

				String operationsAvailable = qfOperations.stream().map(QFOperationEnum::getValue)
						.collect(Collectors.joining(","));
				builder.append(operationsAvailable).append("</i>]");

			}

		}

		return builder.toString();

	}

	private Operation getOperation(PathItem item, RequestMethod method) {

		switch (method) {

		case DELETE:
			return item.getDelete();
		case HEAD:
			return item.getHead();
		case OPTIONS:
			return item.getOptions();
		case PATCH:
			return item.getPatch();
		case POST:
			return item.getPost();
		case PUT:
			return item.getPut();
		case TRACE:
			return item.getTrace();
		case GET:
		default:
			return item.getGet();

		}

	}

}
