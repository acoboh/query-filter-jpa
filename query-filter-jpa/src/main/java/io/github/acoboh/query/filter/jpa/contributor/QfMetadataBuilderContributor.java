package io.github.acoboh.query.filter.jpa.contributor;

import org.hibernate.boot.MetadataBuilder;
import org.hibernate.boot.spi.MetadataBuilderContributor;

/**
 * Custom {@linkplain MetadataBuilderContributor} to allow PostgreSQL Array
 * operations
 *
 * @author Adrián Cobo
 */
public class QfMetadataBuilderContributor implements MetadataBuilderContributor {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void contribute(MetadataBuilder metadataBuilder) {
		for (ArrayFunction func : ArrayFunction.values()) {
			metadataBuilder.applySqlFunction(func.getName(), func.getFunction());
		}
	}

}
