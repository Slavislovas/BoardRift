package com.socialnetwork.boardrift.service;

import com.socialnetwork.boardrift.repository.ChatRoomRepository;
import com.socialnetwork.boardrift.repository.UserRepository;
import com.socialnetwork.boardrift.repository.model.ChatRoomEntity;
import com.socialnetwork.boardrift.repository.model.UserEntity;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@RequiredArgsConstructor
@Service
public class ChatRoomService {
    private final ChatRoomRepository chatRoomRepository;
    private final UserRepository userRepository;

    public Optional<String> getChatRoomId(Long senderId, Long recipientId, boolean createNewRoomIfNotExists) {
        return chatRoomRepository.findBySenderIdAndRecipientId(senderId, recipientId)
                .map(ChatRoomEntity::getChatId)
                .or(() -> {
                    if (createNewRoomIfNotExists) {
                        String chatId = createChat(senderId, recipientId);
                        return Optional.of(chatId);
                    }
                    return Optional.empty();
                });
    }

    private String createChat(Long senderId, Long recipientId) {
        String chatId = String.format("%d_%d", senderId, recipientId);

        UserEntity sender = userRepository.findById(senderId).orElseThrow(() -> new EntityNotFoundException("Sender with id: " + senderId + " was not found"));
        UserEntity recipient = userRepository.findById(recipientId).orElseThrow(() -> new EntityNotFoundException("Recipient with id: " + senderId + " was not found"));

        ChatRoomEntity senderRecipient = new ChatRoomEntity(null, chatId, sender, recipient);
        ChatRoomEntity recipientSender = new ChatRoomEntity(null, chatId, recipient, sender);

        chatRoomRepository.save(senderRecipient);
        chatRoomRepository.save(recipientSender);

        return chatId;
    }
}
