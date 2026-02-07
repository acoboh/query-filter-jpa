package io.github.acoboh.query.filter.jpa.converters;

import io.github.acoboh.query.filter.jpa.annotations.QFParam;
import io.github.acoboh.query.filter.jpa.domain.FilterBlogDef;
import io.github.acoboh.query.filter.jpa.model.PostBlog;
import io.github.acoboh.query.filter.jpa.operations.QFOperationEnum;
import io.github.acoboh.query.filter.jpa.processor.QueryFilter;
import io.github.acoboh.query.filter.jpa.spring.SpringIntegrationTestBase;
import org.jspecify.annotations.NonNull;
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

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test for spring converters
 *
 * @author Adrián Cobo
 */
@SpringJUnitWebConfig(SpringIntegrationTestBase.Config.class)
@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ConverterTests {

    @Autowired
    private ConversionService conversionService;

    private static class AnnotationClass {

        @SuppressWarnings("unused")
        public static void paramQueryFilterBlogDef(
                @QFParam(FilterBlogDef.class) QueryFilter<@NonNull PostBlog> filter) {
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

        Object converted = conversionService.convert("author=eq:a", sourceDescriptor, targetDescriptor);
        assertThat(converted).isNotNull();
        assertThat(converted.getClass()).isAssignableFrom(QueryFilter.class);

        QueryFilter<?> queryFilter = (QueryFilter<?>) converted;

        assertThat(queryFilter.getEntityClass()).isEqualTo(PostBlog.class);
        assertThat(queryFilter.getPredicateClass()).isEqualTo(FilterBlogDef.class);

        assertThat(queryFilter.getInitialInput()).isEqualTo("author=eq:a");

        var elemOperation = queryFilter.getFirstActualElementOperation("author");
        assertThat(elemOperation).isNotNull();

        assertThat(elemOperation.getFirst()).isEqualTo(QFOperationEnum.EQUAL);
        assertThat(elemOperation.getSecond()).containsExactly("a");

    }

    private static class AnnotationClassBase64 {

        @SuppressWarnings("unused")
        public static void paramQueryFilterBlogDef(
                @QFParam(value = FilterBlogDef.class, base64Encoded = true) QueryFilter<@NonNull PostBlog> filter) {
            // For custom annotations
        }
    }

    @Test
    @DisplayName("2. Test conversion filter to query filter FilterBlogDef with base64 encoding")
    void testConversionFilterToQueryFilterBase64() throws NoSuchMethodException, SecurityException {

        String toBase64 = Base64.getEncoder().encodeToString("author=eq:a".getBytes());

        Method method = AnnotationClassBase64.class.getDeclaredMethod("paramQueryFilterBlogDef", QueryFilter.class);
        Annotation[] annotations = method.getParameters()[0].getAnnotations();

        TypeDescriptor sourceDescriptor = new TypeDescriptor(ResolvableType.forClass(String.class), String.class, null);

        ResolvableType type = ResolvableType.forClassWithGenerics(QueryFilter.class, PostBlog.class);

        TypeDescriptor targetDescriptor = new TypeDescriptor(type, QueryFilter.class, annotations);

        Object converted = conversionService.convert(toBase64, sourceDescriptor, targetDescriptor);
        assertThat(converted).isNotNull();
        assertThat(converted.getClass()).isAssignableFrom(QueryFilter.class);

        QueryFilter<?> queryFilter = (QueryFilter<?>) converted;

        assertThat(queryFilter.getEntityClass()).isEqualTo(PostBlog.class);
        assertThat(queryFilter.getPredicateClass()).isEqualTo(FilterBlogDef.class);

        assertThat(queryFilter.getInitialInput()).isEqualTo("author=eq:a");

        var elemOperation = queryFilter.getFirstActualElementOperation("author");
        assertThat(elemOperation).isNotNull();

        assertThat(elemOperation.getFirst()).isEqualTo(QFOperationEnum.EQUAL);
        assertThat(elemOperation.getSecond()).containsExactly("a");

    }

}
