package io.github.acoboh.query.filter.jpa.spel;

import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.PropertyValue;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.ServletRequestParameterPropertyValues;
import org.springframework.web.servlet.View;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * SPEL Context base to resolve SpEL expressions
 *
 * @author Adrián Cobo
 */
public abstract class SpelResolverContext {

	/**
	 * Http servlet request parameter
	 */
	protected final HttpServletRequest request;

	/**
	 * Http servlet response parameter
	 */
	protected final HttpServletResponse response;

	/**
	 * Default constructor
	 *
	 * @param request
	 *            the servlet request
	 * @param response
	 *            the servlet response
	 */
	protected SpelResolverContext(HttpServletRequest request, HttpServletResponse response) {
		this.request = request;
		this.response = response;
	}

	/**
	 * Evaluate any expression
	 *
	 * @param securityExpression
	 *            expression to evaluate
	 * @param contextValues
	 *            actual context values
	 * @return object evaluated
	 */
	public Object evaluate(String securityExpression, MultiValueMap<String, Object> contextValues) {

		ExpressionParser expressionParser = getExpressionParser();

		Expression expression = expressionParser.parseExpression(securityExpression);

		EvaluationContext context = getEvaluationContext();

		if (request != null) {
			fillContextWithRequestValues(context);
		}

		fillContextWithMap(context, contextValues);

		return expression.getValue(context);

	}

	/**
	 * Get expression parser to resolve de SpEL expression
	 *
	 * @return the expression parser to use
	 */
	public abstract ExpressionParser getExpressionParser();

	/**
	 * Get the evaluation context of the expression
	 *
	 * @return evaluation context to use
	 */
	public abstract EvaluationContext getEvaluationContext();

	private void fillContextWithRequestValues(EvaluationContext context) {

		Object pathObject = request.getAttribute(View.PATH_VARIABLES);
		if (pathObject instanceof Map<?, ?> map) {
			context.setVariable("_pathVariables", map);
		}

		var properties = new ServletRequestParameterPropertyValues(request);
		Map<String, Object> requestParams = properties.getPropertyValueList().stream().filter(e -> e.getValue() != null)
				.collect(Collectors.toMap(PropertyValue::getName, PropertyValue::getValue));

		context.setVariable("_parameters", requestParams);
	}

	private void fillContextWithMap(EvaluationContext context, MultiValueMap<String, Object> contextValues) {
		contextValues.forEach((k, v) -> {
			if (v.size() > 1) {
				context.setVariable(k, v);
			} else if (v.size() == 1) {
				context.setVariable(k, v.get(0));
			}
		});
	}
}
