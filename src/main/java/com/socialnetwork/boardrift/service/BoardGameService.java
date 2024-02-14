package com.socialnetwork.boardrift.service;

import com.socialnetwork.boardrift.feign.BGGApiConnection;
import com.socialnetwork.boardrift.rest.model.BGGSearchResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@RequiredArgsConstructor
@Service
public class BoardGameService {
    private final BGGApiConnection bggApiConnection;

    public BGGSearchResponse searchBoardGames(String query) {
        query = query.replaceAll(" ", "+");
        BGGSearchResponse response = bggApiConnection.searchForBoardGames("boardgame", query);
        if (response.getItems() == null) {
            response.setItems(new ArrayList<>());
        }
       return response;
    }
}
