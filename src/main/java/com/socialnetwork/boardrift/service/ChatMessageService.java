package com.socialnetwork.boardrift.service;

import com.socialnetwork.boardrift.repository.ChatMessageRepository;
import com.socialnetwork.boardrift.repository.model.ChatMessageEntity;
import com.socialnetwork.boardrift.repository.model.UserEntity;
import com.socialnetwork.boardrift.rest.model.ChatMessageDto;
import com.socialnetwork.boardrift.util.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

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
    private final UserMapper userMapper;

    public ChatMessageDto saveChatMessage(ChatMessageDto chatMessageDto) throws IllegalAccessException {
        String chatId = chatRoomService.getChatRoomId(chatMessageDto.getSenderId(), chatMessageDto.getRecipientId(), true).orElseThrow();

        UserEntity senderEntity = userService.getUserEntityById(chatMessageDto.getSenderId());
        UserEntity recipientUserEntity = userService.getUserEntityById(chatMessageDto.getRecipientId());
        ChatMessageEntity chatMessageEntity = new ChatMessageEntity(null, chatId, chatMessageDto.getContent(), new Date(), senderEntity, recipientUserEntity);

        ChatMessageEntity savedChatMessage = chatMessageRepository.save(chatMessageEntity);

        return new ChatMessageDto(savedChatMessage.getChatId(),
                savedChatMessage.getContent(),
                savedChatMessage.getTimestamp(),
                null,
                null,
                userMapper.entityToMinimalRetrievalDto(chatMessageEntity.getSender()),
                userMapper.entityToMinimalRetrievalDto(chatMessageEntity.getRecipient()));
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
        UserEntity senderUserEntity = userService.getUserEntityByEmail(senderUserDetails.getUsername());

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
                            null,
                            null,
                            userMapper.entityToMinimalRetrievalDto(chatMessageEntity.getSender()),
                            userMapper.entityToMinimalRetrievalDto(chatMessageEntity.getRecipient()));
                })
                .sorted(Comparator.comparing(ChatMessageDto::getTimestamp))
                .collect(Collectors.toList());
    }
}
