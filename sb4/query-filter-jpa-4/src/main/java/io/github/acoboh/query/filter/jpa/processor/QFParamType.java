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
     * {@code
     * authorName=eq:Adrian
     * }
     */
    RHS_COLON("(([^&=]+)=([a-zA-Z]+):((?:[^&]|&[^a-zA-Z0-9])*[^&]*))|(sort=([^&]+))", // Pattern Regex
            "RHS Colon") {
        @Override
        String extractOP(String value) {
            int indexOf = value.indexOf(':');
            if (indexOf == -1) {
                return "eq";
            }
            return value.substring(0, indexOf);
        }

        @Override
        String extractValue(String value) {
            int indexOf = value.indexOf(':');
            if (indexOf == -1) {
                return value;
            }
            return value.substring(indexOf + 1);
        }

        @Override
        String buildParam(String field, String op, String value) {
            return String.format("%s=%s:%s", field, op, value);
        }
    },

    /**
     * LHS Brackets standard
     * <p>
     * Example:
     * <p>
     * {@code
     * authorName[eq]=Adrian
     * }
     */
    LHS_BRACKETS("(([^&=]+)\\[([a-zA-Z]+)\\]=((?:[^&]|&[^a-zA-Z0-9])*[^&]*))|(sort=([^&]+))", // Pattern Regex
            "LHS Brackets") {
        @Override
        String extractOP(String value) {
            int indexOf = value.indexOf('[');
            if (indexOf == -1) {
                return "eq";
            }
            return value.substring(indexOf + 1, value.indexOf(']'));
        }

        @Override
        String extractValue(String value) {
            int indexOf = value.indexOf(']');
            if (indexOf == -1) {
                return value;
            }
            return value.substring(indexOf + 1);
        }

        @Override
        String buildParam(String field, String op, String value) {
            return String.format("%s[%s]=%s", field, op, value);
        }
    }; // Name

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
    public String getBeautifulName() {
        return beatifulName;
    }

    /**
     * Extract operator from value
     * 
     * @param value value to extract operator from
     * @return operator
     */
    abstract String extractOP(String value);

    /**
     * Extract value from value
     * 
     * @param value value to extract value from
     * @return value
     */
    abstract String extractValue(String value);

    /**
     * Build param from field, operator and value
     * 
     * @param field field name
     * @param op    operator
     * @param value value
     * @return built param
     */
    abstract String buildParam(String field, String op, String value);

}
