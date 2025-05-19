package io.github.acoboh.query.filter.jpa.config;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.ResolvableType;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.lang.NonNull;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

import io.github.acoboh.query.filter.jpa.annotations.EnableQueryFilter;
import io.github.acoboh.query.filter.jpa.annotations.QFDefinitionClass;
import io.github.acoboh.query.filter.jpa.processor.QFProcessor;

/**
 * Query filter bean factory post processor for QueryFilter custom beans
 *
 * @author Adrián Cobo
 */
@Configuration
public class QFBeanFactoryPostProcessor implements ApplicationContextAware, BeanFactoryPostProcessor, Ordered {

	private static final Logger LOGGER = LoggerFactory.getLogger(QFBeanFactoryPostProcessor.class);

	private ApplicationContext applicationContext;

	/** {@inheritDoc} */
	@Override
	public void setApplicationContext(@NonNull ApplicationContext applicationContext) throws BeansException {
		Assert.notNull(applicationContext, "ApplicationContext cannot be null");
		this.applicationContext = applicationContext;
	}

	private Set<Class<?>> getClassAnnotatedWithQFDef(List<String> packages) {

		Assert.notNull(packages, "packages must not be null");

		Set<Class<?>> classSet = new HashSet<>();

		ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
		scanner.setResourceLoader(this.applicationContext);
		scanner.addIncludeFilter(new AnnotationTypeFilter(QFDefinitionClass.class));

		ClassLoader classLoader = applicationContext.getClassLoader();

		for (String pack : packages) {
			LOGGER.debug("Checking package with time annotated {}", pack);
			if (pack.startsWith("$")) {
				LOGGER.debug("Ignoring variable package, {}", pack);
				continue;
			}

			for (BeanDefinition beanDefinition : scanner.findCandidateComponents(pack)) {
				if (beanDefinition.getBeanClassName() == null) {
					continue;
				}

				try {
					Class<?> clazz = ClassUtils.forName(beanDefinition.getBeanClassName(), classLoader);
					LOGGER.debug("Found class {} with @QFDefinitionClass annotation", clazz.getName());
					classSet.add(clazz);
				} catch (ClassNotFoundException e) {
					LOGGER.error("Could not load class {}", beanDefinition.getBeanClassName(), e);
				}
			}

		}

		return classSet;
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
	public void postProcessBeanFactory(@NonNull ConfigurableListableBeanFactory beanFactory) throws BeansException {
		Assert.isInstanceOf(ConfigurableBeanFactory.class, beanFactory, "BeanFactory must be ConfigurableBeanFactory");

		DefaultListableBeanFactory defaultListableBeanFactory = (DefaultListableBeanFactory) beanFactory;

		LOGGER.debug("Processing all query filter definition classes...");
		processMetamodel(defaultListableBeanFactory);
	}

	private void processMetamodel(DefaultListableBeanFactory beanFactory) {

		List<String> packagesToAnalyze = getBeansWithAnnotation(EnableQueryFilter.class, false, (scan, instance) -> {
			List<String> scanPackages = new ArrayList<>();
			scanPackages.addAll(Arrays.asList(scan.basePackages()));
			scanPackages.addAll(Stream.of(scan.basePackageClasses()).map(e -> e.getPackage().getName()).toList());
			return scanPackages;
		});

		if (packagesToAnalyze.isEmpty()) {
			LOGGER.debug("Trying get component scan beans to search for QueryFilter classes...");
			packagesToAnalyze = getBeansWithAnnotation(ComponentScan.class, true, (scan, instance) -> {
				List<String> scanPackages = new ArrayList<>();
				scanPackages.addAll(Arrays.asList(scan.basePackages()));
				scanPackages.addAll(Stream.of(scan.basePackageClasses()).map(e -> e.getPackage().getName()).toList());
				return scanPackages;

			});
		}

		if (packagesToAnalyze.isEmpty()) {
			LOGGER.debug("Trying get SpringBootApplication beans to search for QueryFilter classes...");
			packagesToAnalyze = getBeansWithAnnotation(SpringBootApplication.class, false,
					(scan, instance) -> List.of(instance.getClass().toString()));

		}

		Set<Class<?>> classSet = getClassAnnotatedWithQFDef(packagesToAnalyze);

		for (Class<?> cl : classSet) {
			registerQueryFilterClass(cl, beanFactory);
		}

	}

	private void registerQueryFilterClass(Class<?> cl, ConfigurableListableBeanFactory beanFactory) {

		String beanName = cl.getName() + "queryFilterBean";

		QFDefinitionClass annotationClass = cl.getAnnotation(QFDefinitionClass.class);
		if (annotationClass == null) {
			LOGGER.warn("The class {} missing annotation QueryFilterClass", cl);
			return;
		}

		RootBeanDefinition beanDefinition = new RootBeanDefinition();
		beanDefinition.setBeanClass(QFProcessor.class);

		// Create arguments constructor values
		var args = new ConstructorArgumentValues();
		args.addGenericArgumentValue(cl);
		args.addGenericArgumentValue(annotationClass.value());
		args.addGenericArgumentValue(new RuntimeBeanReference("applicationContextAwareSupport"));

		// Bean definition
		beanDefinition.setConstructorArgumentValues(args);

		ResolvableType resolvableType = ResolvableType.forClassWithGenerics(QFProcessor.class, cl,
				annotationClass.value());
		beanDefinition.setTargetType(resolvableType);

		beanDefinition.setAutowireMode(AbstractBeanDefinition.AUTOWIRE_BY_TYPE);

		DefaultListableBeanFactory bf = (DefaultListableBeanFactory) beanFactory;
		bf.registerBeanDefinition(beanName, beanDefinition);

	}

	/** {@inheritDoc} */
	@Override
	public int getOrder() {
		return Ordered.LOWEST_PRECEDENCE;
	}

	@FunctionalInterface
	interface SupplierPackages<T extends Annotation> {
		List<String> getPackages(T annotation, Object instance);
	}

}
