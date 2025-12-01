package io.github.acoboh.query.filter.jpa.processor;

import java.util.List;

/**
 * Query Filter Field Information.
 * <p>
 * This class holds the information of a field in a query filter.
 * </p>
 *
 * @author Adrián Cobo
 * @since 1.0.0
 */
public record QFFieldInfo(String name, String operation, List<String> values) {
}
