package io.github.acoboh.query.filter.jpa.repositories;

import io.github.acoboh.query.filter.jpa.model.Comments;
import org.jspecify.annotations.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface CommentsRepository extends JpaSpecificationExecutor<@NonNull Comments>, JpaRepository<@NonNull Comments, @NonNull Integer> {
}
