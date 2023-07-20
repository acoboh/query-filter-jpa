package io.github.acoboh.query.filter.jpa.spel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;
import org.springframework.context.annotation.Configuration;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.util.MultiValueMap;

/**
 * SPEL Context resolver bean
 *
 * @author Adrián Cobo
 */
@Configuration
@ConditionalOnMissingClass("org.springframework.security.access.expression.SecurityExpressionHandler")
public class SpelContextResolver implements SpelResolverInterface {

	private static final Logger LOGGER = LoggerFactory.getLogger(SpelContextResolver.class);

	@Override
	public Object evaluate(String securityExpression, MultiValueMap<String, Object> contextValues) {
		LOGGER.trace("Resolving expression {}", securityExpression);

		ExpressionParser expressionParser = new SpelExpressionParser();

		Expression expression = expressionParser.parseExpression(securityExpression);

		EvaluationContext evaluationContext = new StandardEvaluationContext();

		contextValues.forEach((k, v) -> {
			if (v.size() > 1) {
				evaluationContext.setVariable(k, v);
			} else if (v.size() == 1) {
				evaluationContext.setVariable(k, v.get(0));
			}
		});

		return expression.getValue(evaluationContext);
	}

}
