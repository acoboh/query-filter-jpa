package org.query.filter.jpa.openapi.data.jpa.config;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.query.filter.jpa.openapi.data.jpa.annotations.EnableQueryFilterOpenApi;
import org.reflections.Reflections;
import org.reflections.util.ConfigurationBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.RestController;

import io.github.acoboh.query.filter.jpa.annotations.EnableQueryFilter;
import io.github.acoboh.query.filter.jpa.annotations.QFParam;
import io.github.acoboh.query.filter.jpa.processor.QFProcessor;

/**
 * Auto-Configuration class for OpenAPI standard
 *
 * @author Adrián Cobo
 * @version $Id: $Id
 */
@Configuration
public class QueryFilterOpenApiAutoconfigurer {

	private static final Logger LOGGER = LoggerFactory.getLogger(QueryFilterOpenApiAutoconfigurer.class);
	private static final ParameterNameDiscoverer PARAMETER_NAME_DISCOVERER = new DefaultParameterNameDiscoverer();

	@Bean
	OpenApiCustomiserImpl openApiCustomiser(ApplicationContext applicationContext,
			List<QFProcessor<?, ?>> filterProcessors) {

		LOGGER.info("Getting all controllers for spring-doc documentation");

		Set<Class<?>> classSetControllers = getClassAnnotated(
				getComponentScanPackageRestControllers(applicationContext), RestController.class);
		List<QFEndpoint> qfEndpoints = new ArrayList<>();

		Map<Class<?>, QFProcessor<?, ?>> mapProcessors = filterProcessors.stream()
				.collect(Collectors.toMap(e -> e.getFilterClass(), e -> e));

		for (Class<?> cl : classSetControllers) {
			qfEndpoints.addAll(processController(cl, mapProcessors));
		}

		return new OpenApiCustomiserImpl(qfEndpoints);

	}

	private List<QFEndpoint> processController(Class<?> beanClass, Map<Class<?>, QFProcessor<?, ?>> mapQfProcessor) {

		Assert.notNull(beanClass, "beanClass cannot be null");

		List<QFEndpoint> qfEndpoints = new ArrayList<>();

		LOGGER.debug("Processing controller {}", beanClass.getName());
		for (Method method : beanClass.getMethods()) {
			String paramNames[] = null;
			for (int i = 0; i < method.getParameterCount(); i++) {

				Parameter param = method.getParameters()[i];
				if (param.isAnnotationPresent(QFParam.class)) {
					QFParam qfParam = param.getAnnotation(QFParam.class);

					QFProcessor<?, ?> qfProcessor = mapQfProcessor.get(qfParam.value());
					if (qfProcessor == null) {
						throw new BeanCreationException(
								"Error creating api documentation. Missing QueryFilterProcessor of class "
										+ qfParam.value());
					}
					LOGGER.debug(param.getName());

					if (paramNames == null) {
						paramNames = PARAMETER_NAME_DISCOVERER.getParameterNames(method);
					}

					qfEndpoints
							.add(new QFEndpoint(beanClass, method, param, qfProcessor, paramNames[i], qfParam.type()));
				}

			}

		}

		return qfEndpoints;
	}

	private static Set<Class<?>> getClassAnnotated(List<String> packages, Class<? extends Annotation> annotation) {

		Assert.notNull(packages, "packages must not be null");

		Set<Class<?>> classSet = new HashSet<>();

		for (String pack : packages) {
			LOGGER.debug("Checking package with time annotated {}", pack);
			if (pack.startsWith("$")) {
				LOGGER.debug("Ignoring variable package, {}", pack);
				continue;
			}

			final String packRName = prefixPattern(pack);
			final String packBIName = prefixPattern("BOOT-INF.classes." + pack);

			LOGGER.trace("Package regex {} and {}", packRName, packBIName);

			Reflections reflect = new Reflections(new ConfigurationBuilder().forPackages(pack).filterInputsBy(p -> {
				boolean matches = p.matches(packRName) || p.matches(packBIName);
				LOGGER.trace("Pack {} matches {}", p, matches);
				return matches;
			}));

			Set<Class<?>> classFound = reflect.getTypesAnnotatedWith(annotation);

			if (LOGGER.isDebugEnabled()) {
				classFound.forEach(e -> LOGGER.debug("Adding class {} with QueryFilterClass Annotation", e));
			}

			classSet.addAll(classFound);

		}

		return classSet;
	}

	private static String prefixPattern(String fqn) {
		if (!fqn.endsWith("."))
			fqn += ".";
		return fqn.replace(".", "\\.").replace("$", "\\$") + ".*";
	}

	private static List<String> getComponentScanPackageRestControllers(ApplicationContext applicationContext) {
		List<String> scanPackages = new ArrayList<>();

		applicationContext.getBeansWithAnnotation(EnableQueryFilter.class).forEach((name, instance) -> {

			EnableQueryFilterOpenApi scan = AnnotatedElementUtils.findMergedAnnotation(instance.getClass(),
					EnableQueryFilterOpenApi.class);

			if (scan == null) {
				LOGGER.warn("No EnableQueryFilter annotation found on class {}", instance.getClass());
				return;
			}

			scanPackages.addAll(Arrays.asList(scan.basePackages()));
			scanPackages.addAll(Stream.of(scan.basePackageClasses()).map(e -> e.getPackage().getName())
					.collect(Collectors.toList()));

		});

		if (scanPackages.isEmpty()) {
			return getDefaultComponentScanPackages(applicationContext);
		}

		LOGGER.debug("Scanning annotations in packages for RestController {}", Arrays.toString(scanPackages.toArray()));
		return scanPackages;
	}

	private static List<String> getDefaultComponentScanPackages(ApplicationContext applicationContext) {

		List<String> scanPackages = new ArrayList<>();

		applicationContext.getBeansWithAnnotation(ComponentScan.class).forEach((name, instance) -> {

			Set<ComponentScan> scans = AnnotatedElementUtils.findMergedRepeatableAnnotations(instance.getClass(),
					ComponentScan.class);

			for (ComponentScan scan : scans) {
				scanPackages.addAll(Arrays.asList(scan.basePackages()));
			}

		});

		LOGGER.debug("Getting default annotation in packages for {}", Arrays.toString(scanPackages.toArray()));

		if (scanPackages.isEmpty()) {
			LOGGER.debug("Default packages were empty. Adding spring boot root bean classes");
			Map<String, Object> springBootAppBeans = applicationContext
					.getBeansWithAnnotation(SpringBootApplication.class);
			if (!springBootAppBeans.isEmpty()) {

				for (Entry<String, Object> bean : springBootAppBeans.entrySet()) {
					scanPackages.add(bean.getValue().getClass().getPackage().getName());
				}
			}

			LOGGER.debug("Added spring boot bean annotated classes {}", Arrays.toString(scanPackages.toArray()));
		}

		return scanPackages;
	}

}
