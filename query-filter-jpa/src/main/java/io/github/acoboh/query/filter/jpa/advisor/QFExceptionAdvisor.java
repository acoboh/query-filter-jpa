package io.github.acoboh.query.filter.jpa.advisor;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import io.github.acoboh.query.filter.jpa.exceptions.QueryFilterException;
import io.github.acoboh.query.filter.jpa.exceptions.language.ExceptionLanguageResolver;
import io.github.acoboh.query.filter.jpa.properties.AdvisorProperties;
import io.github.acoboh.query.filter.jpa.properties.QueryFilterProperties;

/**
 * Class with {@linkplain ControllerAdvice} annotation for multi-language exception support
 *
 * @author Adrián Cobo
 * 
 */
@ControllerAdvice
@ConditionalOnProperty(name = "query-filter.advisor.enabled", havingValue = "true", matchIfMissing = true)
public class QFExceptionAdvisor {

	private final ResourceBundleMessageSource messageSource;

	/**
	 * Default constructor
	 *
	 * @param properties query filter properties
	 */
	protected QFExceptionAdvisor(QueryFilterProperties properties) {
		AdvisorProperties props = properties.getAdvisor();

		messageSource = new ResourceBundleMessageSource();
		messageSource.setBasename(props.getMessageSourceBaseName());
		messageSource.setDefaultEncoding(props.getMessageSourceDefaultEncoding());
		messageSource.setUseCodeAsDefaultMessage(props.isMessageSourceUseCodeAsDefaultMessage());
	}

	/**
	 * Handler for all {@linkplain QueryFilterException} exceptions
	 *
	 * @param ex      exception throw
	 * @param request original {@linkplain HttpServletRequest} request
	 * @return new response
	 */
	@ExceptionHandler(QueryFilterException.class)
	public ResponseEntity<Object> handleEngineExceptions(QueryFilterException ex, HttpServletRequest request) {
		return handleAdvisorMessageResolver(ex, ex, request);
	}

	private ResponseEntity<Object> handleAdvisorMessageResolver(ExceptionLanguageResolver resolver, Exception ex,
			HttpServletRequest request) {
		String message = message(resolver.getMessageCode(), resolver.getArguments());
		return defaultErrorMessage(ex, request, resolver.getHttpStatus(), message, false);
	}

	private ResponseEntity<Object> defaultErrorMessage(Exception e, HttpServletRequest request, HttpStatus status,
			String message, boolean extend) {

		Map<String, Object> map = new LinkedHashMap<>(6);
		map.put("timestamp", new Date());
		map.put("status", status.value());
		map.put("error", status.getReasonPhrase());
		map.put("exception", e.getClass());
		map.put("message", message);
		map.put("path", request.getRequestURI());

		if (extend && e.getCause() != null) {
			map.put("causeClass", e.getCause().getClass());
			map.put("causeMessage", e.getCause().getMessage());
		}

		return ResponseEntity.status(status).body(map);
	}

	private String message(String code, Object... args) {
		return messageSource.getMessage(code, args, LocaleContextHolder.getLocale());
	}

}
