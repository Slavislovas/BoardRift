package com.socialnetwork.boardrift.service;

import com.socialnetwork.boardrift.repository.ChatMessageRepository;
import com.socialnetwork.boardrift.repository.model.ChatMessageEntity;
import com.socialnetwork.boardrift.repository.model.user.UserEntity;
import com.socialnetwork.boardrift.rest.model.ChatMessageDto;
import com.socialnetwork.boardrift.util.mapper.UserMapper;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class ChatMessageService {
    private final ChatMessageRepository chatMessageRepository;
    private final ChatRoomService chatRoomService;
    private final UserService userService;
    private final UserMapper userMapper;
    private final SimpMessagingTemplate messagingTemplate;
    private final JwtService jwtService;

    @Transactional
    public void sendAndSaveChatMessage(ChatMessageDto chatMessageDto, String accessToken) {
        if (accessToken == null) {
            return;
        }

        try {
            accessToken = accessToken.substring(7);
            Long authUserId = Long.valueOf(jwtService.extractId(accessToken));

            if (!authUserId.equals(chatMessageDto.getSenderId())) {
                return;
            }
        } catch (Exception e) {
            return;
        }

        UserEntity senderEntity = userService.getUserEntityById(chatMessageDto.getSenderId());

        if (!userService.recipientAlreadyFriend(senderEntity, chatMessageDto.getRecipientId())) {
            return;
        }

        String chatId = chatRoomService.getChatRoomId(chatMessageDto.getSenderId(), chatMessageDto.getRecipientId(), true).orElseThrow();
        UserEntity recipientUserEntity = userService.getUserEntityById(chatMessageDto.getRecipientId());
        ChatMessageEntity chatMessageEntity = new ChatMessageEntity(null, chatId, chatMessageDto.getContent(), new Date(), true, senderEntity, recipientUserEntity);

        ChatMessageEntity savedChatMessage = chatMessageRepository.save(chatMessageEntity);
        ChatMessageDto savedChatMessageDto = new ChatMessageDto(
                savedChatMessage.getId(),
                savedChatMessage.getChatId(),
                savedChatMessage.getContent(),
                savedChatMessage.getTimestamp(),
                null,
                null,
                userMapper.entityToMinimalRetrievalDto(chatMessageEntity.getSender()),
                userMapper.entityToMinimalRetrievalDto(chatMessageEntity.getRecipient()));

        messagingTemplate.convertAndSendToUser(
                chatMessageDto.getRecipientId().toString(),
                "/queue/messages",
                savedChatMessageDto);
    }

    public List<ChatMessageDto> getChatMessages(Long senderId, Long recipientId) throws IllegalAccessException {
        UserDetails senderUserDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        UserEntity senderUserEntity = userService.getUserEntityByEmail(senderUserDetails.getUsername());

        if (!senderUserEntity.getId().equals(senderId) && !senderUserEntity.getId().equals(recipientId)) {
            throw new IllegalAccessException("You cannot view other people's chat messages");
        }

        String chatId = chatRoomService.getChatRoomId(senderId, recipientId, true).orElseThrow();
        List<ChatMessageEntity> chatMessageEntities = chatMessageRepository.findByChatId(chatId);

        chatMessageEntities.forEach(chatMessageEntity -> chatMessageEntity.setUnread(false));
        chatMessageRepository.saveAll(chatMessageEntities);

        return chatMessageEntities
                .stream()
                .map(chatMessageEntity -> new ChatMessageDto(
                        chatMessageEntity.getId(),
                        chatMessageEntity.getChatId(),
                        chatMessageEntity.getContent(),
                        chatMessageEntity.getTimestamp(),
                        null,
                        null,
                        userMapper.entityToMinimalRetrievalDto(chatMessageEntity.getSender()),
                        userMapper.entityToMinimalRetrievalDto(chatMessageEntity.getRecipient())))
                .collect(Collectors.toList());
    }

    public void setChatMessageToRead(ChatMessageDto chatMessageDto, String accessToken) {
        if (accessToken == null) {
            return;
        }

        try {
            accessToken = accessToken.substring(7);
            Long authUserId = Long.valueOf(jwtService.extractId(accessToken));

            if (!authUserId.equals(chatMessageDto.getRecipient().getId())) {
                return;
            }
        } catch (Exception e) {
            return;
        }

        ChatMessageEntity chatMessage = chatMessageRepository.findById(chatMessageDto.getId()).orElseThrow(() -> new EntityNotFoundException("Chat message with id: " + chatMessageDto.getChatId() + " was not found"));
        chatMessage.setUnread(false);
        chatMessageRepository.save(chatMessage);
    }
}
