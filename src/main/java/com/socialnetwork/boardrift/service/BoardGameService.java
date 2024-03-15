package com.socialnetwork.boardrift.service;

import com.socialnetwork.boardrift.feign.BGGV1ApiConnection;
import com.socialnetwork.boardrift.feign.BGGV2ApiConnection;
import com.socialnetwork.boardrift.rest.model.BGGSearchResponse;
import com.socialnetwork.boardrift.rest.model.BGGThingResponse;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@RequiredArgsConstructor
@Service
public class BoardGameService {
    private final BGGV1ApiConnection bggv1ApiConnection;
    private final BGGV2ApiConnection bggV2ApiConnection;

    public BGGSearchResponse searchBoardGames(String query) {
        query = query.replaceAll(" ", "+");
        BGGSearchResponse response = bggV2ApiConnection.searchForBoardGames("boardgame", query);
        if (response.getItems() == null) {
            response.setItems(new ArrayList<>());
        }
       return response;
    }

    public BGGThingResponse getBoardGameById(Long boardGameId) {
        BGGThingResponse response = bggv1ApiConnection.getBoardGameById(boardGameId);

        if (response.getItems() == null) {
            throw new EntityNotFoundException("Board game with id: " + boardGameId + " was not found");
        }

        return response;
    }
}
