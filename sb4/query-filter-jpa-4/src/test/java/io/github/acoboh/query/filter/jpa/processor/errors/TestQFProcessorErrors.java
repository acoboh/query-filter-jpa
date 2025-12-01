package io.github.acoboh.query.filter.jpa.processor.errors;

import io.github.acoboh.query.filter.jpa.exceptions.definition.QFDiscriminatorException;
import io.github.acoboh.query.filter.jpa.exceptions.definition.QueryFilterDefinitionException;
import io.github.acoboh.query.filter.jpa.filtererrors.discriminator.DiscriminatorFilterErrorDef;
import io.github.acoboh.query.filter.jpa.filtererrors.discriminator.OperationAllowedError1Def;
import io.github.acoboh.query.filter.jpa.model.PostBlog;
import io.github.acoboh.query.filter.jpa.model.discriminators.Topic;
import io.github.acoboh.query.filter.jpa.processor.QFProcessor;
import io.github.acoboh.query.filter.jpa.spring.SpringIntegrationTestBase;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

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

    @Test
    @DisplayName("2. Test QFProcessor QFElement with not allowed operation")
    void testQFElementNotAllowedOperation() {

        QueryFilterDefinitionException ex = assertThrows(QueryFilterDefinitionException.class, () -> {
            new QFProcessor<>(OperationAllowedError1Def.class, PostBlog.class, appContext);
        });

        assertThat(ex.getMessage()).isEqualTo(
                "Allowed operations [ENDS_WITH] not valid for class int on field likes of filter class io.github.acoboh.query.filter.jpa.filtererrors.discriminator.OperationAllowedError1Def");
    }
}
