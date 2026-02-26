package io.github.acoboh.query.filter.jpa.controllers;

import io.github.acoboh.query.filter.jpa.annotations.QFMultiParam;
import io.github.acoboh.query.filter.jpa.annotations.QFParam;
import io.github.acoboh.query.filter.jpa.domain.FilterBlogDef;
import io.github.acoboh.query.filter.jpa.model.PostBlog;
import io.github.acoboh.query.filter.jpa.processor.QFFieldInfo;
import io.github.acoboh.query.filter.jpa.processor.QueryFilter;
import io.github.acoboh.query.filter.jpa.spring.SpringIntegrationTestBase;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit.jupiter.web.SpringJUnitWebConfig;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

@SpringJUnitWebConfig(SpringIntegrationTestBase.Config.class)
@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Import(PostBlogExampleControllerTest.TestMultiParamController.class)
class PostBlogExampleControllerTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setup(WebApplicationContext wac) {
        this.mockMvc = webAppContextSetup(wac)
                .addDispatcherServletCustomizer(ds -> ds.setDetectAllHandlerAdapters(true)).build();
    }

    @RestController
    static class TestMultiParamController {
        @GetMapping("/test")
        public List<QFFieldInfo> test(
                @QFMultiParam(value = FilterBlogDef.class, ignoreUnknown = false) QueryFilter<PostBlog> filter) {
            return filter.getAllFieldValues();
        }

        @GetMapping("/test-filter-param")
        public List<QFFieldInfo> testSingle(@RequestParam @QFParam(FilterBlogDef.class) QueryFilter<PostBlog> filter) {
            return filter.getAllFieldValues();
        }
    }

    @Test
    @DisplayName("Test multi param")
    void testMultiParam() throws Exception {
        String jsonExpected = """
                [
                  {
                    "name": "postType",
                    "operation": "eq",
                    "values": [
                      "VIDEO"
                    ]
                  },
                  {
                    "name": "author",
                    "operation": "eq",
                    "values": [
                      "acoboh"
                    ]
                  }
                ]
                """;

        mockMvc.perform( // Get request
                get("/test") //
                        .queryParam("postType", "VIDEO") //
                        .queryParam("author", "acoboh") //
        ).andDo(e -> {
            System.out.println("Response: " + e.getResponse().getContentAsString());
        }).andExpect(status().isOk()) //
                .andExpect(content().json(jsonExpected));

    }

    @Test
    @DisplayName("Test single param")
    void testSingleParam() throws Exception {
        String jsonExpected = """
                [
                  {
                    "name": "postType",
                    "operation": "eq",
                    "values": [
                      "VIDEO"
                    ]
                  },
                  {
                    "name": "author",
                    "operation": "eq",
                    "values": [
                      "acoboh"
                    ]
                  }
                ]
                """;

        mockMvc.perform( // Get request
                get("/test-filter-param") //
                        .queryParam("filter", "postType=eq:VIDEO&author=eq:acoboh") //
        ).andDo(e -> {
            System.out.println("Response: " + e.getResponse().getContentAsString());
        }).andExpect(status().isOk()) //
                .andExpect(content().json(jsonExpected));
    }

}
