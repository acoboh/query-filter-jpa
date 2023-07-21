package io.github.acoboh.query.filter.jpa.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

/**
 * Query filter configuration properties
 * 
 * @author Adrián Cobo
 *
 */
@ConfigurationProperties(value = "query-filter")
@Configuration
@Validated
public class QueryFilterProperties {

	private AdvisorProperties advisor = new AdvisorProperties();

	/**
	 * Get advisor properties
	 * 
	 * @return advisor properties
	 */
	public AdvisorProperties getAdvisor() {
		return advisor;
	}

	/**
	 * Set advisor properties
	 * 
	 * @param advisor
	 */
	public void setAdvisor(AdvisorProperties advisor) {
		this.advisor = advisor;
	}

}
