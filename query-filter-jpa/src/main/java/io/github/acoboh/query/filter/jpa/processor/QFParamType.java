package io.github.acoboh.query.filter.jpa.processor;

import java.util.regex.Pattern;

public enum QFParamType {

	// @formatter:off
    RHS_COLON(
            "[a-zA-Z0-9\\.]+\\=(?:[a-zA-Z]+|\\~|\\^|\\$)\\:[a-zA-Z0-9\\p{L}\\,\\s\\:\\-\\_\\.\\*\\%/\\(\\)\\@\\+\\{\\}\\'\\=]*",
            "([a-zA-Z0-9\\.]+)\\=([a-zA-Z]+)\\:([a-zA-Z0-9\\p{L}\\,\\s\\:\\-\\_\\.\\*\\%/\\(\\)\\@\\+\\{\\}\\'\\=]*)",
            "RHS Colon"),

    LHS_BRACKETS(
            "[a-zA-Z0-9\\.]+\\[[a-zA-Z]+]\\=[a-zA-Z0-9\\p{L}\\,\\s\\:\\-\\_\\.\\*\\%/\\(\\)\\@\\+\\{\\}\\=\\']*",
            "([a-zA-Z0-9\\.])+\\[([a-zA-Z]+)]\\=([a-zA-Z0-9\\p{L}\\,\\s\\:\\-\\_\\.\\*\\%/\\(\\)\\@\\+\\{\\}\\=\\']*)",
            "LHS Brackets");
    // @formatter:on

	private final String fullRegex;

	private final Pattern pattern;

	private final String beatifulName;

	QFParamType(String fullRegex, String regexPattern, String beatifulName) {
		this.fullRegex = fullRegex;
		this.pattern = Pattern.compile(regexPattern);
		this.beatifulName = beatifulName;
	}

	public String getFullRegex() {
		return fullRegex;
	}

	public Pattern getPattern() {
		return pattern;
	}

	public String getBeatifulName() {
		return beatifulName;
	}

}
