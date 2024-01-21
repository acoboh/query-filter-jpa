package io.github.acoboh.query.filter.jpa.processor;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
import io.github.acoboh.query.filter.jpa.exceptions.QueryFilterException;
import io.github.acoboh.query.filter.jpa.model.Comments;
import io.github.acoboh.query.filter.jpa.model.PostBlog;
import io.github.acoboh.query.filter.jpa.repositories.PostBlogRepository;
import io.github.acoboh.query.filter.jpa.spring.SpringIntegrationTestBase;

/**
 * Relational tests
 * 
 * @author Adrián Cobo
 *
 */
@SpringJUnitWebConfig(SpringIntegrationTestBase.Config.class)
@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class RelationalTest {

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

		Set<Comments> commentsList = new HashSet<>();

		Comments comment = new Comments();
		comment.setId(1);
		comment.setAuthor("Author 1");
		comment.setComment("Comment 1");
		comment.setLikes(1);
		comment.setPostBlog(POST_EXAMPLE);

		Comments comment2 = new Comments();
		comment2.setId(2);
		comment2.setAuthor("Author 2");
		comment2.setComment("Comment 2");
		comment2.setLikes(2);
		comment2.setPostBlog(POST_EXAMPLE);

		commentsList.add(comment);
		commentsList.add(comment2);

		POST_EXAMPLE.setComments(commentsList);

		POST_EXAMPLE_2.setUuid(UUID.randomUUID());
		POST_EXAMPLE_2.setAuthor("Author 2");
		POST_EXAMPLE_2.setText("Text 2");
		POST_EXAMPLE_2.setAvgNote(0.5d);
		POST_EXAMPLE_2.setLikes(0);
		POST_EXAMPLE_2.setCreateDate(LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS)); // Truncated to avoid rounding issues with Java > 8 and BBDD
		POST_EXAMPLE_2.setLastTimestamp(Timestamp.valueOf(LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS))); // Truncated to avoid rounding issues with Java > 8 and BBDD
		POST_EXAMPLE_2.setPublished(false);
		POST_EXAMPLE_2.setPostType(PostBlog.PostType.VIDEO);

		Set<Comments> commentsList2 = new HashSet<>();

		Comments comment3 = new Comments();
		comment3.setId(3);
		comment3.setAuthor("Author 3");
		comment3.setComment("Comment 3");
		comment3.setLikes(3);
		comment3.setPostBlog(POST_EXAMPLE_2);

		Comments comment4 = new Comments();
		comment4.setId(4);
		comment4.setAuthor("Author 4");
		comment4.setComment("Comment 4");
		comment4.setLikes(4);
		comment4.setPostBlog(POST_EXAMPLE_2);

		commentsList2.add(comment3);
		commentsList2.add(comment4);

		POST_EXAMPLE_2.setComments(commentsList2);
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

		assertThat(repository.findAll()).hasSize(2).containsExactlyInAnyOrder(POST_EXAMPLE, POST_EXAMPLE_2);

	}

	@Test
	@DisplayName("1. Test empty query")
	@Order(2)
	void query() throws QueryFilterException {
		QueryFilter<PostBlog> qf = queryFilterProcessor.newQueryFilter("", QFParamType.RHS_COLON);
		assertThat(qf).isNotNull();

		List<PostBlog> list = repository.findAll(qf);
		assertThat(list).hasSize(2).containsExactlyInAnyOrder(POST_EXAMPLE, POST_EXAMPLE_2);

	}

	@Test
	@DisplayName("2. Test by comment likes")
	@Order(3)
	void queryByCommentLikes() throws QueryFilterException {
		QueryFilter<PostBlog> qf = queryFilterProcessor.newQueryFilter("commentLikes=gt:2", QFParamType.RHS_COLON);
		assertThat(qf).isNotNull();

		List<PostBlog> list = repository.findAll(qf);
		assertThat(list).hasSize(1).containsExactlyInAnyOrder(POST_EXAMPLE_2);

		// Test greater or equal than 2

		qf = queryFilterProcessor.newQueryFilter("commentLikes=gte:2", QFParamType.RHS_COLON);
		assertThat(qf).isNotNull();

		list = repository.findAll(qf);
		assertThat(list).hasSize(2).containsExactlyInAnyOrder(POST_EXAMPLE, POST_EXAMPLE_2);

		// Test less than 3
		qf = queryFilterProcessor.newQueryFilter("commentLikes=lt:3", QFParamType.RHS_COLON);
		assertThat(qf).isNotNull();

		list = repository.findAll(qf);
		assertThat(list).hasSize(1).containsExactlyInAnyOrder(POST_EXAMPLE);

		// Test less or equal than 3
		qf = queryFilterProcessor.newQueryFilter("commentLikes=lte:3", QFParamType.RHS_COLON);
		assertThat(qf).isNotNull();

		list = repository.findAll(qf);
		assertThat(list).hasSize(2).containsExactlyInAnyOrder(POST_EXAMPLE, POST_EXAMPLE_2);
	}

	@Test
	@DisplayName("3. Test by author")
	@Order(4)
	void queryByAuthor() throws QueryFilterException {

		QueryFilter<PostBlog> qf = queryFilterProcessor.newQueryFilter("commentAuthor=eq:Author 1",
				QFParamType.RHS_COLON);
		assertThat(qf).isNotNull();

		List<PostBlog> list = repository.findAll(qf);
		assertThat(list).hasSize(1).containsExactlyInAnyOrder(POST_EXAMPLE);

		// Test IN operator

		qf = queryFilterProcessor.newQueryFilter("commentAuthor=in:Author 1,Author 2", QFParamType.RHS_COLON);
		assertThat(qf).isNotNull();

		list = repository.findAll(qf);
		assertThat(list).hasSize(1).containsExactlyInAnyOrder(POST_EXAMPLE);

		// Test NOT IN operator
		qf = queryFilterProcessor.newQueryFilter("commentAuthor=nin:Author 1,Author 2", QFParamType.RHS_COLON);
		assertThat(qf).isNotNull();

		list = repository.findAll(qf);
		assertThat(list).hasSize(1).containsExactlyInAnyOrder(POST_EXAMPLE_2);

		// Test LIKE operator
		qf = queryFilterProcessor.newQueryFilter("commentAuthor=like:Author", QFParamType.RHS_COLON);
		assertThat(qf).isNotNull();

		list = repository.findAll(qf);
		assertThat(list).hasSize(2).containsExactlyInAnyOrder(POST_EXAMPLE, POST_EXAMPLE_2);

		// Test StartsWith operator
		qf = queryFilterProcessor.newQueryFilter("commentAuthor=starts:Author", QFParamType.RHS_COLON);
		assertThat(qf).isNotNull();

		list = repository.findAll(qf);
		assertThat(list).hasSize(2).containsExactlyInAnyOrder(POST_EXAMPLE, POST_EXAMPLE_2);

		// Test EndsWith operator
		qf = queryFilterProcessor.newQueryFilter("commentAuthor=ends:3", QFParamType.RHS_COLON);
		assertThat(qf).isNotNull();

		list = repository.findAll(qf);
		assertThat(list).hasSize(1).containsExactlyInAnyOrder(POST_EXAMPLE_2);

	}

	@Test
	@DisplayName("4. Test by clear BBDD")
	@Order(5)
	void clearBBDD() {
		repository.deleteAll();
		assertThat(repository.findAll()).isEmpty();
	}

}
