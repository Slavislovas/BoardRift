package com.socialnetwork.boardrift.repository;

import com.socialnetwork.boardrift.repository.model.user.SuspensionEntity;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SuspensionRepository extends JpaRepository<SuspensionEntity, Long> {

    @Transactional
    void deleteByUserId(Long userId);
}