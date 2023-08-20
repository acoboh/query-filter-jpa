package io.github.acoboh.query.filter.jpa.converters;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ResolvableType;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit.jupiter.web.SpringJUnitWebConfig;
import org.springframework.test.context.web.WebAppConfiguration;

import io.github.acoboh.query.filter.jpa.annotations.QFParam;
import io.github.acoboh.query.filter.jpa.domain.FilterBlogDef;
import io.github.acoboh.query.filter.jpa.model.PostBlog;
import io.github.acoboh.query.filter.jpa.processor.QueryFilter;
import io.github.acoboh.query.filter.jpa.spring.SpringIntegrationTest;

/**
 * Test for spring converters
 * 
 * @author Adrián Cobo
 *
 */
@SpringJUnitWebConfig(SpringIntegrationTest.Config.class)
@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ConverterTests {

	@Autowired
	private ConversionService conversionService;

	private static class AnnotationClass {

		@SuppressWarnings("unused")
		public static void paramQueryFilterBlogDef(@QFParam(FilterBlogDef.class) QueryFilter<PostBlog> filter) {
			// For custom annotations
		}

	}

	@Test
	@DisplayName("1. Test conversion filter to query filter FilterBlogDef")
	void testConversionFilterToQueryFilter() throws NoSuchMethodException, SecurityException {

		Method method = AnnotationClass.class.getDeclaredMethod("paramQueryFilterBlogDef", QueryFilter.class);
		Annotation[] annotations = method.getParameters()[0].getAnnotations();

		TypeDescriptor sourceDescriptor = new TypeDescriptor(ResolvableType.forClass(String.class), String.class, null);

		ResolvableType type = ResolvableType.forClassWithGenerics(QueryFilter.class, PostBlog.class);

		TypeDescriptor targetDescriptor = new TypeDescriptor(type, QueryFilter.class, annotations);

		Object converted = conversionService.convert("", sourceDescriptor, targetDescriptor);

		assertThat(converted.getClass()).isAssignableFrom(QueryFilter.class);

		QueryFilter<?> queryFilter = (QueryFilter<?>) converted;

		assertThat(queryFilter.getEntityClass()).isEqualTo(PostBlog.class);
		assertThat(queryFilter.getPredicateClass()).isEqualTo(FilterBlogDef.class);

	}

}
