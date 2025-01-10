package io.github.acoboh.query.filter.jpa.processor.errors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit.jupiter.web.SpringJUnitWebConfig;
import org.springframework.test.context.web.WebAppConfiguration;

import io.github.acoboh.query.filter.jpa.exceptions.definition.QFDiscriminatorException;
import io.github.acoboh.query.filter.jpa.filtererrors.discriminator.DiscriminatorFilterErrorDef;
import io.github.acoboh.query.filter.jpa.model.discriminators.Topic;
import io.github.acoboh.query.filter.jpa.processor.QFProcessor;
import io.github.acoboh.query.filter.jpa.spring.SpringIntegrationTestBase;

@SpringJUnitWebConfig(SpringIntegrationTestBase.Config.class)
@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class TestQFProcessorErrors {

	@Autowired
	private ApplicationContext appContext;

	@Test
	@DisplayName("1. Test QFProcessor Discriminator with not inherited class")
	void testDiscriminatorNotInherited() {

		QFDiscriminatorException ex = assertThrows(QFDiscriminatorException.class, () -> {
			new QFProcessor<>(DiscriminatorFilterErrorDef.class, Topic.class, appContext);
		});

		assertThat(ex.getMessage()).isEqualTo(
				"Entity class 'class io.github.acoboh.query.filter.jpa.model.discriminators.Topic' is not assignable from value class 'class io.github.acoboh.query.filter.jpa.model.PostBlog'");
	}
}
