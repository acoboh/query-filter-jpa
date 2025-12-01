package io.github.acoboh.query.filter.jpa.hints;

import java.util.Set;

import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.ReflectionHints;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportRuntimeHints;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.type.filter.AnnotationTypeFilter;

import io.github.acoboh.query.filter.jpa.annotations.EnableQueryFilter;
import io.github.acoboh.query.filter.jpa.annotations.QFDefinitionClass;

/**
 * <p>
 * HintsRegistrarDef class.
 * </p>
 *
 * @author Adrián Cobo
 */
@Configuration
@ImportRuntimeHints(HintsRegistrarDef.QFRuntimeHintsRegistrar.class)
public class HintsRegistrarDef {

	HintsRegistrarDef() {
		// Empty default constructor
	}

	static class QFRuntimeHintsRegistrar implements RuntimeHintsRegistrar {

		private static final Logger LOGGER = LoggerFactory.getLogger(QFRuntimeHintsRegistrar.class);

		@Override
		public void registerHints(@NonNull RuntimeHints hints, ClassLoader classLoader) {
			registerManualHints(hints);
			processQFClasses(classLoader, hints.reflection());
		}

		private void registerManualHints(RuntimeHints hints) {
			var rh = hints.reflection();
			// Add for security
			rh.registerType(jakarta.servlet.http.HttpServletRequest.class, MemberCategory.values());
			hints.proxies().registerJdkProxy(jakarta.servlet.http.HttpServletRequest.class);

			rh.registerType(jakarta.servlet.http.HttpServletResponse.class, MemberCategory.values());
			hints.proxies().registerJdkProxy(jakarta.servlet.http.HttpServletResponse.class);

			hints.resources().registerPattern("queryfilter-messages/messages_*.properties");
		}

		private void processQFClasses(ClassLoader classLoader, ReflectionHints rh) {
			// Create a component provider that includes non-infrastructure classes
			var scanner = new ClassPathScanningCandidateComponentProvider(false);

			// Add filter to detect classes annotated with EnableQueryFilter
			scanner.addIncludeFilter(new AnnotationTypeFilter(EnableQueryFilter.class));
			scanner.setResourceLoader(new PathMatchingResourcePatternResolver(classLoader));

			// Find candidate components
			Set<BeanDefinition> candidates = scanner.findCandidateComponents("");

			LOGGER.info("Found {} classes annotated with EnableQueryFilter", candidates.size());

			for (BeanDefinition beanDefinition : candidates) {
				try {
					Class<?> clazz = Class.forName(beanDefinition.getBeanClassName(), false, classLoader);
					LOGGER.info("Processing EnableQueryFilter annotated class {}", clazz.getName());
					EnableQueryFilter qfAnnotation = clazz.getAnnotation(EnableQueryFilter.class);
					if (qfAnnotation == null) {
						continue;
					}
					for (var pack : qfAnnotation.basePackages()) {
						LOGGER.info("Processing package from annotation EnableQueryFilter {}", pack);
						registerClassesInPackage(pack, rh, classLoader);
					}

					for (var packClass : qfAnnotation.basePackageClasses()) {
						LOGGER.info("Processing package class from annotation EnableQueryFilter {}",
								packClass.getName());
						registerClassesInPackage(packClass.getPackageName(), rh, classLoader);
					}

				} catch (ClassNotFoundException e) {
					LOGGER.error("Could not find class {}", beanDefinition.getBeanClassName(), e);
				}
			}
		}

		private void registerClassesInPackage(String basePackage, ReflectionHints rh, ClassLoader classLoader) {
			var scanner = new ClassPathScanningCandidateComponentProvider(false);
			scanner.addIncludeFilter(new AnnotationTypeFilter(QFDefinitionClass.class));
			scanner.setResourceLoader(new PathMatchingResourcePatternResolver(classLoader));

			Set<BeanDefinition> candidates = scanner.findCandidateComponents(basePackage);

			for (BeanDefinition beanDefinition : candidates) {
				try {
					Class<?> clazz = Class.forName(beanDefinition.getBeanClassName(), false, classLoader);
					LOGGER.info("Processing class {}", clazz.getName());
					rh.registerType(clazz);
				} catch (ClassNotFoundException e) {
					LOGGER.error("Could not find class {}", beanDefinition.getBeanClassName(), e);
				}
			}
		}
	}
}
