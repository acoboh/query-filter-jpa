package io.github.acoboh.query.filter.jpa.processor.definitions;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import io.github.acoboh.query.filter.jpa.exceptions.definition.QFElementException;
import io.github.acoboh.query.filter.jpa.exceptions.definition.QueryFilterDefinitionException;
import io.github.acoboh.query.filter.jpa.processor.QFAttribute;
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
	private final Metamodel metamodel;
	private List<QFAttribute> attributes;
	private Class<?> finalClass;

	private final Class<?> subclassMapping;
	private final String subClassMappingPath;

	FieldClassProcessor(Class<?> rootClass, String pathField, Class<?> subclassMapping, String subClassMappingPath,
			Metamodel metamodel) {
		Assert.notNull(pathField, "Path field cannot be null");
		this.rootClass = rootClass;
		this.pathField = pathField;
		this.subclassMapping = subclassMapping;
		this.subClassMappingPath = subClassMappingPath;
		this.metamodel = metamodel;
	}

	/**
	 * Get the attributes of the path field
	 *
	 * @return list of attributes
	 * @throws io.github.acoboh.query.filter.jpa.exceptions.definition.QueryFilterDefinitionException
	 *             if any.
	 */
	public List<QFAttribute> getAttributes() throws QueryFilterDefinitionException {

		if (attributes != null) {
			return attributes;
		}

		LOGGER.debug("Parsing with metamodel");

		String[] splitLevel = pathField.split("\\.");
		attributes = new ArrayList<>(splitLevel.length);

		var entity = metamodel.entity(rootClass);

		// Level subclass
		String[] levelsSubClass = getLevelSubClassIfAvailable();

		Attribute<?, ?> prevAttribute = null;

		int actualLevel = -1;
		for (String level : splitLevel) {
			LOGGER.trace("Processing level {}", level);
			actualLevel++;

			Class<?> treatClass;

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
				throw new QueryFilterDefinitionException("Error processing level {}. Exception", e, level);
			}

		}

		finalClass = attributes.get(attributes.size() - 1).getAttribute().getJavaType();

		return attributes;
	}

	private String @Nullable [] getLevelSubClassIfAvailable() throws QFElementException {
		String[] levelsSubClass = null;

		if (subclassMapping != null && !Void.class.equals(subclassMapping)) {
			LOGGER.trace("Processing subclass mapping {}", subclassMapping);
			if (subClassMappingPath != null && !subClassMappingPath.isEmpty()
					&& !pathField.startsWith(subClassMappingPath)) {
				LOGGER.trace("Subclass mapping path '{}' not present in path field '{}'", subClassMappingPath,
						pathField);
				throw new QFElementException(pathField, subclassMapping);
			}
			if (subClassMappingPath == null || subClassMappingPath.isEmpty()) {
				levelsSubClass = new String[0];
			} else {
				levelsSubClass = subClassMappingPath.split("\\.");
			}

		}
		return levelsSubClass;
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

}
