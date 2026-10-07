package io.github.acoboh.query.filter.jpa.operations;

import io.github.acoboh.query.filter.jpa.contributor.ArrayFunction;
import io.github.acoboh.query.filter.jpa.processor.match.QFElementMatch;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.util.MultiValueMap;

import java.util.List;

class PredicateUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(PredicateUtils.class);

    /**
     * Explicit LIKE escape character (the JPA default).
     * <p>
     * It is passed explicitly to the JPA like/notLike operations, because the
     * 2-argument like is rendered by some Hibernate versions without any usable
     * escape (they even emit "escape ''", which deactivates the database default
     * escape), so patterns with backslash-escaped wildcards would not work.
     */
    private static final char LIKE_ESCAPE_CHAR = '\\';

    private PredicateUtils() {

    }

    /**
     * Join expressions on single array. Used for array functions to unify the
     * parameters
     *
     * @param cb       Criteria builder of the query
     * @param exp      expression to join
     * 
     * @param literals list of literals to join
     * 
     * @return an array of {@link jakarta.persistence.criteria.Expression} objects
     */
    public static Expression<?>[] joinExp(CriteriaBuilder cb, Expression<?> exp, List<Object> literals) {

        Expression<?>[] array = new Expression[literals.size() + 1];

        array[0] = exp;

        for (int i = 1; i < array.length; i++) {
            array[i] = cb.literal(literals.get(i - 1));
        }

        return array;
    }

    /**
     * Creates a predicate for the given expression and value using the like
     * operator.
     * <p>
     * The value is wrapped with {@code %} wildcards to match any string that contains it.
     * Any LIKE special character in the value ({@code %}, {@code _} and the LIKE escape
     * character {@code \}) is escaped, so it is matched literally: for example
     * {@code 100%} only matches strings containing a literal {@code %}. Use
     * {@link QFOperationEnum#REGULAR_LIKE} to keep user-provided wildcards unescaped.
     *
     * @param criteriaBuilder criteriaBuilder of the query
     * @param exp             expression to apply the predicate
     * @param value           the value to match against
     * @param caseSensitive   true if the predicate should be case-sensitive, false
     *                        otherwise
     * @return a {@link jakarta.persistence.criteria.Predicate} which is the result
     *         of the like operation
     */
    public static Predicate parseLikePredicate(CriteriaBuilder criteriaBuilder, Expression<String> exp, String value,
            boolean caseSensitive, boolean like) {
        String finalValue = "%".concat(escapeLikeWildcards(value)).concat("%");
        return finalLikeSensitive(criteriaBuilder, exp, finalValue, caseSensitive, like);
    }

    /**
     * Creates a predicate for the given expression and value using the like
     * operator.
     * <p>
     * This method is used to create a 'starts with' predicate. The value is
     * concatenated with a '%' character to match any string that starts with the
     * given value. LIKE special characters in the value ({@code %}, {@code _} and the
     * LIKE escape character {@code \}) are escaped, so they are matched literally.
     * </p>
     *
     * @param criteriaBuilder criteriaBuilder of the query
     * @param exp             expression to apply the predicate
     * @param value           the value to match against
     * @param caseSensitive   true if the predicate should be case-sensitive, false
     * @return a {@link jakarta.persistence.criteria.Predicate} which is the result
     *         of the like operation
     */
    public static Predicate parseStartsPredicate(CriteriaBuilder criteriaBuilder, Expression<String> exp, String value,
            boolean caseSensitive, boolean like) {
        String finalValue = escapeLikeWildcards(value).concat("%");
        return finalLikeSensitive(criteriaBuilder, exp, finalValue, caseSensitive, like);
    }

    /**
     * Creates a predicate for the given expression and value using the like
     * operator.
     * <p>
     * This method is used to create an 'ends with' predicate. The value is
     * concatenated with a '%' character to match any string that ends with the
     * given value. LIKE special characters in the value ({@code %}, {@code _} and the
     * LIKE escape character {@code \}) are escaped, so they are matched literally.
     * </p>
     *
     * @param criteriaBuilder criteriaBuilder of the query
     * @param exp             expression to apply the predicate
     * @param value           the value to match against
     * @param caseSensitive   true if the predicate should be case-sensitive, false
     *                        otherwise
     * @return a {@link jakarta.persistence.criteria.Predicate} which is the result
     *         of the like operation
     */
    public static Predicate parseEndsPredicate(CriteriaBuilder criteriaBuilder, Expression<String> exp, String value,
            boolean caseSensitive, boolean like) {
        String finalValue = "%".concat(escapeLikeWildcards(value));
        return finalLikeSensitive(criteriaBuilder, exp, finalValue, caseSensitive, like);
    }

    /**
     * Escapes the LIKE special characters of a raw user value so the LIKE operator
     * matches them literally: the LIKE escape character ({@code \}) is doubled, and
     * the {@code %} and {@code _} wildcards are prefixed with the escape character.
     * <p>
     * The backslash must be escaped first, otherwise the escapes added for the other
     * characters would be escaped twice.
     * <p>
     * The backslash is used because it is the default escape character of the main
     * relational databases (PostgreSQL, H2...), and it is the same character that
     * {@link #finalLikeSensitive} passes explicitly to the JPA like operations, so
     * the escaped pattern is portable.
     *
     * @param value raw value coming from the user input
     * @return the value with the LIKE escape character, the {@code %} and {@code _}
     *         wildcards escaped
     */
    private static String escapeLikeWildcards(String value) {
        return value.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }

    /**
     * Creates a predicate for the given expression and value using the like
     * operator.
     * <p>
     * The pattern is evaluated with the JPA default escape character ({@code \}),
     * so {@code \%}, {@code \_} and {@code \\} inside it are matched literally, while
     * an unescaped {@code %} or {@code _} keeps the wildcard behavior.
     * <p>
     * The escape character is passed explicitly to the JPA operations, because the
     * 2-argument like is rendered by some Hibernate versions without any usable
     * escape (they emit "escape ''", which deactivates the database default escape).
     *
     * @param criteriaBuilder criteriaBuilder of the query
     * @param exp             expression to apply the predicate
     * @param value           value to match against
     * @param caseSensitive   true if the predicate should be case-sensitive, false
     *                        otherwise
     * @return a {@link jakarta.persistence.criteria.Predicate} object
     */
    public static Predicate finalLikeSensitive(CriteriaBuilder criteriaBuilder, Expression<String> exp, String value,
            boolean caseSensitive, boolean like) {

        if (caseSensitive) {
            LOGGER.trace("Case sensitive true on like expression");
            if (like) {
                LOGGER.trace("Like is true on like expression");
                return criteriaBuilder.like(exp, value, LIKE_ESCAPE_CHAR);
            }
            return criteriaBuilder.notLike(exp, value, LIKE_ESCAPE_CHAR);
        }

        if (like) {
            LOGGER.trace("Case sensitive false on like expression");
            return criteriaBuilder.like(criteriaBuilder.lower(exp), value.toLowerCase(LocaleContextHolder.getLocale()),
                    LIKE_ESCAPE_CHAR);
        }

        LOGGER.trace("Case sensitive false on not like expression");
        return criteriaBuilder.notLike(criteriaBuilder.lower(exp), value.toLowerCase(LocaleContextHolder.getLocale()),
                LIKE_ESCAPE_CHAR);
    }

    /**
     * Create a predicate for array functions
     *
     * @param path          path to the field
     * @param cb            criteria builder of the query
     * @param match         the match object
     * @param index         the index of the parsed value
     * @param arrayFunction the array function to use
     * @param mlContext     multi value map context of all the parsed values
     * @param retValue      true if the function should return true, false otherwise
     * @return a {@link jakarta.persistence.criteria.Predicate} object
     */
    public static Predicate defaultArrayPredicate(Path<?> path, CriteriaBuilder cb, QFElementMatch match, int index,
            ArrayFunction arrayFunction, MultiValueMap<String, Object> mlContext, boolean retValue) {

        match.parsedValues(index).forEach(e -> mlContext.add(match.getDefinition().getFilterName(), e));

        return cb.equal(
                cb.function(arrayFunction.getName(), Boolean.class, joinExp(cb, path, match.parsedValues(index))),
                retValue);
    }

}
