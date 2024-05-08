package com.socialnetwork.boardrift.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.socialnetwork.boardrift.enumeration.Role;
import com.socialnetwork.boardrift.repository.ChatMessageRepository;
import com.socialnetwork.boardrift.repository.ChatRoomRepository;
import com.socialnetwork.boardrift.repository.UserRepository;
import com.socialnetwork.boardrift.repository.model.ChatMessageEntity;
import com.socialnetwork.boardrift.repository.model.ChatRoomEntity;
import com.socialnetwork.boardrift.repository.model.user.UserEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
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
public class ChatControllerIntegrationTests {
    @MockBean
    ChatMessageRepository chatMessageRepository;

    @MockBean
    ChatRoomRepository chatRoomRepository;

    @MockBean
    UserRepository userRepository;

    @Autowired
    MockMvc mockMvc;

    ChatMessageEntity chatMessage;
    UserEntity userEntity;
    UserEntity userEntity2;
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
        chatMessage = new ChatMessageEntity(1L, "1_2", "text", new Date(), true, userEntity, userEntity2);
    }

    @WithMockUser(username = "email@gmail.com", authorities = "ROLE_ADMIN")
    @Test
    void getChatMessagesShouldSucceed() throws Exception {
        Mockito.when(userRepository.findByEmail(any())).thenReturn(Optional.of(userEntity));
        Mockito.when(chatRoomRepository.findBySenderIdAndRecipientId(any(), any())).
                thenReturn(Optional.of(new ChatRoomEntity(1L, "1_2", userEntity, userEntity2)));
        Mockito.when(chatMessageRepository.findByChatId(any())).thenReturn(List.of(chatMessage));
        Mockito.when(chatMessageRepository.saveAll(any())).thenReturn(List.of(chatMessage));

        mockMvc.perform(get("/messages/1/2"))
                .andExpect(status().isOk());
    }
}
