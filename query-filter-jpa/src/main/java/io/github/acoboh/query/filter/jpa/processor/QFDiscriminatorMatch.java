package io.github.acoboh.query.filter.jpa.processor;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.acoboh.query.filter.jpa.annotations.QFDiscriminator;
import io.github.acoboh.query.filter.jpa.exceptions.QFDiscriminatorNotFoundException;

/**
 * Class with info about the discriminator matching for filtering
 *
 * @author Adrián Cobo
 
 */
public class QFDiscriminatorMatch {

	private static final Logger LOGGER = LoggerFactory.getLogger(QFDiscriminatorMatch.class);

	private final List<String> values;

	private final List<Class<?>> matchingClasses;
	private final QFDefinition definition;

	private final Class<?> entityClass;

	private final boolean isRoot;
	private List<QFPath> path;

	/**
	 * Default constructor
	 *
	 * @param values     list of values
	 * @param definition definition of the field
	 * @throws io.github.acoboh.query.filter.jpa.exceptions.QFDiscriminatorNotFoundException if any discriminator exception occurs
	 */
	public QFDiscriminatorMatch(List<String> values, QFDefinition definition) throws QFDiscriminatorNotFoundException {

		if (!definition.isDiscriminatorFilter()) {
			throw new IllegalArgumentException(
					"Can not construct any discriminator filter with definition without discriminator annotations");
		}

		this.values = values;
		this.definition = definition;
		this.matchingClasses = new ArrayList<>();

		for (String parsedValue : values) {

			Class<?> foundClass = Stream.of(definition.getDiscriminatorAnnotation().value())
					.filter(e -> e.name().equals(parsedValue)).map(QFDiscriminator.Value::type).findFirst()
					.orElse(null);
			if (foundClass == null) {
				throw new QFDiscriminatorNotFoundException(parsedValue, definition.getFilterName());
			}
			matchingClasses.add(foundClass);

		}

		if (!definition.getPaths().isEmpty()) {
			path = definition.getPaths().get(0);
			if (path.isEmpty()) {
				LOGGER.error("Error. Unexpected empty path for discriminator match {}", definition.getFilterName());
			}
			entityClass = path.get(path.size() - 1).getFieldClass();
			isRoot = false;

		} else {
			entityClass = definition.getEntityClass();
			isRoot = true;
		}

	}

	/**
	 * Get matching classes
	 *
	 * @return matching classes
	 */
	public List<Class<?>> getMatchingClasses() {
		return matchingClasses;
	}

	/**
	 * Get entity class
	 *
	 * @return entity class
	 */
	public Class<?> getEntityClass() {
		return entityClass;
	}

	/**
	 * Get if the field is root or nested levels
	 *
	 * @return true if root. False if the field is on nested levels
	 */
	public boolean isRoot() {
		return isRoot;
	}

	/**
	 * Get original field definition
	 *
	 * @return original field definition
	 */
	public QFDefinition getDefinition() {
		return definition;
	}

	/**
	 * Get all values
	 *
	 * @return values
	 */
	public List<String> getValues() {
		return values;
	}

	/**
	 * Get list of paths for nested levels
	 *
	 * @return paths for nested levels
	 */
	public List<QFPath> getPath() {
		return path;
	}

}
