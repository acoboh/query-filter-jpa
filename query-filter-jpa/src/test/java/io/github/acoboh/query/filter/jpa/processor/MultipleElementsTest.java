package io.github.acoboh.query.filter.jpa.processor;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
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

import io.github.acoboh.query.filter.jpa.domain.FilterBlogMultipleElementsDef;
import io.github.acoboh.query.filter.jpa.exceptions.QueryFilterException;
import io.github.acoboh.query.filter.jpa.model.PostBlog;
import io.github.acoboh.query.filter.jpa.repositories.PostBlogRepository;
import io.github.acoboh.query.filter.jpa.spring.SpringIntegrationTest;

/**
 * Multiple elements tests
 * 
 * @author Adrián Cobo
 *
 */
@SpringJUnitWebConfig(SpringIntegrationTest.Config.class)
@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class MultipleElementsTest {

	private static final PostBlog POST_EXAMPLE = new PostBlog();
	private static final PostBlog POST_EXAMPLE_2 = new PostBlog();

	static {
		POST_EXAMPLE.setUuid(UUID.randomUUID());
		POST_EXAMPLE.setAuthor("text");
		POST_EXAMPLE.setText("text");
		POST_EXAMPLE.setAvgNote(2.5d);
		POST_EXAMPLE.setLikes(100);
		POST_EXAMPLE.setCreateDate(LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS)); // Truncated to avoid rounding issues with Java > 8 and BBDD
		POST_EXAMPLE.setLastTimestamp(Timestamp.valueOf(LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS))); // Truncated to avoid rounding issues with Java > 8 and BBDD
		POST_EXAMPLE.setPublished(true);
		POST_EXAMPLE.setPostType(PostBlog.PostType.TEXT);

		POST_EXAMPLE_2.setUuid(UUID.randomUUID());
		POST_EXAMPLE_2.setAuthor("Author 2");
		POST_EXAMPLE_2.setText("text");
		POST_EXAMPLE_2.setAvgNote(0.5d);
		POST_EXAMPLE_2.setLikes(0);
		POST_EXAMPLE_2.setCreateDate(LocalDateTime.now());
		POST_EXAMPLE_2.setLastTimestamp(Timestamp.from(Instant.now()));
		POST_EXAMPLE_2.setPublished(false);
		POST_EXAMPLE_2.setPostType(PostBlog.PostType.VIDEO);
	}

	@Autowired
	private QFProcessor<FilterBlogMultipleElementsDef, PostBlog> queryFilterProcessor;

	@Autowired
	private PostBlogRepository repository;

	@Test
	@DisplayName("0. Setup")
	@Order(0)
	void setup() {

		assertThat(queryFilterProcessor).isNotNull();
		assertThat(repository).isNotNull();

		assertThat(repository.findAll()).isEmpty();

		repository.saveAndFlush(POST_EXAMPLE);
		repository.saveAndFlush(POST_EXAMPLE_2);

		assertThat(repository.findAll()).hasSize(2).containsExactlyInAnyOrder(POST_EXAMPLE, POST_EXAMPLE_2);
	}

	@Test
	@DisplayName("1. Query multiple AND")
	@Order(1)
	void queryMultipleAnd() throws QueryFilterException {

		QueryFilter<PostBlog> qf = queryFilterProcessor.newQueryFilter("multipleAnd=eq:text", QFParamType.RHS_COLON);

		assertThat(qf).isNotNull();

		List<PostBlog> list = repository.findAll(qf);
		assertThat(list).hasSize(1).containsExactly(POST_EXAMPLE);
	}

	@Test
	@DisplayName("2. Query multiple default")
	@Order(2)
	void queryMultipleDefault() throws QueryFilterException {

		QueryFilter<PostBlog> qf = queryFilterProcessor.newQueryFilter("multipleDefault=eq:text",
				QFParamType.RHS_COLON);

		assertThat(qf).isNotNull();

		List<PostBlog> list = repository.findAll(qf);
		assertThat(list).hasSize(1).containsExactly(POST_EXAMPLE);
	}

	@Test
	@DisplayName("3. Query multiple OR")
	@Order(2)
	void queryMultipleOr() throws QueryFilterException {

		QueryFilter<PostBlog> qf = queryFilterProcessor.newQueryFilter("multipleOr=eq:text", QFParamType.RHS_COLON);

		assertThat(qf).isNotNull();

		List<PostBlog> list = repository.findAll(qf);
		assertThat(list).hasSize(2).containsExactlyInAnyOrder(POST_EXAMPLE, POST_EXAMPLE_2);
	}

	@Test
	@DisplayName("5. Test by clear BBDD")
	@Order(Ordered.LOWEST_PRECEDENCE)
	void clearBBDD() {
		repository.deleteAll();
		assertThat(repository.findAll()).isEmpty();
	}

}
