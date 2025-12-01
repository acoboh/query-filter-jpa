package io.github.acoboh.query.filter.jpa.processor;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Set;
import java.util.UUID;

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

import io.github.acoboh.query.filter.jpa.domain.FilterBlogJoinType;
import io.github.acoboh.query.filter.jpa.model.Comments;
import io.github.acoboh.query.filter.jpa.model.PostBlog;
import io.github.acoboh.query.filter.jpa.repositories.PostBlogRepository;
import io.github.acoboh.query.filter.jpa.spring.SpringIntegrationTestBase;

/**
 * Join types element test
 *
 * @author Adrián Cobo
 */
@SpringJUnitWebConfig(SpringIntegrationTestBase.Config.class)
@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class JoinTypesElementTest {

	private static final PostBlog POST_EXAMPLE = new PostBlog();
	private static final PostBlog POST_EXAMPLE_2 = new PostBlog();
	private static final Comments COMMENT_EXAMPLE = new Comments();

	static {
		POST_EXAMPLE.setUuid(UUID.randomUUID());
		POST_EXAMPLE.setAuthor("author 1");
		POST_EXAMPLE.setText("text 1");
		POST_EXAMPLE.setAvgNote(2.5d);
		POST_EXAMPLE.setLikes(100);
		POST_EXAMPLE.setCreateDate(LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS));
		POST_EXAMPLE.setLastTimestamp(Timestamp.valueOf(LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS)));

		COMMENT_EXAMPLE.setId(1);
		COMMENT_EXAMPLE.setAuthor("author");
		COMMENT_EXAMPLE.setPostBlog(POST_EXAMPLE);

		POST_EXAMPLE.setComments(Set.of(COMMENT_EXAMPLE));

		POST_EXAMPLE_2.setUuid(UUID.randomUUID());
		POST_EXAMPLE_2.setAuthor("author 2");
		POST_EXAMPLE_2.setText("text 2");
		POST_EXAMPLE_2.setAvgNote(2.5d);
		POST_EXAMPLE_2.setLikes(100);
		POST_EXAMPLE_2.setCreateDate(LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS));
		POST_EXAMPLE_2.setLastTimestamp(Timestamp.valueOf(LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS)));
	}

	@Autowired
	private QFProcessor<FilterBlogJoinType, PostBlog> queryFilterProcessor;

	@Autowired
	private PostBlogRepository repository;

	@Test
	@DisplayName("0. Setup")
	@Order(1)
	void setup() {

		assertThat(queryFilterProcessor).isNotNull();
		assertThat(repository).isNotNull();

		assertThat(repository.findAll()).isEmpty();

		repository.saveAndFlush(POST_EXAMPLE);
		repository.saveAndFlush(POST_EXAMPLE_2);
	}

	@Test
	@DisplayName("1. Test join types return only the first data")
	@Order(2)
	void testJoinTypes() {

		// Execute
		var qf = queryFilterProcessor.newQueryFilter("commentAuthor=null:true", QFParamType.RHS_COLON);

		var result = repository.findAll(qf);

		assertThat(result).isNotNull().hasSize(1);
		assertThat(result.get(0)).isEqualTo(POST_EXAMPLE_2);

	}

	@Test
	@DisplayName("END. Test by clear BBDD")
	@Order(Ordered.LOWEST_PRECEDENCE)
	void clearBBDD() {
		repository.deleteAll();
		assertThat(repository.findAll()).isEmpty();
	}

}
