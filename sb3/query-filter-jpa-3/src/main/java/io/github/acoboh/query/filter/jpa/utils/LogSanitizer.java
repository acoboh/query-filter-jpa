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

}
