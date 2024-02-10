package com.socialnetwork.boardrift.rest.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatNotificationDto {
    private String chatId;
    private Long senderId;
    private Long recipientId;
    private String contents;
}
