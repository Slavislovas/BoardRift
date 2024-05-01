package com.socialnetwork.boardrift.unit.controller;

import com.socialnetwork.boardrift.rest.controller.NotificationController;
import com.socialnetwork.boardrift.rest.model.NotificationDto;
import com.socialnetwork.boardrift.rest.model.NotificationPageDto;
import com.socialnetwork.boardrift.service.NotificationService;
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
import org.springframework.mock.web.MockHttpServletRequest;

import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
public class NotificationControllerUnitTests {
    @Mock
    NotificationService notificationService;

    @InjectMocks
    NotificationController notificationController;

    NotificationPageDto notificationPageDto;

    @BeforeEach
    void init() {
        notificationPageDto = new NotificationPageDto("url", false,
                List.of(new NotificationDto(1L, "description", "redirectUrl", 1L, new Date(), false)));
    }

    @Test
    void getNotificationsShouldSucceed() {
        Mockito.when(notificationService.getNotifications(any(), any(), any())).thenReturn(notificationPageDto);
        ResponseEntity<NotificationPageDto> result = notificationController.getNotifications(1, 10, new MockHttpServletRequest());

        Assertions.assertEquals(HttpStatus.OK, result.getStatusCode());
        Assertions.assertEquals(notificationPageDto, result.getBody());
    }

    @Test
    void setNotificationToReadShouldSucceed() throws IllegalAccessException {
        Map<String, Object> expected = Map.of("test", "test");
        Mockito.when(notificationService.setNotificationToRead(any())).thenReturn(expected);
        ResponseEntity<Map<String, Object>> result = notificationController.setNotificationToRead(1L);

        Assertions.assertEquals(HttpStatus.OK, result.getStatusCode());
        Assertions.assertEquals(expected, result.getBody());
    }
}
