package io.github.acoboh.query.filter.jpa.processor.match;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ValueNode;
import io.github.acoboh.query.filter.jpa.exceptions.QFJsonParseException;
import io.github.acoboh.query.filter.jpa.operations.QFOperationJsonEnum;
import io.github.acoboh.query.filter.jpa.processor.QFSpecificationPart;
import io.github.acoboh.query.filter.jpa.processor.QueryUtils;
import io.github.acoboh.query.filter.jpa.processor.definitions.QFDefinitionJson;
import io.github.acoboh.query.filter.jpa.spel.SpelResolverContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.MultiValueMap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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

    private final Map<String, String> mapValues;

    private final QFOperationJsonEnum operation;

    /**
     * Default constructor
     *
     * @param value      value
     * @param operation  operation
     * @param definition definition
     * @throws io.github.acoboh.query.filter.jpa.exceptions.QFJsonParseException if any JSON parsing exception
     */
    public QFJsonElementMatch(String value, QFOperationJsonEnum operation, QFDefinitionJson definition)
            throws QFJsonParseException {

        this.definition = definition;
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

    /**
     * Secondary constructor to bypass the JSON parsing options
     *
     * @param value      map of values
     * @param operation  operation
     * @param definition definition on json field
     */
    public QFJsonElementMatch(Map<String, String> value, QFOperationJsonEnum operation, QFDefinitionJson definition) {
        this.definition = definition;
        this.operation = operation;

        this.mapValues = value;
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
    public QFDefinitionJson getDefinition() {
        return definition;
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

    @Override
    public <E> void processPart(Root<E> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder,
                                Map<String, List<Predicate>> predicatesMap, Map<String, Path<?>> pathsMap,
                                MultiValueMap<String, Object> mlmap, SpelResolverContext spelResolver, Class<E> entityClass) {

        predicatesMap.computeIfAbsent(definition.getFilterName(), t -> new ArrayList<>())
                .add(operation.generateJsonPredicate(
                        QueryUtils.getObject(root, definition.getPaths(), pathsMap, true, false, criteriaBuilder),
                        criteriaBuilder, this));

    }

}
