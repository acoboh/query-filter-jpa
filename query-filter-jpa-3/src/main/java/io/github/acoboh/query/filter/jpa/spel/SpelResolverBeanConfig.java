package io.github.acoboh.query.filter.jpa.spel;

import java.util.List;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.expression.SecurityExpressionHandler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Auto-configuration class to instantiate SpelResolverContext beans
 *
 * @author Adrián Cobo
 */
@Configuration
public class SpelResolverBeanConfig {

	private static final String SECURITY_EXPRESSION_CLASS = "org.springframework.security.access.expression.SecurityExpressionHandler";

	@Bean
	@ConditionalOnMissingClass(SECURITY_EXPRESSION_CLASS)
	SpelResolverContextBasic spelResolverContextBasic(HttpServletRequest request, HttpServletResponse response) {
		return new SpelResolverContextBasic(request, response);
	}

	@Bean
	@ConditionalOnClass(name = SECURITY_EXPRESSION_CLASS)
	SecuritySpelResolverContext securitySpelResolverContext(
			List<SecurityExpressionHandler<?>> securityExpressionHandlers, HttpServletRequest request,
			HttpServletResponse response) {
		return new SecuritySpelResolverContext(securityExpressionHandlers, request, response);
	}

}
