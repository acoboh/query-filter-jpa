package io.github.acoboh.query.filter.jpa.config;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@Component("applicationContextAwareSupport")
public class ApplicationContextAwareSupport implements ApplicationContextAware {

	private ApplicationContext applicationContextAware;

	@Override
	public void setApplicationContext(@NonNull ApplicationContext applicationContext) {
		this.applicationContextAware = applicationContext;
	}

	public ApplicationContext getApplicationContext() {
		return applicationContextAware;
	}
}
