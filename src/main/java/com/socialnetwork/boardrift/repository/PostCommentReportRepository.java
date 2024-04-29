package com.socialnetwork.boardrift.repository;

import com.socialnetwork.boardrift.repository.model.post.PostCommentReportEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PostCommentReportRepository extends JpaRepository<PostCommentReportEntity, Long> {
}
