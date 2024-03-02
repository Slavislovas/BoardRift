package com.socialnetwork.boardrift.repository;

import com.socialnetwork.boardrift.repository.model.board_game.PlayedGameEntity;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlayedGameRepository extends JpaRepository<PlayedGameEntity, Long> {
    List<PlayedGameEntity> findByUserId(Long id, Pageable pageable);
    void deleteByIdAndUserId(Long id, Long userId);
}
