package io.github.acoboh.query.filter.jpa.processor;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.acoboh.query.filter.jpa.annotations.QFDiscriminator;
import io.github.acoboh.query.filter.jpa.exceptions.QFDiscriminatorNotFoundException;

public class QFDiscriminatorMatch {

	private static final Logger LOGGER = LoggerFactory.getLogger(QFDiscriminatorMatch.class);

	private final List<String> values;

	private final List<Class<?>> matchingClasses;
	private final QFDefinition definition;

	private final Class<?> entityClass;

	private final boolean isRoot;
	private List<QFPath> path;

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

	public List<Class<?>> getMatchingClasses() {
		return matchingClasses;
	}

	public Class<?> getEntityClass() {
		return entityClass;
	}

	public boolean isRoot() {
		return isRoot;
	}

	public QFDefinition getDefinition() {
		return definition;
	}

	public List<String> getValues() {
		return values;
	}

	public List<QFPath> getPath() {
		return path;
	}

}
