package com.socialnetwork.boardrift.unit.controller;

import com.socialnetwork.boardrift.rest.controller.AuthenticationController;
import com.socialnetwork.boardrift.rest.model.AuthenticationRequestDto;
import com.socialnetwork.boardrift.rest.model.AuthenticationResponseDto;
import com.socialnetwork.boardrift.rest.model.RefreshTokenRequestDto;
import com.socialnetwork.boardrift.service.AuthenticationService;
import com.socialnetwork.boardrift.util.exception.FieldValidationException;
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
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.MapBindingResult;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;

import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
public class AuthenticationControllerUnitTests {
    @Mock
    AuthenticationService authenticationService;

    @InjectMocks
    AuthenticationController authenticationController;

    AuthenticationRequestDto authenticationRequestDto;
    RefreshTokenRequestDto refreshTokenRequestDto;
    AuthenticationResponseDto authenticationResponseDto;

    @BeforeEach
    void init() {
        authenticationRequestDto = new AuthenticationRequestDto("Username", "Password");
        refreshTokenRequestDto = new RefreshTokenRequestDto("refreshToken");
        authenticationResponseDto = new AuthenticationResponseDto("accessToken", "refreshToken");
    }

    @Test
    void authenticateShouldSucceedWhenRequestBodyValid() {
        Mockito.when(authenticationService.authenticate(any())).thenReturn(authenticationResponseDto);
        ResponseEntity<AuthenticationResponseDto> result = authenticationController.authenticate(authenticationRequestDto, new MapBindingResult(Collections.EMPTY_MAP, "userRegistrationDto"));
        Assertions.assertEquals(HttpStatus.OK, result.getStatusCode());
        Assertions.assertEquals(authenticationResponseDto, result.getBody());
    }

    @Test
    void authenticateShouldFailWhenRequestBodyInvalid() {
        BindingResult bindingResult = new MapBindingResult(new HashMap<>(), "userRegistrationDto");
        bindingResult.addError(new FieldError("fieldError", "username", "Username is required"));

        Assertions.assertThrows(FieldValidationException.class, () -> authenticationController.authenticate(authenticationRequestDto, bindingResult));
    }

    @Test
    void refreshTokenShouldSucceedWhenRequestBodyValid() throws IOException {
        Mockito.when(authenticationService.refreshToken(any())).thenReturn(authenticationResponseDto);
        ResponseEntity<AuthenticationResponseDto> result = authenticationController.refreshToken(refreshTokenRequestDto, new MapBindingResult(Collections.EMPTY_MAP, "userRegistrationDto"));
        Assertions.assertEquals(HttpStatus.OK, result.getStatusCode());
        Assertions.assertEquals(authenticationResponseDto, result.getBody());
    }

    @Test
    void refreshTokenShouldFailWhenRequestBodyInvalid() {
        BindingResult bindingResult = new MapBindingResult(new HashMap<>(), "userRegistrationDto");
        bindingResult.addError(new FieldError("fieldError", "token", "Token is required"));

        Assertions.assertThrows(FieldValidationException.class, () -> authenticationController.refreshToken(refreshTokenRequestDto, bindingResult));
    }
}
