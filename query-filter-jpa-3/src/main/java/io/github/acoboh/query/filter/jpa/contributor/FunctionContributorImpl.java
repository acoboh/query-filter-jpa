package io.github.acoboh.query.filter.jpa.contributor;

import org.hibernate.boot.model.FunctionContributions;
import org.hibernate.boot.model.FunctionContributor;

/**
 * Function contributor implementation
 *
 * @author Adrián Cobo
 */
public class FunctionContributorImpl implements FunctionContributor {

	/** {@inheritDoc} */
	@Override
	public void contributeFunctions(FunctionContributions functionContributions) {
		for (var fEnum : ArrayFunction.values()) {
			var function = new QfArraySQLFunction(fEnum.getName(), fEnum.getOperator(),
					functionContributions.getTypeConfiguration());
			functionContributions.getFunctionRegistry().register(fEnum.getName(), function);
		}

	}

}
