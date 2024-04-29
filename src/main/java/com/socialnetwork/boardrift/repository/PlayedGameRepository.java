package com.socialnetwork.boardrift.repository;

import com.socialnetwork.boardrift.repository.model.board_game.PlayedGameEntity;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlayedGameRepository extends JpaRepository<PlayedGameEntity, Long> {
    @Query("SELECT p FROM PlayedGameEntity p WHERE p.user.id = :id AND p.includeToStatistics = true")
    List<PlayedGameEntity> findByUserId(Long id, Pageable pageable);

    @Transactional
    @Modifying
    @Query("DELETE FROM PlayedGameEntity p WHERE p.id IN :ids")
    void deleteByIdIn(@Param("ids") List<Long> ids);
}
