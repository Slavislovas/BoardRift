package com.socialnetwork.boardrift.unit.controller;

import com.socialnetwork.boardrift.enumeration.Role;
import com.socialnetwork.boardrift.enumeration.UserStatus;
import com.socialnetwork.boardrift.repository.model.user.SuspensionEntity;
import com.socialnetwork.boardrift.repository.model.user.UserEntity;
import com.socialnetwork.boardrift.rest.controller.UserController;
import com.socialnetwork.boardrift.rest.model.FriendRequestDto;
import com.socialnetwork.boardrift.rest.model.user.UserRegistrationDto;
import com.socialnetwork.boardrift.rest.model.user.UserRetrievalDto;
import com.socialnetwork.boardrift.rest.model.user.UserRetrievalMinimalDto;
import com.socialnetwork.boardrift.service.UserService;
import com.socialnetwork.boardrift.util.exception.FieldValidationException;
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
import org.springframework.web.servlet.view.RedirectView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

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
                "2001-11-16", "Password@123", "", "", "", true, false, false, false, "",
                Role.ROLE_USER, UserStatus.OFFLINE, false, Collections.EMPTY_LIST, Collections.EMPTY_SET,
                Collections.EMPTY_SET, Collections.EMPTY_LIST, Collections.EMPTY_SET, Collections.EMPTY_SET, new ArrayList<>(), null);

        userRegistrationDto = new UserRegistrationDto("Name", "Lastname",
                "email@gmail.com", "2001-11-16", "Password@123");

        userRetrievalDto = new UserRetrievalDto(1L, "Name", "Lastname", "email@gmail.com", "2001-11-16", "", "", "", "", false, false,
        false, false, false, false, false, false, false, new ArrayList<>());
        userRetrievalMinimalDto = new UserRetrievalMinimalDto(1L, "Name", "Lastname", "", UserStatus.OFFLINE, false, 0, false);
        friendRequestDto = new FriendRequestDto(userRetrievalMinimalDto, userRetrievalMinimalDto);
    }

    @Test
    void searchFriendsByNameShouldSucceed() throws IllegalAccessException {
        // Arrange
        Set<UserRetrievalMinimalDto> expectedUsers = new HashSet<>(); // assuming this is populated somehow
        when(userService.searchUsers(any(), any(), any())).thenReturn(expectedUsers);

        // Act
        ResponseEntity<Set<UserRetrievalMinimalDto>> response = userController.searchUsers("userId", 0, 2);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedUsers, response.getBody());
        verify(userService, times(1)).searchUsers("userId", 0, 2);
    }

    @Test
    void createUserShouldPassWhenRequestBodyValid() {
        when(userService.createUser(any(), any())).thenReturn(userRetrievalDto);

        ResponseEntity<UserRetrievalDto> result = userController.createUser(userRegistrationDto, new MapBindingResult(Collections.EMPTY_MAP, "userRegistrationDto"), new MockHttpServletRequest());
        assertEquals(HttpStatus.CREATED, result.getStatusCode());
        assertEquals(userRetrievalDto, result.getBody());
    }

    @Test
    void createUserShouldFailWhenRequestBodyInvalid() {
        BindingResult bindingResult = new MapBindingResult(new HashMap<>(), "userRegistrationDto");
        bindingResult.addError(new FieldError("fieldError", "name", "Name is invalid"));

        assertThrows(FieldValidationException.class, () -> userController.createUser(userRegistrationDto, bindingResult, new MockHttpServletRequest()));
    }

    @Test
    void confirmUserRegistrationShouldSucceed() {
        Mockito.doNothing().when(userService).confirmUserRegistration(any());
        RedirectView result = userController.confirmUserRegistration("token");

        assertEquals("null/login", result.getUrl());
    }

    @Test
    void getReceivedFriendRequestsShouldSucceed() {
        when(userService.getReceivedFriendRequests(any(), any())).thenReturn(List.of(userRetrievalMinimalDto));
        ResponseEntity<List<UserRetrievalMinimalDto>> result = userController.getReceivedFriendRequests(1, 1);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(List.of(userRetrievalMinimalDto), result.getBody());
    }

    @Test
    void getSentFriendRequestsShouldSucceed() {
        when(userService.getSentFriendRequests()).thenReturn(Set.of(userRetrievalMinimalDto));
        ResponseEntity<Set<UserRetrievalMinimalDto>> result = userController.getSentFriendRequests();

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(Set.of(userRetrievalMinimalDto), result.getBody());
    }

    @Test
    void getFriendsShouldSucceed() throws IllegalAccessException {
        when(userService.getFriends(any())).thenReturn(Set.of(userRetrievalMinimalDto));
        ResponseEntity<Set<UserRetrievalMinimalDto>> result = userController.getFriends(1L);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(Set.of(userRetrievalMinimalDto), result.getBody());
    }

    @Test
    void sendFriendRequestShouldSucceed() throws IllegalAccessException {
        when(userService.sendFriendRequest(any())).thenReturn(friendRequestDto);
        ResponseEntity<FriendRequestDto> result = userController.sendFriendRequest(1L);

        assertEquals(HttpStatus.CREATED, result.getStatusCode());
        assertEquals(friendRequestDto, result.getBody());
    }

    @Test
    void acceptFriendRequestShouldSucceed() {
        when(userService.acceptFriendRequest(any())).thenReturn(userRetrievalMinimalDto);
        ResponseEntity<UserRetrievalMinimalDto> result = userController.acceptFriendRequest(1L);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(userRetrievalMinimalDto, result.getBody());
    }

    @Test
    void declineFriendRequestShouldSucceed() {
        when(userService.declineFriendRequest(any())).thenReturn("Friend request declined successfully");
        ResponseEntity<String> result = userController.declineFriendRequest(1L);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals("Friend request declined successfully", result.getBody());
    }
}
