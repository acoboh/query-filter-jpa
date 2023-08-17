package io.github.acoboh.query.filter.jpa.processor;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.acoboh.query.filter.jpa.exceptions.definition.QFElementException;
import io.github.acoboh.query.filter.jpa.exceptions.definition.QFFieldLevelException;
import io.github.acoboh.query.filter.jpa.exceptions.definition.QFMissingFieldException;
import io.github.acoboh.query.filter.jpa.exceptions.definition.QFTypeException;

/**
 * Class utils to parse filter classes
 *
 * @author Adrián Cobo
 */
class ClassUtils {

	private static final Logger LOGGER = LoggerFactory.getLogger(ClassUtils.class);

	private ClassUtils() {

	}

	/**
	 * Check base abstract object for query filter utilities
	 *
	 * @param fullPath      Full path of the property
	 * @param actualField   Actual field level
	 * @param nextFieldPath Next field level
	 * @param objectField   Field object
	 * @param fieldClass    Field class
	 * @param list          List of paths
	 * @param isEndObject   if the field must be final type
	 * @return Final class
	 * @throws io.github.acoboh.query.filter.jpa.exceptions.definition.QFTypeException         it the field can not be parsed
	 * @throws io.github.acoboh.query.filter.jpa.exceptions.definition.QFMissingFieldException if the field is missing
	 * @throws io.github.acoboh.query.filter.jpa.exceptions.definition.QFFieldLevelException   if the field can access more levels or
	 *                                                                                         has no nested fields
	 * @throws io.github.acoboh.query.filter.jpa.exceptions.definition.QFElementException      if the field is not present on any
	 *                                                                                         class
	 */
	protected static Class<?> checkAbstractObject(String fullPath, String actualField, String nextFieldPath,
			Field objectField, Class<?> fieldClass, List<QFPath> list, boolean isEndObject)
			throws QFTypeException, QFMissingFieldException, QFFieldLevelException, QFElementException {

		boolean ignore = false;

		if (!list.isEmpty()) {

			QFPath path = list.get(list.size() - 1);
			if (path.getField().equals(objectField)) {
				ignore = true;
			}

		}

		if (!ignore) {
			list.add(new QFPath(QFPath.QueryFilterElementDefType.PROPERTY, objectField, actualField));
		}

		LOGGER.trace("Added path as propery for field {} of class {}", actualField, fieldClass);

		if (isPrimitiveOrBasic(fieldClass, list)) {
			LOGGER.trace("Field is primitive of basic. Stopping recursive path search");
			if (!nextFieldPath.isEmpty()) {
				LOGGER.error("Field {} is not finished at level {}. Remaining {}", actualField, fullPath,
						nextFieldPath);
				throw new QFFieldLevelException(fullPath, nextFieldPath);
			}

			return fieldClass;
		}

		if (fieldClass.isArray()) {
			LOGGER.trace("Field is array. Parsing generic arguments");
			return checkArrayField(fullPath, actualField, nextFieldPath, objectField, fieldClass, list, isEndObject);
		} else if (fieldClass.isAssignableFrom(List.class) || fieldClass.isAssignableFrom(Set.class)) {
			LOGGER.trace("Field is list of set. Parsing generic arguments");
			return checkListField(fullPath, actualField, nextFieldPath, objectField, list, isEndObject);
		} else if (fieldClass.isAssignableFrom(Map.class)) {
			LOGGER.trace("Field is map. Unable to parse");
			list.get(list.size() - 1).setFinal(true); // Force final
			return fieldClass;
		} else {
			LOGGER.trace("Field is object. Getting next level");
			return checkGenericObject(fullPath, nextFieldPath, fieldClass, list, isEndObject);
		}
	}

	/**
	 * Check the array field
	 * 
	 * @param fullPath       Full path of field
	 * @param actualField    Actual field name
	 * @param nextLevelField Next levels fields
	 * @param objectField    Field of an actual object
	 * @param fieldClass     Class of actual field class
	 * @param list           List of paths until current level
	 * @param isEndObject    Object must be final class or not
	 * @return Class of a final object
	 * @throws QFTypeException         Process and check the array object type
	 * @throws QFFieldLevelException   thrown when in the matching class, there is no more level to find nested
	 * @throws QFTypeException         thrown when type parsing error
	 * @throws QFMissingFieldException thrown when trying to parse missing fields from query filter class
	 * @throws QFElementException      Missing field exception
	 */
	private static Class<?> checkArrayField(String fullPath, String actualField, String nextLevelField,
			Field objectField, Class<?> fieldClass, List<QFPath> list, boolean isEndObject)
			throws QFTypeException, QFMissingFieldException, QFFieldLevelException, QFElementException {

		list.get(list.size() - 1).setType(QFPath.QueryFilterElementDefType.LIST);
		LOGGER.trace("Setting {} of path {} as list", actualField, fullPath);

		// Recursive for object
		Class<?> elementClass = fieldClass.getComponentType();
		list.get(list.size() - 1).setFieldClass(elementClass);
		LOGGER.trace("Getting attribute of array level {} type {}", actualField, elementClass);
		return checkAbstractObject(fullPath, actualField, nextLevelField, objectField, elementClass, list, isEndObject);

	}

	/**
	 * @param fullpath       Full path of field
	 * @param actualField    Actual field name
	 * @param nextLevelField Next levels fields
	 * @param objectField    Field of the actual object
	 * @param list           List of paths until current level
	 * @param isEndObject    Object must be final class or not
	 * @return Class of the final object
	 * @throws QFElementException      Missing field exception
	 * @throws QFMissingFieldException Check List class type
	 * @throws QFFieldLevelException   thrown when in the matching class, there is no more level to find nested
	 * @throws QFTypeException         thrown when type parsing error
	 */
	private static Class<?> checkListField(String fullpath, String actualField, String nextLevelField,
			Field objectField, List<QFPath> list, boolean isEndObject)
			throws QFTypeException, QFMissingFieldException, QFFieldLevelException, QFElementException {

		if (objectField.getType().equals(List.class)) {
			list.get(list.size() - 1).setType(QFPath.QueryFilterElementDefType.LIST);
			LOGGER.trace("Field attribute is list type {}", actualField);
		} else if (objectField.getType().equals(Set.class)) {
			list.get(list.size() - 1).setType(QFPath.QueryFilterElementDefType.SET);
			LOGGER.trace("Field attribute is set type {}", actualField);
		}

		Type type = objectField.getGenericType();
		LOGGER.trace("Getting parameterized type of {}", type);
		if (type instanceof ParameterizedType
				&& (objectField.getType().equals(List.class) || objectField.getType().equals(Set.class))) {
			Class<?> listClass = (Class<?>) ((ParameterizedType) type).getActualTypeArguments()[0];
			LOGGER.trace("Parameterized type read {}", listClass);
			list.get(list.size() - 1).setFieldClass(listClass);
			return checkAbstractObject(fullpath, actualField, nextLevelField, objectField, listClass, list,
					isEndObject);
		} else {
			throw new QFTypeException(fullpath, "It is not a valid list type");
		}

	}

	/**
	 * @param fullpath       Full path of field
	 * @param nextLevelField Next levels fields
	 * @param declaringClass Class of the object
	 * @param list           List of paths until current level
	 * @param isEndObject    Object must be final class or not
	 * @return Final class of the object
	 * @throws QFElementException      Missing field exception
	 * @throws QFTypeException         Check the generic object class type
	 * @throws QFFieldLevelException   thrown when in the matching class, there is no more level to find nested
	 * @throws QFTypeException         thrown when type parsing error
	 * @throws QFMissingFieldException thrown when trying to parse missing fields from query filter class
	 */
	private static Class<?> checkGenericObject(String fullpath, String nextLevelField, Class<?> declaringClass,
			List<QFPath> list, boolean isEndObject)
			throws QFMissingFieldException, QFTypeException, QFFieldLevelException, QFElementException {

		LOGGER.trace("Checking next level {} continue", nextLevelField);

		if (isEndObject && (nextLevelField == null || nextLevelField.isEmpty())) {
			LOGGER.error("Unexpected end of path level. Can not match against full object of type {}. Review path {}",
					declaringClass, fullpath);
			throw new QFMissingFieldException(fullpath, declaringClass);
		}

		LOGGER.trace("End level reached of full path {}", fullpath);

		// Check classType
		String[] splitLevel = nextLevelField.split("\\.");
		LOGGER.trace("Getting next fields size {}", splitLevel.length);

		if (splitLevel.length <= 0) {
			LOGGER.error("Unexpected end of path field: {}", nextLevelField);
			throw new QFMissingFieldException(fullpath, declaringClass);
		}

		if (splitLevel[0].isEmpty()) {
			LOGGER.trace("Getting final split level on Object. Not primitive or final class {}", declaringClass);
			list.get(list.size() - 1).setFinal(false);
			return declaringClass;
		}

		LOGGER.trace("Getting field with name '{}' of class '{}'", splitLevel[0], declaringClass);
		Field fieldObject = getDeclaredFieldSuperclass(declaringClass, splitLevel[0]);
		if (fieldObject == null) {
			LOGGER.trace("Field not found with {} on class {}", splitLevel[0], declaringClass);
			throw new QFElementException(nextLevelField, declaringClass);
		}

		Class<?> nextClassLevel = fieldObject.getType();
		LOGGER.trace("Field {} class {}", splitLevel[0], nextClassLevel);

		int firstDot = nextLevelField.indexOf('.');
		String nextLevel = firstDot == -1 ? "" : nextLevelField.substring(firstDot + 1);

		LOGGER.trace("Processing {} with next level {}", splitLevel[0], nextLevel);
		return checkAbstractObject(fullpath, splitLevel[0], nextLevel, fieldObject, nextClassLevel, list, isEndObject);

	}

	/**
	 * <p>
	 * getDeclaredFieldSuperclass.
	 * </p>
	 *
	 * @param fromClass a {@link java.lang.Class} object
	 * @param fieldName a {@link java.lang.String} object
	 * @return a {@link java.lang.reflect.Field} object
	 */
	public static Field getDeclaredFieldSuperclass(Class<?> fromClass, String fieldName) {
		Field fieldClazz = null;
		try {
			fieldClazz = fromClass.getDeclaredField(fieldName);
			LOGGER.trace("Field {} found on class {}", fieldName, fieldClazz);
		} catch (NoSuchFieldException e) {
			LOGGER.trace("No field found named '{}' in class '{}'", fieldName, fromClass.getName());
			if (fromClass.getSuperclass() != null) {
				return getDeclaredFieldSuperclass(fromClass.getSuperclass(), fieldName);
			}
			LOGGER.trace("End of searching field name {} on class {}", fieldName, fromClass);

		}

		return fieldClazz;
	}

	/**
	 * Check primitive or basic field
	 * <p>
	 * If the field is enum, the path is modified
	 *
	 * @param fieldClass Class to check
	 * @param list       List of paths
	 * @return true if primitive, false otherwise
	 */
	private static boolean isPrimitiveOrBasic(Class<?> fieldClass, List<QFPath> list) {

		boolean isPrimitive = fieldClass.isPrimitive() || fieldClass.isAssignableFrom(Double.class)
				|| fieldClass.isAssignableFrom(Double.TYPE) || fieldClass.isAssignableFrom(Integer.class)
				|| fieldClass.isAssignableFrom(Integer.TYPE) || fieldClass.isAssignableFrom(Long.class)
				|| fieldClass.isAssignableFrom(Long.TYPE) || fieldClass.isAssignableFrom(Short.class)
				|| fieldClass.isAssignableFrom(Short.TYPE) || fieldClass.isAssignableFrom(Float.class)
				|| fieldClass.isAssignableFrom(Float.TYPE) || fieldClass.isAssignableFrom(Boolean.class)
				|| fieldClass.isAssignableFrom(Boolean.TYPE) || fieldClass.isAssignableFrom(Number.class)
				|| fieldClass.isAssignableFrom(String.class) || fieldClass.isAssignableFrom(Enum.class)
				|| fieldClass.isAssignableFrom(UUID.class) || fieldClass.isEnum()
				|| fieldClass.isAssignableFrom(LocalDateTime.class) || fieldClass.isAssignableFrom(Timestamp.class);

		if (fieldClass.isAssignableFrom(Enum.class) || fieldClass.isEnum()) {
			list.get(list.size() - 1).setType(QFPath.QueryFilterElementDefType.ENUM);
		}

		return isPrimitive;

	}

}
