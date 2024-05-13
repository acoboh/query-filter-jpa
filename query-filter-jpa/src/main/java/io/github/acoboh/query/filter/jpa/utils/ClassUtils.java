package io.github.acoboh.query.filter.jpa.utils;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.acoboh.query.filter.jpa.exceptions.definition.QFTypeException;

/**
 * Class introspection utilities
 */
public class ClassUtils {

	private static final Logger LOGGER = LoggerFactory.getLogger(ClassUtils.class);

	private ClassUtils() {
		// Private constructor
	}

	/**
	 * Get the field of a class
	 *
	 * @param fromClass Class to get the field
	 * @param fieldName Field name to get
	 * @return Field found
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
	 * Get class of a list or set type class
	 * 
	 * @param field Field to check
	 * @return Class of the list
	 * @throws QFTypeException
	 */
	public static Class<?> getClassOfList(Field field) throws QFTypeException {

		Type type = field.getGenericType();
		LOGGER.trace("Getting parameterized type of {}", type);
		if (type instanceof ParameterizedType
				&& (field.getType().equals(List.class) || field.getType().equals(Set.class))) {
			Class<?> listClass = (Class<?>) ((ParameterizedType) type).getActualTypeArguments()[0];
			LOGGER.trace("List class is {}", listClass);
			return listClass;
		}

		throw new QFTypeException(null, "It is not a valid list type");
	}

	/**
	 * Check primitive or basic field
	 * <p>
	 * If the field is enum, the path is modified
	 *
	 * @param fieldClass Class to check
	 * @return true if primitive, false otherwise
	 */
	public static boolean isPrimitiveOrBasic(Class<?> fieldClass) {
		return fieldClass.isPrimitive() || fieldClass.isAssignableFrom(Double.class)
				|| fieldClass.isAssignableFrom(Double.TYPE) || fieldClass.isAssignableFrom(Integer.class)
				|| fieldClass.isAssignableFrom(Integer.TYPE) || fieldClass.isAssignableFrom(Long.class)
				|| fieldClass.isAssignableFrom(Long.TYPE) || fieldClass.isAssignableFrom(Short.class)
				|| fieldClass.isAssignableFrom(Short.TYPE) || fieldClass.isAssignableFrom(Float.class)
				|| fieldClass.isAssignableFrom(Float.TYPE) || fieldClass.isAssignableFrom(Boolean.class)
				|| fieldClass.isAssignableFrom(Boolean.TYPE) || fieldClass.isAssignableFrom(Number.class)
				|| fieldClass.isAssignableFrom(String.class) || fieldClass.isAssignableFrom(Enum.class)
				|| fieldClass.isAssignableFrom(UUID.class) || fieldClass.isAssignableFrom(LocalDateTime.class)
				|| fieldClass.isAssignableFrom(Timestamp.class);

	}

	/**
	 * Check if the field is an enum
	 *
	 * @param fieldClass Class to check
	 * @return true if enum, false otherwise
	 */
	public static boolean isEnum(Class<?> fieldClass) {
		return fieldClass.isEnum() || fieldClass.isAssignableFrom(Enum.class);
	}

	/**
	 * Check if the field is a list, array or set
	 *
	 * @param fieldClass Class to check
	 * @return true if list, array or set, false otherwise
	 */
	public static boolean isListArrayOrSet(Class<?> fieldClass) {
		return fieldClass.isArray() || fieldClass.isAssignableFrom(List.class)
				|| fieldClass.isAssignableFrom(Set.class);
	}

}
