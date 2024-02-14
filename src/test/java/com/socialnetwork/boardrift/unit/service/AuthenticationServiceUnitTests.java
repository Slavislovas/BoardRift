package com.socialnetwork.boardrift.unit.service;

import com.socialnetwork.boardrift.enumeration.Role;
import com.socialnetwork.boardrift.enumeration.UserStatus;
import com.socialnetwork.boardrift.repository.UserRepository;
import com.socialnetwork.boardrift.repository.model.RefreshTokenEntity;
import com.socialnetwork.boardrift.repository.model.UserEntity;
import com.socialnetwork.boardrift.rest.model.AuthenticationRequestDto;
import com.socialnetwork.boardrift.rest.model.AuthenticationResponseDto;
import com.socialnetwork.boardrift.rest.model.RefreshTokenRequestDto;
import com.socialnetwork.boardrift.service.AuthenticationService;
import com.socialnetwork.boardrift.service.JwtService;
import com.socialnetwork.boardrift.util.exception.EmailNotVerifiedException;
import com.socialnetwork.boardrift.util.exception.InvalidLoginCredentialsException;
import com.socialnetwork.boardrift.util.exception.TokenRefreshException;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.time.Instant;
import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
public class AuthenticationServiceUnitTests {
    @Mock
    UserRepository userRepository;

    @Mock
    JwtService jwtService;

    @Mock
    AuthenticationManager authenticationManager;

    @InjectMocks
    AuthenticationService authenticationService;

    AuthenticationRequestDto authenticationRequestDto;
    RefreshTokenRequestDto refreshTokenRequestDto;
    AuthenticationResponseDto authenticationResponseDto;
    UserEntity userEntity;
    RefreshTokenEntity refreshTokenEntity;

    @BeforeEach
    void init() {
        authenticationRequestDto = new AuthenticationRequestDto("Username", "Password");
        refreshTokenRequestDto = new RefreshTokenRequestDto("refreshToken");
        authenticationResponseDto = new AuthenticationResponseDto("accessToken", "refreshToken");
        userEntity = new UserEntity(1L, "Name", "Lastname", "email@gmail.com",
                "2001-11-16", "Username", "Password@123", true, false, "",
                Role.ROLE_USER, UserStatus.OFFLINE, false, Collections.EMPTY_SET,
                Collections.EMPTY_SET, Collections.EMPTY_LIST, Collections.EMPTY_SET, Collections.EMPTY_SET);
        refreshTokenEntity = new RefreshTokenEntity("refreshToken", new Date(), userEntity);
    }

    @Test
    void authenticateShouldSucceed() {
        userEntity.setEmailVerified(true);
        Mockito.when(authenticationManager.authenticate(any())).thenReturn(new UsernamePasswordAuthenticationToken(null, null, null));
        Mockito.when(userRepository.findByUsername(any())).thenReturn(Optional.of(userEntity));
        Mockito.when(jwtService.checkIfRefreshTokenExistsByUserId(any())).thenReturn(true);
        Mockito.doNothing().when(jwtService).deleteRefreshTokenByUserId(any());
        Mockito.when(jwtService.saveRefreshToken(any())).thenReturn(new RefreshTokenEntity("refreshToken", refreshTokenEntity.getExpirationDate(), userEntity));
        Mockito.when(jwtService.generateToken(any())).thenReturn("accessToken");

        AuthenticationResponseDto result = authenticationService.authenticate(authenticationRequestDto);

        Assertions.assertEquals(authenticationResponseDto, result);
    }

    @Test
    void authenticationShouldFailWhenInvalidLoginCredentials() {
        Mockito.when(authenticationManager.authenticate(any())).thenThrow(RuntimeException.class);
        Assertions.assertThrows(InvalidLoginCredentialsException.class, () -> authenticationService.authenticate(authenticationRequestDto));
    }

    @Test
    void authenticationShouldFailWhenUserNotFound() {
        Mockito.when(authenticationManager.authenticate(any())).thenReturn(new UsernamePasswordAuthenticationToken(null, null, null));
        Mockito.when(userRepository.findByUsername(any())).thenReturn(Optional.empty());

        Assertions.assertThrows(EntityNotFoundException.class, () -> authenticationService.authenticate(authenticationRequestDto));
    }

    @Test
    void authenticationShouldFailWhenEmailNotVerified() {
        Mockito.when(authenticationManager.authenticate(any())).thenReturn(new UsernamePasswordAuthenticationToken(null, null, null));
        Mockito.when(userRepository.findByUsername(any())).thenReturn(Optional.of(userEntity));

        Assertions.assertThrows(EmailNotVerifiedException.class, () -> authenticationService.authenticate(authenticationRequestDto));
    }

    @Test
    void refreshTokenShouldSucceed() {
        refreshTokenEntity.setExpirationDate(new Date(new Date().getTime() + 5000));
        Mockito.when(jwtService.findRefreshTokenEntityByToken(any())).thenReturn(refreshTokenEntity);
        Mockito.when(jwtService.generateToken(refreshTokenEntity.getUser())).thenReturn("accessToken");

        AuthenticationResponseDto result = authenticationService.refreshToken(refreshTokenRequestDto);

        Assertions.assertEquals(authenticationResponseDto, result);
    }

    @Test
    void refreshTokenShouldFailWhenTokenInvalid() {
        refreshTokenEntity.setExpirationDate(new Date(System.currentTimeMillis() - 500000));
        Mockito.when(jwtService.findRefreshTokenEntityByToken(any())).thenReturn(refreshTokenEntity);
        Mockito.doNothing().when(jwtService).deleteRefreshTokenByUserId(refreshTokenEntity.getUser().getId());

        Assertions.assertThrows(TokenRefreshException.class, () -> authenticationService.refreshToken(refreshTokenRequestDto));
    }
}
