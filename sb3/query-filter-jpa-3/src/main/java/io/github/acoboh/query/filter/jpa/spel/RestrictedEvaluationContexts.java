package io.github.acoboh.query.filter.jpa.spel;

import org.jspecify.annotations.Nullable;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.expression.AccessException;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.MethodExecutor;
import org.springframework.expression.MethodResolver;
import org.springframework.expression.PropertyAccessor;
import org.springframework.expression.TypedValue;
import org.springframework.expression.spel.SpelEvaluationException;
import org.springframework.expression.spel.SpelMessage;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.util.List;
import java.util.Set;

/**
 * Hardening of {@link StandardEvaluationContext} instances created by third
 * parties (e.g. Spring Security expression handlers).
 * <p>
 * Filter SpEL expressions only come from trusted code (definition classes or the
 * programmatic API), but this is applied as defense in depth: it removes the
 * features of SpEL that allow escaping to arbitrary Java code.
 *
 * @author Adrián Cobo
 */
final class RestrictedEvaluationContexts {

    private static final Set<String> BLOCKED_NAMES = Set.of("getClass", "class", "classLoader", "getClassLoader",
            "forName", "getRuntime", "exec", "loadClass", "invoke", "newInstance");

    private static final List<String> BLOCKED_PACKAGES = List.of("java.lang.reflect.", "java.lang.invoke.",
            "java.lang.ProcessBuilder", "java.lang.Runtime", "java.lang.System", "java.lang.Thread",
            "java.lang.ClassLoader", "java.lang.Module", "sun.", "jdk.internal.");

    private RestrictedEvaluationContexts() {
        // Utility class
    }

    /**
     * Restricts the evaluation context in place when it is a
     * {@link StandardEvaluationContext}. Other implementations are returned as they
     * are (e.g. {@code SimpleEvaluationContext} is already restricted).
     *
     * @param context context to harden
     * @return the same context
     */
    static EvaluationContext restrict(EvaluationContext context) {
        if (context instanceof StandardEvaluationContext standard) {
            standard.setTypeLocator(typeName -> {
                throw new SpelEvaluationException(SpelMessage.TYPE_NOT_FOUND, typeName);
            });
            standard.setConstructorResolvers(List.of());
            standard.setMethodResolvers(List.of(new RestrictedMethodResolver(standard.getMethodResolvers())));
            standard.setPropertyAccessors(standard.getPropertyAccessors().stream()
                    .<PropertyAccessor>map(RestrictedPropertyAccessor::new).toList());
        }
        return context;
    }

    private static boolean isBlocked(@Nullable Object target, String name) {
        if (BLOCKED_NAMES.contains(name)) {
            return true;
        }
        if (target == null) {
            return false;
        }
        if (target instanceof Class<?> || target instanceof ClassLoader || target instanceof Module) {
            return true;
        }
        String className = target.getClass().getName();
        return BLOCKED_PACKAGES.stream().anyMatch(className::startsWith);
    }

    private record RestrictedMethodResolver(List<MethodResolver> delegates) implements MethodResolver {

        private RestrictedMethodResolver(List<MethodResolver> delegates) {
            this.delegates = List.copyOf(delegates);
        }

        @Override
        public @Nullable MethodExecutor resolve(EvaluationContext context, Object targetObject, String name,
                List<TypeDescriptor> argumentTypes) throws AccessException {
            if (isBlocked(targetObject, name)) {
                return null;
            }
            for (MethodResolver delegate : delegates) {
                MethodExecutor executor = delegate.resolve(context, targetObject, name, argumentTypes);
                if (executor != null) {
                    return executor;
                }
            }
            return null;
        }
    }

    private record RestrictedPropertyAccessor(PropertyAccessor delegate) implements PropertyAccessor {

        @Override
        public Class<?> @Nullable [] getSpecificTargetClasses() {
            return delegate.getSpecificTargetClasses();
        }

        @Override
        public boolean canRead(EvaluationContext context, @Nullable Object target, String name)
                throws AccessException {
            return !isBlocked(target, name) && delegate.canRead(context, target, name);
        }

        @Override
        public TypedValue read(EvaluationContext context, @Nullable Object target, String name)
                throws AccessException {
            if (isBlocked(target, name)) {
                throw new AccessException("Property '" + name + "' is not accessible in query filter expressions");
            }
            return delegate.read(context, target, name);
        }

        @Override
        public boolean canWrite(EvaluationContext context, @Nullable Object target, String name) {
            return false; // Filter expressions are read-only
        }

        @Override
        public void write(EvaluationContext context, @Nullable Object target, String name, @Nullable Object newValue)
                throws AccessException {
            throw new AccessException("Query filter expressions are read-only");
        }
    }
}
