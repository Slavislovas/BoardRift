package com.socialnetwork.boardrift.repository;

import com.socialnetwork.boardrift.repository.model.post.PlayedGamePostEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PlayedGamePostRepository extends JpaRepository<PlayedGamePostEntity, Long> {
}
