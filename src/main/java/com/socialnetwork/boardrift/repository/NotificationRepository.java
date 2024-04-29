package com.socialnetwork.boardrift.repository;

import com.socialnetwork.boardrift.repository.model.NotificationEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<NotificationEntity, Long> {
    List<NotificationEntity> findByRecipientId(Long id, Pageable pageable);
    Boolean existsByRecipientIdAndUnreadTrue(Long id);
}
