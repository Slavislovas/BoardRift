package com.socialnetwork.boardrift.unit.controller;

import com.socialnetwork.boardrift.enumeration.UserStatus;
import com.socialnetwork.boardrift.rest.controller.PostController;
import com.socialnetwork.boardrift.rest.model.post.PostCommentDto;
import com.socialnetwork.boardrift.rest.model.post.PostCommentPageDto;
import com.socialnetwork.boardrift.rest.model.post.PostPageDto;
import com.socialnetwork.boardrift.rest.model.post.ReportDto;
import com.socialnetwork.boardrift.rest.model.post.played_game_post.PlayedGamePostCreationDto;
import com.socialnetwork.boardrift.rest.model.user.UserRetrievalMinimalDto;
import com.socialnetwork.boardrift.rest.model.post.played_game_post.PlayedGamePostRetrievalDto;
import com.socialnetwork.boardrift.rest.model.post.poll_post.PollPostCreationDto;
import com.socialnetwork.boardrift.rest.model.post.poll_post.PollPostRetrievalDto;
import com.socialnetwork.boardrift.rest.model.post.simple_post.SimplePostCreationDto;
import com.socialnetwork.boardrift.rest.model.post.simple_post.SimplePostRetrievalDto;
import com.socialnetwork.boardrift.service.PostService;
import com.socialnetwork.boardrift.util.exception.FieldValidationException;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.parameters.P;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.MapBindingResult;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
    PostPageDto postPageDto;
    ReportDto reportDto;
    SimplePostCreationDto simplePostCreationDto;
    PollPostCreationDto pollPostCreationDto;
    PlayedGamePostCreationDto playedGamePostCreationDto;
    SimplePostRetrievalDto simplePostRetrievalDto;
    PollPostRetrievalDto pollPostRetrievalDto;
    PostCommentDto postCommentDto;

    @BeforeEach
    void init() {
        userRetrievalMinimalDto = new UserRetrievalMinimalDto(1L, "Name", "Lastname", "", false, 0, false);
        playedGamePostRetrievalDto = new PlayedGamePostRetrievalDto(1L, 2L, "gameName", "gamePicture",
                "Description", "no-score", new Date(), 500, 300, 1.20,
                userRetrievalMinimalDto, false, 0, new HashSet<>(), 5, 0, false, false, null, new ArrayList<>());
        postCommentPageDto = new PostCommentPageDto("nextUrl", new ArrayList<>());
        postPageDto = new PostPageDto("nextUrl", new ArrayList<>());
        reportDto = new ReportDto(1L , "test", null);
        simplePostCreationDto = new SimplePostCreationDto();
        pollPostCreationDto = new PollPostCreationDto();
        playedGamePostCreationDto = new PlayedGamePostCreationDto();
        simplePostRetrievalDto = new SimplePostRetrievalDto();
        pollPostRetrievalDto = new PollPostRetrievalDto();
        postCommentDto = new PostCommentDto();
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

    @Test
    void getPostsByUserIdShouldSucceed() throws IllegalAccessException {
        when(postService.getPostsByUserId(any(), any(), any(), any())).thenReturn(postPageDto);
        ResponseEntity<PostPageDto> result = postController.getPostsByUserId(1L, 1, 1, new MockHttpServletRequest());

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(postPageDto, result.getBody());
    }

    @Test
    void getFeedShouldSucceed() {
        when(postService.getFeed(any(), any(), any())).thenReturn(postPageDto);
        ResponseEntity<PostPageDto> result = postController.getFeed(1, 1, new MockHttpServletRequest());

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(postPageDto, result.getBody());
    }

    @Test
    void getReportedPostsShouldSucceed() {
        when(postService.getReportedPosts(any(), any(), any())).thenReturn(postPageDto);
        ResponseEntity<PostPageDto> result = postController.getReportedPosts(1, 1, new MockHttpServletRequest());

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(postPageDto, result.getBody());
    }

    @Test
    void getReportedCommentsShouldSucceed() {
        when(postService.getReportedComments(any(), any(), any())).thenReturn(postCommentPageDto);
        ResponseEntity<PostCommentPageDto> result = postController.getReportedComments(1, 1, new MockHttpServletRequest());

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(postCommentPageDto, result.getBody());
    }

    @Test
    void likePostShouldSucceed() {
        doNothing().when(postService).likePost(any(), any());
        ResponseEntity<Void> result = postController.likePost("type", 1L);

        assertEquals(HttpStatus.OK, result.getStatusCode());
    }

    @Test
    void reportPostShouldSucceed() {
        when(postService.reportPost(any(), any(), any())).thenReturn(reportDto);
        ResponseEntity<ReportDto> result = postController.reportPost("type", 1L, reportDto,  new MapBindingResult(Collections.EMPTY_MAP, "userRegistrationDto"));

        assertEquals(HttpStatus.CREATED, result.getStatusCode());
        assertEquals(reportDto, result.getBody());
    }

    @Test
    void reportPostShouldFailWhenInvalidRequestBody() {
        BindingResult bindingResult = new MapBindingResult(new HashMap<>(), "userRegistrationDto");
        bindingResult.addError(new FieldError("fieldError", "name", "Name is invalid"));

        assertThrows(FieldValidationException.class, () -> postController.reportPost("type", 1L, reportDto,  bindingResult));
    }

    @Test
    void reportPostCommentShouldSucceed() {
        when(postService.reportComment(any(), any(), any(), any())).thenReturn(reportDto);
        ResponseEntity<ReportDto> result = postController.reportPostComment("type", 1L, 1L, reportDto,  new MapBindingResult(Collections.EMPTY_MAP, "userRegistrationDto"));

        assertEquals(HttpStatus.CREATED, result.getStatusCode());
        assertEquals(reportDto, result.getBody());
    }

    @Test
    void reportPostCommentShouldFailWhenInvalidRequestBody() {
        BindingResult bindingResult = new MapBindingResult(new HashMap<>(), "userRegistrationDto");
        bindingResult.addError(new FieldError("fieldError", "name", "Name is invalid"));

        assertThrows(FieldValidationException.class, () -> postController.reportPostComment("type", 1L, 1L, reportDto,  bindingResult));
    }

    @Test
    void editSimplePostShouldSucceed() throws IllegalAccessException {
        when(postService.editSimplePost(any(), any())).thenReturn(simplePostRetrievalDto);
        ResponseEntity<SimplePostRetrievalDto> result = postController.editSimplePost(1L, simplePostCreationDto,  new MapBindingResult(Collections.EMPTY_MAP, "userRegistrationDto"));

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(simplePostRetrievalDto, result.getBody());
    }

    @Test
    void editSimplePostShouldFailWhenInvalidRequestBody() {
        BindingResult bindingResult = new MapBindingResult(new HashMap<>(), "userRegistrationDto");
        bindingResult.addError(new FieldError("fieldError", "name", "Name is invalid"));

        assertThrows(FieldValidationException.class, () -> postController.editSimplePost(1L, simplePostCreationDto,  bindingResult));
    }

    @Test
    void editPollPostShouldSucceed() throws IllegalAccessException {
        when(postService.editPollPost(any(), any())).thenReturn(pollPostRetrievalDto);
        ResponseEntity<PollPostRetrievalDto> result = postController.editPollPost(1L, pollPostCreationDto,  new MapBindingResult(Collections.EMPTY_MAP, "userRegistrationDto"));

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(pollPostRetrievalDto, result.getBody());
    }

    @Test
    void editPollPostShouldFailWhenInvalidRequestBody() {
        BindingResult bindingResult = new MapBindingResult(new HashMap<>(), "userRegistrationDto");
        bindingResult.addError(new FieldError("fieldError", "name", "Name is invalid"));

        assertThrows(FieldValidationException.class, () -> postController.editPollPost(1L, pollPostCreationDto,  bindingResult));
    }

    @Test
    void editPlayedGamePostShouldSucceed() throws IllegalAccessException {
        when(postService.editPlayedGamePost(any(), any())).thenReturn(playedGamePostRetrievalDto);
        ResponseEntity<PlayedGamePostRetrievalDto> result = postController.editPlayedGamePost(1L, playedGamePostCreationDto,  new MapBindingResult(Collections.EMPTY_MAP, "userRegistrationDto"));

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(playedGamePostRetrievalDto, result.getBody());
    }

    @Test
    void editPlayedGamePostShouldFailWhenInvalidRequestBody() {
        BindingResult bindingResult = new MapBindingResult(new HashMap<>(), "userRegistrationDto");
        bindingResult.addError(new FieldError("fieldError", "name", "Name is invalid"));

        assertThrows(FieldValidationException.class, () -> postController.editPlayedGamePost(1L, playedGamePostCreationDto,  bindingResult));
    }

    @Test
    void deletePostCommentShouldSucceed() throws IllegalAccessException {
        doNothing().when(postService).deletePostComment(any(), any(), any());
        ResponseEntity<Void> result = postController.deletePostComment("type", 1L, 1L);

        assertEquals(HttpStatus.OK, result.getStatusCode());
    }

    @Test
    void deleteSimplePostShouldSucceed() throws IllegalAccessException {
        doNothing().when(postService).deleteSimplePost(any());
        ResponseEntity<Void> result = postController.deleteSimplePost( 1L);

        assertEquals(HttpStatus.OK, result.getStatusCode());
    }

    @Test
    void deletePollPostShouldSucceed() throws IllegalAccessException {
        doNothing().when(postService).deletePollPost(any());
        ResponseEntity<Void> result = postController.deletePollPost( 1L);

        assertEquals(HttpStatus.OK, result.getStatusCode());
    }

    @Test
    void deletePlayedGamePostShouldSucceed() throws IllegalAccessException {
        doNothing().when(postService).deletePlayedGamePost(any());
        ResponseEntity<Void> result = postController.deletePlayedGamePost( 1L);

        assertEquals(HttpStatus.OK, result.getStatusCode());
    }

    @Test
    void deletePostReportsShouldSucceed() {
        doNothing().when(postService).deletePostReports(any(), any());
        ResponseEntity<Void> result = postController.deletePostReports( "type", 1L);

        assertEquals(HttpStatus.OK, result.getStatusCode());
    }

    @Test
    void deletePostCommentReportsShouldSucceed() {
        doNothing().when(postService).deleteCommentReports(any(), any(), any());
        ResponseEntity<Void> result = postController.deletePostCommentReports( "type", 1L, 1L);

        assertEquals(HttpStatus.OK, result.getStatusCode());
    }
}
