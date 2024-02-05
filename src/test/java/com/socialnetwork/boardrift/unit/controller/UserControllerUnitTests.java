package com.socialnetwork.boardrift.unit.controller;

import com.socialnetwork.boardrift.enumeration.Role;
import com.socialnetwork.boardrift.enumeration.UserStatus;
import com.socialnetwork.boardrift.repository.model.UserEntity;
import com.socialnetwork.boardrift.rest.controller.UserController;
import com.socialnetwork.boardrift.rest.model.FriendRequestDto;
import com.socialnetwork.boardrift.rest.model.UserRegistrationDto;
import com.socialnetwork.boardrift.rest.model.UserRetrievalDto;
import com.socialnetwork.boardrift.rest.model.UserRetrievalMinimalDto;
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
import java.util.Set;

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
    UserRetrievalMinimalDto userRetrievalMinimalDto;
    FriendRequestDto friendRequestDto;

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

        userRetrievalDto = new UserRetrievalDto(1L, "Name", "Lastname", "email@gmail.com", "2001-11-16", "Username", "");
        userRetrievalMinimalDto = new UserRetrievalMinimalDto(1L, "Name", "Lastname", "2001-11-16", "");
        friendRequestDto = new FriendRequestDto(userRetrievalMinimalDto, userRetrievalMinimalDto);
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

    @Test
    void confirmUserRegistrationShouldSucceed() {
        Mockito.doNothing().when(userService).confirmUserRegistration(any());
        ResponseEntity<Void> result = userController.confirmUserRegistration("token");

        Assertions.assertEquals(HttpStatus.OK, result.getStatusCode());
    }

    @Test
    void getReceivedFriendRequestsShouldSucceed() {
        Mockito.when(userService.getReceivedFriendRequests()).thenReturn(Set.of(userRetrievalMinimalDto));
        ResponseEntity<Set<UserRetrievalMinimalDto>> result = userController.getReceivedFriendRequests();

        Assertions.assertEquals(HttpStatus.OK, result.getStatusCode());
        Assertions.assertEquals(Set.of(userRetrievalMinimalDto), result.getBody());
    }

    @Test
    void getSentFriendRequestsShouldSucceed() {
        Mockito.when(userService.getSentFriendRequests()).thenReturn(Set.of(userRetrievalMinimalDto));
        ResponseEntity<Set<UserRetrievalMinimalDto>> result = userController.getSentFriendRequests();

        Assertions.assertEquals(HttpStatus.OK, result.getStatusCode());
        Assertions.assertEquals(Set.of(userRetrievalMinimalDto), result.getBody());
    }

    @Test
    void getFriendsShouldSucceed() throws IllegalAccessException {
        Mockito.when(userService.getFriends(any())).thenReturn(Set.of(userRetrievalMinimalDto));
        ResponseEntity<Set<UserRetrievalMinimalDto>> result = userController.getFriends(1L);

        Assertions.assertEquals(HttpStatus.OK, result.getStatusCode());
        Assertions.assertEquals(Set.of(userRetrievalMinimalDto), result.getBody());
    }

    @Test
    void sendFriendRequestShouldSucceed() {
        Mockito.when(userService.sendFriendRequest(any())).thenReturn(friendRequestDto);
        ResponseEntity<FriendRequestDto> result = userController.sendFriendRequest(1L);

        Assertions.assertEquals(HttpStatus.CREATED, result.getStatusCode());
        Assertions.assertEquals(friendRequestDto, result.getBody());
    }

    @Test
    void acceptFriendRequestShouldSucceed() {
        Mockito.when(userService.acceptFriendRequest(any())).thenReturn(userRetrievalMinimalDto);
        ResponseEntity<UserRetrievalMinimalDto> result = userController.acceptFriendRequest(1L);

        Assertions.assertEquals(HttpStatus.OK, result.getStatusCode());
        Assertions.assertEquals(userRetrievalMinimalDto, result.getBody());
    }

    @Test
    void declineFriendRequestShouldSucceed() {
        Mockito.when(userService.declineFriendRequest(any())).thenReturn("Friend request declined successfully");
        ResponseEntity<String> result = userController.declineFriendRequest(1L);

        Assertions.assertEquals(HttpStatus.OK, result.getStatusCode());
        Assertions.assertEquals("Friend request declined successfully", result.getBody());
    }
}
