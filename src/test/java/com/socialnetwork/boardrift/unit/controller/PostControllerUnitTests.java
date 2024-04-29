package com.socialnetwork.boardrift.unit.controller;

import com.socialnetwork.boardrift.enumeration.UserStatus;
import com.socialnetwork.boardrift.rest.controller.PostController;
import com.socialnetwork.boardrift.rest.model.post.PostCommentDto;
import com.socialnetwork.boardrift.rest.model.post.PostCommentPageDto;
import com.socialnetwork.boardrift.rest.model.user.UserRetrievalMinimalDto;
import com.socialnetwork.boardrift.rest.model.post.played_game_post.PlayedGamePostRetrievalDto;
import com.socialnetwork.boardrift.rest.model.post.poll_post.PollPostCreationDto;
import com.socialnetwork.boardrift.rest.model.post.poll_post.PollPostRetrievalDto;
import com.socialnetwork.boardrift.rest.model.post.simple_post.SimplePostCreationDto;
import com.socialnetwork.boardrift.rest.model.post.simple_post.SimplePostRetrievalDto;
import com.socialnetwork.boardrift.service.PostService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class PostControllerUnitTests {
    @Mock
    PostService postService;

    @InjectMocks
    PostController postController;

    PlayedGamePostRetrievalDto playedGamePostRetrievalDto;
    UserRetrievalMinimalDto userRetrievalMinimalDto;

    PostCommentPageDto postCommentPageDto;

    @BeforeEach
    void init() {
        userRetrievalMinimalDto = new UserRetrievalMinimalDto(1L, "Name", "Lastname", "", UserStatus.OFFLINE, false, 0, false);
        playedGamePostRetrievalDto = new PlayedGamePostRetrievalDto(1L, 2L, "gameName", "gamePicture",
                "Description", "no-score", new Date(), 500, 300, 1.20,
                userRetrievalMinimalDto, false, 0, new HashSet<>(), 5, 0, false, false, null, new ArrayList<>());
        postCommentPageDto = new PostCommentPageDto("nextUrl", new ArrayList<>());
    }

    @Test
    public void createSimplePostShouldSucceed() {
        SimplePostCreationDto simplePostCreationDto = new SimplePostCreationDto();
        BindingResult bindingResult = mock(BindingResult.class);
        SimplePostRetrievalDto expectedDto = new SimplePostRetrievalDto();

        when(postService.createSimplePost(simplePostCreationDto)).thenReturn(expectedDto);

        ResponseEntity<SimplePostRetrievalDto> response = postController.createSimplePost(simplePostCreationDto, bindingResult);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(expectedDto, response.getBody());
    }

    @Test
    public void createPollPostShouldSucceed() {
        PollPostCreationDto pollPostCreationDto = new PollPostCreationDto();
        BindingResult bindingResult = mock(BindingResult.class);

        PollPostRetrievalDto expectedDto = new PollPostRetrievalDto();
        when(postService.createPollPost(pollPostCreationDto)).thenReturn(expectedDto);

        ResponseEntity<PollPostRetrievalDto> response = postController.createPollPost(pollPostCreationDto, bindingResult);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(expectedDto, response.getBody());
    }

    @Test
    public void createPollVoteShouldSucceed() {
        Long pollId = 1L;
        Long optionId = 1L;

        ResponseEntity<Void> response = postController.createPollVote(pollId, optionId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(postService, times(1)).createPollVote(pollId, optionId);
    }

    @Test
    public void createPostCommentShouldSucceed() {
        String postType = "type";
        Long postId = 1L;

        PostCommentDto commentDto = new PostCommentDto();
        BindingResult bindingResult = mock(BindingResult.class);

        PostCommentDto expectedDto = new PostCommentDto();

        when(postService.createPostComment(postType, postId, commentDto)).thenReturn(expectedDto);

        ResponseEntity<PostCommentDto> response = postController.createPostComment(postType, postId, commentDto, bindingResult);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(expectedDto, response.getBody());
    }

    @Test
    void getPostCommentsShouldSucceed() {
        String postType = "type";
        Long postId = 1L;
        int page = 1;
        int pageSize = 10;

        HttpServletRequest request = mock(HttpServletRequest.class);
        when(postService.getPostComments(postType, postId, page, pageSize, request)).thenReturn(postCommentPageDto);

        ResponseEntity<PostCommentPageDto> response = postController.getPostComments(postType, postId, page, pageSize, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(postCommentPageDto, response.getBody());
        verify(postService, times(1)).getPostComments(postType, postId, page, pageSize, request);
    }

    @Test
    void createPlayedGamePostShouldSucceed() {
        when(postService.createPlayedGamePost(any())).thenReturn(playedGamePostRetrievalDto);
        ResponseEntity<PlayedGamePostRetrievalDto> result = postController.createPlayedGamePost(null, new MapBindingResult(Collections.EMPTY_MAP, "userRegistrationDto"));
        assertEquals(HttpStatus.CREATED, result.getStatusCode());
        assertEquals(playedGamePostRetrievalDto, result.getBody());
    }
}
