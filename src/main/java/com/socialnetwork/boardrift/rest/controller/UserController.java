package com.socialnetwork.boardrift.rest.controller;

import com.socialnetwork.boardrift.rest.model.FriendRequestDto;
import com.socialnetwork.boardrift.rest.model.PlayedGamePageDto;
import com.socialnetwork.boardrift.rest.model.WarningDto;
import com.socialnetwork.boardrift.rest.model.post.played_game_post.PlayedGameDto;
import com.socialnetwork.boardrift.rest.model.user.SuspensionDto;
import com.socialnetwork.boardrift.rest.model.user.UserEditDto;
import com.socialnetwork.boardrift.rest.model.user.UserRegistrationDto;
import com.socialnetwork.boardrift.rest.model.user.UserRetrievalDto;
import com.socialnetwork.boardrift.rest.model.user.UserRetrievalMinimalDto;
import com.socialnetwork.boardrift.rest.model.statistics.UserStatisticsDto;
import com.socialnetwork.boardrift.service.ModeratorService;
import com.socialnetwork.boardrift.service.UserService;
import com.socialnetwork.boardrift.util.validation.RequestValidator;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.view.RedirectView;

import java.util.List;
import java.util.Set;

@RequiredArgsConstructor
@RequestMapping("/users")
@RestController
public class UserController {
    @Value("${client.domain}")
    private String clientDomain;
    private final UserService userService;
    private final ModeratorService administratorService;

    @GetMapping("/{userId}")
    public ResponseEntity<UserRetrievalDto> getUserById(@PathVariable(name = "userId") Long userId) throws IllegalAccessException {
        return ResponseEntity.ok(userService.getUserById(userId));
    }

    @GetMapping("/register/confirm")
    public RedirectView confirmUserRegistration(@RequestParam("token") String token) {
        userService.confirmUserRegistration(token);
        return new RedirectView(clientDomain + "/login");
    }

    @GetMapping("/friend-requests/received")
    public ResponseEntity<List<UserRetrievalMinimalDto>> getReceivedFriendRequests(@RequestParam(value = "page", required = false) Integer page,
                                                                                   @RequestParam(value = "pageSize", required = false) Integer pageSize) {
        return ResponseEntity.ok(userService.getReceivedFriendRequests(page, pageSize));
    }

    @GetMapping("/friend-requests/sent")
    public ResponseEntity<Set<UserRetrievalMinimalDto>> getSentFriendRequests() {
        return ResponseEntity.ok(userService.getSentFriendRequests());
    }

    @GetMapping("/{userId}/friends")
    public ResponseEntity<Set<UserRetrievalMinimalDto>> getFriends(@PathVariable("userId") Long userId) throws IllegalAccessException {
        return ResponseEntity.ok(userService.getFriends(userId));
    }

    @GetMapping("/search")
    public ResponseEntity<Set<UserRetrievalMinimalDto>> searchUsers(@RequestParam(value = "query", required = false) String query,
                                                                    @RequestParam("page") Integer page,
                                                                    @RequestParam("pageSize") Integer pageSize) {
        return ResponseEntity.ok(userService.searchUsers(query, page, pageSize));
    }

    @GetMapping("/{userId}/plays")
    public ResponseEntity<PlayedGamePageDto> getPlayedGamesByUserId(@PathVariable("userId") Long userId,
                                                                    @RequestParam("page") Integer page,
                                                                    @RequestParam("pageSize") Integer pageSize,
                                                                    HttpServletRequest request) throws IllegalAccessException {
        return ResponseEntity.ok(userService.getPlayedGamesByUserId(userId, page, pageSize, request));
    }

    @GetMapping("/plays/{playId}")
    public ResponseEntity<PlayedGameDto> getPlayedGameByPlayId(@PathVariable("playId") Long playId) throws IllegalAccessException {
        return ResponseEntity.ok(userService.getPlayedGameByPlayId(playId));
    }

    @GetMapping("/{userId}/statistics")
    public ResponseEntity<UserStatisticsDto> getStatisticsByUserId(@PathVariable("userId") Long userId) throws IllegalAccessException {
        return ResponseEntity.ok(userService.getStatisticsByUserId(userId));
    }

    @PostMapping("/register")
    public ResponseEntity<UserRetrievalDto> createUser(@Valid @RequestBody UserRegistrationDto userRegistrationDto, BindingResult bindingResult, HttpServletRequest servletRequest) {
        RequestValidator.validateRequest(bindingResult);
        return new ResponseEntity<>(userService.createUser(userRegistrationDto, servletRequest), HttpStatus.CREATED);
    }

    @PostMapping("/{userId}/warnings")
    public ResponseEntity<WarningDto> warnUser(@PathVariable("userId") Long userId,
                                               @Valid @RequestBody WarningDto warningDto,
                                               BindingResult bindingResult) throws IllegalAccessException {
        RequestValidator.validateRequest(bindingResult);
        return new ResponseEntity<>(administratorService.warnUser(userId, warningDto), HttpStatus.CREATED);
    }

    @PostMapping("/{receiverId}/friend-requests/send")
    public ResponseEntity<FriendRequestDto> sendFriendRequest(@PathVariable("receiverId") Long receiverId) throws IllegalAccessException {
        return new ResponseEntity<>(userService.sendFriendRequest(receiverId), HttpStatus.CREATED);
    }

    @PostMapping("/{senderId}/friend-requests/accept")
    public ResponseEntity<UserRetrievalMinimalDto> acceptFriendRequest(@PathVariable("senderId") Long senderId) {
        return ResponseEntity.ok(userService.acceptFriendRequest(senderId));
    }

    @PostMapping("/plays")
    public ResponseEntity<PlayedGameDto> logPlayedGame(@Valid @RequestBody PlayedGameDto playedGameDto, BindingResult bindingResult) {
        RequestValidator.validateRequest(bindingResult);
        return new ResponseEntity<>(userService.logPlayedGame(playedGameDto), HttpStatus.CREATED);
    }

    @PostMapping("/{userId}/suspensions")
    public ResponseEntity<Void> suspendUser(@PathVariable("userId") Long userId,
                                        @RequestBody SuspensionDto suspensionDto) throws IllegalAccessException {
        administratorService.suspendUser(userId, suspensionDto);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @PutMapping(value = "/{userId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UserRetrievalDto> editUserById(@PathVariable("userId") Long userId,
                                                         @RequestParam(name = "profilePicture", required = false) MultipartFile profilePicture,
                                                         @Valid @ModelAttribute UserEditDto userEditDto, BindingResult bindingResult) throws IllegalAccessException {
        RequestValidator.validateRequest(bindingResult);
        return ResponseEntity.ok(userService.editUserById(userId, profilePicture, userEditDto));
    }

    @PutMapping("/plays/{playId}")
    public ResponseEntity<PlayedGameDto> editPlayedGameById(@PathVariable("playId") Long playId,
                                                            @RequestBody PlayedGameDto playedGameDto) throws IllegalAccessException {
        return ResponseEntity.ok(userService.editPlayedGameById(playId, playedGameDto));
    }

    @PutMapping("/plays/{playId}/approve")
    public ResponseEntity<PlayedGameDto> includePlayInStatistics(@PathVariable("playId") Long playId) throws IllegalAccessException {
        return ResponseEntity.ok(userService.includePlayInStatistics(playId));
    }

    @PutMapping("/plays/{playId}/decline")
    public ResponseEntity<PlayedGameDto> excludePlayFromStatistics(@PathVariable("playId") Long playId) throws IllegalAccessException {
        return ResponseEntity.ok(userService.excludePlayFromStatistics(playId));
    }

    @DeleteMapping("/{senderId}/friend-requests/decline")
    public ResponseEntity<String> declineFriendRequest(@PathVariable("senderId") Long senderId) {
        return ResponseEntity.ok(userService.declineFriendRequest(senderId));
    }

    @DeleteMapping("/plays/{playId}")
    public ResponseEntity<Void> deletePlayedGameById(@PathVariable("playId") Long playId) throws IllegalAccessException {
        userService.deletePlayedGameById(playId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{userId}/friends/{friendId}")
    public ResponseEntity<Void> removeFromFriendsList(@PathVariable("userId") Long userId, @PathVariable("friendId") Long friendId) throws IllegalAccessException {
        userService.removeFromFriendsList(userId, friendId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{userId}/warnings/{warningId}")
    public ResponseEntity<Void> deleteWarning(@PathVariable("userId") Long userId, @PathVariable("warningId") Long warningId) {
        administratorService.deleteWarning(userId, warningId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{userId}/suspensions")
    public ResponseEntity<Void> deleteSuspension(@PathVariable("userId") Long userId) {
        administratorService.deleteSuspension(userId);
        return ResponseEntity.ok().build();
    }
}
