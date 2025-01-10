package io.github.acoboh.query.filter.jpa.spel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.GenericTypeResolver;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.security.access.expression.SecurityExpressionHandler;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.FilterInvocation;
import org.springframework.util.CollectionUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * SPEL Context resolver bean
 *
 * @author Adrián Cobo
 */
class SecuritySpelResolverContext extends SpelResolverContext {

    private static final Logger LOGGER = LoggerFactory.getLogger(SecuritySpelResolverContext.class);

    private final SecurityExpressionHandler<FilterInvocation> securityExpressionHandler;

    /**
     * Default constructor
     *
     * @param securityExpressionHandlers security expression handlers
     * @param request                    actual request
     * @param response                   actual response
     */
    protected SecuritySpelResolverContext(List<SecurityExpressionHandler<?>> securityExpressionHandlers,
                                          HttpServletRequest request, HttpServletResponse response) {
        super(request, response);
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

    @Override
    public EvaluationContext getEvaluationContext() {
        if (securityExpressionHandler == null) {
            return new StandardEvaluationContext();
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        FilterInvocation filterInvocation = new FilterInvocation(request, response, (r, q) -> {
            throw new UnsupportedOperationException();
        });

        return securityExpressionHandler.createEvaluationContext(authentication, filterInvocation);
    }

    @Override
    public ExpressionParser getExpressionParser() {
        if (securityExpressionHandler == null) {
            LOGGER.debug("No security expression handler found. Using default expression parser");
            return new SpelExpressionParser();
        } else {
            return securityExpressionHandler.getExpressionParser();
        }
    }

}
