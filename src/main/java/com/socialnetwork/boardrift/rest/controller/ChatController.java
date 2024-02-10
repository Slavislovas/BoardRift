package com.socialnetwork.boardrift.rest.controller;

import com.socialnetwork.boardrift.rest.model.ChatMessageDto;
import com.socialnetwork.boardrift.service.ChatMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequestMapping("/messages")
@RequiredArgsConstructor
@RestController
public class ChatController {
    private final ChatMessageService chatMessageService;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/chat")
    public void processMessage(@Payload ChatMessageDto chatMessageDto) throws IllegalAccessException {
        ChatMessageDto savedChatMessageDto = chatMessageService.saveChatMessage(chatMessageDto);

        messagingTemplate.convertAndSendToUser(
                chatMessageDto.getRecipientId().toString(),
                "/queue/messages",
                savedChatMessageDto);
    }

    @GetMapping("/{senderId}/{recipientId}")
    public ResponseEntity<List<ChatMessageDto>> getChatMessages(@PathVariable("senderId") Long senderId, @PathVariable("recipientId") Long recipientId) throws IllegalAccessException {
        return ResponseEntity.ok(chatMessageService.getChatMessages(senderId, recipientId));
    }
}
