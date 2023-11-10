package io.github.acoboh.query.filter.jpa.processor;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.acoboh.query.filter.jpa.operations.QFCollectionOperationEnum;
import io.github.acoboh.query.filter.jpa.processor.definitions.QFDefinitionCollection;

/**
 * 
 * @author Adrián Cobo
 *
 */
public class QFCollectionMatch {

	private static final Logger LOGGER = LoggerFactory.getLogger(QFCollectionMatch.class);

	private final QFDefinitionCollection definition;

	private final QFCollectionOperationEnum operation;

	private final int value;

	private final List<QFPath> paths;

	/**
	 * Default constructor
	 * 
	 * @param definition definition of element
	 * @param operation  operation of the element
	 * @param value      value of the element operation
	 */
	public QFCollectionMatch(QFDefinitionCollection definition, QFCollectionOperationEnum operation, int value) {
		super();
		this.definition = definition;
		this.operation = operation;
		this.value = value;
//		this.paths = new ArrayList<>(definition.getPaths().get(0)); // TODO fix
		this.paths = new ArrayList<>();
	}

	/**
	 * Get paths
	 * 
	 * @return paths
	 */
	public List<QFPath> getPaths() {
		return paths;
	}

	/**
	 * Get value of the collection operation
	 * 
	 * @return value
	 */
	public int getValue() {
		return value;
	}

}
