package io.github.acoboh.query.filter.jpa.processor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;

import java.sql.Timestamp;
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
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit.jupiter.web.SpringJUnitWebConfig;
import org.springframework.test.context.web.WebAppConfiguration;

import io.github.acoboh.query.filter.jpa.domain.FilterBlogDef;
import io.github.acoboh.query.filter.jpa.exceptions.QFNotValuable;
import io.github.acoboh.query.filter.jpa.exceptions.QueryFilterException;
import io.github.acoboh.query.filter.jpa.model.PostBlog;
import io.github.acoboh.query.filter.jpa.operations.QFOperationEnum;
import io.github.acoboh.query.filter.jpa.repositories.PostBlogRepository;
import io.github.acoboh.query.filter.jpa.spring.SpringIntegrationTestBase;

/**
 * Multiple found tests
 * 
 * @author Adrián Cobo
 *
 */
@SpringJUnitWebConfig(SpringIntegrationTestBase.Config.class)
@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class MultipleFoundTest {

	private static final PostBlog POST_EXAMPLE = new PostBlog();
	private static final PostBlog POST_EXAMPLE_2 = new PostBlog();

	static {
		POST_EXAMPLE.setUuid(UUID.randomUUID());
		POST_EXAMPLE.setAuthor("Author");
		POST_EXAMPLE.setText("Text");
		POST_EXAMPLE.setAvgNote(2.5d);
		POST_EXAMPLE.setLikes(100);
		POST_EXAMPLE.setCreateDate(LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS)); // Truncated to avoid rounding issues with Java > 8 and BBDD
		POST_EXAMPLE.setLastTimestamp(Timestamp.valueOf(LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS))); // Truncated to avoid rounding issues with Java > 8 and BBDD
		POST_EXAMPLE.setPublished(true);
		POST_EXAMPLE.setPostType(PostBlog.PostType.TEXT);

		POST_EXAMPLE_2.setUuid(UUID.randomUUID());
		POST_EXAMPLE_2.setAuthor("Author 2");
		POST_EXAMPLE_2.setText("Text 2");
		POST_EXAMPLE_2.setAvgNote(0.5d);
		POST_EXAMPLE_2.setLikes(0);
		POST_EXAMPLE_2.setCreateDate(LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS)); // Truncated to avoid rounding issues with Java > 8 and BBDD
		POST_EXAMPLE_2.setLastTimestamp(Timestamp.valueOf(LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS))); // Truncated to avoid rounding issues with Java > 8 and BBDD
		POST_EXAMPLE_2.setPublished(false);
		POST_EXAMPLE_2.setPostType(PostBlog.PostType.VIDEO);
	}

	@Autowired
	private QFProcessor<FilterBlogDef, PostBlog> queryFilterProcessor;

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

		assertThat(repository.findAll()).hasSize(2);
	}

	@Test
	@DisplayName("1. Test empty query")
	@Order(2)
	void testEmptyQuery() throws QueryFilterException {

		QueryFilter<PostBlog> qf = queryFilterProcessor.newQueryFilter("", QFParamType.RHS_COLON);
		assertThat(qf).isNotNull();

		List<PostBlog> list = repository.findAll(qf);
		assertThat(list).hasSize(2).containsExactlyInAnyOrder(POST_EXAMPLE, POST_EXAMPLE_2);
	}

	@Test
	@DisplayName("2. Test by author")
	@Order(3)
	void testQueryByAuthor() throws QueryFilterException {

		// Query only for post-example 1
		QueryFilter<PostBlog> qf = queryFilterProcessor.newQueryFilter("author=eq:Author", QFParamType.RHS_COLON);
		assertThat(qf).isNotNull();

		List<PostBlog> list = repository.findAll(qf);
		assertThat(list).hasSize(1).containsExactlyInAnyOrder(POST_EXAMPLE);

		// New query filter for post-example 2
		qf = queryFilterProcessor.newQueryFilter("author=like:author 2", QFParamType.RHS_COLON);
		assertThat(qf).isNotNull();

		// Query for post-example 2
		list = repository.findAll(qf);

		assertThat(list).hasSize(1).containsExactlyInAnyOrder(POST_EXAMPLE_2);

	}

	@Test
	@DisplayName("3. Test by not existing author")
	@Order(4)
	void testQueryByMissingAuthor() throws QueryFilterException {
		QueryFilter<PostBlog> qf = queryFilterProcessor.newQueryFilter("author=like:example", QFParamType.RHS_COLON);
		assertThat(qf).isNotNull();

		List<PostBlog> list = repository.findAll(qf);
		assertThat(list).isEmpty();
	}

	@Test
	@DisplayName("4. Test by avgNote")
	@Order(5)
	void testQueryByAvgNote() throws QueryFilterException {

		// Query greater thant 0.5 (only post-example 1)
		QueryFilter<PostBlog> qf = queryFilterProcessor.newQueryFilter("avgNote=gt:0.5", QFParamType.RHS_COLON);
		assertThat(qf).isNotNull();

		List<PostBlog> list = repository.findAll(qf);
		assertThat(list).hasSize(1).containsExactlyInAnyOrder(POST_EXAMPLE);

		// Query greater or equal 0.5 (all posts)
		qf = queryFilterProcessor.newQueryFilter("avgNote=gte:0.5", QFParamType.RHS_COLON);
		assertThat(qf).isNotNull();

		list = repository.findAll(qf);
		assertThat(list).hasSize(2).containsExactlyInAnyOrder(POST_EXAMPLE, POST_EXAMPLE_2);

		// Query less than 2.5 (only post-example 2)
		qf = queryFilterProcessor.newQueryFilter("avgNote=lt:2.5", QFParamType.RHS_COLON);
		assertThat(qf).isNotNull();

		list = repository.findAll(qf);
		assertThat(list).hasSize(1).containsExactlyInAnyOrder(POST_EXAMPLE_2);

		// Query less or equal 2.5 (all posts)
		qf = queryFilterProcessor.newQueryFilter("avgNote=lte:2.5", QFParamType.RHS_COLON);
		assertThat(qf).isNotNull();

		list = repository.findAll(qf);
		assertThat(list).hasSize(2).containsExactlyInAnyOrder(POST_EXAMPLE, POST_EXAMPLE_2);

	}

	@Test
	@DisplayName("5. Test by create date")
	@Order(6)
	void testQueryByCreateDate() throws QueryFilterException {
		QueryFilter<PostBlog> qf = queryFilterProcessor.newQueryFilter("createDate=gt:2020-01-01T00:00:00Z",
				QFParamType.RHS_COLON);
		assertThat(qf).isNotNull();

		List<PostBlog> list = repository.findAll(qf);
		assertThat(list).hasSize(2).containsExactlyInAnyOrder(POST_EXAMPLE, POST_EXAMPLE_2);

	}

	@Test
	@DisplayName("6. Test by last timestamp")
	@Order(7)
	void testQueryByLastTimestamp() throws QueryFilterException {
		QFNotValuable ex = assertThrows(QFNotValuable.class, () -> queryFilterProcessor
				.newQueryFilter("lastTimestamp=gt:2020-01-01T00:00:00Z", QFParamType.RHS_COLON));

		assertThat(ex).isNotNull();
		assertThat(ex.getField()).isEqualTo("lastTimestamp");
	}

	@Test
	@DisplayName("7. Test by post type")
	@Order(8)
	void testQueryByPostType() throws QueryFilterException {
		QueryFilter<PostBlog> qf = queryFilterProcessor.newQueryFilter("postType=eq:TEXT", QFParamType.RHS_COLON);
		assertThat(qf).isNotNull();

		List<PostBlog> list = repository.findAll(qf);
		assertThat(list).hasSize(1).containsExactlyInAnyOrder(POST_EXAMPLE);

		qf = queryFilterProcessor.newQueryFilter("postType=eq:VIDEO", QFParamType.RHS_COLON);
		assertThat(qf).isNotNull();

		list = repository.findAll(qf);
		assertThat(list).hasSize(1).containsExactlyInAnyOrder(POST_EXAMPLE_2);

		qf = queryFilterProcessor.newQueryFilter("postType=in:VIDEO,TEXT", QFParamType.RHS_COLON);
		assertThat(qf).isNotNull();

		list = repository.findAll(qf);
		assertThat(list).hasSize(2).containsExactlyInAnyOrder(POST_EXAMPLE, POST_EXAMPLE_2);

		qf = queryFilterProcessor.newQueryFilter("postType=nin:TEXT,VIDEO", QFParamType.RHS_COLON);
		assertThat(qf).isNotNull();

		assertThat(repository.findAll(qf)).isEmpty();

	}

	@Test
	@DisplayName("8. Test by published is allowed manually")
	@Order(9)
	void testQueryByPublishedManually() throws QueryFilterException {
		QueryFilter<PostBlog> qf = queryFilterProcessor.newQueryFilter(null, QFParamType.RHS_COLON);
		assertThat(qf).isNotNull();

		qf.addNewField("published", QFOperationEnum.EQUAL, "true");

		List<PostBlog> list = repository.findAll(qf);
		assertThat(list).hasSize(1).containsExactlyInAnyOrder(POST_EXAMPLE);

		qf.overrideField("published", QFOperationEnum.EQUAL, "false");
		list = repository.findAll(qf);

		assertThat(list).hasSize(1).containsExactlyInAnyOrder(POST_EXAMPLE_2);

	}

	@Test
	@DisplayName("9. Test by clear BBDD")
	@Order(10)
	void clearBBDD() {
		repository.deleteAll();
		assertThat(repository.findAll()).isEmpty();
	}
}
