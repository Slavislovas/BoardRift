package com.socialnetwork.boardrift.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.socialnetwork.boardrift.enumeration.Role;
import com.socialnetwork.boardrift.repository.NotificationRepository;
import com.socialnetwork.boardrift.repository.UserRepository;
import com.socialnetwork.boardrift.repository.model.NotificationEntity;
import com.socialnetwork.boardrift.repository.model.user.UserEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@SpringBootTest
public class NotificationControllerIntegrationTests {
    @MockBean
    NotificationRepository notificationRepository;

    @MockBean
    UserRepository userRepository;

    @MockBean
    SimpMessagingTemplate simpMessagingTemplate;

    @Autowired
    MockMvc mockMvc;

    UserEntity userEntity;
    UserEntity userEntity2;
    NotificationEntity notificationEntity;
    ObjectMapper objectMapper;

    @BeforeEach
    void init() {
        Set<UserEntity> userSet = new HashSet<>();
        objectMapper = new ObjectMapper();
        userEntity = new UserEntity(1L, "Name", "Lastname", "email@gmail.com",
                "2001-11-16", "Password@123", "", "", "", true, false, false, false, "",
                Role.ROLE_USER, false, new ArrayList<>(), userSet,
                userSet,  new ArrayList<>(),  userSet, userSet, new ArrayList<>(), null);
        userSet.add(userEntity);
        userEntity2 = new UserEntity(2L, "Name2", "Lastname2", "email2@gmail.com",
                "2001-11-16", "Password@123", "", "", "", true, false, false, false, "",
                Role.ROLE_USER, false, new ArrayList<>(),  userSet,
                userSet,  new ArrayList<>(),  userSet, userSet, new ArrayList<>(), null);
        notificationEntity = new NotificationEntity(1L, "description", "redirectUrl", new Date(), false, userEntity);
    }

    @WithMockUser(username = "email@gmail.com", authorities = "ROLE_ADMIN")
    @Test
    void getNotificationsShouldSucceed() throws Exception {
        Mockito.when(userRepository.findByEmail(any())).thenReturn(Optional.of(userEntity));
        Mockito.when(notificationRepository.findByRecipientId(any(), any())).thenReturn(List.of(notificationEntity));

        mockMvc.perform(get("/notifications")
                        .param("page", "0")
                        .param("pageSize", "1"))
                .andExpect(status().isOk());
    }

    @WithMockUser(username = "email@gmail.com", authorities = "ROLE_ADMIN")
    @Test
    void setNotificationToReadShouldSucceed() throws Exception {
        Mockito.when(userRepository.findByEmail(any())).thenReturn(Optional.of(userEntity));
        Mockito.when(notificationRepository.findById(any())).thenReturn(Optional.of(notificationEntity));
        Mockito.when(notificationRepository.save(any())).thenReturn(notificationEntity);

        mockMvc.perform(put("/notifications/1"))
                .andExpect(status().isOk());
    }

    @WithMockUser(username = "email@gmail.com", authorities = "ROLE_ADMIN")
    @Test
    void deleteNotificationShouldSucceed() throws Exception {
        Mockito.when(userRepository.findByEmail(any())).thenReturn(Optional.of(userEntity));
        Mockito.when(notificationRepository.findById(any())).thenReturn(Optional.of(notificationEntity));
        Mockito.doNothing().when(notificationRepository).delete(any());

        mockMvc.perform(delete("/notifications/1"))
                .andExpect(status().isOk());
    }
}
