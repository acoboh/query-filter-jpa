package io.github.acoboh.query.filter.jpa.processor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ValueNode;

import io.github.acoboh.query.filter.jpa.exceptions.QFFieldOperationException;
import io.github.acoboh.query.filter.jpa.exceptions.QFJsonParseException;
import io.github.acoboh.query.filter.jpa.operations.QFOperationEnum;

/**
 * Class with JSON element matching definition
 *
 * @author Adrián Cobo
 * @version $Id: $Id
 */
public class QFJsonElementMatch {

	private final static Logger LOGGER = LoggerFactory.getLogger(QFJsonElementMatch.class);

	private final static ObjectMapper mapper = new ObjectMapper();

	static {
		mapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
	}

	private final QFDefinition definition;

	private final String jsonValue;

	private final JsonNode valueNode;

	private final Map<String, String> mapValues;

	private final List<QFPath> paths;

	private final QFOperationEnum operation;

	private final boolean caseSensitive;

	/**
	 * Default constructor
	 *
	 * @param value      value
	 * @param operation  operation
	 * @param definition definition
	 * @throws io.github.acoboh.query.filter.jpa.exceptions.QFJsonParseException if any json parsing exception
	 */
	public QFJsonElementMatch(String value, QFOperationEnum operation, QFDefinition definition)
			throws QFJsonParseException {

		this.definition = definition;
		this.jsonValue = value;
		this.operation = operation;
		this.caseSensitive = definition.isCaseSensitive();

		if (!QFOperationEnum.getOperationsOfJson().contains(operation)) {
			throw new QFFieldOperationException(operation, definition.getFilterName());
		}

		paths = definition.getPaths().get(0);

		try {
			this.valueNode = mapper.readTree(value);
		} catch (JsonProcessingException e) {
			LOGGER.error("Error parsing json", e);
			throw new QFJsonParseException(definition.getFilterName(), e);
		}

		mapValues = new HashMap<>();
		addKeys("", valueNode, mapValues, new ArrayList<>());

	}

	private static void addKeys(String currentPath, JsonNode jsonNode, Map<String, String> map, List<Integer> suffix) {
		if (jsonNode.isObject()) {
			ObjectNode objectNode = (ObjectNode) jsonNode;
			Iterator<Map.Entry<String, JsonNode>> iter = objectNode.fields();
			String pathPrefix = currentPath.isEmpty() ? "" : currentPath + "-";

			while (iter.hasNext()) {
				Map.Entry<String, JsonNode> entry = iter.next();
				addKeys(pathPrefix + entry.getKey(), entry.getValue(), map, suffix);
			}
		} else if (jsonNode.isArray()) {
			ArrayNode arrayNode = (ArrayNode) jsonNode;

			for (int i = 0; i < arrayNode.size(); i++) {
				suffix.add(i + 1);
				addKeys(currentPath, arrayNode.get(i), map, suffix);

				if (i + 1 < arrayNode.size()) {
					suffix.remove(arrayNode.size() - 1);
				}
			}

		} else if (jsonNode.isValueNode()) {
			if (currentPath.contains("-")) {
				StringBuilder currentPathBuilder = new StringBuilder(currentPath);
				for (Integer integer : suffix) {
					currentPathBuilder.append("-").append(integer);
				}
				currentPath = currentPathBuilder.toString();

				suffix = new ArrayList<>();
			}

			ValueNode valueNode = (ValueNode) jsonNode;
			map.put(currentPath, valueNode.asText());
		}
	}

	/**
	 * Get original field definition
	 *
	 * @return field definition
	 */
	public QFDefinition getDefinition() {
		return definition;
	}

	/**
	 * Get the original value
	 *
	 * @return original value
	 */
	public String getJsonValue() {
		return jsonValue;
	}

	/**
	 * Get JSON parsed value
	 *
	 * @return json parsed
	 */
	public JsonNode getValueNode() {
		return valueNode;
	}

	/**
	 * Get values as map
	 *
	 * @return map of values
	 */
	public Map<String, String> getMapValues() {
		return mapValues;
	}

	/**
	 * List of nested path levels
	 *
	 * @return nested path levels
	 */
	public List<QFPath> getPaths() {
		return paths;
	}

	/**
	 * Get operation to be applied
	 *
	 * @return operation
	 */
	public QFOperationEnum getOperation() {
		return operation;
	}

	/**
	 * Get if the field is case sensitive
	 *
	 * @return true if the field is case sensitive
	 */
	public boolean isCaseSensitive() {
		return caseSensitive;
	}

}
