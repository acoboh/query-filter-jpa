package io.github.acoboh.query.filter.jpa.processor;

import java.util.regex.Pattern;

/**
 * Enumeration for all parsing standards
 *
 * @author Adrián Cobo
 */
public enum QFParamType {

    /**
     * RHS Colon standard
     * <p>
     * Example:
     * <p>
     * <code>
     * authorName=eq:Adrian
     * </code>
     */
    RHS_COLON("(([^&=]+)=([a-zA-Z]+):((?:[^&]|&[^a-zA-Z0-9])*[^&]*))|(sort=([^&]+))", // Pattern Regex
            "RHS Colon"),

    /**
     * LHS Brackets standard
     * <p>
     * Example:
     * <p>
     * <code>
     * authorName[eq]=Adrian
     * </code>
     */
    LHS_BRACKETS(
            "(([^&=]+)\\[([a-zA-Z]+)\\]=((?:[^&]|&[^a-zA-Z0-9])*[^&]*))|(sort=([^&]+))", // Pattern Regex
            "LHS Brackets"); // Name

    private final Pattern pattern;

    private final String beatifulName;

    QFParamType(String regexPattern, String beatifulName) {
        this.pattern = Pattern.compile(regexPattern);
        this.beatifulName = beatifulName;
    }

    /**
     * Get pattern for parsing
     *
     * @return pattern
     */
    public Pattern getPattern() {
        return pattern;
    }

    /**
     * Beautiful name for documentation
     *
     * @return beautiful name
     */
    public String getBeatifulName() {
        return beatifulName;
    }

}
