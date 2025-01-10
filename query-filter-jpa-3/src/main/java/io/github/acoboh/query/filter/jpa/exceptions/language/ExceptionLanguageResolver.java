package io.github.acoboh.query.filter.jpa.exceptions.language;

import org.springframework.http.HttpStatus;

/**
 * Interface definition methods for language resolving exceptions
 *
 * @author Adrián Cobo
 */
public interface ExceptionLanguageResolver {

	/**
	 * Get the HttpStatus to be returned
	 *
	 * @return HttpStatus
	 */
	HttpStatus getHttpStatus();

	/**
	 * List of arguments to be resolved on the message code
	 *
	 * @return array of arguments
	 */
	Object[] getArguments();

	/**
	 * Message code of the multi-language properties
	 *
	 * @return message code
	 */
	String getMessageCode();

}
