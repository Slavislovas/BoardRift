package com.socialnetwork.boardrift.unit.controller;

import com.socialnetwork.boardrift.rest.controller.ChatController;
import com.socialnetwork.boardrift.rest.model.ChatMessageDto;
import com.socialnetwork.boardrift.service.ChatMessageService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Date;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
public class ChatControllerUnitTests {
    @Mock
    ChatMessageService chatMessageService;

    @InjectMocks
    ChatController chatController;

    ChatMessageDto chatMessageDto1;
    ChatMessageDto chatMessageDto2;

    @BeforeEach
    void init() {
        chatMessageDto1 = new ChatMessageDto(1L, "1_2", "content", new Date(), 1L, 2L, null, null);
        chatMessageDto2 = new ChatMessageDto(2L, "1_2", "content", new Date(), 1L, 2L, null, null);
    }

    @Test
    void getChatMessagesShouldSucceed() throws IllegalAccessException {
        Mockito.when(chatMessageService.getChatMessages(any(), any())).thenReturn(List.of(chatMessageDto1, chatMessageDto2));
        ResponseEntity<List<ChatMessageDto>> result = chatController.getChatMessages(1L, 2L);

        Assertions.assertEquals(HttpStatus.OK, result.getStatusCode());
        Assertions.assertEquals(List.of(chatMessageDto1, chatMessageDto2), result.getBody());
    }

    @Test
    void processMessageShouldSucceed() {
        Mockito.doNothing().when(chatMessageService).sendAndSaveChatMessage(any(), any());
        chatController.processMessage(chatMessageDto1, "token");
    }

    @Test
    void setMessageToReadShouldSucceed() {
        Mockito.doNothing().when(chatMessageService).setChatMessageToRead(any(), any());
        chatController.setMessageToRead(chatMessageDto1, "token");
    }
}
