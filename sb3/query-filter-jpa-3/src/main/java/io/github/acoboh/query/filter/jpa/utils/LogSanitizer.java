package io.github.acoboh.query.filter.jpa.utils;

import org.jspecify.annotations.Nullable;

/**
 * Utility class to sanitize user-supplied values before writing them to logs,
 * preventing log forging/injection via embedded CR/LF characters.
 *
 * @author Adrián Cobo
 */
public class LogSanitizer {

    private LogSanitizer() {
        // Utility class, no need to instantiate
    }

    /**
     * Replace any carriage-return or line-feed character in the value with a space.
     *
     * @param value value to sanitize, may be {@code null}
     * @return sanitized value, or {@code null} if the input was {@code null}
     */
    public static @Nullable String sanitize(@Nullable String value) {
        if (value == null) {
            return null;
        }
        return value.replace("\r", " ").replace("\n", " ");
    }

}
