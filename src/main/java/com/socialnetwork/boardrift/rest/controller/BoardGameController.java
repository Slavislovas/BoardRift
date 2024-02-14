package com.socialnetwork.boardrift.rest.controller;

import com.socialnetwork.boardrift.rest.model.BGGSearchResponse;
import com.socialnetwork.boardrift.rest.model.BoardGameDto;
import com.socialnetwork.boardrift.service.BoardGameService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequestMapping("/board-games")
@RequiredArgsConstructor
@RestController
public class BoardGameController {
    private final BoardGameService boardGameService;

    @GetMapping("/search")
    public ResponseEntity<BGGSearchResponse> searchBoardGames(@RequestParam(name="query") String query) {
        return ResponseEntity.ok(boardGameService.searchBoardGames(query));
    }
}
