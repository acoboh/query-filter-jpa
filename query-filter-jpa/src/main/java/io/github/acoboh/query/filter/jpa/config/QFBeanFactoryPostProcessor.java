package io.github.acoboh.query.filter.jpa.config;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.reflections.Reflections;
import org.reflections.util.ConfigurationBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.ResolvableType;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.util.Assert;

import io.github.acoboh.query.filter.jpa.annotations.EnableQueryFilter;
import io.github.acoboh.query.filter.jpa.annotations.QFDefinitionClass;
import io.github.acoboh.query.filter.jpa.exceptions.definition.QueryFilterDefinitionException;
import io.github.acoboh.query.filter.jpa.processor.QFProcessor;

/**
 * Query filter bean factory post processor for QueryFilter custom beans
 *
 * @author Adrián Cobo
 * 
 */
@Configuration
public class QFBeanFactoryPostProcessor implements ApplicationContextAware, BeanFactoryPostProcessor, Ordered {

	private static final Logger LOGGER = LoggerFactory.getLogger(QFBeanFactoryPostProcessor.class);

	private ApplicationContext applicationContext;

	/** {@inheritDoc} */
	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		Assert.notNull(applicationContext, "ApplicationContext cannot be null");
		this.applicationContext = applicationContext;
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

	private <T extends Annotation> List<String> getBeansWithAnnotation(Class<T> annotation, boolean repeatable,
			SupplierPackages<T> supplier) {

		List<String> packages = new ArrayList<>();

		applicationContext.getBeansWithAnnotation(annotation).forEach((name, instance) -> {

			if (!repeatable) {
				T scan = AnnotatedElementUtils.findMergedAnnotation(instance.getClass(), annotation);
				if (scan != null) {
					packages.addAll(supplier.getPackages(scan, instance));
				}
			} else {
				Set<T> scans = AnnotatedElementUtils.findMergedRepeatableAnnotations(instance.getClass(), annotation);

				for (T scan : scans) {
					packages.addAll(supplier.getPackages(scan, instance));
				}
			}

		});

		LOGGER.debug("Getting beans with annotation {}: {}", annotation, packages);

		return packages;

	}

	/** {@inheritDoc} */
	@Override
	public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
		Assert.notNull(beanFactory, "beanFactory cannot be null");

		LOGGER.info("Configure all query filter definition classes...");

		List<String> packagesToAnalyze = getBeansWithAnnotation(EnableQueryFilter.class, false, (scan, instance) -> {
			List<String> scanPackages = new ArrayList<>();
			scanPackages.addAll(Arrays.asList(scan.basePackages()));
			scanPackages.addAll(Stream.of(scan.basePackageClasses()).map(e -> e.getPackage().getName())
					.collect(Collectors.toList()));
			return scanPackages;
		});

		if (packagesToAnalyze.isEmpty()) {
			LOGGER.debug("Trying get component scan beans to search for QueryFilter classes...");
			packagesToAnalyze = getBeansWithAnnotation(ComponentScan.class, true, (scan, instance) -> {
				List<String> scanPackages = new ArrayList<>();
				scanPackages.addAll(Arrays.asList(scan.basePackages()));
				scanPackages.addAll(Stream.of(scan.basePackageClasses()).map(e -> e.getPackage().getName())
						.collect(Collectors.toList()));
				return scanPackages;

			});
		}

		if (packagesToAnalyze.isEmpty()) {
			LOGGER.debug("Trying get SpringBootApplication beans to search for QueryFilter classes...");
			packagesToAnalyze = getBeansWithAnnotation(SpringBootApplication.class, false,
					(scan, instance) -> Arrays.asList(instance.getClass().toString()));

		}

		Set<Class<?>> classSet = getClassAnnotated(packagesToAnalyze, QFDefinitionClass.class);

		Map<Class<?>, QFProcessor<?, ?>> mapProcessors = new HashMap<>();

		for (Class<?> cl : classSet) {
			try {
				QFProcessor<?, ?> qfp = registerQueryFilterClass(cl, beanFactory);
				mapProcessors.put(cl, qfp);
			} catch (QueryFilterDefinitionException e) {
				throw new BeanCreationException("Error creating bean query filter for class " + cl.getName(), e);
			}
		}

	}

	private QFProcessor<?, ?> registerQueryFilterClass(Class<?> cl, ConfigurableListableBeanFactory beanFactory)
			throws QueryFilterDefinitionException {

		String beanName = cl.getName() + "queryFilterBean";

		QFDefinitionClass annotationClass = cl.getAnnotation(QFDefinitionClass.class);
		if (annotationClass == null) {
			LOGGER.warn("The class {} missing annotation QueryFilterClass", cl);
			return null;
		}

		ResolvableType resolvableType = ResolvableType.forClassWithGenerics(QFProcessor.class, cl,
				annotationClass.value());
		RootBeanDefinition beanDefinition = new RootBeanDefinition();
		beanDefinition.setTargetType(resolvableType);
		beanDefinition.setAutowireMode(AbstractBeanDefinition.AUTOWIRE_BY_TYPE);
		beanDefinition.setAutowireCandidate(true);

		DefaultListableBeanFactory bf = (DefaultListableBeanFactory) beanFactory;

		try {
			bf.registerBeanDefinition(beanName, beanDefinition);
			QFProcessor<?, ?> ret = new QFProcessor<>(cl, annotationClass.value(), applicationContext);
			bf.registerSingleton(beanName, ret);
			return ret;
		} catch (QueryFilterDefinitionException e) {
			LOGGER.error("Error registering bean query filter of class {} for entity class {}", cl,
					annotationClass.value());
			throw e;
		}

	}

	/** {@inheritDoc} */
	@Override
	public int getOrder() {
		return 0;
	}

	@FunctionalInterface
	interface SupplierPackages<T extends Annotation> {
		List<String> getPackages(T annotation, Object instance);
	}

}
