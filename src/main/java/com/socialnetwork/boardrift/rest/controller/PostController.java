package com.socialnetwork.boardrift.rest.controller;

import com.socialnetwork.boardrift.rest.model.PlayedGamePostCreationDto;
import com.socialnetwork.boardrift.rest.model.PlayedGamePostRetrievalDto;
import com.socialnetwork.boardrift.service.PostService;
import com.socialnetwork.boardrift.util.RequestValidator;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/posts")
@RequiredArgsConstructor
@RestController
public class PostController {
    private final PostService postService;

    @PostMapping("/played-game")
    public ResponseEntity<PlayedGamePostRetrievalDto> createPlayedGamePost(@Valid @RequestBody PlayedGamePostCreationDto playedGamePostCreationDto,
                                                                           BindingResult bindingResult) {
        RequestValidator.validateRequest(bindingResult);
        return new ResponseEntity<>(postService.createPlayedGamePost(playedGamePostCreationDto), HttpStatus.CREATED);
    }
}
