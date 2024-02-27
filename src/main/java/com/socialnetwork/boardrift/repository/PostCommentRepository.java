package com.socialnetwork.boardrift.repository;

import com.socialnetwork.boardrift.repository.model.post.PostCommentEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostCommentRepository extends JpaRepository<PostCommentEntity, Long> {
    List<PostCommentEntity> findAllBySimplePostId(Long id, Pageable pageable);
}
