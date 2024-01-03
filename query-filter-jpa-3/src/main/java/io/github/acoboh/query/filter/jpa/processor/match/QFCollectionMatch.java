package io.github.acoboh.query.filter.jpa.processor.match;

import io.github.acoboh.query.filter.jpa.operations.QFCollectionOperationEnum;
import io.github.acoboh.query.filter.jpa.processor.definitions.QFDefinitionCollection;

/**
 * 
 * @author Adrián Cobo
 *
 */
public class QFCollectionMatch {

	private final QFDefinitionCollection definition;

	private final QFCollectionOperationEnum operation;

	private final int value;

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

	}

	/**
	 * Get collection operation
	 * 
	 * @return collection operation
	 */
	public QFCollectionOperationEnum getOperation() {
		return operation;
	}

	/**
	 * Get value of the collection operation
	 * 
	 * @return value
	 */
	public int getValue() {
		return value;
	}

	/**
	 * Get element definition
	 * 
	 * @return element definition
	 */
	public QFDefinitionCollection getDefinition() {
		return definition;
	}

}
