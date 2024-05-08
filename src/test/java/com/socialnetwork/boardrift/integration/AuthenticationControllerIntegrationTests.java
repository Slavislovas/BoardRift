package com.socialnetwork.boardrift.integration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.socialnetwork.boardrift.enumeration.Role;
import com.socialnetwork.boardrift.repository.RefreshTokenRepository;
import com.socialnetwork.boardrift.repository.UserRepository;
import com.socialnetwork.boardrift.repository.VerificationTokenRepository;
import com.socialnetwork.boardrift.repository.model.RefreshTokenEntity;
import com.socialnetwork.boardrift.repository.model.VerificationTokenEntity;
import com.socialnetwork.boardrift.repository.model.user.SuspensionEntity;
import com.socialnetwork.boardrift.repository.model.user.UserEntity;
import com.socialnetwork.boardrift.rest.model.AuthenticationRequestDto;
import com.socialnetwork.boardrift.rest.model.AuthenticationResponseDto;
import com.socialnetwork.boardrift.rest.model.PasswordResetProcessDto;
import com.socialnetwork.boardrift.rest.model.PasswordResetRequestDto;
import com.socialnetwork.boardrift.rest.model.RefreshTokenRequestDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@SpringBootTest
public class AuthenticationControllerIntegrationTests {
    @MockBean
    RefreshTokenRepository refreshTokenRepository;

    @MockBean
    UserRepository userRepository;

    @MockBean
    VerificationTokenRepository verificationTokenRepository;

    @MockBean
    ApplicationEventPublisher applicationEventPublisher;

    @Autowired
    MockMvc mockMvc;

    RefreshTokenEntity refreshTokenEntity;
    AuthenticationRequestDto authenticationRequestDto;
    RefreshTokenRequestDto refreshTokenRequestDto;
    AuthenticationResponseDto authenticationResponseDto;
    UserEntity userEntity;
    ObjectMapper objectMapper;
    PasswordResetRequestDto passwordResetRequestDto;
    PasswordResetProcessDto passwordResetProcessDto;

    @BeforeEach
    void init() {
        authenticationRequestDto = new AuthenticationRequestDto("Username", "Password");
        refreshTokenRequestDto = new RefreshTokenRequestDto("refreshToken");
        authenticationResponseDto = new AuthenticationResponseDto("accessToken", "refreshToken");
        userEntity = new UserEntity(1L, "Name", "Lastname", "email@gmail.com",
                "2001-11-16", "Password@123", "", "", "", true, false, false, false, "",
                Role.ROLE_USER, false, Collections.EMPTY_LIST, Collections.EMPTY_SET,
                Collections.EMPTY_SET, Collections.EMPTY_LIST, Collections.EMPTY_SET, Collections.EMPTY_SET, new ArrayList<>(),
                new SuspensionEntity(1L, LocalDate.now(), "test", userEntity));
        refreshTokenEntity = new RefreshTokenEntity("refreshToken", new Date(new Date().getTime() + 5000), userEntity);
        objectMapper = new ObjectMapper();
        passwordResetRequestDto = new PasswordResetRequestDto("email@gmail.com");
        passwordResetProcessDto = new PasswordResetProcessDto("Password@123", "Password@123", "test");
    }

    @WithMockUser(username = "email@gmail.com", authorities = "ROLE_USER")
    @Test
    void refreshTokenShouldSucceed() throws Exception {
        Mockito.when(refreshTokenRepository.findByToken(any())).thenReturn(Optional.of(refreshTokenEntity));

        mockMvc.perform(post("/auth/refresh-token")
                .content(objectMapper.writeValueAsString(refreshTokenRequestDto))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @WithMockUser(username = "email@gmail.com", authorities = "ROLE_USER")
    @Test
    void createPasswordResetRequestShouldSucceed() throws Exception {
        Mockito.when(userRepository.findByEmail(any())).thenReturn(Optional.of(userEntity));
        Mockito.when(refreshTokenRepository.findByToken(any())).thenReturn(Optional.of(refreshTokenEntity));
        Mockito.when(verificationTokenRepository.save(any())).thenReturn(new VerificationTokenEntity());
        Mockito.doNothing().when(applicationEventPublisher).publishEvent(any());

        mockMvc.perform(post("/auth/reset-password")
                        .content(objectMapper.writeValueAsString(passwordResetRequestDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @WithMockUser(username = "email@gmail.com", authorities = "ROLE_USER")
    @Test
    void proccessPasswordResetRequestShouldSucceed() throws Exception {
        Mockito.when(userRepository.findByEmail(any())).thenReturn(Optional.of(userEntity));
        Mockito.when(verificationTokenRepository.findByTokenAndType(any(), any())).thenReturn(Optional.of(new VerificationTokenEntity()));
        Mockito.when(userRepository.save(any())).thenReturn(userEntity);
        Mockito.doNothing().when(verificationTokenRepository).delete(any());

        mockMvc.perform(put("/auth/reset-password")
                        .content(objectMapper.writeValueAsString(passwordResetProcessDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @WithMockUser(username = "email@gmail.com", authorities = "ROLE_USER")
    @Test
    void logoutShouldSucceed() throws Exception {
        Mockito.doNothing().when(refreshTokenRepository).deleteByToken(any());

        mockMvc.perform(delete("/auth/logout")
                        .content(objectMapper.writeValueAsString(refreshTokenRequestDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
}
