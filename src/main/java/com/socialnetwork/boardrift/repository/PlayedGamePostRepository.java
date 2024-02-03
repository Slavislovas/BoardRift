package com.socialnetwork.boardrift.repository;

import com.socialnetwork.boardrift.repository.model.played_game_post.PlayedGamePostEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PlayedGamePostRepository extends JpaRepository<PlayedGamePostEntity, Long> {
}
