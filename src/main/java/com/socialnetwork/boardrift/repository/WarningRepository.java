package com.socialnetwork.boardrift.repository;

import com.socialnetwork.boardrift.repository.model.WarningEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WarningRepository extends JpaRepository<WarningEntity, Long> {
    void deleteByIdAndRecipientId(Long userId, Long warningId);
    void deleteAllByRecipientId(Long userId);
}
