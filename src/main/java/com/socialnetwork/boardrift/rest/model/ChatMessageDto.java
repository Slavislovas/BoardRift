package com.socialnetwork.boardrift.rest.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageDto {
    String chatId;
    private String content;
    private Date timestamp;
    private Long senderId;
    private Long recipientId;
    private UserRetrievalMinimalDto sender = null;
    private UserRetrievalMinimalDto recipient = null;
}
