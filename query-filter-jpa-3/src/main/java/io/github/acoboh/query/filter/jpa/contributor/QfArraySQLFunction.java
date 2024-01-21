package io.github.acoboh.query.filter.jpa.contributor;

import java.util.List;

import org.hibernate.QueryException;
import org.hibernate.query.sqm.function.AbstractSqmSelfRenderingFunctionDescriptor;
import org.hibernate.query.sqm.produce.function.ArgumentTypesValidator;
import org.hibernate.query.sqm.produce.function.FunctionParameterType;
import org.hibernate.query.sqm.produce.function.StandardArgumentsValidators;
import org.hibernate.query.sqm.produce.function.StandardFunctionArgumentTypeResolvers;
import org.hibernate.query.sqm.produce.function.StandardFunctionReturnTypeResolvers;
import org.hibernate.sql.ast.SqlAstNodeRenderingMode;
import org.hibernate.sql.ast.SqlAstTranslator;
import org.hibernate.sql.ast.spi.SqlAppender;
import org.hibernate.sql.ast.tree.SqlAstNode;
import org.hibernate.type.StandardBasicTypes;
import org.hibernate.type.spi.TypeConfiguration;

/**
 * SQL Function implementation of PostgreSQL Array operations
 *
 * @author Adrián Cobo
 */

class QfArraySQLFunction extends AbstractSqmSelfRenderingFunctionDescriptor {

	/**
	 * <p>
	 * Constructor for QfArraySQLFunction.
	 * </p>
	 * 
	 * @param name              Name of the function
	 * @param operator          used on SQL queries
	 * @param typeConfiguration Type configuration for resolving basic type registry
	 */
	public QfArraySQLFunction(String name, String operator, TypeConfiguration typeConfiguration) {
		super(name,
				new ArgumentTypesValidator(StandardArgumentsValidators.min(2), FunctionParameterType.ANY,
						FunctionParameterType.COMPARABLE),
				StandardFunctionReturnTypeResolvers
						.invariant(typeConfiguration.getBasicTypeRegistry().resolve(StandardBasicTypes.BOOLEAN)),
				StandardFunctionArgumentTypeResolvers.NULL);
		this.operator = operator;
	}

	private String operator;

	/**
	 * Get the operator used on SQL queries
	 * 
	 * @return operator
	 */
	public String getOperator() {
		return operator;
	}

	@Override
	public void render(SqlAppender sqlAppender, List<? extends SqlAstNode> sqlAstArguments,
			SqlAstTranslator<?> walker) {

		if (sqlAstArguments.size() < 2) {
			throw new QueryException("Array function not enough arguments", sqlAppender.toString());
		}

		sqlAppender.append("(");
		// Add first argument
		walker.render(sqlAstArguments.get(0), SqlAstNodeRenderingMode.DEFAULT);

		sqlAppender.append(operator);
		sqlAppender.append("ARRAY[");

		String prefix = "";

		for (int i = 1; i < sqlAstArguments.size(); i++) {
			sqlAppender.append(prefix);
			walker.render(sqlAstArguments.get(i), SqlAstNodeRenderingMode.DEFAULT);
			prefix = ", ";
		}

		sqlAppender.append("]) and true ");

	}

}
