package io.github.acoboh.query.filter.jpa.processor;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
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

import io.github.acoboh.query.filter.jpa.domain.FilterBlogSpELDef;
import io.github.acoboh.query.filter.jpa.exceptions.QueryFilterException;
import io.github.acoboh.query.filter.jpa.model.Comments;
import io.github.acoboh.query.filter.jpa.model.PostBlog;
import io.github.acoboh.query.filter.jpa.operations.QFOperationEnum;
import io.github.acoboh.query.filter.jpa.repositories.PostBlogRepository;
import io.github.acoboh.query.filter.jpa.spring.SpringIntegrationTest;

/**
 * SpEL tests
 * 
 * @author Adrián Cobo
 *
 */
@SpringJUnitWebConfig(SpringIntegrationTest.Config.class)
@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SpELTest {

	private static final PostBlog POST_EXAMPLE = new PostBlog();
	private static final PostBlog POST_EXAMPLE_2 = new PostBlog();

	static {
		POST_EXAMPLE.setUuid(UUID.randomUUID());
		POST_EXAMPLE.setAuthor("Author");
		POST_EXAMPLE.setText("Text");
		POST_EXAMPLE.setAvgNote(2.5d);
		POST_EXAMPLE.setLikes(1);
		POST_EXAMPLE.setCreateDate(LocalDateTime.now());
		POST_EXAMPLE.setLastTimestamp(Timestamp.from(Instant.now()));
		POST_EXAMPLE.setPublished(true);
		POST_EXAMPLE.setPostType(PostBlog.PostType.TEXT);

		Set<Comments> commentsList = new HashSet<>();

		Comments comment = new Comments();
		comment.setId(1);
		comment.setAuthor("Author 1");
		comment.setComment("Comment 1");
		comment.setLikes(100);
		comment.setPostBlog(POST_EXAMPLE);

		Comments comment2 = new Comments();
		comment2.setId(2);
		comment2.setAuthor("Author 2");
		comment2.setComment("Comment 2");
		comment2.setLikes(200);
		comment2.setPostBlog(POST_EXAMPLE);

		commentsList.add(comment);
		commentsList.add(comment2);

		POST_EXAMPLE.setComments(commentsList);

		POST_EXAMPLE_2.setUuid(UUID.randomUUID());
		POST_EXAMPLE_2.setAuthor("Author 2");
		POST_EXAMPLE_2.setText("Text 2");
		POST_EXAMPLE_2.setAvgNote(0.5d);
		POST_EXAMPLE_2.setLikes(2);
		POST_EXAMPLE_2.setCreateDate(LocalDateTime.now());
		POST_EXAMPLE_2.setLastTimestamp(Timestamp.from(Instant.now()));
		POST_EXAMPLE_2.setPublished(false);
		POST_EXAMPLE_2.setPostType(PostBlog.PostType.VIDEO);

		Set<Comments> commentsList2 = new HashSet<>();

		Comments comment3 = new Comments();
		comment3.setId(3);
		comment3.setAuthor("Author 3");
		comment3.setComment("Comment 3");
		comment3.setLikes(300);
		comment3.setPostBlog(POST_EXAMPLE_2);

		Comments comment4 = new Comments();
		comment4.setId(4);
		comment4.setAuthor("Author 4");
		comment4.setComment("Comment 4");
		comment4.setLikes(400);
		comment4.setPostBlog(POST_EXAMPLE_2);

		commentsList2.add(comment3);
		commentsList2.add(comment4);

		POST_EXAMPLE_2.setComments(commentsList2);
	}

	@Autowired
	private QFProcessor<FilterBlogSpELDef, PostBlog> queryFilterProcessor;
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
	@DisplayName("1. Test default SpEL expression")
	@Order(1)
	void testDefaultSpELExpression() throws QueryFilterException {

		// Filter for likes = 1. Automatically get comments with likes > 10
		QueryFilter<PostBlog> qf = queryFilterProcessor.newQueryFilter("likes=gte:1", QFParamType.RHS_COLON);
		assertThat(qf).isNotNull();

		List<PostBlog> blogList = repository.findAll(qf);
		assertThat(blogList).hasSize(2).containsExactlyInAnyOrder(POST_EXAMPLE, POST_EXAMPLE_2);

		// Custom SpEL expression programmatically
		qf = queryFilterProcessor.newQueryFilter("likes=gte:1", QFParamType.RHS_COLON);
		qf.overrideField("commentLikes", QFOperationEnum.EQUAL, "#likes * 100");

		blogList = repository.findAll(qf);
		assertThat(blogList).hasSize(1).containsExactlyInAnyOrder(POST_EXAMPLE);
	}

	@Test
	@DisplayName("2. Test custom SpEL expression as array expression")
	@Order(2)
	void testCustomSpELExpression() throws QueryFilterException {

		QueryFilter<PostBlog> qf = queryFilterProcessor.newQueryFilter("likes=in:0,1,2", QFParamType.RHS_COLON);
		assertThat(qf).isNotNull();

		qf.overrideField("commentLikes", QFOperationEnum.EQUAL, "(#likes['0'] + #likes['1']) * 100");

		List<PostBlog> blogList = repository.findAll(qf);

		// Execute likes IN (0,1,2) and comments equal to (100) [because (0 + 1) * 100 = 100]
		// Only returns the first blog
		assertThat(blogList).hasSize(1).containsExactlyInAnyOrder(POST_EXAMPLE);

		// Test with commentLikes > likes['2']
		qf.overrideField("commentLikes", QFOperationEnum.GREATER_THAN, "(#likes['2'] + 1 ) * 100");

		blogList = repository.findAll(qf);

		// Execute likes IN (0,1,2) and comments greater than 300
		assertThat(blogList).hasSize(1).containsExactlyInAnyOrder(POST_EXAMPLE_2);

	}

	@Test
	@DisplayName("3. Test by clear BBDD")
	@Order(3)
	void clearBBDD() {
		repository.deleteAll();
		assertThat(repository.findAll()).isEmpty();
	}

}
