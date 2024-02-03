package com.socialnetwork.boardrift.repository;

import com.socialnetwork.boardrift.repository.model.board_game.BoardGameCategoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BoardGameCategoryRepository extends JpaRepository<BoardGameCategoryEntity, Long> {
}