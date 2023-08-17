package io.github.acoboh.query.filter.jpa.spel;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.GenericTypeResolver;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.security.access.expression.SecurityExpressionHandler;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.FilterInvocation;
import org.springframework.util.CollectionUtils;
import org.springframework.util.MultiValueMap;

/**
 * SPEL Context resolver bean
 *
 * @author Adrián Cobo
 
 */
@Configuration
@ConditionalOnClass(SecurityExpressionHandler.class)
public class SecuritySpelContextResolver implements SpelResolverInterface {

	private static final Logger LOGGER = LoggerFactory.getLogger(SecuritySpelContextResolver.class);

	private final SecurityExpressionHandler<FilterInvocation> securityExpressionHandler;

	private HttpServletRequest request;

	private HttpServletResponse response;

	/**
	 * Default constructor
	 *
	 * @param securityExpressionHandlers security expression handlers
	 * @param request                    actual request
	 * @param response                   actual response
	 */
	public SecuritySpelContextResolver(List<SecurityExpressionHandler<?>> securityExpressionHandlers,
			HttpServletRequest request, HttpServletResponse response) {
		securityExpressionHandler = getFilterSecurityHandler(securityExpressionHandlers);
	}

	@SuppressWarnings("unchecked")
	private static SecurityExpressionHandler<FilterInvocation> getFilterSecurityHandler(
			List<SecurityExpressionHandler<?>> securityExpressionHandlers) {

		if (CollectionUtils.isEmpty(securityExpressionHandlers)) {
			LOGGER.warn("No security expression handlers found. Default expression handler will be used.");
			return null;
		}

		return (SecurityExpressionHandler<FilterInvocation>) securityExpressionHandlers.stream()
				.filter(handler -> FilterInvocation.class.equals(
						GenericTypeResolver.resolveTypeArgument(handler.getClass(), SecurityExpressionHandler.class)))
				.findAny()
				.orElseThrow(() -> new IllegalStateException(
						"No filter invocation security expression handler has been found! Handlers: "
								+ securityExpressionHandlers.size()));
	}

	/** {@inheritDoc} */
	public Object evaluate(String securityExpression, MultiValueMap<String, Object> contextValues) {
		LOGGER.trace("Resolving expression {}", securityExpression);

		ExpressionParser expressionParser;
		if (securityExpressionHandler == null) {
			LOGGER.debug("No security expression handler found. Using default expression parser");
			expressionParser = new SpelExpressionParser();
		} else {
			expressionParser = securityExpressionHandler.getExpressionParser();
		}

		Expression expression = expressionParser.parseExpression(securityExpression);

		EvaluationContext evaluationContext = createEvaluationContext(securityExpressionHandler, request, response);

		contextValues.forEach((k, v) -> {
			if (v.size() > 1) {
				evaluationContext.setVariable(k, v);
			} else if (v.size() == 1) {
				evaluationContext.setVariable(k, v.get(0));
			}
		});

		return expression.getValue(evaluationContext);
	}

	private EvaluationContext createEvaluationContext(SecurityExpressionHandler<FilterInvocation> handler,
			HttpServletRequest request, HttpServletResponse response) {
		if (handler == null) {
			return new StandardEvaluationContext();
		}

		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		FilterInvocation filterInvocation = new FilterInvocation(request, response, (r, q) -> {
			throw new UnsupportedOperationException();
		});

		return handler.createEvaluationContext(authentication, filterInvocation);
	}

}
