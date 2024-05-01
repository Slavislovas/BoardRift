package com.socialnetwork.boardrift.feign;

import com.socialnetwork.boardrift.rest.model.BGGSearchResponse;
import com.socialnetwork.boardrift.rest.model.BGGThingResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "bgg-api-v2", url = "https://boardgamegeek.com/xmlapi2/")
public interface BGGV2ApiConnection {
    @GetMapping("/search")
    BGGSearchResponse searchForBoardGames(@RequestParam(name="type") String type,
                                          @RequestParam(name="query") String query);

    @GetMapping("/thing")
    BGGThingResponse getBoardGameById(@RequestParam(name="id") Long id);
}
