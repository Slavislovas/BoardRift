package com.socialnetwork.boardrift.service;

import com.socialnetwork.boardrift.repository.ChatMessageRepository;
import com.socialnetwork.boardrift.repository.model.ChatMessageEntity;
import com.socialnetwork.boardrift.repository.model.UserEntity;
import com.socialnetwork.boardrift.rest.model.ChatMessageDto;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class ChatMessageService {
    private final ChatMessageRepository chatMessageRepository;
    private final ChatRoomService chatRoomService;
    private final UserService userService;

    public ChatMessageDto saveChatMessage(ChatMessageDto chatMessageDto) throws IllegalAccessException {
//        UserDetails senderUserDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
//        UserEntity senderUserEntity = userService.getUserEntityByUsername(senderUserDetails.getUsername());
//
//        if (tryingToSendMessageToNonFriend(chatMessageDto, senderUserEntity)) {
//            throw new IllegalAccessException("You cannot send a message to a user who is not your friend");
//        }

        String chatId = chatRoomService.getChatRoomId(chatMessageDto.getSenderId(), chatMessageDto.getRecipientId(), true).orElseThrow();

        UserEntity senderEntity = userService.getUserEntityById(chatMessageDto.getSenderId());
        UserEntity recipientUserEntity = userService.getUserEntityById(chatMessageDto.getRecipientId());
        ChatMessageEntity chatMessageEntity = new ChatMessageEntity(null, chatId, chatMessageDto.getContent(), new Date(), senderEntity, recipientUserEntity);

        ChatMessageEntity savedChatMessage = chatMessageRepository.save(chatMessageEntity);

        return new ChatMessageDto(savedChatMessage.getChatId(),
                savedChatMessage.getContent(),
                savedChatMessage.getTimestamp(),
                chatMessageEntity.getSender().getId(),
                chatMessageEntity.getRecipient().getId());
    }

    private boolean tryingToSendMessageToNonFriend(ChatMessageDto chatMessageDto, UserEntity senderUserEntity) {
        Set<UserEntity> senderFriends = senderUserEntity.getFriends();
        senderFriends.addAll(senderUserEntity.getFriendOf());

        return senderFriends
                .stream()
                .noneMatch(userEntity -> userEntity.getId().equals(chatMessageDto.getSenderId()));
    }

    public List<ChatMessageDto> getChatMessages(Long senderId, Long recipientId) throws IllegalAccessException {
        UserDetails senderUserDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        UserEntity senderUserEntity = userService.getUserEntityByUsername(senderUserDetails.getUsername());

        if (!senderUserEntity.getId().equals(senderId) && !senderUserEntity.getId().equals(recipientId)) {
            throw new IllegalAccessException("You cannot view other people's chat messages");
        }

        String chatId = chatRoomService.getChatRoomId(senderId, recipientId, true).orElseThrow();
        List<ChatMessageEntity> chatMessageEntities = chatMessageRepository.findByChatId(chatId);

        return chatMessageEntities
                .stream()
                .map(chatMessageEntity -> {
                    return new ChatMessageDto(chatMessageEntity.getChatId(),
                            chatMessageEntity.getContent(),
                            chatMessageEntity.getTimestamp(),
                            chatMessageEntity.getSender().getId(),
                            chatMessageEntity.getRecipient().getId());
                })
                .sorted(Comparator.comparing(ChatMessageDto::getTimestamp))
                .collect(Collectors.toList());
    }
}
