package io.github.acoboh.query.filter.jpa.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(value = "query-filter")
@Configuration
@Validated
public class QueryFilterProperties {

	private AdvisorProperties advisor = new AdvisorProperties();

	public AdvisorProperties getAdvisor() {
		return advisor;
	}

	public void setAdvisor(AdvisorProperties advisor) {
		this.advisor = advisor;
	}

}
