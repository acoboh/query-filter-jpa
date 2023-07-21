package io.github.acoboh.query.filter.jpa.utils;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.acoboh.query.filter.jpa.predicate.PredicatePart;

/**
 * Utility class to parse predicates
 *
 * @author Adrián Cobo
 * @version $Id: $Id
 */
public class StringParseUtils {

	private static final Logger LOGGER = LoggerFactory.getLogger(StringParseUtils.class);

	/**
	 * Get all parsed parts
	 *
	 * @param predicateExp predicate expression
	 * @return parts
	 */
	public static List<PredicatePart> parseParts(String predicateExp) {

		int level = 0;

		List<PredicatePart> ret = new ArrayList<>();

		StringBuilder builder = new StringBuilder();

		for (int i = 0; i < predicateExp.length(); i++) {

			char cTemp = predicateExp.charAt(i);

			switch (cTemp) {
			case '(':
				// Starts
				level++;

				if (level > 1) {
					builder.append(cTemp);
				}

				break;

			case ')':
				level--;
				if (level == 0) {
					ret.add(new PredicatePart(balanceLevelParenthesis(builder.toString()), true));
					builder = new StringBuilder();
				} else if (level > 0) {
					builder.append(cTemp);
				}

				break;
			case ' ':
				if (level == 0) {

					String s = builder.toString();
					if (!s.isEmpty()) {
						ret.add(new PredicatePart(balanceLevelParenthesis(builder.toString()), false));
					}

					builder = new StringBuilder();
				} else {
					builder.append(cTemp);
				}
				break;

			default:
				builder.append(cTemp);

			}
		}

		String finalS = builder.toString();
		if (!finalS.isEmpty()) {
			ret.add(new PredicatePart(balanceLevelParenthesis(builder.toString()), false));
		}

		LOGGER.trace("End of parse");

		return ret;

	}

	/**
	 * Balance the level parenthesis of any expression
	 *
	 * @param exp expression
	 * @return balance level parenthesis
	 */
	public static String balanceLevelParenthesis(String exp) {

		// (X OR A) AND (X OR B) -> INVALID
		// ((X OR A) AND (X OR B)) -> VALID

		exp = exp.replaceAll(" +", " ").trim();
		if (!exp.startsWith("(") && !exp.endsWith(")")) {
			return exp;
		}

		int level = 0;

		for (int i = 0; i < exp.length(); i++) {

			char cTemp = exp.charAt(i);

			switch (cTemp) {
			case '(':
				level++;
				break;
			case ')':
				level--;
				if (level == 0 && i < exp.length() - 1) {
					return exp;
				}

				break;
			default:
				break;
			}

		}

		if (level == 0) {
			String ret = exp.substring(1, exp.length() - 1);
			if (ret.startsWith("(") && ret.endsWith(")")) {
				return balanceLevelParenthesis(ret);
			}
			return ret;

		} else {
			throw new IllegalStateException("Illegal expression. Review parenthesis");
		}

	}
}
