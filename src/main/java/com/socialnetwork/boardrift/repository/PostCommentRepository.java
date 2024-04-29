package com.socialnetwork.boardrift.repository;

import com.socialnetwork.boardrift.repository.model.post.PostCommentEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PostCommentRepository extends JpaRepository<PostCommentEntity, Long> {
    List<PostCommentEntity> findAllBySimplePostId(Long id, Pageable pageable);

    Optional<PostCommentEntity> findByIdAndSimplePostId(Long postId, Long postCommentId);

    @Query("SELECT pc FROM PostCommentEntity pc " +
            "WHERE SIZE(pc.reports) > 0" +
            "ORDER BY SIZE(pc.reports) DESC")
    List<PostCommentEntity> findReportedComments(Pageable pageable);
}
