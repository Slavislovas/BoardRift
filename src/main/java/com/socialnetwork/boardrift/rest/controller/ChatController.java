package com.socialnetwork.boardrift.rest.controller;

import com.socialnetwork.boardrift.rest.model.ChatMessageDto;
import com.socialnetwork.boardrift.service.ChatMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
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

    @MessageMapping("/chat")
    public void processMessage(@Payload ChatMessageDto chatMessageDto,
                               @Header(value = "Authorization", required = false) String accessToken) {
        chatMessageService.sendAndSaveChatMessage(chatMessageDto, accessToken);
    }

    @MessageMapping("/chat/messages/read")
    public void setMessageToRead(@Payload ChatMessageDto chatMessageDto,
                               @Header(value = "Authorization", required = false) String accessToken) {
        chatMessageService.setChatMessageToRead(chatMessageDto, accessToken);
    }

    @GetMapping("/{senderId}/{recipientId}")
    public ResponseEntity<List<ChatMessageDto>> getChatMessages(@PathVariable("senderId") Long senderId,
                                                                @PathVariable("recipientId") Long recipientId) throws IllegalAccessException {
        return ResponseEntity.ok(chatMessageService.getChatMessages(senderId, recipientId));
    }
}
