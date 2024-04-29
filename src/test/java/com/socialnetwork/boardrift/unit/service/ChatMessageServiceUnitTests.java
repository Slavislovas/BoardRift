package com.socialnetwork.boardrift.unit.service;

import com.socialnetwork.boardrift.repository.ChatMessageRepository;
import com.socialnetwork.boardrift.repository.model.ChatMessageEntity;
import com.socialnetwork.boardrift.repository.model.user.UserEntity;
import com.socialnetwork.boardrift.rest.model.ChatMessageDto;
import com.socialnetwork.boardrift.rest.model.user.UserRetrievalMinimalDto;
import com.socialnetwork.boardrift.service.ChatMessageService;
import com.socialnetwork.boardrift.service.ChatRoomService;
import com.socialnetwork.boardrift.service.JwtService;
import com.socialnetwork.boardrift.service.UserService;
import com.socialnetwork.boardrift.util.mapper.UserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith({MockitoExtension.class})
public class ChatMessageServiceUnitTests {
    @Mock
    private ChatMessageRepository chatMessageRepository;
    @Mock
    private ChatRoomService chatRoomService;
    @Mock
    private UserService userService;
    @Mock
    private UserMapper userMapper;
    @Mock
    private SimpMessagingTemplate messagingTemplate;
    @Mock
    private JwtService jwtService;
    @InjectMocks
    ChatMessageService chatMessageService;

    @BeforeEach
    void setUp() {
        Authentication authentication = new UsernamePasswordAuthenticationToken(new UserEntity(), null);
        SecurityContext securityContext = mock(SecurityContext.class);
        lenient().when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void sendAndSaveChatMessage_shouldSucceed() {
        ChatMessageDto chatMessageDto = new ChatMessageDto();
        UserEntity sender = new UserEntity();
        UserEntity recipient = new UserEntity();
        ChatMessageEntity chatMessage = new ChatMessageEntity(1L, "1_2", "text", new Date(), true, sender, recipient);

        sender.setId(1L);
        recipient.setId(2L);

        chatMessageDto.setSenderId(1L);
        chatMessageDto.setRecipientId(2L);

        Mockito.when(jwtService.extractId(any())).thenReturn("1");
        Mockito.when(userService.getUserEntityById(1L)).thenReturn(sender);
        Mockito.when(userService.getUserEntityById(2L)).thenReturn(recipient);
        Mockito.when(userService.recipientAlreadyFriend(any(), any())).thenReturn(true);
        Mockito.when(chatRoomService.getChatRoomId(1L, 2L, true)).thenReturn(Optional.of("1_2"));
        Mockito.when(chatMessageRepository.save(any())).thenReturn(chatMessage);
        Mockito.when(userMapper.entityToMinimalRetrievalDto(any())).thenReturn(null);
        Mockito.doNothing().when(messagingTemplate).convertAndSendToUser(any(), any(), any());

        chatMessageService.sendAndSaveChatMessage(chatMessageDto, "Bearer test");
    }

    @Test
    void getChatMessages_shouldSucceed() throws IllegalAccessException {
        UserEntity userEntity = new UserEntity();
        userEntity.setId(1L);

        ChatMessageEntity chatMessage = new ChatMessageEntity(1L, "1_2", "text", new Date(), false, null, null);
        ChatMessageDto chatMessageDto = new ChatMessageDto(1L, "1_2", "text", chatMessage.getTimestamp(), null, null, null, null);
        Mockito.when(userService.getUserEntityByEmail(any())).thenReturn(userEntity);
        Mockito.when(chatRoomService.getChatRoomId(1L, 2L, true)).thenReturn(Optional.of("1_2"));
        Mockito.when(chatMessageRepository.findByChatId("1_2")).thenReturn(List.of(chatMessage));
        Mockito.when(chatMessageRepository.saveAll(any())).thenReturn(List.of(chatMessage));
        Mockito.when(userMapper.entityToMinimalRetrievalDto(any())).thenReturn(null);

        List<ChatMessageDto> result = chatMessageService.getChatMessages(1L, 2L);

        assertTrue(result.containsAll(List.of(chatMessageDto)));
    }

    @Test
    void setChatMessageToRead_shouldSucceed() {
        UserRetrievalMinimalDto sender = new UserRetrievalMinimalDto();
        UserRetrievalMinimalDto recipient = new UserRetrievalMinimalDto();

        sender.setId(2L);
        recipient.setId(1L);

        ChatMessageEntity chatMessage = new ChatMessageEntity(1L, "1_2", "text", new Date(), false, null, null);
        ChatMessageDto chatMessageDto = new ChatMessageDto(1L, "1_2", "text", chatMessage.getTimestamp(), 2L, 1L, sender, recipient);

        Mockito.when(jwtService.extractId(any())).thenReturn("1");
        Mockito.when(chatMessageRepository.findById(any())).thenReturn(Optional.of(chatMessage));
        Mockito.when(chatMessageRepository.save(any())).thenReturn(chatMessage);

        chatMessageService.setChatMessageToRead(chatMessageDto, "Bearer token");

        verify(chatMessageRepository, times(1)).save(any());
    }
}
