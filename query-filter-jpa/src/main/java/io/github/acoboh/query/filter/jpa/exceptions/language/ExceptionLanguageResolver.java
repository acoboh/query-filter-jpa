package io.github.acoboh.query.filter.jpa.exceptions.language;

import org.springframework.http.HttpStatus;

public interface ExceptionLanguageResolver {

	public HttpStatus getHttpStatus();

	public Object[] getArguments();

	public String getMessageCode();

}
