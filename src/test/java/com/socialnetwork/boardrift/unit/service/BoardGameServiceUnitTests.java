package com.socialnetwork.boardrift.unit.service;

import com.socialnetwork.boardrift.feign.BGGV1ApiConnection;
import com.socialnetwork.boardrift.feign.BGGV2ApiConnection;
import com.socialnetwork.boardrift.rest.model.BGGSearchResponse;
import com.socialnetwork.boardrift.rest.model.BGGThingResponse;
import com.socialnetwork.boardrift.service.BoardGameService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith({MockitoExtension.class})
public class BoardGameServiceUnitTests {
    @Mock
    private BGGV1ApiConnection bggv1ApiConnection;
    @Mock
    private BGGV2ApiConnection bggV2ApiConnection;
    @InjectMocks
    private BoardGameService boardGameService;

    @Test
    void searchBoardGames_shouldSucceed() {
        BGGSearchResponse expected = new BGGSearchResponse();
        BGGSearchResponse.Item item = new BGGSearchResponse.Item(1L, new BGGSearchResponse.Name("name"), new BGGSearchResponse.YearPublished(2020));
        BGGSearchResponse.Item item2 = new BGGSearchResponse.Item(2L, new BGGSearchResponse.Name("name2"), new BGGSearchResponse.YearPublished(2020));
        BGGSearchResponse.Item item3 = new BGGSearchResponse.Item(3L, new BGGSearchResponse.Name("name3"), new BGGSearchResponse.YearPublished(2020));

        expected.setItems(List.of(item, item2, item3));

        Mockito.when(bggV2ApiConnection.searchForBoardGames(any(), any())).thenReturn(expected);

        BGGSearchResponse result = boardGameService.searchBoardGames("query");
        assertTrue(expected.getItems().containsAll(result.getItems()));
    }

    @Test
    void searchBoardGames_shouldSucceed_emptyResult() {
        BGGSearchResponse expected = new BGGSearchResponse();

        Mockito.when(bggV2ApiConnection.searchForBoardGames(any(), any())).thenReturn(expected);

        BGGSearchResponse result = boardGameService.searchBoardGames("query");
        assertTrue(result.getItems().isEmpty());
    }

    @Test
    void getBoardGameById_shouldSucceed() {
        BGGThingResponse expected = new BGGThingResponse();
        BGGThingResponse.BoardGame boardGame1 = new BGGThingResponse.BoardGame();
        BGGThingResponse.BoardGame boardGame2 = new BGGThingResponse.BoardGame();
        BGGThingResponse.BoardGame boardGame3 = new BGGThingResponse.BoardGame();

        boardGame1.setId("1");
        boardGame2.setId("2");
        boardGame3.setId("3");

        expected.setItems(List.of(boardGame1, boardGame2, boardGame3));

        Mockito.when(bggv1ApiConnection.getBoardGameById(any())).thenReturn(expected);

        BGGThingResponse result = boardGameService.getBoardGameById(1L);

        assertTrue(expected.getItems().containsAll(result.getItems()));
    }

    @Test
    void getBoardGameById_shouldFail() {
        BGGThingResponse expected = new BGGThingResponse();

        Mockito.when(bggv1ApiConnection.getBoardGameById(any())).thenReturn(expected);

        assertThrows(EntityNotFoundException.class, () -> boardGameService.getBoardGameById(1L));
    }
}
