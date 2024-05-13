package io.github.acoboh.query.filter.jpa.processor.definitions;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import io.github.acoboh.query.filter.jpa.exceptions.definition.QFElementException;
import io.github.acoboh.query.filter.jpa.exceptions.definition.QFFieldLevelException;
import io.github.acoboh.query.filter.jpa.exceptions.definition.QueryFilterDefinitionException;
import io.github.acoboh.query.filter.jpa.processor.QFPath;
import io.github.acoboh.query.filter.jpa.processor.QFPath.QFElementDefType;
import io.github.acoboh.query.filter.jpa.utils.ClassUtils;

class FieldClassProcessor {

	private static final Logger LOGGER = LoggerFactory.getLogger(FieldClassProcessor.class);

	private final Class<?> rootClass;
	private final String pathField;
	private final boolean checkFinal;
	private List<QFPath> paths;
	private Class<?> finalClass;

	FieldClassProcessor(Class<?> rootClass, String pathField, boolean checkFinal) {
		Assert.notNull(pathField, "Path field cannot be null");
		this.rootClass = rootClass;
		this.pathField = pathField;
		this.checkFinal = checkFinal;
	}

	public List<QFPath> getPaths() throws QueryFilterDefinitionException {
		if (paths != null) {
			return paths;
		}

		paths = new ArrayList<>();

		String[] splitLevel = pathField.split("\\.");
		if (splitLevel.length == 0) {
			throw new QFElementException(pathField, rootClass);
		}

		Class<?> levelClass = rootClass;

		for (String level : splitLevel) {
			LOGGER.trace("Processing level {}", level);

			Field fieldObject = ClassUtils.getDeclaredFieldSuperclass(levelClass, level);
			if (fieldObject == null) {
				throw new QFElementException(pathField, levelClass);
			}

			QFPath path = createQPathOfField(fieldObject, level);
			paths.add(path);

			// Check path is final and nested levels are present
			if (path.isFinal() && splitLevel.length != paths.size()) {
				throw new QFFieldLevelException(pathField, level);
			}

			// Final iteration. Double check final class
			if (splitLevel.length == paths.size() && !path.isFinal()) {
				path.setFinal(couldBeFinal(path.getFieldClass()));
			}

			levelClass = path.getFieldClass();

		}

		// Final check
		if (checkFinal && !paths.get(paths.size() - 1).isFinal()) {
			throw new QFFieldLevelException(pathField, splitLevel[splitLevel.length - 1]);
		}

		finalClass = levelClass;

		return paths;

	}

	/**
	 * Get final class
	 *
	 * @return final class
	 */
	public Class<?> getFinalClass() {
		return finalClass;
	}

	private static QFPath createQPathOfField(Field field, String path) throws QueryFilterDefinitionException {

		LOGGER.trace("Processing field {}", field);

		Class<?> fieldClass = field.getType();

		Class<?> finalClass = fieldClass;
		boolean isFinal = false;
		QFElementDefType type;

		if (fieldClass.isAssignableFrom(Enum.class) || fieldClass.isEnum()) {
			type = QFElementDefType.ENUM;
			isFinal = true;
		} else if (ClassUtils.isPrimitiveOrBasic(fieldClass)) {
			isFinal = true;
			type = QFElementDefType.PROPERTY;
		} else if (fieldClass.isArray()) {
			finalClass = fieldClass.getComponentType();
			type = QFElementDefType.LIST;
		} else if (fieldClass.isAssignableFrom(List.class) || fieldClass.isAssignableFrom(Set.class)) {
			finalClass = ClassUtils.getClassOfList(field);
			type = fieldClass.isAssignableFrom(List.class) ? QFElementDefType.LIST : QFElementDefType.SET;
		} else {
			type = QFElementDefType.PROPERTY;
		}

		return new QFPath(field, path, type, finalClass, isFinal);
	}

	private static boolean couldBeFinal(Class<?> clazz) {
		return clazz.isAssignableFrom(Enum.class) || clazz.isEnum() || ClassUtils.isPrimitiveOrBasic(clazz);
	}

}
