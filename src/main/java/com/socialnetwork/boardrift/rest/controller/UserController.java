package com.socialnetwork.boardrift.rest.controller;

import com.socialnetwork.boardrift.rest.model.FriendRequestDto;
import com.socialnetwork.boardrift.rest.model.PlayedGamePageDto;
import com.socialnetwork.boardrift.rest.model.post.played_game_post.PlayedGameDto;
import com.socialnetwork.boardrift.rest.model.user.UserEditDto;
import com.socialnetwork.boardrift.rest.model.user.UserRegistrationDto;
import com.socialnetwork.boardrift.rest.model.user.UserRetrievalDto;
import com.socialnetwork.boardrift.rest.model.user.UserRetrievalMinimalDto;
import com.socialnetwork.boardrift.service.UserService;
import com.socialnetwork.boardrift.util.RequestValidator;
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
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.view.RedirectView;

import java.util.Set;

@RequiredArgsConstructor
@RequestMapping("/users")
@RestController
public class UserController {
    @Value("${client.domain}")
    private String clientDomain;
    private final UserService userService;

    @GetMapping("/{userId}")
    public ResponseEntity<UserRetrievalDto> getUserById(@PathVariable(name = "userId") Long userId) {
        return ResponseEntity.ok(userService.getUserById(userId));
    }

    @GetMapping("/register/confirm")
    public RedirectView confirmUserRegistration(@RequestParam("token") String token) {
        userService.confirmUserRegistration(token);
        return new RedirectView(clientDomain + "/login");
    }

    @GetMapping("/friend-requests/received")
    public ResponseEntity<Set<UserRetrievalMinimalDto>> getReceivedFriendRequests() {
        return ResponseEntity.ok(userService.getReceivedFriendRequests());
    }

    @GetMapping("/friend-requests/sent")
    public ResponseEntity<Set<UserRetrievalMinimalDto>> getSentFriendRequests() {
        return ResponseEntity.ok(userService.getSentFriendRequests());
    }

    @GetMapping("/{userId}/friends")
    public ResponseEntity<Set<UserRetrievalMinimalDto>> getFriends(@PathVariable("userId") Long userId) throws IllegalAccessException {
        return ResponseEntity.ok(userService.getFriends(userId));
    }

    @GetMapping("/{userId}/friends/search")
    public ResponseEntity<Set<UserRetrievalMinimalDto>> searchFriendsByName(@PathVariable("userId") Long userId, @RequestParam(value = "query", required = false) String query) throws IllegalAccessException {
        return ResponseEntity.ok(userService.searchFriendsByName(userId, query));
    }

    @GetMapping("/{userId}/plays")
    public ResponseEntity<PlayedGamePageDto> getPlayedGamesByUserId(@PathVariable("userId") Long userId,
                                                                    @RequestParam("page") Integer page,
                                                                    @RequestParam("pageSize") Integer pageSize,
                                                                    HttpServletRequest request) throws IllegalAccessException {
        return ResponseEntity.ok(userService.getPlayedGamesByUserId(userId, page, pageSize, request));
    }

    @PostMapping("/register")
    public ResponseEntity<UserRetrievalDto> createUser(@Valid @RequestBody UserRegistrationDto userRegistrationDto, BindingResult bindingResult, HttpServletRequest servletRequest) {
        RequestValidator.validateRequest(bindingResult);
        return new ResponseEntity<>(userService.createUser(userRegistrationDto, servletRequest), HttpStatus.CREATED);
    }

    @PostMapping("/{receiverId}/friend-requests/send")
    public ResponseEntity<FriendRequestDto> sendFriendRequest(@PathVariable("receiverId") Long receiverId) {
        return new ResponseEntity<>(userService.sendFriendRequest(receiverId), HttpStatus.CREATED);
    }

    @PostMapping("/{senderId}/friend-requests/accept")
    public ResponseEntity<UserRetrievalMinimalDto> acceptFriendRequest(@PathVariable("senderId") Long senderId) {
        return ResponseEntity.ok(userService.acceptFriendRequest(senderId));
    }

    @PostMapping("/plays")
    public ResponseEntity<PlayedGameDto> logPlayedGame(@RequestBody PlayedGameDto playedGameDto) {
        return new ResponseEntity<>(userService.logPlayedGame(playedGameDto), HttpStatus.CREATED);
    }

    @PutMapping(value = "/{userId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UserRetrievalDto> editUserById(@PathVariable("userId") Long userId,
                                                         @RequestParam(name = "profilePicture", required = false) MultipartFile profilePicture,
                                                         @Valid @ModelAttribute UserEditDto userEditDto, BindingResult bindingResult) throws IllegalAccessException {
        RequestValidator.validateRequest(bindingResult);
        return ResponseEntity.ok(userService.editUserById(userId, profilePicture, userEditDto));
    }

    @DeleteMapping("/{senderId}/friend-requests/decline")
    public ResponseEntity<String> declineFriendRequest(@PathVariable("senderId") Long senderId) {
        return ResponseEntity.ok(userService.declineFriendRequest(senderId));
    }

    @DeleteMapping("/plays/{playId}")
    public ResponseEntity<Void> deletePlayedGameById(@PathVariable("playId") Long playId) {
        userService.deletePlayedGameById(playId);
        return ResponseEntity.ok().build();
    }
}
