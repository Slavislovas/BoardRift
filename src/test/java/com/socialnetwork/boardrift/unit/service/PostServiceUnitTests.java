package com.socialnetwork.boardrift.unit.service;

import com.socialnetwork.boardrift.repository.PlayedGamePostRepository;
import com.socialnetwork.boardrift.repository.PlayedGameRepository;
import com.socialnetwork.boardrift.repository.PollPostRepository;
import com.socialnetwork.boardrift.repository.PostCommentRepository;
import com.socialnetwork.boardrift.repository.SimplePostRepository;
import com.socialnetwork.boardrift.repository.model.UserEntity;
import com.socialnetwork.boardrift.repository.model.post.PlayedGamePostEntity;
import com.socialnetwork.boardrift.repository.model.post.PostCommentEntity;
import com.socialnetwork.boardrift.repository.model.post.PollOptionEntity;
import com.socialnetwork.boardrift.repository.model.post.PollOptionVoteEntity;
import com.socialnetwork.boardrift.repository.model.post.PollPostEntity;
import com.socialnetwork.boardrift.repository.model.post.SimplePostEntity;
import com.socialnetwork.boardrift.rest.model.BGGThingResponse;
import com.socialnetwork.boardrift.rest.model.PostCommentDto;
import com.socialnetwork.boardrift.rest.model.PostCommentPageDto;
import com.socialnetwork.boardrift.rest.model.played_game_post.PlayedGamePostCreationDto;
import com.socialnetwork.boardrift.rest.model.played_game_post.PlayedGamePostRetrievalDto;
import com.socialnetwork.boardrift.rest.model.poll_post.PollOptionDto;
import com.socialnetwork.boardrift.rest.model.poll_post.PollPostCreationDto;
import com.socialnetwork.boardrift.rest.model.poll_post.PollPostRetrievalDto;
import com.socialnetwork.boardrift.rest.model.simple_post.SimplePostCreationDto;
import com.socialnetwork.boardrift.rest.model.simple_post.SimplePostRetrievalDto;
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
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collections;
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
        playerDto.setId(1L);
        playerDto.setPoints(200);

        PlayedGamePostCreationDto.SelectedPlayerDto playerDto2 = new PlayedGamePostCreationDto.SelectedPlayerDto();
        playerDto2.setId(2L);
        playerDto2.setPoints(100);

        playedGamePostCreationDto.setPlayers(new HashSet<>(Set.of(playerDto, playerDto2)));

        UserEntity userEntity = new UserEntity();
        userEntity.setUsername(userDetails.getUsername());
        userEntity.setPlayedGames(new ArrayList<>());
        when(userService.getUserEntityByUsername(any())).thenReturn(userEntity);
        when(userService.getUserEntityById(any())).thenReturn(userEntity);

        BGGThingResponse boardGameResponse = new BGGThingResponse();
        BGGThingResponse.Item item = new BGGThingResponse.Item();
        BGGThingResponse.Item.Name name = new BGGThingResponse.Item.Name();
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
        when(userService.getUserEntityByUsername(userDetails.getUsername())).thenReturn(userEntity);

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

        when(userService.getUserEntityByUsername(userDetails.getUsername())).thenReturn(voterEntity);

        PollPostEntity pollPostEntity = new PollPostEntity();
        pollPostEntity.setId(pollId);

        PollOptionEntity pollOptionEntity = new PollOptionEntity();
        pollOptionEntity.setId(optionId);
        pollOptionEntity.setVotes(new HashSet<>());

        pollPostEntity.setOptions(Collections.singleton(pollOptionEntity));

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

        when(userService.getUserEntityByUsername(userDetails.getUsername())).thenReturn(voterEntity);

        PollPostEntity pollPostEntity = new PollPostEntity();
        pollPostEntity.setId(pollId);

        PollOptionEntity pollOptionEntity = new PollOptionEntity();
        pollOptionEntity.setId(optionId);

        PollOptionVoteEntity existingVote = new PollOptionVoteEntity();
        existingVote.setId(2L);
        existingVote.setVoter(voterEntity);
        pollOptionEntity.setVotes(Set.of(existingVote));

        pollPostEntity.setOptions(Collections.singleton(pollOptionEntity));

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

        when(userService.getUserEntityByUsername(userDetails.getUsername())).thenReturn(userEntity);

        SimplePostEntity simplePostEntity = new SimplePostEntity();
        when(simplePostRepository.findById(postId)).thenReturn(java.util.Optional.of(simplePostEntity));

        PostCommentEntity savedPostCommentEntity = new PostCommentEntity(1L, commentDto.getText(), null, simplePostEntity, null, null, userEntity);
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

        when(userService.getUserEntityByUsername(userDetails.getUsername())).thenReturn(userEntity);

        PlayedGamePostEntity playedGamePostEntity = new PlayedGamePostEntity();
        when(playedGamePostRepository.findById(postId)).thenReturn(java.util.Optional.of(playedGamePostEntity));

        PostCommentEntity savedPostCommentEntity = new PostCommentEntity(1L, commentDto.getText(), null, null, playedGamePostEntity, null, userEntity);
        when(postCommentRepository.save(any())).thenReturn(savedPostCommentEntity);

        PostCommentDto expectedCommentDto = new PostCommentDto();

        when(postMapper.postCommentEntityToDto(any())).thenReturn(expectedCommentDto);

        PostCommentDto result = postService.createPostComment(postType, postId, commentDto);

        assertEquals(expectedCommentDto, result);
        verify(playedGamePostRepository, times(1)).findById(postId);
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
        when(userService.getUserEntityByUsername(any())).thenReturn(userEntity);

        SimplePostEntity savedSimplePostEntity = new SimplePostEntity();
        when(simplePostRepository.save(any(SimplePostEntity.class))).thenReturn(savedSimplePostEntity);

        SimplePostRetrievalDto expectedDto = new SimplePostRetrievalDto();
        when(postMapper.simplePostEntityToRetrievalDto(savedSimplePostEntity)).thenReturn(expectedDto);

        // Act
        SimplePostRetrievalDto result = postService.createSimplePost(simplePostCreationDto);

        // Assert
        assertEquals(expectedDto, result);
        verify(userService, times(1)).getUserEntityByUsername(any());
        verify(simplePostRepository, times(1)).save(any(SimplePostEntity.class));
        verify(postMapper, times(1)).simplePostEntityToRetrievalDto(savedSimplePostEntity);
    }

    @Test
    void createPollPost_ShouldReturnDto_WhenValidInput() {
        PollPostCreationDto pollPostCreationDto = new PollPostCreationDto("Question", Collections.singletonList(new PollOptionDto("Option")));

        UserEntity userEntity = new UserEntity();
        when(userService.getUserEntityByUsername(any())).thenReturn(userEntity);

        PollPostEntity savedPollPostEntity = new PollPostEntity();
        when(pollPostRepository.save(any(PollPostEntity.class))).thenReturn(savedPollPostEntity);

        PollPostRetrievalDto expectedDto = new PollPostRetrievalDto();
        when(postMapper.pollPostEntityToRetrievalDto(savedPollPostEntity, false)).thenReturn(expectedDto);

        // Act
        PollPostRetrievalDto result = postService.createPollPost(pollPostCreationDto);

        // Assert
        assertEquals(expectedDto, result);
        verify(userService, times(1)).getUserEntityByUsername(any());
        verify(pollPostRepository, times(1)).save(any(PollPostEntity.class));
        verify(postMapper, times(1)).pollPostEntityToRetrievalDto(savedPollPostEntity, false);
    }

    @ParameterizedTest
    @MethodSource("providePostTypeAndPageSize")
    void getPostComments_ShouldReturnPageDto_WithDifferentPostTypesAndPageSizes(String postType, int pageSize) {
        Long postId = 1L;
        Integer page = 1;
        HttpServletRequest request = new MockHttpServletRequest();

        PostCommentEntity postCommentEntity = new PostCommentEntity();
        postCommentEntity.setText("FirstText");

        PostCommentEntity postCommentEntity2 = new PostCommentEntity();
        postCommentEntity2.setText("SecondText");

        List<PostCommentEntity> commentEntities = List.of(postCommentEntity, postCommentEntity2);

        lenient().when(postCommentRepository.findAllBySimplePostId(eq(postId), any(PageRequest.class))).thenReturn(commentEntities);
        lenient().when(postCommentRepository.findAllByPlayedGamePostId(eq(postId), any(PageRequest.class))).thenReturn(commentEntities);

        PostCommentDto postCommentDto = new PostCommentDto();
        postCommentDto.setText("FirstText");

        PostCommentDto postCommentDto2 = new PostCommentDto();
        postCommentDto2.setText("SecondText");

        when(postMapper.postCommentEntityToDto(postCommentEntity)).thenReturn(postCommentDto);
        when(postMapper.postCommentEntityToDto(postCommentEntity2)).thenReturn(postCommentDto2);

        PostCommentPageDto result = postService.getPostComments(postType, postId, page, pageSize, request);

        assertNotNull(result);
        assertNull(result.getNextPageUrl());
        assertEquals(List.of(postCommentDto, postCommentDto2), result.getComments());

        if ("simple".equals(postType)) {
            verify(postCommentRepository, times(1)).findAllBySimplePostId(eq(postId), any(PageRequest.class));
        } else if ("played-game".equals(postType)) {
            verify(postCommentRepository, times(1)).findAllByPlayedGamePostId(eq(postId), any(PageRequest.class));
        }
    }

    private static Stream<Arguments> providePostTypeAndPageSize() {
        return Stream.of(
                Arguments.of("simple", 2),
                Arguments.of("played-game", 2)
        );
    }
}