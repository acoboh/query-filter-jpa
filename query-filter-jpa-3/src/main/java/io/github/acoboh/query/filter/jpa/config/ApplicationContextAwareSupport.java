package io.github.acoboh.query.filter.jpa.config;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

/**
 * <p>
 * Class to get the Spring {@link ApplicationContext} and wrap it in a bean.
 * </p>
 * Used to allow Spring AOP construct proxies to be injected into the
 * {@link io.github.acoboh.query.filter.jpa.processor.QFProcessor} constructors
 *
 * @author Adrián Cobo
 */
@Component("applicationContextAwareSupport")
public class ApplicationContextAwareSupport implements ApplicationContextAware {

	private ApplicationContext applicationContextAware;

	/** {@inheritDoc} */
	@Override
	public void setApplicationContext(@NonNull ApplicationContext applicationContext) {
		this.applicationContextAware = applicationContext;
	}

	/**
	 * <p>
	 * Get the Spring {@link ApplicationContext}
	 * </p>
	 *
	 * @return a {@link org.springframework.context.ApplicationContext} object
	 */
	public ApplicationContext getApplicationContext() {
		return applicationContextAware;
	}
}
