package io.github.acoboh.query.filter.jpa.utils;

import io.github.acoboh.query.filter.jpa.exceptions.definition.QFTypeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Class introspection utilities
 *
 * @author Adrián Cobo
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
     * @throws io.github.acoboh.query.filter.jpa.exceptions.definition.QFTypeException if
     *                                                                                 any.
     */
    public static Class<?> getClassOfList(Field field) throws QFTypeException {

        Type type = field.getGenericType();
        LOGGER.trace("Getting parameterized type of {}", type);
        if (type instanceof ParameterizedType pType
                && (field.getType().equals(List.class) || field.getType().equals(Set.class))) {
            Class<?> listClass = (Class<?>) pType.getActualTypeArguments()[0];
            LOGGER.trace("List class is {}", listClass);
            return listClass;
        }

        throw new QFTypeException(null, "It is not a valid list type");
    }

    private static final List<Class<?>> ASSIGNABLE_BASIC_PRIMITIVE_CLASSES = List.of( // All assignable classes
            Double.class, Double.TYPE, // Doubles
            Integer.class, Integer.TYPE, // Integers
            Long.class, Long.TYPE, // Longs
            Short.class, Short.TYPE, // Shorts
            Float.class, Float.TYPE, // Floats
            Boolean.class, Boolean.TYPE, // Booleans
            Number.class, // Numbers
            String.class, // Strings
            Enum.class, // Enums
            UUID.class, // UUID
            LocalDateTime.class, // LocalDateTime
            Timestamp.class, // Timestamp
            Instant.class // Instant
    );

    /**
     * Check primitive or basic field
     * <p>
     * If the field is enum, the path is modified
     *
     * @param fieldClass Class to check
     * @return true if primitive, false otherwise
     */
    public static boolean isPrimitiveOrBasic(Class<?> fieldClass) {
        return fieldClass.isPrimitive()
                || ASSIGNABLE_BASIC_PRIMITIVE_CLASSES.stream().anyMatch(e -> e.isAssignableFrom(fieldClass));

    }

    /**
     * Check if the field is an enum
     *
     * @param fieldClass Class to check
     * @return true if enum, false otherwise
     */
    public static boolean isEnum(Class<?> fieldClass) {
        return fieldClass.isEnum() || Enum.class.isAssignableFrom(fieldClass);
    }

    /**
     * Check if the field is a list, array or set
     *
     * @param fieldClass Class to check
     * @return true if list, array or set, false otherwise
     */
    public static boolean isListArrayOrSet(Class<?> fieldClass) {
        return fieldClass.isArray() || List.class.isAssignableFrom(fieldClass)
                || Set.class.isAssignableFrom(fieldClass);
    }

}
