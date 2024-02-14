package com.socialnetwork.boardrift.feign;

import com.socialnetwork.boardrift.rest.model.BGGSearchResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "bgg-api", url = "https://boardgamegeek.com/xmlapi2/")
public interface BGGApiConnection {
    @GetMapping("/search")
    BGGSearchResponse searchForBoardGames(@RequestParam(name="type") String type, @RequestParam(name="query") String query);
}
