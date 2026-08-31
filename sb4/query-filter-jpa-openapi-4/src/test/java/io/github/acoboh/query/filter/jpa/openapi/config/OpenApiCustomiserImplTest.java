package io.github.acoboh.query.filter.jpa.openapi.config;

import io.github.acoboh.query.filter.jpa.annotations.QFMultiParam;
import io.github.acoboh.query.filter.jpa.annotations.QFParam;
import io.github.acoboh.query.filter.jpa.openapi.domain.PostFilterDef;
import io.github.acoboh.query.filter.jpa.openapi.model.Post;
import io.github.acoboh.query.filter.jpa.openapi.spring.OpenApiIntegrationTestBase;
import io.github.acoboh.query.filter.jpa.processor.QueryFilter;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit.jupiter.web.SpringJUnitWebConfig;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests the OpenAPI documentation customizer ({@link OpenApiCustomiserImpl},
 * resolved here through its public {@link OpenApiCustomizer} contract) against
 * a real {@link io.github.acoboh.query.filter.jpa.processor.QFProcessor} built
 * from {@link PostFilterDef}, covering both {@code @QFParam} and
 * {@code @QFMultiParam} controller endpoints.
 *
 * @author Adrián Cobo
 */
@SpringJUnitWebConfig(OpenApiIntegrationTestBase.Config.class)
@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@Import(OpenApiCustomiserImplTest.TestController.class)
class OpenApiCustomiserImplTest {

    @Autowired
    private OpenApiCustomizer openApiCustomizer;

    @RestController
    static class TestController {

        @GetMapping("/posts")
        String getPostsSingle(
                @QFParam(PostFilterDef.class) @RequestParam(name = "filter", required = false, defaultValue = "") QueryFilter<Post> filter) {
            return "ok";
        }

        @GetMapping("/posts-multi")
        String getPostsMulti(@QFMultiParam(PostFilterDef.class) QueryFilter<Post> filter) {
            return "ok";
        }
    }

    /**
     * Builds a placeholder {@link OpenAPI} document with both controller endpoints
     * already present (each with a single "filter" query parameter placeholder, as
     * springdoc would produce before this library's customizer runs), since
     * {@link OpenApiCustomizer#customise(OpenAPI)} processes every
     * {@code @QFParam}/ {@code @QFMultiParam} handler method registered in the
     * shared test context regardless of which endpoint a given test cares about.
     */
    private static OpenAPI fakeOpenApi() {
        Paths paths = new Paths();
        for (String path : new String[] { "/posts", "/posts-multi" }) {
            Parameter placeholder = new Parameter();
            placeholder.setName("filter");
            placeholder.setIn("query");

            Operation operation = new Operation();
            operation.addParametersItem(placeholder);

            PathItem pathItem = new PathItem();
            pathItem.setGet(operation);

            paths.put(path, pathItem);
        }

        OpenAPI openApi = new OpenAPI();
        openApi.setPaths(paths);
        return openApi;
    }

    @Test
    @DisplayName("@QFParam: overrides the wrapped filter parameter's description and forces a string schema")
    void qfParamDescribesFilterableFieldsAndForcesStringSchema() {
        OpenAPI openApi = fakeOpenApi();

        openApiCustomizer.customise(openApi);

        Parameter param = openApi.getPaths().get("/posts").getGet().getParameters().get(0);

        assertThat(param.getName()).isEqualTo("filter");

        Schema<?> schema = param.getSchema();
        assertThat(schema.getType()).isEqualTo("string");

        String description = param.getDescription();
        assertThat(description).contains("RHS Colon");
        // Plain element with restricted operations
        assertThat(description).contains("title").contains("eq").contains("like");
        // Enum element
        assertThat(description).contains("postType").contains("Enum values").contains("TEXT").contains("VIDEO");
        // Sortable-only element
        assertThat(description).contains("lastUpdateSortable").contains("Sortable");
        // Blocked element must never be documented
        assertThat(description).doesNotContain("published");
    }

    @Test
    @DisplayName("@QFMultiParam: replaces the wrapped filter parameter with one parameter per field, plus sort")
    void qfMultiParamGeneratesOneParameterPerFieldAndSortParam() {
        OpenAPI openApi = fakeOpenApi();

        openApiCustomizer.customise(openApi);

        Operation operation = openApi.getPaths().get("/posts-multi").getGet();

        assertThat(operation.getParameters()).extracting(Parameter::getName).contains("title", "postType", "sort")
                .doesNotContain("filter", "lastUpdateSortable", "published");

        Parameter sortParam = operation.getParameters().stream().filter(p -> "sort".equals(p.getName())).findFirst()
                .orElseThrow();
        assertThat(sortParam.getDescription()).contains("lastUpdateSortable");
        assertThat(sortParam.getExplode()).isTrue();

        Parameter titleParam = operation.getParameters().stream().filter(p -> "title".equals(p.getName())).findFirst()
                .orElseThrow();
        assertThat(titleParam.getDescription()).contains("eq").contains("like");
        assertThat(titleParam.getExplode()).isTrue();
        assertThat(titleParam.getIn()).isEqualTo("query");

        assertThat(operation.getDescription()).contains("RHS Colon");
    }
}
