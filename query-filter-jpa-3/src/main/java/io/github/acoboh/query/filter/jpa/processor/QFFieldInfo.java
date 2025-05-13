package io.github.acoboh.query.filter.jpa.processor;

import java.util.List;

public record QFFieldInfo(String name, String operation, List<String> values) {
}
