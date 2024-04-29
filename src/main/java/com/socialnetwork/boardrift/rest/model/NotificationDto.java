package com.socialnetwork.boardrift.rest.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class NotificationDto {
    private Long id;
    private String description;
    private String redirectUrl;
    private Long recipientId;
    private Date creationDate;
    private Boolean unread;
}
