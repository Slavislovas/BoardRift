package com.socialnetwork.boardrift.unit.controller;

import com.socialnetwork.boardrift.rest.controller.BoardGameController;
import com.socialnetwork.boardrift.rest.model.BGGSearchResponse;
import com.socialnetwork.boardrift.rest.model.BGGThingResponse;
import com.socialnetwork.boardrift.service.BoardGameService;
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

import java.util.List;

import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
public class BoardGameControllerUnitTests {
    @Mock
    BoardGameService boardGameService;

    @InjectMocks
    BoardGameController boardGameController;

    BGGSearchResponse bggSearchResponse;
    BGGThingResponse bggThingResponse;

    @BeforeEach
    void init() {
        bggSearchResponse = new BGGSearchResponse(List.of(new BGGSearchResponse.Item(1L, null, null), new BGGSearchResponse.Item(2L, null, null)));
        bggThingResponse = new BGGThingResponse(List.of(new BGGThingResponse.BoardGame("strategy", "1", "thumbnail", "image", "category", null, null, null, null, null),
                new BGGThingResponse.BoardGame("strategy", "2", "thumbnail", "image", "category", null, null, null, null, null)));
    }

    @Test
    void searchBoardGamesShouldSucceed() {
        Mockito.when(boardGameService.searchBoardGames(any())).thenReturn(bggSearchResponse);
        ResponseEntity<BGGSearchResponse> result = boardGameController.searchBoardGames("query");
        Assertions.assertEquals(HttpStatus.OK, result.getStatusCode());
        Assertions.assertEquals(bggSearchResponse, result.getBody());
    }

    @Test
    void getBoardGameByIdShouldSucceed() {
        Mockito.when(boardGameService.getBoardGameById(any())).thenReturn(bggThingResponse);
        ResponseEntity<BGGThingResponse> result = boardGameController.getBoardGameById(1L);
        Assertions.assertEquals(HttpStatus.OK, result.getStatusCode());
        Assertions.assertEquals(bggThingResponse, result.getBody());
    }
}
