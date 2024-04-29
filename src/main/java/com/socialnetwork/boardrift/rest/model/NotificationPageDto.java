package com.socialnetwork.boardrift.rest.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class NotificationPageDto {
    String nextPageUrl;
    Boolean unreadNotifications;
    List<NotificationDto> notifications;
}
