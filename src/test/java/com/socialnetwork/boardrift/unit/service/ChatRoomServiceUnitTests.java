package com.socialnetwork.boardrift.unit.service;

import com.socialnetwork.boardrift.repository.ChatMessageRepository;
import com.socialnetwork.boardrift.repository.ChatRoomRepository;
import com.socialnetwork.boardrift.repository.UserRepository;
import com.socialnetwork.boardrift.repository.model.ChatRoomEntity;
import com.socialnetwork.boardrift.repository.model.user.UserEntity;
import com.socialnetwork.boardrift.service.ChatRoomService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith({MockitoExtension.class})
public class ChatRoomServiceUnitTests {
    @Mock
    private ChatRoomRepository chatRoomRepository;

    @Mock
    private ChatMessageRepository chatMessageRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ChatRoomService chatRoomService;

    @Test
    void getChatRoomId_shouldSucceed_createChatRoomIfEmptyTrue() {
        UserEntity sender = new UserEntity();
        UserEntity recipient = new UserEntity();

        sender.setId(1L);
        recipient.setId(2L);

        ChatRoomEntity chatRoomEntity = new ChatRoomEntity(1L, "1_2", sender, recipient);

        Mockito.when(chatRoomRepository.findBySenderIdAndRecipientId(any(), any())).thenReturn(Optional.empty());
        Mockito.lenient().when(userRepository.findById(1L)).thenReturn(Optional.of(sender));
        Mockito.lenient().when(userRepository.findById(2L)).thenReturn(Optional.of(recipient));
        Mockito.lenient().when(chatRoomRepository.save(any())).thenReturn(chatRoomEntity);

        Optional<String> result = chatRoomService.getChatRoomId(sender.getId(), recipient.getId(), true);
        assertTrue(result.isPresent());
        assertEquals("1_2", result.get());
    }

    @Test
    void getChatRoomId_shouldSucceed_createChatRoomIfEmptyFalse() {
        UserEntity sender = new UserEntity();
        UserEntity recipient = new UserEntity();

        sender.setId(1L);
        recipient.setId(2L);

        Mockito.when(chatRoomRepository.findBySenderIdAndRecipientId(any(), any())).thenReturn(Optional.empty());

        Optional<String> result = chatRoomService.getChatRoomId(sender.getId(), recipient.getId(), false);
        assertTrue(result.isEmpty());
    }

    @Test
    void deleteChatRoomByChatId() {
        Mockito.doNothing().when(chatRoomRepository).deleteByChatId(any());
        Mockito.doNothing().when(chatMessageRepository).deleteByChatId(any());

        chatRoomService.deleteChatRoomByChatId("test");
    }
}
