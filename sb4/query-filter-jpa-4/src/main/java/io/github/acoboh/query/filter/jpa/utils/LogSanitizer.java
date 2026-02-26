package io.github.acoboh.query.filter.jpa.utils;

import org.jspecify.annotations.Nullable;

public class LogSanitizer {

    private LogSanitizer() {
        // Utility class, no need to instantiate
    }

    public static @Nullable String sanitize(@Nullable String value) {
        if (value == null) {
            return null;
        }
        return value.replace("\r", " ").replace("\n", " ");
    }

    public static @Nullable Object[] sanitize(Object... values) {
        Object[] sanitizedValues = new Object[values.length];
        for (int i = 0; i < values.length; i++) {
            Object value = values[i];
            if (value instanceof String s) {
                sanitizedValues[i] = sanitize(s);
            } else {
                sanitizedValues[i] = value;
            }
        }
        return sanitizedValues;
    }
}
