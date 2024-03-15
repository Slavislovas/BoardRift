package com.socialnetwork.boardrift.unit.service;

import com.socialnetwork.boardrift.repository.PlayedGamePostRepository;
import com.socialnetwork.boardrift.repository.PlayedGameRepository;
import com.socialnetwork.boardrift.repository.PollPostRepository;
import com.socialnetwork.boardrift.repository.PostCommentRepository;
import com.socialnetwork.boardrift.repository.PostLikeRepository;
import com.socialnetwork.boardrift.repository.SimplePostRepository;
import com.socialnetwork.boardrift.repository.model.UserEntity;
import com.socialnetwork.boardrift.repository.model.board_game.PlayedGameEntity;
import com.socialnetwork.boardrift.repository.model.post.PlayedGamePostEntity;
import com.socialnetwork.boardrift.repository.model.post.PostCommentEntity;
import com.socialnetwork.boardrift.repository.model.post.PollOptionEntity;
import com.socialnetwork.boardrift.repository.model.post.PollOptionVoteEntity;
import com.socialnetwork.boardrift.repository.model.post.PollPostEntity;
import com.socialnetwork.boardrift.repository.model.post.PostLikeEntity;
import com.socialnetwork.boardrift.repository.model.post.SimplePostEntity;
import com.socialnetwork.boardrift.rest.model.BGGThingResponse;
import com.socialnetwork.boardrift.rest.model.post.PostCommentDto;
import com.socialnetwork.boardrift.rest.model.post.PostCommentPageDto;
import com.socialnetwork.boardrift.rest.model.post.played_game_post.PlayedGamePostCreationDto;
import com.socialnetwork.boardrift.rest.model.post.played_game_post.PlayedGamePostRetrievalDto;
import com.socialnetwork.boardrift.rest.model.post.poll_post.PollOptionDto;
import com.socialnetwork.boardrift.rest.model.post.poll_post.PollPostCreationDto;
import com.socialnetwork.boardrift.rest.model.post.poll_post.PollPostRetrievalDto;
import com.socialnetwork.boardrift.rest.model.post.simple_post.SimplePostCreationDto;
import com.socialnetwork.boardrift.rest.model.post.simple_post.SimplePostRetrievalDto;
import com.socialnetwork.boardrift.service.BoardGameService;
import com.socialnetwork.boardrift.service.PostService;
import com.socialnetwork.boardrift.service.UserService;
import com.socialnetwork.boardrift.util.exception.DuplicatePollVoteException;
import com.socialnetwork.boardrift.util.exception.FieldValidationException;
import com.socialnetwork.boardrift.util.mapper.PostMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith({MockitoExtension.class})
public class PostServiceUnitTests {

    @Mock
    private PlayedGamePostRepository playedGamePostRepository;

    @Mock
    private PlayedGameRepository playedGameRepository;

    @Mock
    private SimplePostRepository simplePostRepository;

    @Mock
    private PollPostRepository pollPostRepository;

    @Mock
    private PostCommentRepository postCommentRepository;

    @Mock
    private PostLikeRepository postLikeRepository;

    @Mock
    private UserService userService;

    @Mock
    private BoardGameService boardGameService;

    @Mock
    private PostMapper postMapper;

    @InjectMocks
    private PostService postService;

    @BeforeEach
    void setUp() {
        Authentication authentication = new UsernamePasswordAuthenticationToken(new UserEntity(), null);
        SecurityContext securityContext = mock(SecurityContext.class);
        lenient().when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    @ParameterizedTest
    @ValueSource(strings = {"highest-score", "lowest-score", "no-score"})
    void createPlayedGamePost_ShouldReturnDto_WhenValidInput(String scoringSystem) {
        UserDetails userDetails = new UserEntity();
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(userDetails, null));

        PlayedGamePostCreationDto playedGamePostCreationDto = new PlayedGamePostCreationDto();
        playedGamePostCreationDto.setScoringSystem(scoringSystem);
        playedGamePostCreationDto.setPlayedGameId(1L);
        playedGamePostCreationDto.setPostCreatorPoints(200);

        PlayedGamePostCreationDto.SelectedPlayerDto playerDto = new PlayedGamePostCreationDto.SelectedPlayerDto();
        playerDto.setPlayedGameId(1L);
        playerDto.setPoints(200);

        PlayedGamePostCreationDto.SelectedPlayerDto playerDto2 = new PlayedGamePostCreationDto.SelectedPlayerDto();
        playerDto2.setPlayedGameId(2L);
        playerDto2.setPoints(100);

        playedGamePostCreationDto.setPlayers(new HashSet<>(Set.of(playerDto, playerDto2)));

        UserEntity userEntity = new UserEntity();
        userEntity.setUsername(userDetails.getUsername());
        userEntity.setPlayedGames(new ArrayList<>());
        when(userService.getUserEntityByEmail(any())).thenReturn(userEntity);
        when(userService.getUserEntityById(any())).thenReturn(userEntity);

        BGGThingResponse boardGameResponse = new BGGThingResponse();
        BGGThingResponse.BoardGame item = new BGGThingResponse.BoardGame();
        BGGThingResponse.BoardGame.Name name = new BGGThingResponse.BoardGame.Name();
        name.setValue("Test Game");
        item.setNames(List.of(name));
        item.setImage("test_image");
        boardGameResponse.setItems(List.of(item));
        when(boardGameService.getBoardGameById(playedGamePostCreationDto.getPlayedGameId())).thenReturn(boardGameResponse);

        PlayedGamePostEntity playedGamePostEntity = new PlayedGamePostEntity();
        when(playedGamePostRepository.save(any())).thenReturn(playedGamePostEntity);

        PlayedGamePostRetrievalDto playedGamePostRetrievalDto = new PlayedGamePostRetrievalDto();
        when(postMapper.playedGamePostEntityToRetrievalDto(playedGamePostEntity)).thenReturn(playedGamePostRetrievalDto);

        PlayedGamePostRetrievalDto result = postService.createPlayedGamePost(playedGamePostCreationDto);

        assertNotNull(result);
        assertSame(playedGamePostRetrievalDto, result);

        verify(playedGamePostRepository, times(1)).save(any());
        verify(postMapper, times(1)).playedGamePostEntityToRetrievalDto(playedGamePostEntity);
    }

    @Test
    void createPlayedGamePost_ShouldThrowException_WhenInvalidScoringSystem() {
        UserDetails userDetails = new UserEntity();
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(userDetails, null));

        PlayedGamePostCreationDto playedGamePostCreationDto = new PlayedGamePostCreationDto();
        playedGamePostCreationDto.setScoringSystem("invalid-system");
        playedGamePostCreationDto.setPlayers(new HashSet<>());

        UserEntity userEntity = new UserEntity();
        userEntity.setUsername(userDetails.getUsername());
        when(userService.getUserEntityByEmail(userDetails.getUsername())).thenReturn(userEntity);

        assertThrows(FieldValidationException.class, () -> postService.createPlayedGamePost(playedGamePostCreationDto));

        verify(playedGamePostRepository, never()).save(any());
        verify(postMapper, never()).playedGamePostEntityToRetrievalDto(any());
    }

    @Test
    void createPollVote_WhenUserHasNotVoted_ShouldCreateVote() {
        Long pollId = 1L;
        Long optionId = 1L;

        UserDetails userDetails = new UserEntity();
        UserEntity voterEntity = new UserEntity();
        voterEntity.setId(1L);

        when(userService.getUserEntityByEmail(userDetails.getUsername())).thenReturn(voterEntity);

        PollPostEntity pollPostEntity = new PollPostEntity();
        pollPostEntity.setId(pollId);

        PollOptionEntity pollOptionEntity = new PollOptionEntity();
        pollOptionEntity.setId(optionId);
        pollOptionEntity.setVotes(new HashSet<>());

        pollPostEntity.setOptions(List.of(pollOptionEntity));

        when(pollPostRepository.findById(pollId)).thenReturn(Optional.of(pollPostEntity));

        postService.createPollVote(pollId, optionId);

        verify(pollPostRepository, times(1)).findById(pollId);
        verify(pollPostRepository, times(1)).save(pollPostEntity);
    }

    @Test
    void createPollVote_WhenUserHasAlreadyVoted_ShouldThrowException() {
        Long pollId = 1L;
        Long optionId = 1L;

        UserDetails userDetails = new UserEntity();
        UserEntity voterEntity = new UserEntity();
        voterEntity.setId(1L);

        when(userService.getUserEntityByEmail(userDetails.getUsername())).thenReturn(voterEntity);

        PollPostEntity pollPostEntity = new PollPostEntity();
        pollPostEntity.setId(pollId);

        PollOptionEntity pollOptionEntity = new PollOptionEntity();
        pollOptionEntity.setId(optionId);

        PollOptionVoteEntity existingVote = new PollOptionVoteEntity();
        existingVote.setId(2L);
        existingVote.setVoter(voterEntity);
        pollOptionEntity.setVotes(Set.of(existingVote));

        pollPostEntity.setOptions(List.of(pollOptionEntity));

        when(pollPostRepository.findById(pollId)).thenReturn(Optional.of(pollPostEntity));

        assertThrows(DuplicatePollVoteException.class, () -> postService.createPollVote(pollId, optionId));

        verify(pollPostRepository, times(1)).findById(pollId);
        verify(pollPostRepository, never()).save(pollPostEntity);
    }

    @Test
    void createPostComment_WithSimplePost_ShouldCreateComment() {
        String postType = "simple";
        Long postId = 1L;
        PostCommentDto commentDto = new PostCommentDto();

        UserDetails userDetails = new UserEntity();
        UserEntity userEntity = new UserEntity();
        userEntity.setUsername(userDetails.getUsername());

        when(userService.getUserEntityByEmail(userDetails.getUsername())).thenReturn(userEntity);

        SimplePostEntity simplePostEntity = new SimplePostEntity();
        when(simplePostRepository.findById(postId)).thenReturn(java.util.Optional.of(simplePostEntity));

        PostCommentEntity savedPostCommentEntity = new PostCommentEntity(1L, commentDto.getText(), null, simplePostEntity, userEntity);
        when(postCommentRepository.save(any())).thenReturn(savedPostCommentEntity);

        PostCommentDto expectedCommentDto = new PostCommentDto();

        when(postMapper.postCommentEntityToDto(any())).thenReturn(expectedCommentDto);

        PostCommentDto result = postService.createPostComment(postType, postId, commentDto);

        assertEquals(expectedCommentDto, result);
        verify(simplePostRepository, times(1)).findById(postId);
        verify(postCommentRepository, times(1)).save(any());
    }

    @Test
    void createPostComment_WithPlayedGamePost_ShouldCreateComment() {
        String postType = "played-game";
        Long postId = 1L;
        PostCommentDto commentDto = new PostCommentDto();

        UserDetails userDetails = new UserEntity();
        UserEntity userEntity = new UserEntity();
        userEntity.setUsername(userDetails.getUsername());

        when(userService.getUserEntityByEmail(userDetails.getUsername())).thenReturn(userEntity);

        PlayedGamePostEntity playedGamePostEntity = new PlayedGamePostEntity();
        when(playedGamePostRepository.findById(postId)).thenReturn(java.util.Optional.of(playedGamePostEntity));

        PostCommentEntity savedPostCommentEntity = new PostCommentEntity(1L, commentDto.getText(), Instant.now(), playedGamePostEntity.getBasePost(), userEntity);
        when(postCommentRepository.save(any())).thenReturn(savedPostCommentEntity);

        PostCommentDto expectedCommentDto = new PostCommentDto();

        when(postMapper.postCommentEntityToDto(any())).thenReturn(expectedCommentDto);

        PostCommentDto result = postService.createPostComment(postType, postId, commentDto);

        assertEquals(expectedCommentDto, result);
        verify(playedGamePostRepository, times(1)).findById(postId);
        verify(postCommentRepository, times(1)).save(any());
    }

    @Test
    void createPostComment_WithPollPost_ShouldCreateComment() {
        String postType = "poll";
        Long postId = 1L;
        PostCommentDto commentDto = new PostCommentDto();

        UserDetails userDetails = new UserEntity();
        UserEntity userEntity = new UserEntity();
        userEntity.setUsername(userDetails.getUsername());

        when(userService.getUserEntityByEmail(userDetails.getUsername())).thenReturn(userEntity);

        PollPostEntity pollPostEntity = new PollPostEntity();
        when(pollPostRepository.findById(postId)).thenReturn(java.util.Optional.of(pollPostEntity));

        PostCommentEntity savedPostCommentEntity = new PostCommentEntity(1L, commentDto.getText(), Instant.now(), pollPostEntity.getBasePost(), userEntity);
        when(postCommentRepository.save(any())).thenReturn(savedPostCommentEntity);

        PostCommentDto expectedCommentDto = new PostCommentDto();

        when(postMapper.postCommentEntityToDto(any())).thenReturn(expectedCommentDto);

        PostCommentDto result = postService.createPostComment(postType, postId, commentDto);

        assertEquals(expectedCommentDto, result);
        verify(pollPostRepository, times(1)).findById(postId);
        verify(postCommentRepository, times(1)).save(any());
    }

    @Test
    void createPostComment_WithInvalidPostType_ShouldThrowException() {
        String postType = "invalid";
        Long postId = 1L;
        PostCommentDto commentDto = new PostCommentDto();

        assertThrows(FieldValidationException.class, () -> postService.createPostComment(postType, postId, commentDto));
    }

    @Test
    void createSimplePost_ShouldReturnDto_WhenValidInput() {
        SimplePostCreationDto simplePostCreationDto = new SimplePostCreationDto("Description");

        UserEntity userEntity = new UserEntity();
        when(userService.getUserEntityByEmail(any())).thenReturn(userEntity);

        SimplePostEntity savedSimplePostEntity = new SimplePostEntity();
        when(simplePostRepository.save(any(SimplePostEntity.class))).thenReturn(savedSimplePostEntity);

        SimplePostRetrievalDto expectedDto = new SimplePostRetrievalDto();
        when(postMapper.simplePostEntityToRetrievalDto(savedSimplePostEntity)).thenReturn(expectedDto);

        // Act
        SimplePostRetrievalDto result = postService.createSimplePost(simplePostCreationDto);

        // Assert
        assertEquals(expectedDto, result);
        verify(userService, times(1)).getUserEntityByEmail(any());
        verify(simplePostRepository, times(1)).save(any(SimplePostEntity.class));
        verify(postMapper, times(1)).simplePostEntityToRetrievalDto(savedSimplePostEntity);
    }

    @Test
    void createPollPost_ShouldReturnDto_WhenValidInput() {
        PollPostCreationDto pollPostCreationDto = new PollPostCreationDto("Question", Collections.singletonList(new PollOptionDto(1L, "Option")));

        UserEntity userEntity = new UserEntity();
        when(userService.getUserEntityByEmail(any())).thenReturn(userEntity);

        PollPostEntity savedPollPostEntity = new PollPostEntity();
        when(pollPostRepository.save(any(PollPostEntity.class))).thenReturn(savedPollPostEntity);

        PollPostRetrievalDto expectedDto = new PollPostRetrievalDto();
        when(postMapper.pollPostEntityToRetrievalDto(savedPollPostEntity)).thenReturn(expectedDto);

        // Act
        PollPostRetrievalDto result = postService.createPollPost(pollPostCreationDto);

        // Assert
        assertEquals(expectedDto, result);
        verify(userService, times(1)).getUserEntityByEmail(any());
        verify(pollPostRepository, times(1)).save(any(PollPostEntity.class));
        verify(postMapper, times(1)).pollPostEntityToRetrievalDto(savedPollPostEntity);
    }

    @ParameterizedTest
    @MethodSource("providePostTypeAndPageSize")
    void getPostComments_ShouldReturnPageDto(String postType, int pageSize) {
        Long postId = 1L;
        Integer page = 1;
        HttpServletRequest request = new MockHttpServletRequest();

        PostCommentEntity postCommentEntity = new PostCommentEntity();
        postCommentEntity.setText("FirstText");

        PostCommentEntity postCommentEntity2 = new PostCommentEntity();
        postCommentEntity2.setText("SecondText");

        List<PostCommentEntity> commentEntities = List.of(postCommentEntity, postCommentEntity2);

        SimplePostEntity simplePost  = new SimplePostEntity(1L, "description", new Date(), new UserEntity(), commentEntities, new HashSet<>(), null, null, null);

        PlayedGamePostEntity playedGamePostEntity = new PlayedGamePostEntity(postId,
                100, 20, 50.0,
                "lowest-score",
                simplePost,
                new PlayedGameEntity());

        PollPostEntity pollPost = new PollPostEntity(1L, new ArrayList<>(), simplePost);

        lenient().when(postCommentRepository.findAllBySimplePostId(eq(postId), any(PageRequest.class))).thenReturn(commentEntities);
        lenient().when(playedGamePostRepository.findById(any())).thenReturn(Optional.of(playedGamePostEntity));
        lenient().when(simplePostRepository.findById(any())).thenReturn(Optional.of(simplePost));
        lenient().when(pollPostRepository.findById(any())).thenReturn(Optional.of(pollPost));


        PostCommentDto postCommentDto = new PostCommentDto();
        postCommentDto.setText("FirstText");

        PostCommentDto postCommentDto2 = new PostCommentDto();
        postCommentDto2.setText("SecondText");

        when(postMapper.postCommentEntityToDto(postCommentEntity)).thenReturn(postCommentDto);
        when(postMapper.postCommentEntityToDto(postCommentEntity2)).thenReturn(postCommentDto2);

        try (MockedStatic mockedStatic = Mockito.mockStatic(ServletUriComponentsBuilder.class)) {
            mockedStatic.when(ServletUriComponentsBuilder::fromCurrentContextPath).thenReturn(mock(ServletUriComponentsBuilder.class));

            PostCommentPageDto result = postService.getPostComments(postType, postId, page, pageSize, request);

            assertNotNull(result);
            assertEquals(List.of(postCommentDto, postCommentDto2), result.getComments());

            verify(postCommentRepository, times(1)).findAllBySimplePostId(eq(postId), any(PageRequest.class));
            assertEquals("null?page=2&pageSize=2", result.getNextPageUrl());
        }
    }

    private static Stream<Arguments> providePostTypeAndPageSize() {
        return Stream.of(
                Arguments.of("simple", 2),
                Arguments.of("played-game", 2),
                Arguments.of("poll", 2)
        );
    }

    @Test
    void getPostComments_WithInvalidPostType_ShouldThrowException() {
        String postType = "invalid";
        Long postId = 1L;
        PostCommentDto commentDto = new PostCommentDto();

        assertThrows(FieldValidationException.class, () -> postService.getPostComments(postType, postId, 5, 15, null));
    }

    @Test
    void likePost_SimplePost_Success() {
        UserEntity userEntity = new UserEntity();
        userEntity.setId(1L);
        when(userService.getUserEntityByEmail(any())).thenReturn(userEntity);

        Long postId = 1L;
        when(simplePostRepository.findById(postId)).thenReturn(Optional.of(new SimplePostEntity()));

        postService.likePost("simple", postId);

        verify(postLikeRepository).findBySimplePostIdAndLikeOwnerId(postId, userEntity.getId());
        verify(postLikeRepository).save(any(PostLikeEntity.class));
    }

    @Test
    void likePost_SimplePost_Unlike_Success() {
        UserEntity userEntity = new UserEntity();
        userEntity.setId(1L);
        when(userService.getUserEntityByEmail(any())).thenReturn(userEntity);

        when(simplePostRepository.findById(any())).thenReturn(Optional.of(new SimplePostEntity()));

        Long postId = 1L;
        when(postLikeRepository.findBySimplePostIdAndLikeOwnerId(postId, userEntity.getId())).thenReturn(Optional.of(new PostLikeEntity()));

        postService.likePost("simple", postId);

        verify(postLikeRepository).findBySimplePostIdAndLikeOwnerId(postId, userEntity.getId());
        verify(postLikeRepository).delete(any(PostLikeEntity.class));
    }

    @Test
    void likePost_PlayedGamePost_Success() {
        UserEntity userEntity = new UserEntity();
        userEntity.setId(1L);
        when(userService.getUserEntityByEmail(any())).thenReturn(userEntity);

        Long postId = 1L;

        PlayedGamePostEntity playedGamePostEntity = new PlayedGamePostEntity(postId,
                100, 20, 50.0,
                "lowest-score",
                new SimplePostEntity(1L, "description", new Date(), userEntity, new ArrayList<>(), new HashSet<>(), null, null, null),
                new PlayedGameEntity());

        when(playedGamePostRepository.findById(postId)).thenReturn(Optional.of(playedGamePostEntity));

        postService.likePost("played-game", postId);

        verify(postLikeRepository).findBySimplePostIdAndLikeOwnerId(playedGamePostEntity.getBasePost().getId(), userEntity.getId());
        verify(postLikeRepository).save(any(PostLikeEntity.class));
    }

    @Test
    void likePost_PlayedGamePost_Unlike_Success() {
        UserEntity userEntity = new UserEntity();
        when(userService.getUserEntityByEmail(any())).thenReturn(userEntity);

        Long postId = 1L;

        PlayedGamePostEntity playedGamePostEntity = new PlayedGamePostEntity(postId,
                100, 20, 50.0,
                "lowest-score",
                new SimplePostEntity(1L, "description", new Date(), userEntity, new ArrayList<>(), new HashSet<>(), null, null, null),
                new PlayedGameEntity());

        when(playedGamePostRepository.findById(postId)).thenReturn(Optional.of(playedGamePostEntity));
        when(postLikeRepository.findBySimplePostIdAndLikeOwnerId(playedGamePostEntity.getBasePost().getId(), userEntity.getId())).thenReturn(Optional.of(new PostLikeEntity()));
        postService.likePost("played-game", postId);

        verify(postLikeRepository).findBySimplePostIdAndLikeOwnerId(playedGamePostEntity.getBasePost().getId(), userEntity.getId());
        verify(postLikeRepository).delete(any(PostLikeEntity.class));
    }

    @Test
    void likePost_PollPost_Success() {
        UserEntity userEntity = new UserEntity();
        userEntity.setId(1L);
        when(userService.getUserEntityByEmail(any())).thenReturn(userEntity);

        Long postId = 1L;

        PollPostEntity pollPostEntity = new PollPostEntity(postId,
                new ArrayList<>(),
                new SimplePostEntity(1L, "description",
                        new Date(), userEntity, new ArrayList<>(),
                        new HashSet<>(), null,
                        null, null));

        when(pollPostRepository.findById(postId)).thenReturn(Optional.of(pollPostEntity));
        when(postLikeRepository.findBySimplePostIdAndLikeOwnerId(any(), any())).thenReturn(Optional.empty());
        when(postLikeRepository.save(any())).thenReturn(new PostLikeEntity());

        postService.likePost("poll", postId);

        verify(postLikeRepository).findBySimplePostIdAndLikeOwnerId(pollPostEntity.getBasePost().getId(), userEntity.getId());
        verify(postLikeRepository).save(any(PostLikeEntity.class));
    }

    @Test
    void likePost_PollPost_Unlike_Success() {
        UserEntity userEntity = new UserEntity();
        userEntity.setId(1L);
        when(userService.getUserEntityByEmail(any())).thenReturn(userEntity);

        Long postId = 1L;

        PollPostEntity pollPostEntity = new PollPostEntity(postId,
                new ArrayList<>(),
                new SimplePostEntity(1L, "description",
                        new Date(), userEntity, new ArrayList<>(),
                        new HashSet<>(), null,
                        null, null));

        when(pollPostRepository.findById(postId)).thenReturn(Optional.of(pollPostEntity));
        when(postLikeRepository.findBySimplePostIdAndLikeOwnerId(any(), any())).thenReturn(Optional.of(new PostLikeEntity()));

        postService.likePost("poll", postId);

        verify(postLikeRepository).findBySimplePostIdAndLikeOwnerId(pollPostEntity.getBasePost().getId(), userEntity.getId());
        verify(postLikeRepository).delete(any(PostLikeEntity.class));
    }

    @Test
    void likePost_InvalidPostType_ThrowException() {
        Long postId = 1L;

        FieldValidationException exception = org.junit.jupiter.api.Assertions.assertThrows(FieldValidationException.class, () -> {
            postService.likePost("invalid", postId);
        });
        assertEquals("This post type does not support likes", exception.getFieldErrors().get("postType"));
    }
}