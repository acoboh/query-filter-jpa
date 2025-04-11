package io.github.acoboh.query.filter.jpa.processor.definitions;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import io.github.acoboh.query.filter.jpa.exceptions.definition.QFElementException;
import io.github.acoboh.query.filter.jpa.exceptions.definition.QueryFilterDefinitionException;
import io.github.acoboh.query.filter.jpa.processor.QFAttribute;
import io.github.acoboh.query.filter.jpa.utils.ClassUtils;
import jakarta.persistence.metamodel.Attribute;
import jakarta.persistence.metamodel.ManagedType;
import jakarta.persistence.metamodel.Metamodel;
import jakarta.persistence.metamodel.PluralAttribute;
import jakarta.persistence.metamodel.SingularAttribute;
import jakarta.persistence.metamodel.Type;

class FieldClassProcessor {

	private static final Logger LOGGER = LoggerFactory.getLogger(FieldClassProcessor.class);

	private final Class<?> rootClass;
	private final String pathField;
	private final boolean checkFinal;
	private final Metamodel metamodel;
	// private List<QFPath> paths;
	private List<QFAttribute> attributes;
	private Class<?> finalClass;

	private final Class<?> subclassMapping;
	private final String subClassMappingPath;

	FieldClassProcessor(Class<?> rootClass, String pathField, boolean checkFinal, Class<?> subclassMapping,
			String subClassMappingPath, Metamodel metamodel) {
		Assert.notNull(pathField, "Path field cannot be null");
		this.rootClass = rootClass;
		this.pathField = pathField;
		this.checkFinal = checkFinal;
		this.subclassMapping = subclassMapping;
		this.subClassMappingPath = subClassMappingPath;
		this.metamodel = metamodel;
	}

// @formatter:off
//	    public List<QFPath> getPaths() throws QueryFilterDefinitionException {
//		if (paths != null) {
//			return paths;
//		}
//
//		paths = new ArrayList<>();
//
//		String[] splitLevel = pathField.split("\\.");
//		if (splitLevel.length == 0) {
//			throw new QFElementException(pathField, rootClass);
//		}
//
//		String[] levelsSubClass = null;
//
//		if (subclassMapping != null && !Void.class.equals(subclassMapping)) {
//			LOGGER.trace("Processing subclass mapping {}", subclassMapping);
//			if (subClassMappingPath != null && !subClassMappingPath.isEmpty()
//					&& !pathField.startsWith(subClassMappingPath)) {
//				LOGGER.trace("Subclass mapping path '{}' not present in path field '{}'", subClassMappingPath,
//						pathField);
//				throw new QFElementException(pathField, subclassMapping);
//			}
//			if (subClassMappingPath.isEmpty()) {
//				levelsSubClass = new String[0];
//			} else {
//				levelsSubClass = subClassMappingPath.split("\\.");
//			}
//
//		}
//
//		Class<?> levelClass = rootClass;
//
//		int actualLevel = -1;
//		for (String level : splitLevel) {
//			Class<?> treatClass = null;
//			actualLevel++;
//
//			LOGGER.trace("Processing level {}", level);
//
//			if (levelsSubClass != null && levelsSubClass.length == actualLevel) {
//				// Check levelClass is parent of subclassMapping
//				if (!levelClass.isAssignableFrom(subclassMapping)) {
//					throw new QFElementException(pathField, subclassMapping);
//				}
//				treatClass = subclassMapping;
//				levelClass = subclassMapping;
//			}
//
//			Field fieldObject = ClassUtils.getDeclaredFieldSuperclass(levelClass, level);
//			if (fieldObject == null) {
//				throw new QFElementException(pathField, levelClass);
//			}
//
//			QFPath path = createQPathOfField(fieldObject, level, treatClass);
//			paths.add(path);
//
//			// Check path is final and nested levels are present
//			if (path.isFinal() && splitLevel.length != paths.size()) {
//				throw new QFFieldLevelException(pathField, level);
//			}
//
//			// Final iteration. Double check final class
//			if (splitLevel.length == paths.size() && !path.isFinal()) {
//				path.setFinal(couldBeFinal(path.getFieldClass()));
//			}
//
//			levelClass = path.getFieldClass();
//
//		}
//
//		// Final check
//		if (checkFinal && !paths.get(paths.size() - 1).isFinal()) {
//			throw new QFFieldLevelException(pathField, splitLevel[splitLevel.length - 1]);
//		}
//
//		finalClass = levelClass;
//
//		return paths;
//
//	}
// @formatter:on

	public List<QFAttribute> getAttributes() throws QueryFilterDefinitionException {

		if (attributes != null) {
			return attributes;
		}

		LOGGER.debug("Parsing with metamodel");

		String[] splitLevel = pathField.split("\\.");
		attributes = new ArrayList<>(splitLevel.length);

		var entity = metamodel.entity(rootClass);

		// Level subclass
		String[] levelsSubClass = null;

		if (subclassMapping != null && !Void.class.equals(subclassMapping)) {
			LOGGER.trace("Processing subclass mapping {}", subclassMapping);
			if (subClassMappingPath != null && !subClassMappingPath.isEmpty()
					&& !pathField.startsWith(subClassMappingPath)) {
				LOGGER.trace("Subclass mapping path '{}' not present in path field '{}'", subClassMappingPath,
						pathField);
				throw new QFElementException(pathField, subclassMapping);
			}
			if (subClassMappingPath.isEmpty()) {
				levelsSubClass = new String[0];
			} else {
				levelsSubClass = subClassMappingPath.split("\\.");
			}

		}

		Attribute<?, ?> prevAttribute = null;

		int actualLevel = -1;
		for (String level : splitLevel) {
			LOGGER.trace("Processing level {}", level);
			actualLevel++;

			Class<?> treatClass = null;

			if (levelsSubClass != null && levelsSubClass.length == actualLevel) {
				// Check levelClass is parent of subclassMapping
				if ((actualLevel == 0 && !entity.getJavaType().isAssignableFrom(subclassMapping)) || (actualLevel > 0
						&& prevAttribute != null && !prevAttribute.getJavaType().isAssignableFrom(subclassMapping))) {
					throw new QFElementException(pathField, subclassMapping);
				}
				treatClass = subclassMapping;
				entity = metamodel.entity(subclassMapping);
				prevAttribute = entity.getAttribute(level);
				attributes.add(new QFAttribute(prevAttribute, treatClass));
				continue;
			}

			try {
				if (prevAttribute == null) {
					prevAttribute = entity.getAttribute(level);
				} else {
					prevAttribute = getAttribute(prevAttribute, level);
				}

				attributes.add(new QFAttribute(prevAttribute, null));
			} catch (Exception e) {
				LOGGER.error("Error processing level {}. Exception", level, e);
				throw e;
			}

		}

		// if (checkFinal) {
		// Class<?> finalClass = attributes.get(attributes.size() -
		// 1).getAttribute().getJavaType();
		// if (!couldBeFinal(finalClass)) {
		// throw new QFFieldLevelException(pathField, splitLevel[splitLevel.length -
		// 1]);
		// }
		// }

		finalClass = attributes.get(attributes.size() - 1).getAttribute().getJavaType();

		return attributes;
	}

	private Attribute<?, ?> getAttribute(Attribute<?, ?> prevAttribute, String level) {

		if (prevAttribute instanceof PluralAttribute<?, ?, ?> pluralAttribute) {
			LOGGER.trace("Processing list attribute {}", prevAttribute);
			var elemType = pluralAttribute.getElementType();
			return processType(elemType, level);

		} else if (prevAttribute instanceof SingularAttribute<?, ?> singularAttribute) {
			LOGGER.trace("Processing singular attribute {}", prevAttribute);
			return processType(singularAttribute.getType(), level);
		}

		// TODO
		throw new IllegalArgumentException("Attribute type not supported");

	}

	private Attribute<?, ?> processType(Type<?> type, String level) {
		if (type instanceof ManagedType<?> managedType) {
			return managedType.getAttribute(level);
		}
		throw new IllegalArgumentException("Type is not a managed type");
	}

	/**
	 * Get final class
	 *
	 * @return final class
	 */
	public Class<?> getFinalClass() {
		return finalClass;
	}

// @formatter:off
//	private static QFPath createQPathOfField(Field field, String path, Class<?> treatClass)
//			throws QueryFilterDefinitionException {
//
//		LOGGER.trace("Processing field {}", field);
//
//		Class<?> fieldClass = field.getType();
//
//		Class<?> finalClass = fieldClass;
//		boolean isFinal = false;
//		QFElementDefType type;
//
//		if (Enum.class.isAssignableFrom(fieldClass) || fieldClass.isEnum()) {
//			type = QFElementDefType.ENUM;
//			isFinal = true;
//		} else if (ClassUtils.isPrimitiveOrBasic(fieldClass)) {
//			isFinal = true;
//			type = QFElementDefType.PROPERTY;
//		} else if (fieldClass.isArray()) {
//			finalClass = fieldClass.getComponentType();
//			type = QFElementDefType.LIST;
//		} else if (List.class.isAssignableFrom(fieldClass) || Set.class.isAssignableFrom(fieldClass)) {
//			finalClass = ClassUtils.getClassOfList(field);
//			type = List.class.isAssignableFrom(fieldClass) ? QFElementDefType.LIST : QFElementDefType.SET;
//		} else {
//			type = QFElementDefType.PROPERTY;
//		}
//
//		return new QFPath(field, path, type, finalClass, isFinal, treatClass);
//	}
// @formatter:on

	private static boolean couldBeFinal(Class<?> clazz) {
		return Enum.class.isAssignableFrom(clazz) || clazz.isEnum() || ClassUtils.isPrimitiveOrBasic(clazz);
	}
}
