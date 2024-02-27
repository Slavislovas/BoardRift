package com.socialnetwork.boardrift.rest.controller;

import com.socialnetwork.boardrift.rest.model.post.FeedPageDto;
import com.socialnetwork.boardrift.rest.model.post.PostCommentDto;
import com.socialnetwork.boardrift.rest.model.post.PostCommentPageDto;
import com.socialnetwork.boardrift.rest.model.post.played_game_post.PlayedGamePostCreationDto;
import com.socialnetwork.boardrift.rest.model.post.played_game_post.PlayedGamePostRetrievalDto;
import com.socialnetwork.boardrift.rest.model.post.poll_post.PollPostCreationDto;
import com.socialnetwork.boardrift.rest.model.post.poll_post.PollPostRetrievalDto;
import com.socialnetwork.boardrift.rest.model.post.simple_post.SimplePostCreationDto;
import com.socialnetwork.boardrift.rest.model.post.simple_post.SimplePostRetrievalDto;
import com.socialnetwork.boardrift.service.PostService;
import com.socialnetwork.boardrift.util.RequestValidator;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/posts")
@RequiredArgsConstructor
@RestController
public class PostController {
    private final PostService postService;

    @GetMapping("/feed")
    public ResponseEntity<FeedPageDto> getFeed(@RequestParam(name = "page") Integer page,
                                               @RequestParam(name ="pageSize") Integer pageSize,
                                               HttpServletRequest request) {
        return ResponseEntity.ok(postService.getFeed(page, pageSize, request));
    }

    @GetMapping("/{postType}/{postId}/comments")
    public ResponseEntity<PostCommentPageDto> getPostComments(@PathVariable(name = "postType") String postType,
                                                              @PathVariable(name = "postId") Long postId,
                                                              @RequestParam(name = "page") Integer page,
                                                              @RequestParam(name = "pageSize") Integer pageSize,
                                                              HttpServletRequest request) {
        return ResponseEntity.ok(postService.getPostComments(postType, postId, page, pageSize, request));
    }

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

    @PostMapping("/{postType}/{postId}/comments")
    public ResponseEntity<PostCommentDto> createPostComment(@PathVariable(name = "postType") String postType,
                                                            @PathVariable(name = "postId") Long postId,
                                                            @Valid @RequestBody PostCommentDto commentDto,
                                                            BindingResult bindingResult) {
        RequestValidator.validateRequest(bindingResult);
        return new ResponseEntity<>(postService.createPostComment(postType, postId, commentDto), HttpStatus.CREATED);
    }

    @PostMapping("/{postType}/{postId}/likes")
    public ResponseEntity<Void> likePost(@PathVariable(name = "postType") String postType,
                                         @PathVariable(name = "postId") Long postId) {
        postService.likePost(postType, postId);
        return ResponseEntity.ok().build();
    }
}
