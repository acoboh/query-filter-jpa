package io.github.acoboh.query.filter.jpa.processor;

import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.MultiValueMap;

import io.github.acoboh.query.filter.jpa.exceptions.QFDateParsingException;
import io.github.acoboh.query.filter.jpa.exceptions.QFEnumException;
import io.github.acoboh.query.filter.jpa.exceptions.QFFieldOperationException;
import io.github.acoboh.query.filter.jpa.operations.QFOperationEnum;
import io.github.acoboh.query.filter.jpa.processor.definitions.QFDefinitionElement;
import io.github.acoboh.query.filter.jpa.spel.SpelResolverContext;
import io.github.acoboh.query.filter.jpa.utils.DateUtils;

/**
 * Class with info about the filtered field. Contains all the entity fields of the same filter field
 *
 * @author Adrián Cobo
 * 
 */
public class QFElementMatch {

	private static final Logger LOGGER = LoggerFactory.getLogger(QFElementMatch.class);

	private final QFDefinitionElement definition;

	private final boolean subquery;

	private final List<String> originalValues;

	private final List<List<QFPath>> paths;

	private final QFOperationEnum operation;
	private final List<Class<?>> matchClasses;
	private final List<Boolean> isEnumList;

	private boolean caseSensitive;
	private List<String> processedValues;
	private List<List<Object>> parsedValues;

	private DateTimeFormatter formatter;

	private boolean initialized = false;

	/**
	 * Default constructor
	 *
	 * @param values     list of matching values
	 * @param operation  operation to be applied
	 * @param definition field definition
	 */
	public QFElementMatch(List<String> values, QFOperationEnum operation, QFDefinitionElement definition) {

		this.definition = definition;
		this.originalValues = values;
		this.operation = operation;
		this.subquery = definition.isSubQuery();
		this.caseSensitive = definition.isCaseSensitive();

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
	 * @throws io.github.acoboh.query.filter.jpa.exceptions.QFFieldOperationException if any operation exception
	 * @throws io.github.acoboh.query.filter.jpa.exceptions.QFEnumException           if any enumeration exception
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected boolean initialize(SpelResolverContext spelResolver, MultiValueMap<String, Object> context)
			throws QFFieldOperationException, QFEnumException {

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
		for (List<QFPath> path : paths) {

			QFPath lastPath = path.get(path.size() - 1);
			Class<?> finalClass = lastPath.getFieldClass();

			// Check operation
			checkOperation(finalClass);

			matchClasses.add(finalClass);

			// Check is an enumeration
			boolean isEnum = lastPath.getType() == QFPath.QueryFilterElementDefType.ENUM;
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

	/**
	 * Get field definition
	 *
	 * @return field definition
	 */
	public QFDefinitionElement getDefinition() {
		return definition;
	}

	/**
	 * Get if the field must be in a subquery
	 *
	 * @return true if the filter is subqueried
	 */
	public boolean isSubquery() {
		return subquery;
	}

	/**
	 * List of original matching values
	 *
	 * @return original matching values
	 */
	public List<String> getOriginalValues() {
		return originalValues;
	}

	/**
	 * Get list of nested paths for each model field
	 *
	 * @return list of nested paths
	 */
	public List<List<QFPath>> getPaths() {
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
	 * If the string is case sensitive
	 *
	 * @return true if the string is case sensitive
	 */
	public boolean isCaseSensitive() {
		return caseSensitive;
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
		if (!initialized) {
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
		if (!initialized) {
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
		if (!initialized) {
			throw new IllegalStateException();
		}
		return parsedValues.get(index).get(0);
	}

	/**
	 * Get if the matching element must be evaluated
	 *
	 * @return true if must to be evaluated
	 */
	public boolean needToEvaluate() {
		if (!initialized) {
			throw new IllegalStateException();
		}

		if (definition.isBlankIgnore()) {
			return !processedValues.isEmpty();
		}

		return true;

	}

	private Object parseTimestamp(String value, Class<?> finalClass) {

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
		case GREATER_THAN:
		case GREATER_EQUAL_THAN:
		case LESS_THAN:
		case LESS_EQUAL_THAN:
			if (!Comparable.class.isAssignableFrom(clazz) && !clazz.isPrimitive()) {
				throw new QFFieldOperationException(operation, definition.getFilterName());
			}
			break;

		case ENDS_WITH:
		case STARTS_WITH:
		case LIKE:
			if (!String.class.isAssignableFrom(clazz)) {
				throw new QFFieldOperationException(operation, definition.getFilterName());
			}
			break;
		default:
			LOGGER.trace("Operation {} allowed on field {} by default", operation, definition.getFilterName());
		}
	}

	private void checkOperationIsArrayTyped() throws QFFieldOperationException {

		if (!operation.isArrayTyped()) {
			throw new QFFieldOperationException(operation, definition.getFilterName());
		}

	}

	private List<String> parseResults(Object spelResolved) {

		if (spelResolved == null) {
			return Collections.emptyList();
		}

		Class<?> originalClass = spelResolved.getClass();

		if (spelResolved instanceof String[]) {
			return Arrays.asList((String[]) spelResolved);
		} else if (spelResolved instanceof Collection<?>) {
			return ((Collection<?>) spelResolved).stream().map(Object::toString).collect(Collectors.toList());
		} else if (String.class.equals(originalClass)) {
			return Collections.singletonList((String) spelResolved);
		} else if (boolean.class.equals(originalClass)) {
			return Collections.singletonList(String.valueOf((boolean) spelResolved));
		} else if (byte.class.equals(originalClass)) {
			return Collections.singletonList(String.valueOf((byte) spelResolved));
		} else if (short.class.equals(originalClass)) {
			return Collections.singletonList(String.valueOf((short) spelResolved));
		} else if (int.class.equals(originalClass)) {
			return Collections.singletonList(String.valueOf((int) spelResolved));
		} else if (long.class.equals(originalClass)) {
			return Collections.singletonList(String.valueOf((long) spelResolved));
		} else if (float.class.equals(originalClass)) {
			return Collections.singletonList(String.valueOf((float) spelResolved));
		} else if (double.class.equals(originalClass)) {
			return Collections.singletonList(String.valueOf((double) spelResolved));
		} else if (char.class.equals(originalClass)) {
			return Collections.singletonList(String.valueOf((char) spelResolved));
		}

		// Primitive array copy
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

		if (retList != null) {
			return retList;
		}

		return Collections.singletonList(spelResolved.toString());
	}

}
