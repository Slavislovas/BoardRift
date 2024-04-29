package com.socialnetwork.boardrift.repository;

import com.socialnetwork.boardrift.repository.model.ChatMessageEntity;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessageEntity, Long> {
    List<ChatMessageEntity> findByChatId(String s);

    @Transactional
    void deleteByChatId(String chatId);
}
