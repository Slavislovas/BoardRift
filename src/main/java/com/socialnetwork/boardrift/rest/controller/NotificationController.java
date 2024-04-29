package com.socialnetwork.boardrift.rest.controller;

import com.socialnetwork.boardrift.rest.model.NotificationDto;
import com.socialnetwork.boardrift.rest.model.NotificationPageDto;
import com.socialnetwork.boardrift.service.NotificationService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RequiredArgsConstructor
@RestController
public class NotificationController {
    private final NotificationService notificationService;

    @GetMapping("/notifications")
    public ResponseEntity<NotificationPageDto> getNotifications(@RequestParam(value = "page", required = false) Integer page,
                                                                @RequestParam(value = "pageSize", required = false) Integer pageSize,
                                                                HttpServletRequest request) {
        return ResponseEntity.ok(notificationService.getNotifications(page, pageSize, request));
    }

    @PutMapping("/notifications/{notificationId}")
    public ResponseEntity<Map<String, Object>> setNotificationToRead(@PathVariable("notificationId") Long notificationId) throws IllegalAccessException {
        return ResponseEntity.ok(notificationService.setNotificationToRead(notificationId));
    }

    @DeleteMapping("/notifications/{notificationId}")
    public ResponseEntity<Void> deleteNotification(@PathVariable("notificationId") Long notificationId) throws IllegalAccessException {
        notificationService.deleteNotification(notificationId);
        return ResponseEntity.ok().build();
    }
}
