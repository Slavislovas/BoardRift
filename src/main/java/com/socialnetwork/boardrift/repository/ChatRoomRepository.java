package com.socialnetwork.boardrift.repository;

import com.socialnetwork.boardrift.repository.model.ChatRoomEntity;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoomEntity, Long> {
    Optional<ChatRoomEntity> findBySenderIdAndRecipientId(Long senderId, Long recipientId);

    @Transactional
    void deleteByChatId(String chatId);
}
