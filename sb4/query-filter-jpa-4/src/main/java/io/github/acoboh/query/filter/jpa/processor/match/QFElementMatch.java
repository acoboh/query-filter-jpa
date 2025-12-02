package io.github.acoboh.query.filter.jpa.processor.match;

import io.github.acoboh.query.filter.jpa.exceptions.*;
import io.github.acoboh.query.filter.jpa.operations.QFOperationEnum;
import io.github.acoboh.query.filter.jpa.processor.QFAttribute;
import io.github.acoboh.query.filter.jpa.processor.QFSpecificationPart;
import io.github.acoboh.query.filter.jpa.processor.QueryInfo;
import io.github.acoboh.query.filter.jpa.processor.QueryUtils;
import io.github.acoboh.query.filter.jpa.processor.definitions.QFDefinitionElement;
import io.github.acoboh.query.filter.jpa.spel.SpelResolverContext;
import io.github.acoboh.query.filter.jpa.utils.DateUtils;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.MultiValueMap;

import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

/**
 * Class with info about the filtered field. Contains all the entity fields of
 * the same filter field
 *
 * @author Adrián Cobo
 */
public class QFElementMatch implements QFSpecificationPart {

    private static final Logger LOGGER = LoggerFactory.getLogger(QFElementMatch.class);

    private final QFDefinitionElement definition;

    private final List<String> originalValues;

    private final List<List<QFAttribute>> paths;

    private final QFOperationEnum operation;
    private final List<Class<?>> matchClasses;
    private final List<Boolean> isEnumList;

    private @Nullable List<String> processedValues;
    private @Nullable List<List<Object>> parsedValues;

    private final @Nullable DateTimeFormatter formatter;

    private boolean initialized = false;

    /**
     * Default constructor
     *
     * @param values     list of matching values
     * @param operation  operation to be applied
     * @param definition field definition
     */
    public QFElementMatch(List<String> values, QFOperationEnum operation, QFDefinitionElement definition) {

        if (!definition.isOperationAllowed(operation)) {
            throw new QFOperationNotAllowed(definition.getFilterName(), operation.getValue());
        }

        this.definition = definition;
        this.originalValues = values;
        this.operation = operation;

        if (!operation.isValid(values, definition.isArrayTyped())) {
            throw new QFFilterNotValid(operation, definition.getFilterName());
        }

        formatter = definition.getDateTimeFormatter();

        paths = definition.getPaths();
        matchClasses = new ArrayList<>(paths.size());
        isEnumList = new ArrayList<>(paths.size());

        if (!definition.isSpelExpression()) {
            initialize(null, null);
        }

    }

    /**
     * Initialize spel expressions
     *
     * @param spelResolver spel resolver interface
     * @param context      context of values
     * @return true if initialized successfully
     * @throws io.github.acoboh.query.filter.jpa.exceptions.QFFieldOperationException if
     *                                                                                any
     *                                                                                operation
     *                                                                                exception
     * @throws io.github.acoboh.query.filter.jpa.exceptions.QFEnumException           if
     *                                                                                any
     *                                                                                enumeration
     *                                                                                exception
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public boolean initialize(@Nullable SpelResolverContext spelResolver,
            @Nullable MultiValueMap<String, Object> context) throws QFFieldOperationException, QFEnumException {

        if (definition.isSpelExpression() && !originalValues.isEmpty()) {
            if (spelResolver == null) {
                throw new IllegalStateException(
                        "Unable to evaluate spel expressions on missing bean SpelContextResolver");
            }

            String firstValue = originalValues.get(0);
            Object result = spelResolver.evaluate(firstValue, context);
            processedValues = parseResults(result);
            initialized = false;
        }

        if (initialized) {
            return true;
        }

        if (processedValues == null) {
            processedValues = new ArrayList<>(originalValues);
        }

        parsedValues = new ArrayList<>(paths.size());
        for (var path : paths) {

            QFAttribute lastPath = path.get(path.size() - 1);
            Class<?> finalClass = lastPath.getAttribute().getJavaType();

            // Check operation
            checkOperation(finalClass);

            matchClasses.add(finalClass);

            // Check is an enumeration
            boolean isEnum = lastPath.isEnum();
            isEnumList.add(isEnum);

            List<Object> parsedPathValue = new ArrayList<>(processedValues.size());
            for (String val : processedValues) {

                if (formatter != null && operation != QFOperationEnum.ISNULL) {
                    parsedPathValue.add(parseTimestamp(val, finalClass));
                } else if (finalClass.equals(Double.class) || finalClass.equals(double.class)) {
                    parsedPathValue.add(Double.valueOf(val));
                } else if (finalClass.equals(Integer.class) || finalClass.equals(int.class)) {
                    parsedPathValue.add(Integer.valueOf(val));
                } else if (finalClass.equals(Boolean.class) || finalClass.equals(boolean.class)
                        || operation == QFOperationEnum.ISNULL) {
                    parsedPathValue.add(Boolean.valueOf(val));
                } else if (isEnum) { // Parse enum
                    Class<? extends Enum> enumClass = (Class<? extends Enum>) finalClass;
                    try {
                        parsedPathValue.add(Enum.valueOf(enumClass, val));
                    } catch (IllegalArgumentException e) {
                        Enum[] constants = enumClass.getEnumConstants();
                        String[] allowed = new String[constants.length];

                        for (int i = 0; i < constants.length; i++) { // Get allowed values
                            allowed[i] = constants[i].name();
                        }

                        throw new QFEnumException(definition.getFilterName(), val, enumClass, allowed);
                    }

                } else if (finalClass.equals(UUID.class)) {
                    parsedPathValue.add(UUID.fromString(val));
                } else { // Use as string
                    parsedPathValue.add(val);
                }

            }

            parsedValues.add(parsedPathValue);
        }

        initialized = true;
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public List<String> getOriginalValuesAsString() {
        return originalValues;
    }

    /** {@inheritDoc} */
    @Override
    public String getOperationAsString() {
        return operation.getValue();
    }

    /**
     * Get field definition
     *
     * @return field definition
     */
    @Override
    public QFDefinitionElement getDefinition() {
        return definition;
    }

    /**
     * Get list of nested paths for each model field
     *
     * @return list of nested paths
     */
    public List<List<QFAttribute>> getPaths() {
        return paths;
    }

    /**
     * Get the selected operation
     *
     * @return selected operation
     */
    public QFOperationEnum getOperation() {
        return operation;
    }

    /**
     * Get final class of each model field
     *
     * @param index index model field
     * @return final class of the field
     */
    public Class<?> matchClass(int index) {
        if (!initialized) {
            throw new IllegalStateException();
        }

        return matchClasses.get(index);
    }

    /**
     * List of parsed values on each field
     *
     * @param index index of field
     * @return list of values
     */
    public List<Object> parsedValues(int index) {
        if (!initialized || parsedValues == null || parsedValues.size() <= index) {
            throw new IllegalStateException();
        }
        return parsedValues.get(index);
    }

    /**
     * Get first single value
     *
     * @return first single value
     */
    public String getSingleValue() {
        if (!initialized || processedValues == null || processedValues.isEmpty()) {
            throw new IllegalStateException();
        }
        return processedValues.get(0);
    }

    /**
     * Get the first parsed value of the field
     *
     * @param index index of model field
     * @return first parsed value
     */
    public Object getPrimaryParsedValue(int index) {
        if (!initialized || parsedValues == null || parsedValues.isEmpty()) {
            throw new IllegalStateException();
        }
        return parsedValues.get(index).get(0);
    }

    /**
     * Get if the matching element must be evaluated
     *
     * @return true if it must be evaluated
     */
    public boolean needToEvaluate() {
        if (!initialized) {
            throw new IllegalStateException();
        }

        if (definition.isBlankIgnore()) {
            return processedValues != null && !processedValues.isEmpty();
        }

        return true;

    }

    private Object parseTimestamp(String value, Class<?> finalClass) {

        if (formatter == null || definition.getDateAnnotation() == null) {
            throw new IllegalStateException("Date formatter is null");
        }

        try {
            Object parsedValue = DateUtils.parseDate(formatter, value, finalClass, definition.getDateAnnotation());
            if (parsedValue == null) {
                throw new IllegalStateException("Unsupported date class " + finalClass);
            }

            return parsedValue;

        } catch (DateTimeParseException e) {
            throw new QFDateParsingException(definition.getFilterName(), value,
                    definition.getDateAnnotation().timeFormat(), e);
        }

    }

    private void checkOperation(Class<?> clazz) throws QFFieldOperationException {

        if (definition.isArrayTyped()) {
            checkOperationIsArrayTyped();
            return;
        }

        switch (operation) {
        case GREATER_THAN, GREATER_EQUAL_THAN, LESS_THAN, LESS_EQUAL_THAN -> {
            if (!Comparable.class.isAssignableFrom(clazz) && !clazz.isPrimitive()) {
                throw new QFFieldOperationException(operation, definition.getFilterName());
            }
        }
        case ENDS_WITH, STARTS_WITH, LIKE -> {
            if (!String.class.isAssignableFrom(clazz)) {
                throw new QFFieldOperationException(operation, definition.getFilterName());
            }
        }
        default -> LOGGER.trace("Operation {} allowed on field {} by default", operation, definition.getFilterName());
        }
    }

    private void checkOperationIsArrayTyped() throws QFFieldOperationException {

        if (!operation.isArrayTyped()) {
            throw new QFFieldOperationException(operation, definition.getFilterName());
        }

    }

    private List<String> parseResults(@Nullable Object spelResolved) {

        if (spelResolved == null) {
            return Collections.emptyList();
        }

        Class<?> originalClass = spelResolved.getClass();

        if (spelResolved instanceof String[] casted) {
            return Arrays.asList(casted);
        } else if (spelResolved instanceof Collection<?>) {
            return ((Collection<?>) spelResolved).stream().map(Object::toString).toList();
        } else if (String.class.equals(originalClass)) {
            return Collections.singletonList((String) spelResolved);
        }

        // Primitive array copy
        List<String> retList = getStringFromArrays(spelResolved, originalClass);

        if (retList != null) {
            return retList;
        }

        return Collections.singletonList(spelResolved.toString());
    }

    @Nullable
    private List<String> getStringFromArrays(Object spelResolved, Class<?> originalClass) {
        List<String> retList = null;
        if (boolean[].class.equals(originalClass)) {
            boolean[] fromArray = (boolean[]) spelResolved;
            retList = new ArrayList<>(fromArray.length);
            for (boolean b : fromArray) {
                retList.add(String.valueOf(b));
            }

        } else if (byte[].class.equals(originalClass)) {
            byte[] fromArray = (byte[]) spelResolved;
            retList = new ArrayList<>(fromArray.length);
            for (byte b : fromArray) {
                retList.add(String.valueOf(b));
            }

        } else if (short[].class.equals(originalClass)) {
            short[] fromArray = (short[]) spelResolved;
            retList = new ArrayList<>(fromArray.length);
            for (short value : fromArray) {
                retList.add(String.valueOf(value));
            }

        } else if (int[].class.equals(originalClass)) {
            int[] fromArray = (int[]) spelResolved;
            retList = new ArrayList<>(fromArray.length);
            for (int j : fromArray) {
                retList.add(String.valueOf(j));
            }

        } else if (long[].class.equals(originalClass)) {
            long[] fromArray = (long[]) spelResolved;
            retList = new ArrayList<>(fromArray.length);
            for (long l : fromArray) {
                retList.add(String.valueOf(l));
            }

        } else if (float[].class.equals(originalClass)) {
            float[] fromArray = (float[]) spelResolved;
            retList = new ArrayList<>(fromArray.length);
            for (float v : fromArray) {
                retList.add(String.valueOf(v));
            }

        } else if (double[].class.equals(originalClass)) {
            double[] fromArray = (double[]) spelResolved;
            retList = new ArrayList<>(fromArray.length);
            for (double v : fromArray) {
                retList.add(String.valueOf(v));
            }

        } else if (char[].class.equals(originalClass)) {
            char[] fromArray = (char[]) spelResolved;
            retList = new ArrayList<>(fromArray.length);
            for (char c : fromArray) {
                retList.add(String.valueOf(c));
            }
        }
        return retList;
    }

    /** {@inheritDoc} */
    @Override
    public <E> void processPart(QueryInfo<E> queryInfo, Map<String, List<Predicate>> predicatesMap,
            Map<String, Path<?>> pathsMap, MultiValueMap<String, Object> mlmap, SpelResolverContext spelResolver,
            Class<E> entityClass) {

        if (definition.isSubQuery()) {
            LOGGER.trace("Element match is subquery");
            processPartAsSubQuery(queryInfo, predicatesMap, mlmap, spelResolver, entityClass);
        } else {
            LOGGER.trace("Element match is basic");
            processAsElement(queryInfo, predicatesMap, pathsMap, mlmap, spelResolver);
        }

    }

    private <E> void processAsElement(QueryInfo<E> queryInfo, Map<String, List<Predicate>> predicatesMap,
            Map<String, Path<?>> pathsMap, MultiValueMap<String, Object> mlmap, SpelResolverContext spelResolver) {
        initialize(spelResolver, mlmap);

        if (!needToEvaluate()) {
            return;
        }

        int index = 0;

        List<Predicate> predicates = new ArrayList<>();

        for (var path : paths) {
            predicates.add(operation.generatePredicate(
                    QueryUtils.getObject(queryInfo, path, definition.getJoinTypes(index), pathsMap, false, false),
                    queryInfo.cb(), this, index, mlmap));
            index++;
        }

        Predicate surrondingPredicate = definition.getPredicateOperation().getPredicate(queryInfo.cb(), predicates);

        predicatesMap.computeIfAbsent(definition.getFilterName(), t -> new ArrayList<>()).add(surrondingPredicate);
    }

    private <E> void processPartAsSubQuery(QueryInfo<E> queryInfo, Map<String, List<Predicate>> predicatesMap,
            MultiValueMap<String, Object> mlmap, SpelResolverContext spelResolver, Class<E> entityClass) {
        Map<String, Path<?>> subSelecthMap = new HashMap<>();

        initialize(spelResolver, mlmap);

        if (!needToEvaluate()) {
            return;
        }

        int index = 0;
        for (var path : paths) {

            var query = queryInfo.query();
            if (query == null) {
                throw new IllegalStateException("CriteriaQuery is null");
            }

            Subquery<E> subquery = query.subquery(entityClass);
            Root<E> newRoot = subquery.from(entityClass);

            subquery.select(newRoot);

            QueryInfo<E> subQueryInfo = new QueryInfo<>(newRoot, null, queryInfo.cb(), false);

            Path<?> pathFinal = QueryUtils.getObject(subQueryInfo, path, definition.getJoinTypes(index), subSelecthMap,
                    false, false);

            QFOperationEnum op = operation;
            if (op == QFOperationEnum.NOT_EQUAL) {
                op = QFOperationEnum.EQUAL;
            }

            subquery.where(op.generatePredicate(pathFinal, queryInfo.cb(), this, index, mlmap));

            Predicate finalPredicate = queryInfo.cb().in(queryInfo.root()).value(subquery);

            if (operation == QFOperationEnum.NOT_EQUAL) {
                finalPredicate = queryInfo.cb().not(finalPredicate);
            }

            predicatesMap.computeIfAbsent(definition.getFilterName(), k -> new ArrayList<>()).add(finalPredicate);

            index++;
        }
    }

}
