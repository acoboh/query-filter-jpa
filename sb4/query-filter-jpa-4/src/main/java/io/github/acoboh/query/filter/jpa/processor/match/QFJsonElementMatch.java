package io.github.acoboh.query.filter.jpa.processor.match;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ValueNode;
import io.github.acoboh.query.filter.jpa.exceptions.QFJsonParseException;
import io.github.acoboh.query.filter.jpa.exceptions.QFOperationNotAllowed;
import io.github.acoboh.query.filter.jpa.operations.QFOperationJsonEnum;
import io.github.acoboh.query.filter.jpa.processor.QFSpecificationPart;
import io.github.acoboh.query.filter.jpa.processor.QueryInfo;
import io.github.acoboh.query.filter.jpa.processor.QueryUtils;
import io.github.acoboh.query.filter.jpa.processor.definitions.QFDefinitionJson;
import io.github.acoboh.query.filter.jpa.spel.SpelResolverContext;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.MultiValueMap;

import java.util.*;

/**
 * Class with JSON element matching definition
 *
 * @author Adrián Cobo
 */
public class QFJsonElementMatch implements QFSpecificationPart {

    private static final Logger LOGGER = LoggerFactory.getLogger(QFJsonElementMatch.class);

    private static final ObjectMapper mapper = new ObjectMapper();

    static {
        mapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
    }

    private final QFDefinitionJson definition;
    private final String originalValue;

    private final Map<String, String> mapValues;

    private final QFOperationJsonEnum operation;

    /**
     * Default constructor
     *
     * @param value      value
     * @param operation  operation
     * @param definition definition
     * @throws io.github.acoboh.query.filter.jpa.exceptions.QFJsonParseException if
     *                                                                           any
     *                                                                           JSON
     *                                                                           parsing
     *                                                                           exception
     */
    public QFJsonElementMatch(String value, QFOperationJsonEnum operation, QFDefinitionJson definition)
            throws QFJsonParseException {

        if (!definition.isOperationAllowed(operation)) {
            throw new QFOperationNotAllowed(definition.getFilterName(), operation.getOperation());
        }

        this.definition = definition;
        this.originalValue = value;
        this.operation = operation;

        JsonNode valueNode;

        try {
            valueNode = mapper.readTree(value);
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
            Set<Map.Entry<String, JsonNode>> iter = objectNode.properties();
            String pathPrefix = currentPath.isEmpty() ? "" : currentPath + "-";

            for (var entry : iter) {
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
    @Override
    public QFDefinitionJson getDefinition() {
        return definition;
    }

    /** {@inheritDoc} */
    @Override
    public List<String> getOriginalValuesAsString() {
        return List.of(originalValue);
    }

    /** {@inheritDoc} */
    @Override
    public String getOperationAsString() {
        return operation.getOperation();
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
     * Get operation to be applied
     *
     * @return operation
     */
    public QFOperationJsonEnum getOperation() {
        return operation;
    }

    /** {@inheritDoc} */
    @Override
    public <E> void processPart(QueryInfo<E> queryInfo, Map<String, List<Predicate>> predicatesMap,
            Map<String, Path<?>> pathsMap, MultiValueMap<String, Object> mlmap, SpelResolverContext spelResolver,
            Class<E> entityClass) {

        predicatesMap.computeIfAbsent(definition.getFilterName(), t -> new ArrayList<>())
                .add(operation.generateJsonPredicate(QueryUtils.getObject(queryInfo, definition.getAttributes(),
                        definition.getJoinTypes(), pathsMap, true, false), queryInfo.cb(), this));

    }

}
