package io.github.acoboh.query.filter.jpa.spel;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.expression.EvaluationContext;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

/**
 * SPEL Context resolver bean
 *
 * @author Adrián Cobo
 */
class SpelResolverContextBasic extends SpelResolverContext {

	/**
	 * Default constructor
	 *
	 * @param request
	 *            request
	 * @param response
	 *            response
	 */
	protected SpelResolverContextBasic(HttpServletRequest request, HttpServletResponse response) {
		super(request, response);
	}

	@Override
	public ExpressionParser getExpressionParser() {
		return new SpelExpressionParser();
	}

	@Override
	public EvaluationContext getEvaluationContext() {
		return new StandardEvaluationContext();
	}

}
