package com.socialnetwork.boardrift.rest.controller;

import com.socialnetwork.boardrift.rest.model.post.PostPageDto;
import com.socialnetwork.boardrift.rest.model.post.PostCommentDto;
import com.socialnetwork.boardrift.rest.model.post.PostCommentPageDto;
import com.socialnetwork.boardrift.rest.model.post.ReportDto;
import com.socialnetwork.boardrift.rest.model.post.played_game_post.PlayedGamePostCreationDto;
import com.socialnetwork.boardrift.rest.model.post.played_game_post.PlayedGamePostRetrievalDto;
import com.socialnetwork.boardrift.rest.model.post.poll_post.PollPostCreationDto;
import com.socialnetwork.boardrift.rest.model.post.poll_post.PollPostRetrievalDto;
import com.socialnetwork.boardrift.rest.model.post.simple_post.SimplePostCreationDto;
import com.socialnetwork.boardrift.rest.model.post.simple_post.SimplePostRetrievalDto;
import com.socialnetwork.boardrift.service.PostService;
import com.socialnetwork.boardrift.util.validation.RequestValidator;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class PostController {
    private final PostService postService;

    @GetMapping("/users/{userId}/posts")
    public ResponseEntity<PostPageDto> getPostsByUserId(@PathVariable(name = "userId") Long userId,
                                                        @RequestParam(name = "page") Integer page,
                                                        @RequestParam(name ="pageSize") Integer pageSize,
                                                        HttpServletRequest request) throws IllegalAccessException {
        return ResponseEntity.ok(postService.getPostsByUserId(userId, page, pageSize, request));
    }

    @GetMapping("/posts/feed")
    public ResponseEntity<PostPageDto> getFeed(@RequestParam(name = "page") Integer page,
                                               @RequestParam(name ="pageSize") Integer pageSize,
                                               HttpServletRequest request) {
        return ResponseEntity.ok(postService.getFeed(page, pageSize, request));
    }

    @GetMapping("/posts/reported")
    public ResponseEntity<PostPageDto> getReportedPosts(@RequestParam(name = "page") Integer page,
                                               @RequestParam(name ="pageSize") Integer pageSize,
                                               HttpServletRequest request) {
        return ResponseEntity.ok(postService.getReportedPosts(page, pageSize, request));
    }

    @GetMapping("/comments/reported")
    public ResponseEntity<PostCommentPageDto> getReportedComments(@RequestParam(name = "page") Integer page,
                                                       @RequestParam(name ="pageSize") Integer pageSize,
                                                       HttpServletRequest request) {
        return ResponseEntity.ok(postService.getReportedComments(page, pageSize, request));
    }

    @GetMapping("/posts/{postType}/{postId}/comments")
    public ResponseEntity<PostCommentPageDto> getPostComments(@PathVariable(name = "postType") String postType,
                                                              @PathVariable(name = "postId") Long postId,
                                                              @RequestParam(name = "page") Integer page,
                                                              @RequestParam(name = "pageSize") Integer pageSize,
                                                              HttpServletRequest request) {
        return ResponseEntity.ok(postService.getPostComments(postType, postId, page, pageSize, request));
    }

    @PostMapping("/posts/played-game")
    public ResponseEntity<PlayedGamePostRetrievalDto> createPlayedGamePost(@Valid @RequestBody PlayedGamePostCreationDto playedGamePostCreationDto,
                                                                           BindingResult bindingResult) {
        RequestValidator.validateRequest(bindingResult);
        return new ResponseEntity<>(postService.createPlayedGamePost(playedGamePostCreationDto), HttpStatus.CREATED);
    }

    @PostMapping("/posts/simple")
    public ResponseEntity<SimplePostRetrievalDto> createSimplePost(@Valid @RequestBody SimplePostCreationDto simplePostCreationDto,
                                                                   BindingResult bindingResult) {
        RequestValidator.validateRequest(bindingResult);
        return new ResponseEntity<>(postService.createSimplePost(simplePostCreationDto), HttpStatus.CREATED);
    }

    @PostMapping("/posts/poll")
    public ResponseEntity<PollPostRetrievalDto> createPollPost(@Valid @RequestBody PollPostCreationDto pollPostCreationDto,
                                                               BindingResult bindingResult) {
        RequestValidator.validateRequest(bindingResult);
        return new ResponseEntity<>(postService.createPollPost(pollPostCreationDto), HttpStatus.CREATED);
    }

    @PostMapping("/posts/poll/{pollId}/vote/{optionId}")
    public ResponseEntity<Void> createPollVote(@PathVariable(name = "pollId") Long pollId,
                                               @PathVariable(name = "optionId") Long optionId) {
        postService.createPollVote(pollId, optionId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/posts/{postType}/{postId}/comments")
    public ResponseEntity<PostCommentDto> createPostComment(@PathVariable(name = "postType") String postType,
                                                            @PathVariable(name = "postId") Long postId,
                                                            @Valid @RequestBody PostCommentDto commentDto,
                                                            BindingResult bindingResult) {
        RequestValidator.validateRequest(bindingResult);
        return new ResponseEntity<>(postService.createPostComment(postType, postId, commentDto), HttpStatus.CREATED);
    }

    @PostMapping("/posts/{postType}/{postId}/likes")
    public ResponseEntity<Void> likePost(@PathVariable(name = "postType") String postType,
                                         @PathVariable(name = "postId") Long postId) {
        postService.likePost(postType, postId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/posts/{postType}/{postId}/reports")
    public ResponseEntity<ReportDto> reportPost(@PathVariable("postType") String postType,
                                                @PathVariable("postId") Long postId,
                                                @Valid @RequestBody ReportDto postReportDto, BindingResult bindingResult) {
        RequestValidator.validateRequest(bindingResult);
        return new ResponseEntity<>(postService.reportPost(postType, postId, postReportDto), HttpStatus.CREATED);
    }

    @PostMapping("/posts/{postType}/{postId}/comments/{commentId}/reports")
    public ResponseEntity<ReportDto> reportPostComment(@PathVariable("postType") String postType,
                                                       @PathVariable("postId") Long postId,
                                                       @PathVariable("commentId") Long commentId,
                                                       @Valid @RequestBody ReportDto commentReportDto, BindingResult bindingResult) {
        RequestValidator.validateRequest(bindingResult);
        return new ResponseEntity<>(postService.reportComment(postType, postId, commentId, commentReportDto), HttpStatus.CREATED);
    }

    @PutMapping("/posts/{postType}/{postId}/comments/{commentId}")
    public ResponseEntity<PostCommentDto> editPostComment(@PathVariable(name = "postType") String postType,
                                                              @PathVariable(name = "postId") Long postId,
                                                              @PathVariable(name = "commentId") Long commentId,
                                                              @Valid @RequestBody PostCommentDto postCommentDto, BindingResult bindingResult) throws IllegalAccessException {
        RequestValidator.validateRequest(bindingResult);
        return ResponseEntity.ok(postService.editPostComment(postType, postId, commentId, postCommentDto));
    }

    @PutMapping("/posts/simple/{simplePostId}")
    public ResponseEntity<SimplePostRetrievalDto> editSimplePost(@PathVariable("simplePostId") Long simplePostId,
                                                                 @Valid @RequestBody SimplePostCreationDto simplePostCreationDto,
                                                                 BindingResult bindingResult) throws IllegalAccessException {
        RequestValidator.validateRequest(bindingResult);
        return ResponseEntity.ok(postService.editSimplePost(simplePostId, simplePostCreationDto));
    }

    @PutMapping("/posts/poll/{pollPostId}")
    public ResponseEntity<PollPostRetrievalDto> editPollPost(@PathVariable("pollPostId") Long pollPostId,
                                                             @Valid @RequestBody PollPostCreationDto pollPostCreationDto,
                                                             BindingResult bindingResult) throws IllegalAccessException {
        RequestValidator.validateRequest(bindingResult);
        return ResponseEntity.ok(postService.editPollPost(pollPostId, pollPostCreationDto));
    }

    @PutMapping("/posts/played-game/{playedGamePostId}")
    public ResponseEntity<PlayedGamePostRetrievalDto> editPlayedGamePost(@PathVariable("playedGamePostId") Long playedGamePostId,
                                                                         @Valid @RequestBody PlayedGamePostCreationDto playedGamePostCreationDto,
                                                                         BindingResult bindingResult) throws IllegalAccessException {
        RequestValidator.validateRequest(bindingResult);
        return ResponseEntity.ok(postService.editPlayedGamePost(playedGamePostId, playedGamePostCreationDto));
    }

    @DeleteMapping("/posts/{postType}/{postId}/comments/{commentId}")
    public ResponseEntity<Void> deletePostComment(@PathVariable(name = "postType") String postType,
                                                          @PathVariable(name = "postId") Long postId,
                                                          @PathVariable(name = "commentId") Long commentId) throws IllegalAccessException {
        postService.deletePostComment(postType, postId, commentId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/posts/simple/{simplePostId}")
    public ResponseEntity<Void> deleteSimplePost(@PathVariable("simplePostId") Long simplePostId) throws IllegalAccessException {
        postService.deleteSimplePost(simplePostId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/posts/poll/{pollPostId}")
    public ResponseEntity<Void> deletePollPost(@PathVariable("pollPostId") Long pollPostId) throws IllegalAccessException {
        postService.deletePollPost(pollPostId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/posts/played-game/{playedGamePostId}")
    public ResponseEntity<Void> deletePlayedGamePost(@PathVariable("playedGamePostId") Long playedGamePostId) throws IllegalAccessException {
        postService.deletePlayedGamePost(playedGamePostId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/posts/{postType}/{postId}/reports")
    public ResponseEntity<Void> deletePostReports(@PathVariable("postType") String postType,
                                                 @PathVariable("postId") Long postId) {
        postService.deletePostReports(postType, postId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/posts/{postType}/{postId}/comments/{commentId}/reports")
    public ResponseEntity<Void> deletePostCommentReports(@PathVariable("postType") String postType,
                                                         @PathVariable("postId") Long postId,
                                                         @PathVariable("commentId") Long commentId) {
        postService.deleteCommentReports(postType, postId, commentId);
        return ResponseEntity.ok().build();
    }
}
