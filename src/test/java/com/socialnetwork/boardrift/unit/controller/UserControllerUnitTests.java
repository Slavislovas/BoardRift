package com.socialnetwork.boardrift.unit.controller;

import com.socialnetwork.boardrift.enumeration.Role;
import com.socialnetwork.boardrift.enumeration.UserStatus;
import com.socialnetwork.boardrift.repository.model.UserEntity;
import com.socialnetwork.boardrift.rest.controller.UserController;
import com.socialnetwork.boardrift.rest.model.UserRegistrationDto;
import com.socialnetwork.boardrift.rest.model.UserRetrievalDto;
import com.socialnetwork.boardrift.service.UserService;
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

import java.util.Collections;
import java.util.HashMap;

import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
public class UserControllerUnitTests {
    @Mock
    UserService userService;

    @InjectMocks
    UserController userController;

    UserEntity userEntity;
    UserRegistrationDto userRegistrationDto;
    UserRetrievalDto userRetrievalDto;

    @BeforeEach
    void init(){
        userEntity = new UserEntity(1L, "Name", "Lastname", "email@gmail.com",
                "2001-11-16", "Username", "Password@123", true, false, "",
                Role.ROLE_USER, UserStatus.OFFLINE, false, Collections.EMPTY_SET,
                Collections.EMPTY_SET, Collections.EMPTY_SET, Collections.EMPTY_SET,
                Collections.EMPTY_SET, Collections.EMPTY_SET);

        userRegistrationDto = new UserRegistrationDto("Name", "Lastname",
                "email@gmail.com", "2001-11-16",
                "Username", "Password@123");

        userRetrievalDto = new UserRetrievalDto(1L, "Name", "Lastname", "email@gmail.com", "2001-11-16", "Username");

    }

    @Test
    void createUserShouldPassWhenRequestBodyValid() {
        Mockito.when(userService.createUser(any(), any())).thenReturn(userRetrievalDto);

        ResponseEntity<UserRetrievalDto> result = userController.createUser(userRegistrationDto, new MapBindingResult(Collections.EMPTY_MAP, "userRegistrationDto"), new MockHttpServletRequest());
        Assertions.assertEquals(HttpStatus.CREATED, result.getStatusCode());
        Assertions.assertEquals(userRetrievalDto, result.getBody());
    }

    @Test
    void createUserShouldFailWhenRequestBodyInvalid() {
        BindingResult bindingResult = new MapBindingResult(new HashMap<>(), "userRegistrationDto");
        bindingResult.addError(new FieldError("fieldError", "name", "Name is invalid"));

        Assertions.assertThrows(FieldValidationException.class, () -> userController.createUser(userRegistrationDto, bindingResult, new MockHttpServletRequest()));
    }
}
