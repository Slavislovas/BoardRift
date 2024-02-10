package com.socialnetwork.boardrift.rest.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageDto {
    String chatId;
    private String content;
    private Date timestamp;
    private Long senderId;
    private Long recipientId;
}
