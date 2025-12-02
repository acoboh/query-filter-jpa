package io.github.acoboh.query.filter.jpa.processor;

import io.github.acoboh.query.filter.jpa.domain.FilterAllowedOperationsBlogDef;
import io.github.acoboh.query.filter.jpa.exceptions.QFOperationNotAllowed;
import io.github.acoboh.query.filter.jpa.model.PostBlog;
import io.github.acoboh.query.filter.jpa.operations.QFOperationEnum;
import io.github.acoboh.query.filter.jpa.spring.SpringIntegrationTestBase;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit.jupiter.web.SpringJUnitWebConfig;
import org.springframework.test.context.web.WebAppConfiguration;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringJUnitWebConfig(SpringIntegrationTestBase.Config.class)
@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class OperationAllowedTests {

    private static final Set<QFOperationEnum> ALLOWED_OPERATIONS = Set.of(QFOperationEnum.EQUAL,
            QFOperationEnum.STARTS_WITH, QFOperationEnum.ENDS_WITH, QFOperationEnum.LIKE);

    @Autowired
    private QFProcessor<@NonNull FilterAllowedOperationsBlogDef, @NonNull PostBlog> qfProcessor;

    @Test
    @DisplayName("0. Setup")
    @Order(Ordered.HIGHEST_PRECEDENCE)
    void setup() {
        assertThat(qfProcessor).isNotNull();
    }

    @Test
    @DisplayName("1. Test by author allowed operations")
    @Order(1)
    void testAllowedOperations() {

        for (QFOperationEnum op : ALLOWED_OPERATIONS) {
            assertThat(qfProcessor.newQueryFilter("author=" + op.getValue() + ":a", QFParamType.RHS_COLON)).isNotNull();
        }

    }

    @Test
    @DisplayName("2. Test by author not allowed operations")
    @Order(2)
    void testNotAllowed() {

        for (QFOperationEnum op : QFOperationEnum.values()) {
            if (!ALLOWED_OPERATIONS.contains(op)) {
                String filter = "author=" + op.getValue() + ":a";
                QFOperationNotAllowed ex = assertThrows(QFOperationNotAllowed.class,
                        () -> qfProcessor.newQueryFilter(filter, QFParamType.RHS_COLON));
                assertThat(ex).isNotNull();
                assertThat(ex.getMessage()).isNotNull();
                assertThat(ex.getMessage())
                        .contains("The operation '" + op.getValue() + "' is not allowed for the field 'author'");
                assertThat(ex.getOperation()).isEqualTo(op.getValue());
                assertThat(ex.getField()).isEqualTo("author");
            }
        }

    }

}
