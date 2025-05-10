package io.github.acoboh.query.filter.jpa.processor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit.jupiter.web.SpringJUnitWebConfig;
import org.springframework.test.context.web.WebAppConfiguration;

import io.github.acoboh.query.filter.jpa.domain.FilterAllowedOperationsBlogDef;
import io.github.acoboh.query.filter.jpa.exceptions.QFOperationNotAllowed;
import io.github.acoboh.query.filter.jpa.model.PostBlog;
import io.github.acoboh.query.filter.jpa.operations.QFOperationEnum;
import io.github.acoboh.query.filter.jpa.spring.SpringIntegrationTestBase;

@SpringJUnitWebConfig(SpringIntegrationTestBase.Config.class)
@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class OperationAllowedTests {

	private static final Set<QFOperationEnum> ALLOWED_OPERATIONS = Set.of(QFOperationEnum.EQUAL,
			QFOperationEnum.STARTS_WITH, QFOperationEnum.ENDS_WITH, QFOperationEnum.LIKE);

	@Autowired
	private QFProcessor<FilterAllowedOperationsBlogDef, PostBlog> qfProcessor;

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
				QFOperationNotAllowed ex = assertThrows(QFOperationNotAllowed.class,
						() -> qfProcessor.newQueryFilter("author=" + op.getValue() + ":a", QFParamType.RHS_COLON));
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
