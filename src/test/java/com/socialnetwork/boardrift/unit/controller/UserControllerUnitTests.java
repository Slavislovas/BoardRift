package com.socialnetwork.boardrift.unit.controller;

import com.socialnetwork.boardrift.enumeration.Role;
import com.socialnetwork.boardrift.enumeration.UserStatus;
import com.socialnetwork.boardrift.repository.model.user.SuspensionEntity;
import com.socialnetwork.boardrift.repository.model.user.UserEntity;
import com.socialnetwork.boardrift.rest.controller.UserController;
import com.socialnetwork.boardrift.rest.model.FriendRequestDto;
import com.socialnetwork.boardrift.rest.model.PlayedGamePageDto;
import com.socialnetwork.boardrift.rest.model.WarningDto;
import com.socialnetwork.boardrift.rest.model.post.played_game_post.PlayedGameDto;
import com.socialnetwork.boardrift.rest.model.statistics.UserStatisticsDto;
import com.socialnetwork.boardrift.rest.model.user.SuspensionDto;
import com.socialnetwork.boardrift.rest.model.user.UserEditDto;
import com.socialnetwork.boardrift.rest.model.user.UserRegistrationDto;
import com.socialnetwork.boardrift.rest.model.user.UserRetrievalDto;
import com.socialnetwork.boardrift.rest.model.user.UserRetrievalMinimalDto;
import com.socialnetwork.boardrift.service.ModeratorService;
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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.MapBindingResult;
import org.springframework.web.servlet.view.RedirectView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
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

    @Mock
    ModeratorService moderatorService;

    @InjectMocks
    UserController userController;

    UserEntity userEntity;
    UserRegistrationDto userRegistrationDto;
    UserRetrievalDto userRetrievalDto;
    UserRetrievalMinimalDto userRetrievalMinimalDto;
    FriendRequestDto friendRequestDto;
    PlayedGamePageDto playedGamePageDto;
    UserStatisticsDto userStatisticsDto;
    PlayedGameDto playedGameDto;
    WarningDto warningDto;
    SuspensionDto suspensionDto;
    UserEditDto userEditDto;

    @BeforeEach
    void init(){
        userEntity = new UserEntity(1L, "Name", "Lastname", "email@gmail.com",
                "2001-11-16", "Password@123", "", "", "", true, false, false, false, "",
                Role.ROLE_USER, false, Collections.EMPTY_LIST, Collections.EMPTY_SET,
                Collections.EMPTY_SET, Collections.EMPTY_LIST, Collections.EMPTY_SET, Collections.EMPTY_SET, new ArrayList<>(), null);

        userRegistrationDto = new UserRegistrationDto("Name", "Lastname",
                "email@gmail.com", "2001-11-16", "Password@123");

        userRetrievalDto = new UserRetrievalDto(1L, "Name", "Lastname", "email@gmail.com", "2001-11-16", "", "", "", "", false, false,
        false, false, false, false, false, false, false, new ArrayList<>());
        userRetrievalMinimalDto = new UserRetrievalMinimalDto(1L, "Name", "Lastname", "", false, 0, false);
        friendRequestDto = new FriendRequestDto(userRetrievalMinimalDto, userRetrievalMinimalDto);
        playedGamePageDto = new PlayedGamePageDto("nextPageUrl", List.of(new PlayedGameDto()));
        userStatisticsDto = new UserStatisticsDto(null, 1, 2, null, null);
        playedGameDto = new PlayedGameDto(1L, 1L, "gameName", "gamePictureUrl", 100, false, "no-score", 100, 0, 100.0, new Date(), userRetrievalMinimalDto, Collections.EMPTY_SET, false, "userName");
        warningDto = new WarningDto(1L, "reason", new Date());
        suspensionDto = new SuspensionDto(12, "reason");
        userEditDto = new UserEditDto("name", "lastname", "bio", false, false, false, false, "country", "city");
    }

    @Test
    void searchFriendsByNameShouldSucceed() throws IllegalAccessException {
        Set<UserRetrievalMinimalDto> expectedUsers = new HashSet<>();
        when(userService.searchUsers(any(), any(), any())).thenReturn(expectedUsers);

        ResponseEntity<Set<UserRetrievalMinimalDto>> response = userController.searchUsers("userId", 0, 2);

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

    @Test
    void getUserByIdShouldSucceed() throws IllegalAccessException {
        when(userService.getUserById(any())).thenReturn(userRetrievalDto);
        ResponseEntity<UserRetrievalDto> result = userController.getUserById(1L);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(userRetrievalDto, result.getBody());
    }

    @Test
    void getPlayedGamesByUserIdShouldSucceed() throws IllegalAccessException {
        when(userService.getPlayedGamesByUserId(any(), any(), any(), any())).thenReturn(playedGamePageDto);
        ResponseEntity<PlayedGamePageDto> result = userController.getPlayedGamesByUserId(1L, 1, 1, new MockHttpServletRequest());

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(playedGamePageDto, result.getBody());
    }

    @Test
    void getStatisticsByUserIdShouldSucceed() throws IllegalAccessException {
        when(userService.getStatisticsByUserId(any())).thenReturn(userStatisticsDto);
        ResponseEntity<UserStatisticsDto> result = userController.getStatisticsByUserId(1L);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(userStatisticsDto, result.getBody());
    }

    @Test
    void getPlayedGameByPlayIdShouldSucceed() throws IllegalAccessException {
        when(userService.getPlayedGameByPlayId(any())).thenReturn(playedGameDto);
        ResponseEntity<PlayedGameDto> result = userController.getPlayedGameByPlayId(1L);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(playedGameDto, result.getBody());
    }

    @Test
    void warnUserShouldSucceed() throws IllegalAccessException {
        when(moderatorService.warnUser(any(), any())).thenReturn(warningDto);
        ResponseEntity<WarningDto> result = userController.warnUser(1L, warningDto, new MapBindingResult(Collections.EMPTY_MAP, "userRegistrationDto"));

        assertEquals(HttpStatus.CREATED, result.getStatusCode());
        assertEquals(warningDto, result.getBody());
    }

    @Test
    void warnUserShouldFailWhenInvalidRequestBody() {
        BindingResult bindingResult = new MapBindingResult(new HashMap<>(), "userRegistrationDto");
        bindingResult.addError(new FieldError("fieldError", "name", "Name is invalid"));

        assertThrows(FieldValidationException.class, () -> userController.warnUser(1L, warningDto, bindingResult));
    }

    @Test
    void logPlayShouldSucceed() {
        when(userService.logPlayedGame(any())).thenReturn(playedGameDto);
        ResponseEntity<PlayedGameDto> result = userController.logPlayedGame(playedGameDto, new MapBindingResult(Collections.EMPTY_MAP, "userRegistrationDto"));

        assertEquals(HttpStatus.CREATED, result.getStatusCode());
        assertEquals(playedGameDto, result.getBody());
    }

    @Test
    void logPlayShouldFailWhenInvalidRequestBody() {
        BindingResult bindingResult = new MapBindingResult(new HashMap<>(), "userRegistrationDto");
        bindingResult.addError(new FieldError("fieldError", "name", "Name is invalid"));

        assertThrows(FieldValidationException.class, () -> userController.logPlayedGame(playedGameDto, bindingResult));
    }

    @Test
    void suspendUserShouldSucceed() throws IllegalAccessException {
        doNothing().when(moderatorService).suspendUser(any(), any());
        ResponseEntity<Void> result = userController.suspendUser(1L, suspensionDto);

        assertEquals(HttpStatus.CREATED, result.getStatusCode());
    }

    @Test
    void editUserByIdShouldSucceed() throws IllegalAccessException {
        when(userService.editUserById(any(), any(), any())).thenReturn(userRetrievalDto);
        ResponseEntity<UserRetrievalDto> result = userController.editUserById(1L, new MockMultipartFile("name", new byte[]{}), userEditDto, new MapBindingResult(Collections.EMPTY_MAP, "userRegistrationDto"));

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(userRetrievalDto, result.getBody());
    }

    @Test
    void editUserByIdShouldFailWhenInvalidRequestBody() {
        BindingResult bindingResult = new MapBindingResult(new HashMap<>(), "userRegistrationDto");
        bindingResult.addError(new FieldError("fieldError", "name", "Name is invalid"));

        assertThrows(FieldValidationException.class, () -> userController.editUserById(1L, new MockMultipartFile("name", new byte[]{}), userEditDto, bindingResult));
    }

    @Test
    void includePlayInStatisticsShouldSucceed() throws IllegalAccessException {
        when(userService.includePlayInStatistics(any())).thenReturn(playedGameDto);
        ResponseEntity<PlayedGameDto> result = userController.includePlayInStatistics(1L);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(playedGameDto, result.getBody());
    }

    @Test
    void excludePlayFromStatisticsShouldSucceed() throws IllegalAccessException {
        when(userService.excludePlayFromStatistics(any())).thenReturn(playedGameDto);
        ResponseEntity<PlayedGameDto> result = userController.excludePlayFromStatistics(1L);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(playedGameDto, result.getBody());
    }

    @Test
    void deletePlayedGameShouldSucceed() throws IllegalAccessException {
        doNothing().when(userService).deletePlayedGameById(any());
        ResponseEntity<Void> result = userController.deletePlayedGameById(1L);

        assertEquals(HttpStatus.OK, result.getStatusCode());
    }

    @Test
    void removeFromFriendsListShouldSucceed() throws IllegalAccessException {
        doNothing().when(userService).removeFromFriendsList(any(), any());
        ResponseEntity<Void> result = userController.removeFromFriendsList(1L, 2L);

        assertEquals(HttpStatus.OK, result.getStatusCode());
    }

    @Test
    void deleteWarningShouldSucceed() {
        doNothing().when(moderatorService).deleteWarning(any(), any());
        ResponseEntity<Void> result = userController.deleteWarning(1L, 2L);

        assertEquals(HttpStatus.OK, result.getStatusCode());
    }

    @Test
    void deleteSuspensionShouldSucceed() {
        doNothing().when(moderatorService).deleteSuspension(any());
        ResponseEntity<Void> result = userController.deleteSuspension(1L);

        assertEquals(HttpStatus.OK, result.getStatusCode());
    }
}
