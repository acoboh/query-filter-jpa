package io.github.acoboh.query.filter.jpa.predicate;

import io.github.acoboh.query.filter.jpa.processor.definitions.QFAbstractDefinition;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import org.jspecify.annotations.Nullable;

import java.util.*;

/**
 * Parsed custom predicate expression ({@code @QFPredicate}).
 * <p>
 * Grammar (operators are case-sensitive, identifiers are filter names):
 *
 * <pre>
 * expression := term ( ( AND | OR ) term )*      (a single operator kind per level)
 * term       := IDENTIFIER | '(' expression ')'
 * </pre>
 * <p>
 * The whole expression is validated when it is built (at startup): unknown
 * identifiers, unbalanced parenthesis, misplaced operators or mixed operators on
 * the same level throw an {@link IllegalStateException}. At runtime a group only
 * disappears when none of its fields have predicates.
 *
 * @author Adrián Cobo
 */
class PredicateLevel {

    private final Node root;
    private final Set<String> fieldsSet;

    /**
     * Parse and validate a predicate expression
     *
     * @param expression predicate expression
     * @param fullMap    definitions of the filter (valid identifiers)
     * @throws IllegalStateException if the expression is not valid
     */
    public PredicateLevel(String expression, Map<String, QFAbstractDefinition> fullMap) {
        List<String> tokens = tokenize(expression);
        if (tokens.isEmpty()) {
            throw new IllegalStateException("Empty predicate expression");
        }

        Parser parser = new Parser(tokens, fullMap, expression);
        this.root = parser.parseExpression();
        if (parser.hasNext()) {
            throw invalid(expression, "unexpected '" + parser.peek() + "'");
        }

        Set<String> fields = new LinkedHashSet<>();
        root.collectFields(fields);
        this.fieldsSet = Collections.unmodifiableSet(fields);
    }

    /**
     * Get all the fields used on the expression
     *
     * @return set of filter names
     */
    public Set<String> getFilteredFields() {
        return fieldsSet;
    }

    /**
     * Resolve the expression with the predicates of the request
     *
     * @param cb         criteria builder
     * @param predicates predicates of each filtered field
     * @return the final predicate, or null if none of the fields of the expression
     *         has predicates
     */
    public @Nullable Predicate resolveLevel(CriteriaBuilder cb, Map<String, Predicate> predicates) {
        return root.resolve(cb, predicates);
    }

    // ------------------------------------------------------------------ parsing

    private static List<String> tokenize(String expression) {
        List<String> tokens = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        for (int i = 0; i < expression.length(); i++) {
            char c = expression.charAt(i);
            if (c == '(' || c == ')' || Character.isWhitespace(c)) {
                if (!current.isEmpty()) {
                    tokens.add(current.toString());
                    current.setLength(0);
                }
                if (!Character.isWhitespace(c)) {
                    tokens.add(String.valueOf(c));
                }
            } else {
                current.append(c);
            }
        }
        if (!current.isEmpty()) {
            tokens.add(current.toString());
        }
        return tokens;
    }

    private static IllegalStateException invalid(String expression, String reason) {
        return new IllegalStateException("Invalid predicate expression '" + expression + "': " + reason);
    }

    private static final class Parser {

        private final List<String> tokens;
        private final Map<String, QFAbstractDefinition> fullMap;
        private final String expression;
        private int pos;

        private Parser(List<String> tokens, Map<String, QFAbstractDefinition> fullMap, String expression) {
            this.tokens = tokens;
            this.fullMap = fullMap;
            this.expression = expression;
        }

        boolean hasNext() {
            return pos < tokens.size();
        }

        String peek() {
            return tokens.get(pos);
        }

        Node parseExpression() {
            List<Node> children = new ArrayList<>();
            children.add(parseTerm());

            PredicateOperation operator = null;
            while (hasNext() && !")".equals(peek())) {
                String token = tokens.get(pos++);
                PredicateOperation found = PredicateOperation.getOperator(token);
                if (found == null) {
                    throw invalid(expression, "expected AND/OR but found '" + token + "'");
                }
                if (operator != null && operator != found) {
                    throw invalid(expression, "mixed AND/OR on the same level. Use parenthesis");
                }
                operator = found;
                children.add(parseTerm());
            }

            return children.size() == 1 ? children.get(0) : new Group(Objects.requireNonNull(operator), children);
        }

        Node parseTerm() {
            if (!hasNext()) {
                throw invalid(expression, "unexpected end of expression");
            }
            String token = tokens.get(pos++);
            if ("(".equals(token)) {
                Node inner = parseExpression();
                if (!hasNext() || !")".equals(tokens.get(pos++))) {
                    throw invalid(expression, "unbalanced parenthesis");
                }
                return inner;
            }
            if (")".equals(token)) {
                throw invalid(expression, "unbalanced parenthesis or empty group");
            }
            if (PredicateOperation.getOperator(token) != null) {
                throw invalid(expression, "operator '" + token + "' without left operand");
            }
            if (!fullMap.containsKey(token)) {
                throw invalid(expression, "unknown filter field '" + token + "'");
            }
            return new Field(token);
        }
    }

    // -------------------------------------------------------------------- nodes

    private sealed interface Node permits Field, Group {

        @Nullable
        Predicate resolve(CriteriaBuilder cb, Map<String, Predicate> predicates);

        void collectFields(Set<String> fields);
    }

    private record Field(String name) implements Node {

        @Override
        public @Nullable Predicate resolve(CriteriaBuilder cb, Map<String, Predicate> predicates) {
            return predicates.get(name);
        }

        @Override
        public void collectFields(Set<String> fields) {
            fields.add(name);
        }
    }

    private record Group(PredicateOperation operator, List<Node> children) implements Node {

        private Group {
            children = List.copyOf(children);
        }

        @Override
        public @Nullable Predicate resolve(CriteriaBuilder cb, Map<String, Predicate> predicates) {
            List<Predicate> resolved = new ArrayList<>(children.size());
            for (Node child : children) {
                Predicate predicate = child.resolve(cb, predicates);
                if (predicate != null) {
                    resolved.add(predicate);
                }
            }
            if (resolved.isEmpty()) {
                return null; // none of the fields of this group are being filtered
            }
            if (resolved.size() == 1) {
                return resolved.get(0);
            }
            return operator.getPredicate(cb, resolved);
        }

        @Override
        public void collectFields(Set<String> fields) {
            children.forEach(child -> child.collectFields(fields));
        }
    }
}
