package com.socialnetwork.boardrift.rest.controller;

import com.socialnetwork.boardrift.rest.model.played_game_post.PlayedGamePostCreationDto;
import com.socialnetwork.boardrift.rest.model.played_game_post.PlayedGamePostRetrievalDto;
import com.socialnetwork.boardrift.rest.model.poll_post.PollPostCreationDto;
import com.socialnetwork.boardrift.rest.model.poll_post.PollPostRetrievalDto;
import com.socialnetwork.boardrift.rest.model.simple_post.SimplePostCreationDto;
import com.socialnetwork.boardrift.rest.model.simple_post.SimplePostRetrievalDto;
import com.socialnetwork.boardrift.service.PostService;
import com.socialnetwork.boardrift.util.RequestValidator;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
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
    public ResponseEntity<PlayedGamePostRetrievalDto> createPlayedGamePost(@Valid @RequestBody PlayedGamePostCreationDto playedGamePostCreationDto, BindingResult bindingResult) {
        RequestValidator.validateRequest(bindingResult);
        return new ResponseEntity<>(postService.createPlayedGamePost(playedGamePostCreationDto), HttpStatus.CREATED);
    }

    @PostMapping("/simple")
    public ResponseEntity<SimplePostRetrievalDto> createSimplePost(@Valid @RequestBody SimplePostCreationDto simplePostCreationDto, BindingResult bindingResult) {
        RequestValidator.validateRequest(bindingResult);
        return new ResponseEntity<>(postService.createSimplePost(simplePostCreationDto), HttpStatus.CREATED);
    }

    @PostMapping("/poll")
    public ResponseEntity<PollPostRetrievalDto> createPollPost(@Valid @RequestBody PollPostCreationDto pollPostCreationDto, BindingResult bindingResult) {
        RequestValidator.validateRequest(bindingResult);
        return new ResponseEntity<>(postService.createPollPost(pollPostCreationDto), HttpStatus.CREATED);
    }

    @PostMapping("/poll/{pollId}/vote/{optionId}")
    public ResponseEntity<Void> createPollVote(@PathVariable(name = "pollId") Long pollId, @PathVariable(name = "optionId") Long optionId) {
        postService.createPollVote(pollId, optionId);
        return ResponseEntity.ok().build();
    }
}
