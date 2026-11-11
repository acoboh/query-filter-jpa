package io.github.acoboh.query.filter.jpa.properties;

/**
 * SpEL evaluation configuration properties ({@code query-filter.spel.*})
 *
 * @author Adrián Cobo
 */
public class SpelProperties {

    private boolean restricted = true;

    /**
     * Default constructor
     */
    public SpelProperties() {
        // Default values
    }

    /**
     * Whether the SpEL evaluation context provided by Spring Security is hardened.
     * <p>
     * When {@code true} (default), type references ({@code T(...)}), constructors
     * and access to {@link Class}, {@link ClassLoader} and reflection members are
     * disabled. Set to {@code false} only if your filter expressions need them.
     *
     * @return true if the evaluation context is restricted
     */
    public boolean isRestricted() {
        return restricted;
    }

    /**
     * Set whether the SpEL evaluation context is restricted
     *
     * @param restricted new value
     */
    public void setRestricted(boolean restricted) {
        this.restricted = restricted;
    }
}
