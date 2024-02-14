package com.socialnetwork.boardrift.unit.controller;

import com.socialnetwork.boardrift.enumeration.UserStatus;
import com.socialnetwork.boardrift.rest.controller.PostController;
import com.socialnetwork.boardrift.rest.model.BoardGameDto;
import com.socialnetwork.boardrift.rest.model.PlayedGamePostRetrievalDto;
import com.socialnetwork.boardrift.rest.model.UserRetrievalMinimalDto;
import com.socialnetwork.boardrift.service.PostService;
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
import org.springframework.validation.MapBindingResult;

import java.time.Instant;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;


@ExtendWith(MockitoExtension.class)
public class PostControllerUnitTests {
    @Mock
    PostService postService;

    @InjectMocks
    PostController postController;

    PlayedGamePostRetrievalDto playedGamePostRetrievalDto;
    UserRetrievalMinimalDto userRetrievalMinimalDto;
    BoardGameDto boardGameDto;

    @BeforeEach
    void init() {
        boardGameDto = new BoardGameDto();
        userRetrievalMinimalDto = new UserRetrievalMinimalDto(1L, "Name", "Lastname", "", UserStatus.OFFLINE);
        playedGamePostRetrievalDto = new PlayedGamePostRetrievalDto(1L, 2L, "Description", Instant.now(), 500, 300, 1.20, false, "", userRetrievalMinimalDto, new HashSet<>(), new HashSet<>(), 0);
    }

    @Test
    void createPlayedGamePostShouldSucceed() {
        Mockito.when(postService.createPlayedGamePost(any())).thenReturn(playedGamePostRetrievalDto);
        ResponseEntity<PlayedGamePostRetrievalDto> result = postController.createPlayedGamePost(null, new MapBindingResult(Collections.EMPTY_MAP, "userRegistrationDto"));
        Assertions.assertEquals(HttpStatus.CREATED, result.getStatusCode());
        Assertions.assertEquals(playedGamePostRetrievalDto, result.getBody());
    }


}
