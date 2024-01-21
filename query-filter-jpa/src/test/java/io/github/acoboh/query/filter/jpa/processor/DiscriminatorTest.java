package io.github.acoboh.query.filter.jpa.processor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit.jupiter.web.SpringJUnitWebConfig;
import org.springframework.test.context.web.WebAppConfiguration;

import io.github.acoboh.query.filter.jpa.domain.DiscriminatorFilterDef;
import io.github.acoboh.query.filter.jpa.exceptions.QFDiscriminatorNotFoundException;
import io.github.acoboh.query.filter.jpa.model.discriminators.Announcement;
import io.github.acoboh.query.filter.jpa.model.discriminators.Post;
import io.github.acoboh.query.filter.jpa.model.discriminators.Topic;
import io.github.acoboh.query.filter.jpa.repositories.PostDiscriminatorRepository;
import io.github.acoboh.query.filter.jpa.spring.SpringIntegrationTestBase;

/**
 * Discriminator tests
 * 
 * @author Adrián Cobo
 *
 */
@SpringJUnitWebConfig(SpringIntegrationTestBase.Config.class)
@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class DiscriminatorTest {

	@Autowired
	private QFProcessor<DiscriminatorFilterDef, Topic> queryFilterProcessor;

	@Autowired
	private PostDiscriminatorRepository repository;

	private static final Post POST_EXAMPLE = new Post();
	private static final Announcement ANN_EXAMPLE = new Announcement();

	static {

		POST_EXAMPLE.setOwner("Owner 1");
		POST_EXAMPLE.setTitle("Title 1");
		POST_EXAMPLE.setContent("Content 1");

		ANN_EXAMPLE.setOwner("Owner 2");
		ANN_EXAMPLE.setTitle("Title 2");
		ANN_EXAMPLE.setValidUntil(Timestamp.valueOf(LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS)));

	}

	@Test
	@DisplayName("0. Setup")
	@Order(0)
	void setup() {

		assertThat(queryFilterProcessor).isNotNull();
		assertThat(repository).isNotNull();

		assertThat(repository.findAll()).isEmpty();

		repository.saveAndFlush(POST_EXAMPLE);
		repository.saveAndFlush(ANN_EXAMPLE);

		assertThat(repository.findAll()).hasSize(2).containsExactlyInAnyOrder(POST_EXAMPLE, ANN_EXAMPLE);
	}

	@Test
	@DisplayName("1. Announcement discriminator")
	@Order(1)
	void announcementDiscriminator() {

		QueryFilter<Topic> qf = queryFilterProcessor.newQueryFilter("type=eq:ANNOUNCEMENT", QFParamType.RHS_COLON);

		assertThat(qf).isNotNull();

		List<Topic> list = repository.findAll(qf);
		assertThat(list).hasSize(1).containsExactly(ANN_EXAMPLE);

	}

	@Test
	@DisplayName("2. Post discriminator")
	@Order(2)
	void postDiscriminator() {

		QueryFilter<Topic> qf = queryFilterProcessor.newQueryFilter("type=eq:POST", QFParamType.RHS_COLON);

		assertThat(qf).isNotNull();

		List<Topic> list = repository.findAll(qf);
		assertThat(list).hasSize(1).containsExactly(POST_EXAMPLE);

	}

	@Test
	@DisplayName("3. All discriminator")
	@Order(3)
	void allDiscriminator() {

		QueryFilter<Topic> qf = queryFilterProcessor.newQueryFilter("type=in:ANNOUNCEMENT,POST", QFParamType.RHS_COLON);

		assertThat(qf).isNotNull();

		List<Topic> list = repository.findAll(qf);
		assertThat(list).hasSize(2).containsExactlyInAnyOrder(POST_EXAMPLE, ANN_EXAMPLE);

	}

	@Test
	@DisplayName("4. Error type discriminator")
	@Order(4)
	void errorDiscriminator() {

		QFDiscriminatorNotFoundException ex = assertThrows(QFDiscriminatorNotFoundException.class,
				() -> queryFilterProcessor.newQueryFilter("type=in:POST_ERROR", QFParamType.RHS_COLON));

		assertThat(ex).isNotNull();
		assertThat(ex.getValue()).isEqualTo("POST_ERROR");
		assertThat(ex.getField()).isEqualTo("type");

	}

	@Test
	@DisplayName("5. Test by clear BBDD")
	@Order(10)
	void clearBBDD() {
		repository.deleteAll();
		assertThat(repository.findAll()).isEmpty();
	}

}
