package com.socialnetwork.boardrift.feign;

import com.socialnetwork.boardrift.rest.model.BGGThingResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "bgg-api-v1", url = "https://boardgamegeek.com/xmlapi/")
public interface BGGV1ApiConnection {
    @GetMapping("/boardgame/{boardGameId}")
    BGGThingResponse getBoardGameById(@PathVariable("boardGameId") Long id);
}
