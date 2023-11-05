package io.github.acoboh.query.filter.jpa.spel;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.expression.SecurityExpressionHandler;

/**
 * Auto-configuration class to instantiate SpelResolverContext beans
 * 
 * @author Adrián Cobo
 *
 */
@Configuration
public class SpelResolverBeanConfig {

	private static final String securityExpressionHandlerClass = "org.springframework.security.access.expression.SecurityExpressionHandler";

	@Bean
	@ConditionalOnMissingClass(securityExpressionHandlerClass)
	SpelResolverContextBasic spelResolverContextBasic(HttpServletRequest request, HttpServletResponse response) {
		return new SpelResolverContextBasic(request, response);
	}

	@Bean
	@ConditionalOnClass(name = securityExpressionHandlerClass)
	SecuritySpelResolverContext securitySpelResolverContext(
			List<SecurityExpressionHandler<?>> securityExpressionHandlers, HttpServletRequest request,
			HttpServletResponse response) {
		return new SecuritySpelResolverContext(securityExpressionHandlers, request, response);
	}

}
