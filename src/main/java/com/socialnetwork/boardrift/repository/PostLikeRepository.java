package com.socialnetwork.boardrift.repository;

import com.socialnetwork.boardrift.repository.model.post.PostLikeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PostLikeRepository extends JpaRepository<PostLikeEntity, Long> {
    Optional<PostLikeEntity> findBySimplePostIdAndLikeOwnerId(Long postId, Long likeOwnerId);
}
