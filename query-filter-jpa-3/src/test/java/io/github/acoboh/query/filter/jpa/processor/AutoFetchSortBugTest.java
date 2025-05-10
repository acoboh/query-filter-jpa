package io.github.acoboh.query.filter.jpa.processor;

import static org.assertj.core.api.Assertions.assertThat;

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
import org.springframework.data.domain.Sort;
import org.springframework.data.util.Pair;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit.jupiter.web.SpringJUnitWebConfig;
import org.springframework.test.context.web.WebAppConfiguration;

import io.github.acoboh.query.filter.jpa.domain.FilterCommentBlogDef;
import io.github.acoboh.query.filter.jpa.model.Comments;
import io.github.acoboh.query.filter.jpa.model.PostBlog;
import io.github.acoboh.query.filter.jpa.repositories.CommentsRepository;
import io.github.acoboh.query.filter.jpa.repositories.PostBlogRepository;
import io.github.acoboh.query.filter.jpa.spring.SpringIntegrationTestBase;

@SpringJUnitWebConfig(SpringIntegrationTestBase.Config.class)
@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AutoFetchSortBugTest {

	private static final PostBlog POST_BLOG = new PostBlog();
	private static final Comments COMMENT = new Comments();
	private static final Comments COMMENT2 = new Comments();
	private static final Comments COMMENT3 = new Comments();

	static {
		POST_BLOG.setUuid(UUID.randomUUID());
		POST_BLOG.setAuthor("Author");
		POST_BLOG.setText("Text");
		POST_BLOG.setAvgNote(2.5d);
		POST_BLOG.setLikes(100);

		COMMENT.setId(1);
		COMMENT.setAuthor("Adrián Cobo");
		COMMENT.setComment("Test comment");
		COMMENT.setLikes(10);
		COMMENT.setPostBlog(POST_BLOG);

		COMMENT2.setId(2);
		COMMENT2.setAuthor("Adrián Cobo");
		COMMENT2.setComment("Test comment 2");
		COMMENT2.setLikes(20);
		COMMENT2.setPostBlog(POST_BLOG);

		POST_BLOG.setComments(Set.of(COMMENT, COMMENT2));

		COMMENT3.setId(3);
		COMMENT3.setAuthor("Adrián Cobo");
		COMMENT3.setComment("Test comment 3");
		COMMENT3.setLikes(30);
	}

	@Autowired
	private QFProcessor<FilterCommentBlogDef, Comments> qfProcessor;

	@Autowired
	private PostBlogRepository postBlogRepository;

	@Autowired
	private CommentsRepository commentsRepository;

	@Test
	@DisplayName("0. Setup")
	@Order(Ordered.HIGHEST_PRECEDENCE)
	void setup() {

		assertThat(qfProcessor).isNotNull();
		assertThat(postBlogRepository).isNotNull();
		assertThat(commentsRepository).isNotNull();

		assertThat(postBlogRepository.findAll()).isEmpty();
		assertThat(commentsRepository.findAll()).isEmpty();

		postBlogRepository.save(POST_BLOG);
		commentsRepository.save(COMMENT);
		commentsRepository.save(COMMENT2);
		commentsRepository.save(COMMENT3);

		assertThat(postBlogRepository.findAll()).hasSize(1).containsExactlyInAnyOrder(POST_BLOG);
		assertThat(commentsRepository.findAll()).hasSize(3).containsExactlyInAnyOrder(COMMENT, COMMENT2, COMMENT3);

	}

	@Test
	@DisplayName("1. Test sort by author with autoFetch return 1 element (because is Inner JOIN)")
	@Order(1)
	void testSortByAuthor() {

		var qf = qfProcessor.newQueryFilter("sort=+blogAuthor", QFParamType.RHS_COLON);
		assertThat(qf).isNotNull();

		assertThat(qf.isSorted()).isTrue();

		assertThat(qf.isSortedBy("blogAuthor")).isTrue();

		assertThat(qf.getSortFields()).containsExactly(Pair.of("blogAuthor", Sort.Direction.ASC));

		assertThat(qf.getSortFieldWithFullPath()).containsExactly(Pair.of("postBlog.author", Sort.Direction.ASC));


		var countQuery = commentsRepository.count(qf);
		assertThat(countQuery).isEqualTo(2);

	}

	@Test
	@DisplayName("END. Test by clear BBDD")
	@Order(Ordered.LOWEST_PRECEDENCE)
	void clearBBDD() {

		postBlogRepository.deleteAll();
		assertThat(postBlogRepository.findAll()).isEmpty();

		commentsRepository.deleteAll();
		assertThat(commentsRepository.findAll()).isEmpty();
	}

}
