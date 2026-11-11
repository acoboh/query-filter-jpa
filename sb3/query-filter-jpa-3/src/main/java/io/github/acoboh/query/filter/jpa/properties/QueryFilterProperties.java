package io.github.acoboh.query.filter.jpa.properties;

import jakarta.validation.Valid;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

/**
 * Query filter configuration properties
 *
 * @author Adrián Cobo
 */
@ConfigurationProperties(value = "query-filter")
@Configuration
@Validated
public class QueryFilterProperties {

    private AdvisorProperties advisor = new AdvisorProperties();

    private SpelProperties spel = new SpelProperties();

    private boolean validateEndpoints = true;

    @Valid
    private LimitsProperties limits = new LimitsProperties();

    /**
     * Get the limits applied to the client input
     *
     * @return limits
     */
    public LimitsProperties getLimits() {
        return limits;
    }

    /**
     * Set the limits applied to the client input
     *
     * @param limits limits
     */
    public void setLimits(LimitsProperties limits) {
        this.limits = limits;
    }

    /**
     * Whether controller parameters annotated with {@code @QFParam} or
     * {@code @QFMultiParam} are validated at startup (default {@code true})
     *
     * @return true if endpoints are validated
     */
    public boolean isValidateEndpoints() {
        return validateEndpoints;
    }

    /**
     * Set whether endpoints are validated at startup
     *
     * @param validateEndpoints new value
     */
    public void setValidateEndpoints(boolean validateEndpoints) {
        this.validateEndpoints = validateEndpoints;
    }

    /**
     * Get SpEL properties
     *
     * @return SpEL properties
     */
    public SpelProperties getSpel() {
        return spel;
    }

    /**
     * Set SpEL properties
     *
     * @param spel SpEL properties
     */
    public void setSpel(SpelProperties spel) {
        this.spel = spel;
    }

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
     * @param advisor advisor properties
     */
    public void setAdvisor(AdvisorProperties advisor) {
        this.advisor = advisor;
    }

}
