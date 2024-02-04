package com.socialnetwork.boardrift.repository;

import com.socialnetwork.boardrift.repository.model.RefreshTokenEntity;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshTokenEntity, Long> {
    boolean existsByUserId(Long id);

    Optional<RefreshTokenEntity> findByUserId(Long id);

    @Transactional
    void deleteByUserId(Long id);

    Optional<RefreshTokenEntity> findByToken(String token);
}
