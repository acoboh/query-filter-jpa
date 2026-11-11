package io.github.acoboh.query.filter.jpa.processor;

import io.github.acoboh.query.filter.jpa.exceptions.QFParseException;

import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
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
        String extractOP(String field, String value) {
            int indexOf = value.indexOf(':');
            if (indexOf == -1) {
                return "eq";
            }
            return value.substring(0, indexOf);
        }

        @Override
        String extractValue(String field, String value) {
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

        @Override
        String @Nullable [] parseValuePart(String part) {
            int eq = part.indexOf('=');
            if (eq <= 0) {
                return null;
            }
            String field = part.substring(0, eq);
            if (field.indexOf('&') >= 0) {
                return null;
            }
            String rest = part.substring(eq + 1);
            int opEnd = lettersEnd(rest, 0);
            if (opEnd == 0 || opEnd >= rest.length() || rest.charAt(opEnd) != ':') {
                return null;
            }
            return new String[] { field, rest.substring(0, opEnd), rest.substring(opEnd + 1) };
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
        String extractOP(String field, String value) {
            int indexOf = value.indexOf('[');
            if (indexOf == -1) {
                return "eq";
            }
            int closeIndex = value.indexOf(']', indexOf + 1);
            if (closeIndex == -1) {
                throw new QFParseException(field, value);
            }
            return value.substring(indexOf + 1, closeIndex);
        }

        @Override
        String extractValue(String field, String value) {
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

        @Override
        String @Nullable [] parseValuePart(String part) {
            int eq = part.indexOf('=');
            if (eq <= 0 || part.charAt(eq - 1) != ']') {
                return null;
            }
            String left = part.substring(0, eq);
            int open = left.lastIndexOf('[');
            if (open <= 0) {
                return null;
            }
            if (left.substring(0, open).indexOf('&') >= 0) {
                return null;
            }
            String op = left.substring(open + 1, left.length() - 1);
            if (op.isEmpty() || lettersEnd(op, 0) != op.length()) {
                return null;
            }
            return new String[] { left.substring(0, open), op, part.substring(eq + 1) };
        }
    }; // Name

    private final Pattern pattern;

    private final String beautifulName;

    QFParamType(String regexPattern, String beautifulName) {
        this.pattern = Pattern.compile(regexPattern);
        this.beautifulName = beautifulName;
    }

    /**
     * Get pattern for parsing
     *
     * @return pattern
     * @deprecated the input is no longer parsed with this regular expression (its
     *             backtracking could overflow the stack with large inputs). Kept only
     *             for compatibility, will be removed in the next major version
     */
    @Deprecated(since = "2.1.0", forRemoval = true)
    public Pattern getPattern() {
        return pattern;
    }

    /**
     * Beautiful name for documentation
     *
     * @return beautiful name
     */
    public String getBeautifulName() {
        return beautifulName;
    }

    /**
     * Extract operator from value
     *
     * @param field field name the value belongs to, used to build a helpful error
     *              message if the value is malformed
     * @param value value to extract operator from
     * @return operator
     * @throws QFParseException if the value is not empty but does not follow this
     *                          standard's expected syntax
     */
    abstract String extractOP(String field, String value);

    /**
     * Extract value from value
     *
     * @param field field name the value belongs to, used to build a helpful error
     *              message if the value is malformed
     * @param value value to extract value from
     * @return value
     */
    abstract String extractValue(String field, String value);

    abstract String buildParam(String field, String op, String value);

    /**
     * Parse a value part ({@code field=op:value} or {@code field[op]=value})
     *
     * @param part part of the input, already split by {@link #splitParts(String)}
     * @return array with {@code [field, operation, value]} or null if the part does
     *         not follow this standard
     */
    abstract String @Nullable [] parseValuePart(String part);

    private static final String SORT_PREFIX = "sort=";

    /**
     * Parse a sort part ({@code sort=+field,-other})
     *
     * @param part part of the input, already split by {@link #splitParts(String)}
     * @return the sort value or null if the part is not a sort part
     */
    static @Nullable String parseSortPart(String part) {
        if (!part.startsWith(SORT_PREFIX) || part.length() == SORT_PREFIX.length()) {
            return null;
        }
        String value = part.substring(SORT_PREFIX.length());
        return value.indexOf('&') >= 0 ? null : value;
    }

    /**
     * Split the input in parts in linear time, without regular expressions.
     * <p>
     * Inside a value (after the first {@code =} of a part) an {@code &} is a
     * separator only when it is followed by an ASCII letter or digit (or it is the
     * last char); otherwise the {@code &} and the next char belong to the value.
     * Outside a value an {@code &} is always a separator. These are the same rules
     * as the previous regular expression.
     * Empty parts (e.g. {@code a=eq:1&&&b=eq:2} separators) are ignored.
     *
     * @param input full input
     * @return parts
     */
    static List<String> splitParts(String input) {
        List<String> parts = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inValue = false; // after the first '=' of the current part
        int i = 0;
        int length = input.length();
        while (i < length) {
            char c = input.charAt(i);
            if (c == '&') {
                if (inValue && i + 1 < length && !isAsciiLetterOrDigit(input.charAt(i + 1))) {
                    current.append(c).append(input.charAt(i + 1));
                    i += 2;
                    continue;
                }
                if (!current.isEmpty()) {
                    parts.add(current.toString());
                    current.setLength(0);
                }
                inValue = false;
                i++;
                continue;
            }
            if (c == '=') {
                inValue = true;
            }
            current.append(c);
            i++;
        }
        if (!current.isEmpty()) {
            parts.add(current.toString());
        }
        return parts;
    }

    private static boolean isAsciiLetterOrDigit(char c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9');
    }

    private static int lettersEnd(String value, int from) {
        int i = from;
        while (i < value.length() && ((value.charAt(i) >= 'a' && value.charAt(i) <= 'z')
                || (value.charAt(i) >= 'A' && value.charAt(i) <= 'Z'))) {
            i++;
        }
        return i;
    }

}
