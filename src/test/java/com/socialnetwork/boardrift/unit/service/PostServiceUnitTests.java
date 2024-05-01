package com.socialnetwork.boardrift.unit.service;

import com.socialnetwork.boardrift.enumeration.Role;
import com.socialnetwork.boardrift.enumeration.UserStatus;
import com.socialnetwork.boardrift.repository.PlayedGamePostRepository;
import com.socialnetwork.boardrift.repository.PlayedGameRepository;
import com.socialnetwork.boardrift.repository.PollPostRepository;
import com.socialnetwork.boardrift.repository.PostCommentReportRepository;
import com.socialnetwork.boardrift.repository.PostCommentRepository;
import com.socialnetwork.boardrift.repository.PostLikeRepository;
import com.socialnetwork.boardrift.repository.PostReportRepository;
import com.socialnetwork.boardrift.repository.SimplePostRepository;
import com.socialnetwork.boardrift.repository.model.post.Post;
import com.socialnetwork.boardrift.repository.model.post.PostCommentReportEntity;
import com.socialnetwork.boardrift.repository.model.post.PostReportEntity;
import com.socialnetwork.boardrift.repository.model.user.SuspensionEntity;
import com.socialnetwork.boardrift.repository.model.user.UserEntity;
import com.socialnetwork.boardrift.repository.model.board_game.PlayedGameEntity;
import com.socialnetwork.boardrift.repository.model.post.PlayedGamePostEntity;
import com.socialnetwork.boardrift.repository.model.post.PostCommentEntity;
import com.socialnetwork.boardrift.repository.model.post.PollOptionEntity;
import com.socialnetwork.boardrift.repository.model.post.PollOptionVoteEntity;
import com.socialnetwork.boardrift.repository.model.post.PollPostEntity;
import com.socialnetwork.boardrift.repository.model.post.PostLikeEntity;
import com.socialnetwork.boardrift.repository.model.post.SimplePostEntity;
import com.socialnetwork.boardrift.rest.model.BGGThingResponse;
import com.socialnetwork.boardrift.rest.model.PlayedGamePageDto;
import com.socialnetwork.boardrift.rest.model.post.PostCommentDto;
import com.socialnetwork.boardrift.rest.model.post.PostCommentPageDto;
import com.socialnetwork.boardrift.rest.model.post.PostPageDto;
import com.socialnetwork.boardrift.rest.model.post.ReportDto;
import com.socialnetwork.boardrift.rest.model.post.played_game_post.PlayedGamePostCreationDto;
import com.socialnetwork.boardrift.rest.model.post.played_game_post.PlayedGamePostRetrievalDto;
import com.socialnetwork.boardrift.rest.model.post.poll_post.PollOptionDto;
import com.socialnetwork.boardrift.rest.model.post.poll_post.PollPostCreationDto;
import com.socialnetwork.boardrift.rest.model.post.poll_post.PollPostRetrievalDto;
import com.socialnetwork.boardrift.rest.model.post.simple_post.SimplePostCreationDto;
import com.socialnetwork.boardrift.rest.model.post.simple_post.SimplePostRetrievalDto;
import com.socialnetwork.boardrift.rest.model.user.UserRetrievalMinimalDto;
import com.socialnetwork.boardrift.service.BoardGameService;
import com.socialnetwork.boardrift.service.NotificationService;
import com.socialnetwork.boardrift.service.PostService;
import com.socialnetwork.boardrift.service.UserService;
import com.socialnetwork.boardrift.util.exception.DuplicatePollVoteException;
import com.socialnetwork.boardrift.util.exception.DuplicateReportException;
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
import org.springframework.security.core.parameters.P;
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
    private NotificationService notificationService;

    @Mock
    private UserService userService;

    @Mock
    private BoardGameService boardGameService;

    @Mock
    private PostMapper postMapper;

    @Mock
    private PostReportRepository postReportRepository;

    @Mock
    private PostCommentReportRepository postCommentReportRepository;

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

        when(notificationService.createAndSaveNotificationsForAssociatedPlays(any())).thenReturn(Collections.emptyList());

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
        userEntity.setEmail(userDetails.getUsername());
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
        userEntity.setEmail(userDetails.getUsername());
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
        userEntity.setEmail(userDetails.getUsername());

        when(userService.getUserEntityByEmail(userDetails.getUsername())).thenReturn(userEntity);

        SimplePostEntity simplePostEntity = new SimplePostEntity();
        when(simplePostRepository.findById(postId)).thenReturn(java.util.Optional.of(simplePostEntity));

        PostCommentEntity savedPostCommentEntity = new PostCommentEntity(1L, commentDto.getText(), null, simplePostEntity, userEntity, new ArrayList<>());
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
        userEntity.setEmail(userDetails.getUsername());

        when(userService.getUserEntityByEmail(userDetails.getUsername())).thenReturn(userEntity);

        PlayedGamePostEntity playedGamePostEntity = new PlayedGamePostEntity();
        when(playedGamePostRepository.findById(postId)).thenReturn(java.util.Optional.of(playedGamePostEntity));

        PostCommentEntity savedPostCommentEntity = new PostCommentEntity(1L, commentDto.getText(), Instant.now(), playedGamePostEntity.getBasePost(), userEntity, new ArrayList<>());
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
        userEntity.setEmail(userDetails.getUsername());

        when(userService.getUserEntityByEmail(userDetails.getUsername())).thenReturn(userEntity);

        PollPostEntity pollPostEntity = new PollPostEntity();
        when(pollPostRepository.findById(postId)).thenReturn(java.util.Optional.of(pollPostEntity));

        PostCommentEntity savedPostCommentEntity = new PostCommentEntity(1L, commentDto.getText(), Instant.now(), pollPostEntity.getBasePost(), userEntity, new ArrayList<>());
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
        postCommentEntity.setCommentCreator(new UserEntity(1L, "Name", "Lastname", "email@gmail.com",
                "2001-11-16", "Password@123", "", "", "", true, false, false, false, "",
                Role.ROLE_USER, false, new ArrayList<>(),  new HashSet<>(),
                new HashSet<>(),  new ArrayList<>(),  new HashSet<>(), new HashSet<>(), new ArrayList<>(), null));

        PostCommentEntity postCommentEntity2 = new PostCommentEntity();
        postCommentEntity2.setText("SecondText");
        postCommentEntity2.setCommentCreator(new UserEntity(1L, "Name", "Lastname", "email@gmail.com",
                "2001-11-16", "Password@123", "", "", "", true, false, false, false, "",
                Role.ROLE_USER,  false, new ArrayList<>(),  new HashSet<>(),
                new HashSet<>(),  new ArrayList<>(),  new HashSet<>(), new HashSet<>(), new ArrayList<>(), null));

        List<PostCommentEntity> commentEntities = List.of(postCommentEntity, postCommentEntity2);

        SimplePostEntity simplePost  = new SimplePostEntity(1L, "description", new Date(), new UserEntity(), commentEntities, new HashSet<>(), null, null, new ArrayList<>());

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
        lenient().when(userService.getUserEntityByEmail(any())).thenReturn(new UserEntity(1L, "Name", "Lastname", "email@gmail.com",
                "2001-11-16", "Password@123", "", "", "", true, false, false, false, "",
                Role.ROLE_USER, false, new ArrayList<>(),  new HashSet<>(),
                new HashSet<>(),  new ArrayList<>(),  new HashSet<>(), new HashSet<>(), new ArrayList<>(), null));


        PostCommentDto postCommentDto = new PostCommentDto();
        postCommentDto.setText("FirstText");
        postCommentDto.setReports(List.of(new ReportDto(1L , "test", new UserRetrievalMinimalDto(1L, "test", "test", "test", false, 0, false))));

        PostCommentDto postCommentDto2 = new PostCommentDto();
        postCommentDto2.setText("SecondText");
        postCommentDto2.setReports(List.of(new ReportDto(1L , "test", new UserRetrievalMinimalDto(1L, "test", "test", "test", false, 0, false))));

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
                new SimplePostEntity(1L, "description", new Date(), userEntity, new ArrayList<>(), new HashSet<>(), null, null, new ArrayList<>()),
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
                new SimplePostEntity(1L, "description", new Date(), userEntity, new ArrayList<>(), new HashSet<>(), null, null, new ArrayList<>()),
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
                        null, new ArrayList<>()));

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
                        null, new ArrayList<>()));

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

    @Test
    void getFeedPage_shouldSucceed() {
        UserEntity userEntity = new UserEntity();
        userEntity.setId(1L);
        when(userService.getUserEntityByEmail(any())).thenReturn(userEntity);
        HttpServletRequest request = new MockHttpServletRequest();

        SimplePostEntity simplePostEntity = new SimplePostEntity(1L, "description", new Date(), userEntity, new ArrayList<>(),
                new HashSet<>(), null, null, new ArrayList<>());

        PollOptionVoteEntity pollOptionVoteEntity = new PollOptionVoteEntity(1L, null, userEntity);
        PollOptionEntity pollOptionEntity = new PollOptionEntity(1L, "text", null, Set.of(pollOptionVoteEntity));
        PollPostEntity pollPostEntity = new PollPostEntity(1L, List.of(pollOptionEntity), simplePostEntity);

        PlayedGamePostEntity playedGamePostEntity = new PlayedGamePostEntity(1L, 0, 0, 0.0, "no-score", simplePostEntity, null);

        PlayedGamePostRetrievalDto playedGamePostRetrievalDto = new PlayedGamePostRetrievalDto();
        playedGamePostRetrievalDto.setId(1L);
        playedGamePostRetrievalDto.setCreationDate(new Date());

        SimplePostRetrievalDto simplePostRetrievalDto = new SimplePostRetrievalDto();
        simplePostRetrievalDto.setId(1L);
        simplePostRetrievalDto.setCreationDate(new Date());

        PollPostRetrievalDto pollPostRetrievalDto = new PollPostRetrievalDto();
        pollPostRetrievalDto.setId(1L);
        pollPostRetrievalDto.setCreationDate(new Date());

        when(pollPostRepository.findAllByPostCreatorOrFriends(any(), any())).thenReturn(List.of(pollPostEntity));
        when(simplePostRepository.findAllByPostCreatorOrFriends(any(), any())).thenReturn(List.of(simplePostEntity));
        when(playedGamePostRepository.findAllByPostCreatorOrFriends(any(), any())).thenReturn(List.of(playedGamePostEntity));
        when(postMapper.playedGamePostEntityToRetrievalDto(any())).thenReturn(playedGamePostRetrievalDto);
        when(postMapper.pollPostEntityToRetrievalDto(any())).thenReturn(pollPostRetrievalDto);
        when(postMapper.simplePostEntityToRetrievalDto(any())).thenReturn(simplePostRetrievalDto);

        try (MockedStatic mockedStatic = Mockito.mockStatic(ServletUriComponentsBuilder.class)) {
            mockedStatic.when(ServletUriComponentsBuilder::fromCurrentContextPath).thenReturn(mock(ServletUriComponentsBuilder.class));

            PostPageDto result = postService.getFeed(0, 3, request);
            assertTrue(result.getPosts().containsAll(List.of(playedGamePostRetrievalDto, simplePostRetrievalDto, pollPostRetrievalDto)));
        }
    }

    @Test
    void getPostsByUserId_shouldSucceed() throws IllegalAccessException {
        UserEntity userEntity = new UserEntity();
        userEntity.setId(1L);
        when(userService.getUserEntityByEmail(any())).thenReturn(userEntity);
        HttpServletRequest request = new MockHttpServletRequest();

        SimplePostEntity simplePostEntity = new SimplePostEntity(1L, "description", new Date(), userEntity, new ArrayList<>(),
                new HashSet<>(), null, null, new ArrayList<>());

        PollOptionVoteEntity pollOptionVoteEntity = new PollOptionVoteEntity(1L, null, userEntity);
        PollOptionEntity pollOptionEntity = new PollOptionEntity(1L, "text", null, Set.of(pollOptionVoteEntity));
        PollPostEntity pollPostEntity = new PollPostEntity(1L, List.of(pollOptionEntity), simplePostEntity);

        PlayedGamePostEntity playedGamePostEntity = new PlayedGamePostEntity(1L, 0, 0, 0.0, "no-score", simplePostEntity, null);

        PlayedGamePostRetrievalDto playedGamePostRetrievalDto = new PlayedGamePostRetrievalDto();
        playedGamePostRetrievalDto.setId(1L);
        playedGamePostRetrievalDto.setCreationDate(new Date());

        SimplePostRetrievalDto simplePostRetrievalDto = new SimplePostRetrievalDto();
        simplePostRetrievalDto.setId(1L);
        simplePostRetrievalDto.setCreationDate(new Date());

        PollPostRetrievalDto pollPostRetrievalDto = new PollPostRetrievalDto();
        pollPostRetrievalDto.setId(1L);
        pollPostRetrievalDto.setCreationDate(new Date());

        when(simplePostRepository.findByPostCreatorId(any(), any())).thenReturn(List.of(simplePostEntity));
        when(pollPostRepository.findByBasePostPostCreatorId(any(), any())).thenReturn(List.of(pollPostEntity));
        when(playedGamePostRepository.findByBasePostPostCreatorId(any(), any())).thenReturn(List.of(playedGamePostEntity));
        when(postMapper.playedGamePostEntityToRetrievalDto(any())).thenReturn(playedGamePostRetrievalDto);
        when(postMapper.pollPostEntityToRetrievalDto(any())).thenReturn(pollPostRetrievalDto);
        when(postMapper.simplePostEntityToRetrievalDto(any())).thenReturn(simplePostRetrievalDto);

        try (MockedStatic mockedStatic = Mockito.mockStatic(ServletUriComponentsBuilder.class)) {
            mockedStatic.when(ServletUriComponentsBuilder::fromCurrentContextPath).thenReturn(mock(ServletUriComponentsBuilder.class));

            PostPageDto result = postService.getPostsByUserId(1L, 0, 2, request);
            assertTrue(result.getPosts().containsAll(List.of(pollPostRetrievalDto, simplePostRetrievalDto)));
        }
    }

    @Test
    void getPostsByUserId_shouldFail_userSuspended() throws IllegalAccessException {
        UserEntity userEntity = new UserEntity();
        userEntity.setId(1L);

        UserEntity userEntity2 = new UserEntity();
        userEntity2.setId(2L);
        userEntity2.setSuspension(new SuspensionEntity());

        when(userService.getUserEntityByEmail(any())).thenReturn(userEntity);
        when(userService.getUserEntityById(any())).thenReturn(userEntity2);
        HttpServletRequest request = new MockHttpServletRequest();

        assertThrows(IllegalAccessException.class, () -> postService.getPostsByUserId(2L, 0, 2, request));
    }

    @Test
    void getPostsByUserId_shouldFail_privatePostsAndNotFriend() throws IllegalAccessException {
        UserEntity userEntity = new UserEntity();
        userEntity.setId(1L);
        userEntity.setFriends(new HashSet<>());
        userEntity.setFriendOf(new HashSet<>());

        UserEntity userEntity2 = new UserEntity();
        userEntity2.setId(2L);
        userEntity2.setPublicPosts(false);

        when(userService.getUserEntityByEmail(any())).thenReturn(userEntity);
        when(userService.getUserEntityById(any())).thenReturn(userEntity2);
        HttpServletRequest request = new MockHttpServletRequest();

        assertThrows(IllegalAccessException.class, () -> postService.getPostsByUserId(2L, 0, 2, request));
    }

    @Test
    void getReportedComments_shouldSucceed() {
        HttpServletRequest request = new MockHttpServletRequest();

        SimplePostEntity simplePostEntity = new SimplePostEntity(1L, "description", new Date(), null, new ArrayList<>(),
                new HashSet<>(), null, null, new ArrayList<>());

        SimplePostEntity simplePostEntity2 = new SimplePostEntity(2L, "description", new Date(), null, new ArrayList<>(),
                new HashSet<>(), null, null, new ArrayList<>());

        SimplePostEntity simplePostEntity3 = new SimplePostEntity(3L, "description", new Date(), null, new ArrayList<>(),
                new HashSet<>(), null, null, new ArrayList<>());

        PollOptionVoteEntity pollOptionVoteEntity = new PollOptionVoteEntity(1L, null, null);
        PollOptionEntity pollOptionEntity = new PollOptionEntity(1L, "text", null, Set.of(pollOptionVoteEntity));
        PollPostEntity pollPostEntity = new PollPostEntity(1L, List.of(pollOptionEntity), simplePostEntity);

        PlayedGamePostEntity playedGamePostEntity = new PlayedGamePostEntity(1L, 0, 0, 0.0, "no-score", simplePostEntity, null);

        PlayedGamePostRetrievalDto playedGamePostRetrievalDto = new PlayedGamePostRetrievalDto();
        playedGamePostRetrievalDto.setId(1L);
        playedGamePostRetrievalDto.setCreationDate(new Date());

        SimplePostRetrievalDto simplePostRetrievalDto = new SimplePostRetrievalDto();
        simplePostRetrievalDto.setId(1L);
        simplePostRetrievalDto.setCreationDate(new Date());

        PollPostRetrievalDto pollPostRetrievalDto = new PollPostRetrievalDto();
        pollPostRetrievalDto.setId(1L);
        pollPostRetrievalDto.setCreationDate(new Date());

        simplePostEntity.setChildPlayedGamePost(playedGamePostEntity);
        simplePostEntity2.setChildPollPost(pollPostEntity);

        PostCommentEntity postCommentEntity = new PostCommentEntity(1L, "text", Instant.now(), simplePostEntity, null, new ArrayList<>());
        PostCommentEntity postCommentEntity2 = new PostCommentEntity(2L, "text", Instant.now(), simplePostEntity2, null, new ArrayList<>());
        PostCommentEntity postCommentEntity3 = new PostCommentEntity(3L, "text", Instant.now(), simplePostEntity3, null, new ArrayList<>());

        PostCommentDto postCommentDto = new PostCommentDto(1L, "text", "2024-04-04", null, false, "postType", 1L, new ArrayList<>());
        PostCommentDto postCommentDto2 = new PostCommentDto(2L, "text", "2024-04-04", null, false, "postType", 1L, new ArrayList<>());
        PostCommentDto postCommentDto3 = new PostCommentDto(3L, "text", "2024-04-04", null, false, "postType", 1L, new ArrayList<>());

        when(postCommentRepository.findReportedComments(any())).thenReturn(List.of(postCommentEntity, postCommentEntity2, postCommentEntity3));
        when(postMapper.postCommentEntityToDto(postCommentEntity)).thenReturn(postCommentDto);
        when(postMapper.postCommentEntityToDto(postCommentEntity2)).thenReturn(postCommentDto2);
        when(postMapper.postCommentEntityToDto(postCommentEntity3)).thenReturn(postCommentDto3);

        try (MockedStatic mockedStatic = Mockito.mockStatic(ServletUriComponentsBuilder.class)) {
            mockedStatic.when(ServletUriComponentsBuilder::fromCurrentContextPath).thenReturn(mock(ServletUriComponentsBuilder.class));

            PostCommentPageDto result = postService.getReportedComments( 0, 3, request);
            assertTrue(result.getComments().containsAll(List.of(postCommentDto, postCommentDto2, postCommentDto3)));
        }
    }

    @Test
    void getReportedPosts_shouldSucceed() {
        UserEntity userEntity = new UserEntity();
        userEntity.setId(1L);
        when(userService.getUserEntityByEmail(any())).thenReturn(userEntity);
        HttpServletRequest request = new MockHttpServletRequest();

        SimplePostEntity simplePostEntity = new SimplePostEntity(1L, "description", new Date(), userEntity, new ArrayList<>(),
                new HashSet<>(), null, null, new ArrayList<>());

        PollOptionVoteEntity pollOptionVoteEntity = new PollOptionVoteEntity(1L, null, userEntity);
        PollOptionEntity pollOptionEntity = new PollOptionEntity(1L, "text", null, Set.of(pollOptionVoteEntity));
        PollPostEntity pollPostEntity = new PollPostEntity(1L, List.of(pollOptionEntity), simplePostEntity);

        PlayedGamePostEntity playedGamePostEntity = new PlayedGamePostEntity(1L, 0, 0, 0.0, "no-score", simplePostEntity, null);

        PlayedGamePostRetrievalDto playedGamePostRetrievalDto = new PlayedGamePostRetrievalDto();
        playedGamePostRetrievalDto.setId(1L);
        playedGamePostRetrievalDto.setCreationDate(new Date());
        playedGamePostRetrievalDto.setReports(new ArrayList<>());

        SimplePostRetrievalDto simplePostRetrievalDto = new SimplePostRetrievalDto();
        simplePostRetrievalDto.setId(1L);
        simplePostRetrievalDto.setCreationDate(new Date());
        simplePostRetrievalDto.setReports(new ArrayList<>());

        PollPostRetrievalDto pollPostRetrievalDto = new PollPostRetrievalDto();
        pollPostRetrievalDto.setId(1L);
        pollPostRetrievalDto.setCreationDate(new Date());
        pollPostRetrievalDto.setReports(new ArrayList<>());

        when(simplePostRepository.findReportedPosts(any())).thenReturn(List.of(simplePostEntity));
        when(pollPostRepository.findReportedPosts(any())).thenReturn(List.of(pollPostEntity));
        when(playedGamePostRepository.findReportedPosts(any())).thenReturn(List.of(playedGamePostEntity));
        when(postMapper.playedGamePostEntityToRetrievalDto(any())).thenReturn(playedGamePostRetrievalDto);
        when(postMapper.pollPostEntityToRetrievalDto(any())).thenReturn(pollPostRetrievalDto);
        when(postMapper.simplePostEntityToRetrievalDto(any())).thenReturn(simplePostRetrievalDto);

        try (MockedStatic mockedStatic = Mockito.mockStatic(ServletUriComponentsBuilder.class)) {
            mockedStatic.when(ServletUriComponentsBuilder::fromCurrentContextPath).thenReturn(mock(ServletUriComponentsBuilder.class));

            PostPageDto result = postService.getReportedPosts(0, 2, request);
            assertTrue(result.getPosts().containsAll(List.of(playedGamePostRetrievalDto, simplePostRetrievalDto)));
        }
    }

    @Test
    void editSimplePost_shouldSucceed() throws IllegalAccessException {
        UserEntity userEntity = new UserEntity();
        userEntity.setId(1L);
        userEntity.setEmail("email");

        when(userService.getUserEntityByEmail(any())).thenReturn(userEntity);

        SimplePostEntity simplePostEntity = new SimplePostEntity(1L, "description", new Date(), userEntity, new ArrayList<>(),
                new HashSet<>(), null, null, new ArrayList<>());

        SimplePostRetrievalDto simplePostRetrievalDto = new SimplePostRetrievalDto();
        simplePostRetrievalDto.setId(1L);
        simplePostRetrievalDto.setCreationDate(new Date());
        simplePostRetrievalDto.setReports(new ArrayList<>());

        SimplePostCreationDto simplePostCreationDto = new SimplePostCreationDto("description");

        when(postMapper.simplePostEntityToRetrievalDto(any())).thenReturn(simplePostRetrievalDto);
        when(simplePostRepository.findById(any())).thenReturn(Optional.of(simplePostEntity));
        when(simplePostRepository.save(any())).thenReturn(simplePostEntity);

        SimplePostRetrievalDto result = postService.editSimplePost(1L, simplePostCreationDto);

        assertEquals(simplePostRetrievalDto, result);
        verify(simplePostRepository, times(1)).save(any());
    }

    @Test
    void editSimplePost_shouldFail() {
        UserEntity userEntity = new UserEntity();
        userEntity.setId(1L);
        userEntity.setEmail("email");

        UserEntity userEntity2 = new UserEntity();
        userEntity2.setId(2L);
        userEntity2.setEmail("email2");

        when(userService.getUserEntityByEmail(any())).thenReturn(userEntity);

        SimplePostEntity simplePostEntity = new SimplePostEntity(1L, "description", new Date(), userEntity2, new ArrayList<>(),
                new HashSet<>(), null, null, new ArrayList<>());

        when(simplePostRepository.findById(any())).thenReturn(Optional.of(simplePostEntity));

        assertThrows(IllegalAccessException.class, () -> postService.editSimplePost(1L, null));
    }

    @Test
    void deleteSimplePost_shouldSucceed() throws IllegalAccessException {
        UserEntity userEntity = new UserEntity();
        userEntity.setId(1L);
        userEntity.setEmail("email");

        Authentication authentication = new UsernamePasswordAuthenticationToken(userEntity, null);
        SecurityContext securityContext = mock(SecurityContext.class);
        lenient().when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        SimplePostEntity simplePostEntity = new SimplePostEntity(1L, "description", new Date(), userEntity, new ArrayList<>(),
                new HashSet<>(), null, null, new ArrayList<>());

        when(simplePostRepository.findById(any())).thenReturn(Optional.of(simplePostEntity));
        doNothing().when(simplePostRepository).delete(any());

        postService.deleteSimplePost(1L);
        verify(simplePostRepository, times(1)).delete(any());
    }

    @Test
    void deleteSimplePost_shouldFail() {
        UserEntity userEntity = new UserEntity();
        userEntity.setId(1L);
        userEntity.setEmail("email");

        UserEntity userEntity2 = new UserEntity();
        userEntity2.setId(2L);
        userEntity2.setEmail("email2");

        Authentication authentication = new UsernamePasswordAuthenticationToken(userEntity, null);
        SecurityContext securityContext = mock(SecurityContext.class);
        lenient().when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        SimplePostEntity simplePostEntity = new SimplePostEntity(1L, "description", new Date(), userEntity2, new ArrayList<>(),
                new HashSet<>(), null, null, new ArrayList<>());

        when(simplePostRepository.findById(any())).thenReturn(Optional.of(simplePostEntity));
        assertThrows(IllegalAccessException.class, () -> postService.deleteSimplePost(1L));
    }

    @Test
    void editPollPost_shouldSucceed() throws IllegalAccessException {
        UserEntity userEntity = new UserEntity();
        userEntity.setId(1L);
        userEntity.setEmail("email");
        when(userService.getUserEntityByEmail(any())).thenReturn(userEntity);

        SimplePostEntity simplePostEntity = new SimplePostEntity(1L, "description", new Date(), userEntity, new ArrayList<>(),
                new HashSet<>(), null, null, new ArrayList<>());

        PollOptionEntity pollOptionEntity = new PollOptionEntity(1L, "text", null, new HashSet<>());
        ArrayList<PollOptionEntity> pollOptions = new ArrayList<>();
        pollOptions.add(pollOptionEntity);
        PollPostEntity pollPostEntity = new PollPostEntity(1L, pollOptions, simplePostEntity);


        PollPostRetrievalDto pollPostRetrievalDto = new PollPostRetrievalDto();
        pollPostRetrievalDto.setId(1L);
        pollPostRetrievalDto.setCreationDate(new Date());
        pollPostRetrievalDto.setReports(new ArrayList<>());

        PollOptionDto pollOptionDto = new PollOptionDto(null, "test");
        PollOptionDto pollOptionDto2 = new PollOptionDto(1L, "test");
        ArrayList<PollOptionDto> pollOptionDtos = new ArrayList<>();
        pollOptionDtos.add(pollOptionDto);
        pollOptionDtos.add(pollOptionDto2);

        PollPostCreationDto pollPostCreationDto = new PollPostCreationDto("description", pollOptionDtos);

        when(pollPostRepository.findById(any())).thenReturn(Optional.of(pollPostEntity));
        when(pollPostRepository.save(any())).thenReturn(pollPostEntity);
        when(postMapper.pollPostEntityToRetrievalDto(any())).thenReturn(pollPostRetrievalDto);

        PollPostRetrievalDto result = postService.editPollPost(1L, pollPostCreationDto);
        assertEquals(pollPostRetrievalDto, result);
        verify(pollPostRepository, times(1)).save(any());
    }

    @Test
    void editPollPost_shouldFail_illegalAccess() {
        UserEntity userEntity = new UserEntity();
        userEntity.setId(1L);
        userEntity.setEmail("email");
        when(userService.getUserEntityByEmail(any())).thenReturn(userEntity);

        UserEntity userEntity2 = new UserEntity();
        userEntity2.setId(2L);
        userEntity2.setEmail("2");
        when(userService.getUserEntityByEmail(any())).thenReturn(userEntity);

        SimplePostEntity simplePostEntity = new SimplePostEntity(1L, "description", new Date(), userEntity2, new ArrayList<>(),
                new HashSet<>(), null, null, new ArrayList<>());


        PollOptionEntity pollOptionEntity = new PollOptionEntity(1L, "text", null, new HashSet<>());
        ArrayList<PollOptionEntity> pollOptions = new ArrayList<>();
        pollOptions.add(pollOptionEntity);
        PollPostEntity pollPostEntity = new PollPostEntity(1L, pollOptions, simplePostEntity);


        PollPostRetrievalDto pollPostRetrievalDto = new PollPostRetrievalDto();
        pollPostRetrievalDto.setId(1L);
        pollPostRetrievalDto.setCreationDate(new Date());
        pollPostRetrievalDto.setReports(new ArrayList<>());

        PollOptionDto pollOptionDto = new PollOptionDto(null, "test");
        PollOptionDto pollOptionDto2 = new PollOptionDto(1L, "test");
        ArrayList<PollOptionDto> pollOptionDtos = new ArrayList<>();
        pollOptionDtos.add(pollOptionDto);
        pollOptionDtos.add(pollOptionDto2);

        PollPostCreationDto pollPostCreationDto = new PollPostCreationDto("description", pollOptionDtos);

        when(pollPostRepository.findById(any())).thenReturn(Optional.of(pollPostEntity));

        assertThrows(IllegalAccessException.class, () -> postService.editPollPost(1L, pollPostCreationDto));
    }

    @Test
    void editPollPost_shouldFail_cannotEditPost() {
        UserEntity userEntity = new UserEntity();
        userEntity.setId(1L);
        userEntity.setEmail("email");
        when(userService.getUserEntityByEmail(any())).thenReturn(userEntity);

        SimplePostEntity simplePostEntity = new SimplePostEntity(1L, "description", new Date(), userEntity, new ArrayList<>(),
                new HashSet<>(), null, null, new ArrayList<>());

        PollOptionVoteEntity pollOptionVoteEntity = new PollOptionVoteEntity(1L, null, userEntity);
        PollOptionEntity pollOptionEntity = new PollOptionEntity(1L, "text", null, Set.of(pollOptionVoteEntity));
        ArrayList<PollOptionEntity> pollOptions = new ArrayList<>();
        pollOptions.add(pollOptionEntity);
        PollPostEntity pollPostEntity = new PollPostEntity(1L, pollOptions, simplePostEntity);


        PollPostRetrievalDto pollPostRetrievalDto = new PollPostRetrievalDto();
        pollPostRetrievalDto.setId(1L);
        pollPostRetrievalDto.setCreationDate(new Date());
        pollPostRetrievalDto.setReports(new ArrayList<>());

        PollOptionDto pollOptionDto = new PollOptionDto(null, "test");
        PollOptionDto pollOptionDto2 = new PollOptionDto(1L, "test");
        ArrayList<PollOptionDto> pollOptionDtos = new ArrayList<>();
        pollOptionDtos.add(pollOptionDto);
        pollOptionDtos.add(pollOptionDto2);

        PollPostCreationDto pollPostCreationDto = new PollPostCreationDto("description", pollOptionDtos);

        when(pollPostRepository.findById(any())).thenReturn(Optional.of(pollPostEntity));

        assertThrows(IllegalAccessException.class, () -> postService.editPollPost(1L, pollPostCreationDto));
    }

    @Test
    void deletePollPost_shouldSucceed() throws IllegalAccessException {
        UserEntity userEntity = new UserEntity();
        userEntity.setId(1L);
        userEntity.setEmail("email");

        Authentication authentication = new UsernamePasswordAuthenticationToken(userEntity, null);
        SecurityContext securityContext = mock(SecurityContext.class);
        lenient().when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        SimplePostEntity simplePostEntity = new SimplePostEntity(1L, "description", new Date(), userEntity, new ArrayList<>(),
                new HashSet<>(), null, null, new ArrayList<>());

        PollOptionEntity pollOptionEntity = new PollOptionEntity(1L, "text", null, new HashSet<>());
        ArrayList<PollOptionEntity> pollOptions = new ArrayList<>();
        pollOptions.add(pollOptionEntity);
        PollPostEntity pollPostEntity = new PollPostEntity(1L, pollOptions, simplePostEntity);

        when(pollPostRepository.findById(any())).thenReturn(Optional.of(pollPostEntity));
        doNothing().when(pollPostRepository).delete(any());

        postService.deletePollPost(1L);
    }

    @Test
    void deletePollPost_shouldFail_illegalAccess() {
        UserEntity userEntity = new UserEntity();
        userEntity.setId(1L);
        userEntity.setEmail("email");

        UserEntity userEntity2 = new UserEntity();
        userEntity2.setId(2L);
        userEntity2.setEmail("2");

        Authentication authentication = new UsernamePasswordAuthenticationToken(userEntity, null);
        SecurityContext securityContext = mock(SecurityContext.class);
        lenient().when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        SimplePostEntity simplePostEntity = new SimplePostEntity(1L, "description", new Date(), userEntity2, new ArrayList<>(),
                new HashSet<>(), null, null, new ArrayList<>());

        PollOptionEntity pollOptionEntity = new PollOptionEntity(1L, "text", null, new HashSet<>());
        ArrayList<PollOptionEntity> pollOptions = new ArrayList<>();
        pollOptions.add(pollOptionEntity);
        PollPostEntity pollPostEntity = new PollPostEntity(1L, pollOptions, simplePostEntity);

        when(pollPostRepository.findById(any())).thenReturn(Optional.of(pollPostEntity));
        assertThrows(IllegalAccessException.class, () -> postService.deletePollPost(1L));
    }

    @ParameterizedTest
    @ValueSource(strings = {"highest-score", "lowest-score", "no-score"})
    void editPlayedGamePost_shouldSucceed(String scoringSystem) throws IllegalAccessException {
        UserEntity userEntity = new UserEntity();
        userEntity.setId(1L);
        userEntity.setEmail("email");

        when(userService.getUserEntityByEmail(any())).thenReturn(userEntity);
        when(notificationService.createAndSaveNotificationsForAssociatedPlays(any())).thenReturn(Collections.emptyList());

        SimplePostEntity simplePostEntity = new SimplePostEntity(1L, "description", new Date(), userEntity, new ArrayList<>(),
                new HashSet<>(), null, null, new ArrayList<>());

        PlayedGameEntity playedGameEntity = new PlayedGameEntity(1L ,1L, "gameName", "gamePictureUrl", "gameCategory", 100, false, scoringSystem, new Date(), false, userEntity, null, new HashSet<>(), new HashSet<>());

        PlayedGamePostEntity playedGamePostEntity = new PlayedGamePostEntity(1L, 0, 0, 0.0, "no-score", simplePostEntity, playedGameEntity);

        when(playedGamePostRepository.findById(any())).thenReturn(Optional.of(playedGamePostEntity));

        BGGThingResponse boardGameResponse = new BGGThingResponse();
        BGGThingResponse.BoardGame item = new BGGThingResponse.BoardGame();
        BGGThingResponse.BoardGame.Name name = new BGGThingResponse.BoardGame.Name();
        name.setValue("Test Game");
        item.setNames(List.of(name));
        item.setImage("test_image");
        boardGameResponse.setItems(List.of(item));

        when(boardGameService.getBoardGameById(any())).thenReturn(boardGameResponse);
        lenient().doNothing().when(playedGameRepository).deleteAllById(any());
        when(playedGamePostRepository.save(any())).thenReturn(playedGamePostEntity);

        PlayedGamePostRetrievalDto playedGamePostRetrievalDto = new PlayedGamePostRetrievalDto();
        playedGamePostRetrievalDto.setId(1L);
        playedGamePostRetrievalDto.setCreationDate(new Date());

        when(postMapper.playedGamePostEntityToRetrievalDto(any())).thenReturn(playedGamePostRetrievalDto);

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

        PlayedGamePostRetrievalDto result = postService.editPlayedGamePost(1L, playedGamePostCreationDto);

        assertEquals(playedGamePostRetrievalDto, result);
        verify(playedGamePostRepository, times(1)).save(any());
    }

    @Test
    void editPlayedGamePost_shouldFail_illegalAccess() {
        UserEntity userEntity = new UserEntity();
        userEntity.setId(1L);
        userEntity.setEmail("email");

        UserEntity userEntity2 = new UserEntity();
        userEntity2.setId(2L);
        userEntity2.setEmail("email2");

        when(userService.getUserEntityByEmail(any())).thenReturn(userEntity);

        SimplePostEntity simplePostEntity = new SimplePostEntity(1L, "description", new Date(), userEntity2, new ArrayList<>(),
                new HashSet<>(), null, null, new ArrayList<>());

        PlayedGameEntity playedGameEntity = new PlayedGameEntity(1L ,1L, "gameName", "gamePictureUrl", "gameCategory", 100, false, "no-score", new Date(), false, userEntity, null, new HashSet<>(), new HashSet<>());

        PlayedGamePostEntity playedGamePostEntity = new PlayedGamePostEntity(1L, 0, 0, 0.0, "no-score", simplePostEntity, playedGameEntity);

        when(playedGamePostRepository.findById(any())).thenReturn(Optional.of(playedGamePostEntity));

       assertThrows(IllegalAccessException.class, () -> postService.editPlayedGamePost(1L, new PlayedGamePostCreationDto()));
    }

    @Test
    void editPlayedGamePost_shouldFail_invalidScoringSystem() {
        UserEntity userEntity = new UserEntity();
        userEntity.setId(1L);
        userEntity.setEmail("email");

        when(userService.getUserEntityByEmail(any())).thenReturn(userEntity);

        SimplePostEntity simplePostEntity = new SimplePostEntity(1L, "description", new Date(), userEntity, new ArrayList<>(),
                new HashSet<>(), null, null, new ArrayList<>());

        PlayedGameEntity playedGameEntity = new PlayedGameEntity(1L ,1L, "gameName", "gamePictureUrl", "gameCategory", 100, false, "invalid", new Date(), false, userEntity, null, new HashSet<>(), new HashSet<>());

        PlayedGamePostEntity playedGamePostEntity = new PlayedGamePostEntity(1L, 0, 0, 0.0, "no-score", simplePostEntity, playedGameEntity);

        when(playedGamePostRepository.findById(any())).thenReturn(Optional.of(playedGamePostEntity));

        BGGThingResponse boardGameResponse = new BGGThingResponse();
        BGGThingResponse.BoardGame item = new BGGThingResponse.BoardGame();
        BGGThingResponse.BoardGame.Name name = new BGGThingResponse.BoardGame.Name();
        name.setValue("Test Game");
        item.setNames(List.of(name));
        item.setImage("test_image");
        boardGameResponse.setItems(List.of(item));

        when(boardGameService.getBoardGameById(any())).thenReturn(boardGameResponse);

        PlayedGamePostCreationDto playedGamePostCreationDto = new PlayedGamePostCreationDto();
        playedGamePostCreationDto.setScoringSystem("invalid");

        assertThrows(FieldValidationException.class, () -> postService.editPlayedGamePost(1L, playedGamePostCreationDto));
    }

    @Test
    void deletePlayedGamePost_shouldSucceed() throws IllegalAccessException {
        UserEntity userEntity = new UserEntity();
        userEntity.setId(1L);
        userEntity.setEmail("email");

        Authentication authentication = new UsernamePasswordAuthenticationToken(userEntity, null);
        SecurityContext securityContext = mock(SecurityContext.class);
        lenient().when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        SimplePostEntity simplePostEntity = new SimplePostEntity(1L, "description", new Date(), userEntity, new ArrayList<>(),
                new HashSet<>(), null, null, new ArrayList<>());
        PlayedGameEntity playedGameEntity = new PlayedGameEntity(1L ,1L, "gameName", "gamePictureUrl", "gameCategory", 100, false, "invalid", new Date(), false, userEntity, null, new HashSet<>(), new HashSet<>());
        PlayedGamePostEntity playedGamePostEntity = new PlayedGamePostEntity(1L, 0, 0, 0.0, "no-score", simplePostEntity, playedGameEntity);

        when(playedGamePostRepository.findById(any())).thenReturn(Optional.of(playedGamePostEntity));
        doNothing().when(playedGamePostRepository).delete(any());

        postService.deletePlayedGamePost(1L);
        verify(playedGamePostRepository, times(1)).delete(any());
    }

    @Test
    void deletePlayedGamePost_shouldFail_illegalAccess() {
        UserEntity userEntity = new UserEntity();
        userEntity.setId(1L);
        userEntity.setEmail("email");

        UserEntity userEntity2 = new UserEntity();
        userEntity2.setId(2L);
        userEntity2.setEmail("email2");

        Authentication authentication = new UsernamePasswordAuthenticationToken(userEntity, null);
        SecurityContext securityContext = mock(SecurityContext.class);
        lenient().when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        SimplePostEntity simplePostEntity = new SimplePostEntity(1L, "description", new Date(), userEntity2, new ArrayList<>(), new HashSet<>(), null, null, new ArrayList<>());
        PlayedGameEntity playedGameEntity = new PlayedGameEntity(1L ,1L, "gameName", "gamePictureUrl", "gameCategory", 100, false, "invalid", new Date(), false, userEntity, null, new HashSet<>(), new HashSet<>());
        PlayedGamePostEntity playedGamePostEntity = new PlayedGamePostEntity(1L, 0, 0, 0.0, "no-score", simplePostEntity, playedGameEntity);

        when(playedGamePostRepository.findById(any())).thenReturn(Optional.of(playedGamePostEntity));
        assertThrows(IllegalAccessException.class, () -> postService.deletePlayedGamePost(1L));
    }

    @Test
    void editPostComment_shouldFail_invalidPostType() {
        assertThrows(FieldValidationException.class, () -> postService.editPostComment("invalid", 1L, 1L, null));
    }

    @Test
    void editPostComment_shouldSucceed_postTypeSimple() throws IllegalAccessException {
        UserEntity userEntity = new UserEntity();
        userEntity.setId(1L);
        userEntity.setEmail("email");

        Authentication authentication = new UsernamePasswordAuthenticationToken(userEntity, null);
        SecurityContext securityContext = mock(SecurityContext.class);
        lenient().when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        SimplePostEntity simplePostEntity = new SimplePostEntity(1L, "description", new Date(), userEntity, new ArrayList<>(), new HashSet<>(), null, null, new ArrayList<>());
        PostCommentEntity postCommentEntity = new PostCommentEntity(1L, "text", Instant.now(), simplePostEntity, userEntity, new ArrayList<>());
        simplePostEntity.setComments(List.of(postCommentEntity));

        UserRetrievalMinimalDto userRetrievalMinimalDto = new UserRetrievalMinimalDto(userEntity.getId(), userEntity.getName(), userEntity.getLastname(), userEntity.getProfilePictureUrl(), false, 0, false);
        PostCommentDto postCommentDto = new PostCommentDto(1L, "text", "2024-04-29", userRetrievalMinimalDto, false, "simple", 1L, new ArrayList<>());

        when(simplePostRepository.findById(any())).thenReturn(Optional.of(simplePostEntity));
        when(postCommentRepository.save(any())).thenReturn(postCommentEntity);
        when(postMapper.postCommentEntityToDto(any())).thenReturn(postCommentDto);

        PostCommentDto result = postService.editPostComment("simple", 1L, 1L, postCommentDto);
        assertEquals(postCommentDto, result);
    }

    @Test
    void editPostComment_shouldFail_postTypeSimple_illegalAccess() {
        UserEntity userEntity = new UserEntity();
        userEntity.setId(1L);
        userEntity.setEmail("email");

        UserEntity userEntity2 = new UserEntity();
        userEntity2.setId(12L);
        userEntity2.setEmail("email2");

        Authentication authentication = new UsernamePasswordAuthenticationToken(userEntity, null);
        SecurityContext securityContext = mock(SecurityContext.class);
        lenient().when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        SimplePostEntity simplePostEntity = new SimplePostEntity(1L, "description", new Date(), userEntity2, new ArrayList<>(), new HashSet<>(), null, null, new ArrayList<>());
        PostCommentEntity postCommentEntity = new PostCommentEntity(1L, "text", Instant.now(), simplePostEntity, userEntity2, new ArrayList<>());
        simplePostEntity.setComments(List.of(postCommentEntity));

        UserRetrievalMinimalDto userRetrievalMinimalDto = new UserRetrievalMinimalDto(userEntity2.getId(), userEntity2.getName(), userEntity2.getLastname(), userEntity2.getProfilePictureUrl(), false, 0, false);
        PostCommentDto postCommentDto = new PostCommentDto(1L, "text", "2024-04-29", userRetrievalMinimalDto, false, "simple", 1L, new ArrayList<>());

        when(simplePostRepository.findById(any())).thenReturn(Optional.of(simplePostEntity));

        assertThrows(IllegalAccessException.class, () -> postService.editPostComment("simple", 1L, 1L, postCommentDto));
    }

    @Test
    void editPostComment_shouldSucceed_postTypePlayedGame() throws IllegalAccessException {
        UserEntity userEntity = new UserEntity();
        userEntity.setId(1L);
        userEntity.setEmail("email");

        Authentication authentication = new UsernamePasswordAuthenticationToken(userEntity, null);
        SecurityContext securityContext = mock(SecurityContext.class);
        lenient().when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        SimplePostEntity simplePostEntity = new SimplePostEntity(1L, "description", new Date(), userEntity, new ArrayList<>(), new HashSet<>(), null, null, new ArrayList<>());
        PostCommentEntity postCommentEntity = new PostCommentEntity(1L, "text", Instant.now(), simplePostEntity, userEntity, new ArrayList<>());
        simplePostEntity.setComments(List.of(postCommentEntity));

        PlayedGameEntity playedGameEntity = new PlayedGameEntity(1L ,1L, "gameName", "gamePictureUrl", "gameCategory", 100, false, "invalid", new Date(), false, userEntity, null, new HashSet<>(), new HashSet<>());
        PlayedGamePostEntity playedGamePostEntity = new PlayedGamePostEntity(1L, 0, 0, 0.0, "no-score", simplePostEntity, playedGameEntity);

        UserRetrievalMinimalDto userRetrievalMinimalDto = new UserRetrievalMinimalDto(userEntity.getId(), userEntity.getName(), userEntity.getLastname(), userEntity.getProfilePictureUrl(), false, 0, false);
        PostCommentDto postCommentDto = new PostCommentDto(1L, "text", "2024-04-29", userRetrievalMinimalDto, false, "played-game", 1L, new ArrayList<>());

        when(playedGamePostRepository.findById(any())).thenReturn(Optional.of(playedGamePostEntity));
        when(postCommentRepository.save(any())).thenReturn(postCommentEntity);
        when(postMapper.postCommentEntityToDto(any())).thenReturn(postCommentDto);

        PostCommentDto result = postService.editPostComment("played-game", 1L, 1L, postCommentDto);
        assertEquals(postCommentDto, result);
    }

    @Test
    void editPostComment_shouldFail_postTypePlayedGame_illegalAccess() {
        UserEntity userEntity = new UserEntity();
        userEntity.setId(1L);
        userEntity.setEmail("email");

        UserEntity userEntity2 = new UserEntity();
        userEntity2.setId(12L);
        userEntity2.setEmail("email2");

        Authentication authentication = new UsernamePasswordAuthenticationToken(userEntity, null);
        SecurityContext securityContext = mock(SecurityContext.class);
        lenient().when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        SimplePostEntity simplePostEntity = new SimplePostEntity(1L, "description", new Date(), userEntity2, new ArrayList<>(), new HashSet<>(), null, null, new ArrayList<>());
        PostCommentEntity postCommentEntity = new PostCommentEntity(1L, "text", Instant.now(), simplePostEntity, userEntity2, new ArrayList<>());
        simplePostEntity.setComments(List.of(postCommentEntity));

        PlayedGameEntity playedGameEntity = new PlayedGameEntity(1L ,1L, "gameName", "gamePictureUrl", "gameCategory", 100, false, "invalid", new Date(), false, userEntity2, null, new HashSet<>(), new HashSet<>());
        PlayedGamePostEntity playedGamePostEntity = new PlayedGamePostEntity(1L, 0, 0, 0.0, "no-score", simplePostEntity, playedGameEntity);

        UserRetrievalMinimalDto userRetrievalMinimalDto = new UserRetrievalMinimalDto(userEntity2.getId(), userEntity2.getName(), userEntity2.getLastname(), userEntity2.getProfilePictureUrl(), false, 0, false);
        PostCommentDto postCommentDto = new PostCommentDto(1L, "text", "2024-04-29", userRetrievalMinimalDto, false, "played-game", 1L, new ArrayList<>());

        when(playedGamePostRepository.findById(any())).thenReturn(Optional.of(playedGamePostEntity));

        assertThrows(IllegalAccessException.class, () -> postService.editPostComment("played-game", 1L, 1L, postCommentDto));
    }

    @Test
    void editPostComment_shouldSucceed_postTypePoll() throws IllegalAccessException {
        UserEntity userEntity = new UserEntity();
        userEntity.setId(1L);
        userEntity.setEmail("email");

        Authentication authentication = new UsernamePasswordAuthenticationToken(userEntity, null);
        SecurityContext securityContext = mock(SecurityContext.class);
        lenient().when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        SimplePostEntity simplePostEntity = new SimplePostEntity(1L, "description", new Date(), userEntity, new ArrayList<>(), new HashSet<>(), null, null, new ArrayList<>());
        PostCommentEntity postCommentEntity = new PostCommentEntity(1L, "text", Instant.now(), simplePostEntity, userEntity, new ArrayList<>());
        simplePostEntity.setComments(List.of(postCommentEntity));

        PollPostEntity pollPostEntity = new PollPostEntity(1L,
                new ArrayList<>(),
               simplePostEntity);

        UserRetrievalMinimalDto userRetrievalMinimalDto = new UserRetrievalMinimalDto(userEntity.getId(), userEntity.getName(), userEntity.getLastname(), userEntity.getProfilePictureUrl(), false, 0, false);
        PostCommentDto postCommentDto = new PostCommentDto(1L, "text", "2024-04-29", userRetrievalMinimalDto, false, "poll", 1L, new ArrayList<>());

        when(pollPostRepository.findById(any())).thenReturn(Optional.of(pollPostEntity));
        when(postCommentRepository.save(any())).thenReturn(postCommentEntity);
        when(postMapper.postCommentEntityToDto(any())).thenReturn(postCommentDto);

        PostCommentDto result = postService.editPostComment("poll", 1L, 1L, postCommentDto);
        assertEquals(postCommentDto, result);
    }

    @Test
    void editPostComment_shouldFail_postTypePoll_illegalAccess() {
        UserEntity userEntity = new UserEntity();
        userEntity.setId(1L);
        userEntity.setEmail("email");

        UserEntity userEntity2 = new UserEntity();
        userEntity2.setId(12L);
        userEntity2.setEmail("email2");

        Authentication authentication = new UsernamePasswordAuthenticationToken(userEntity, null);
        SecurityContext securityContext = mock(SecurityContext.class);
        lenient().when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        SimplePostEntity simplePostEntity = new SimplePostEntity(1L, "description", new Date(), userEntity2, new ArrayList<>(), new HashSet<>(), null, null, new ArrayList<>());
        PostCommentEntity postCommentEntity = new PostCommentEntity(1L, "text", Instant.now(), simplePostEntity, userEntity2, new ArrayList<>());
        simplePostEntity.setComments(List.of(postCommentEntity));

        PollPostEntity pollPostEntity = new PollPostEntity(1L,
                new ArrayList<>(),
                simplePostEntity);

        UserRetrievalMinimalDto userRetrievalMinimalDto = new UserRetrievalMinimalDto(userEntity2.getId(), userEntity2.getName(), userEntity2.getLastname(), userEntity2.getProfilePictureUrl(), false, 0, false);
        PostCommentDto postCommentDto = new PostCommentDto(1L, "text", "2024-04-29", userRetrievalMinimalDto, false, "poll", 1L, new ArrayList<>());

        when(pollPostRepository.findById(any())).thenReturn(Optional.of(pollPostEntity));

        assertThrows(IllegalAccessException.class, () -> postService.editPostComment("poll", 1L, 1L, postCommentDto));
    }

    @Test
    void deletePostComment_shouldSucceed_postTypeSimple() throws IllegalAccessException {
        UserEntity userEntity = new UserEntity();
        userEntity.setId(1L);
        userEntity.setEmail("email");

        Authentication authentication = new UsernamePasswordAuthenticationToken(userEntity, null);
        SecurityContext securityContext = mock(SecurityContext.class);
        lenient().when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        SimplePostEntity simplePostEntity = new SimplePostEntity(1L, "description", new Date(), userEntity, new ArrayList<>(), new HashSet<>(), null, null, new ArrayList<>());
        PostCommentEntity postCommentEntity = new PostCommentEntity(1L, "text", Instant.now(), simplePostEntity, userEntity, new ArrayList<>());
        List<PostCommentEntity> comments = new ArrayList<>();
        comments.add(postCommentEntity);
        simplePostEntity.setComments(comments);

        when(simplePostRepository.findById(any())).thenReturn(Optional.of(simplePostEntity));
        when(simplePostRepository.save(any())).thenReturn(simplePostEntity);
        doNothing().when(postCommentRepository).delete(any());

        postService.deletePostComment("simple", 1L, 1L);
        verify(simplePostRepository, times(1)).save(any());
        verify(postCommentRepository, times(1)).delete(any());
    }

    @Test
    void deletePostComment_shouldSucceed_postTypePlayedGame() throws IllegalAccessException {
        UserEntity userEntity = new UserEntity();
        userEntity.setId(1L);
        userEntity.setEmail("email");

        Authentication authentication = new UsernamePasswordAuthenticationToken(userEntity, null);
        SecurityContext securityContext = mock(SecurityContext.class);
        lenient().when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        SimplePostEntity simplePostEntity = new SimplePostEntity(1L, "description", new Date(), userEntity, new ArrayList<>(), new HashSet<>(), null, null, new ArrayList<>());
        PostCommentEntity postCommentEntity = new PostCommentEntity(1L, "text", Instant.now(), simplePostEntity, userEntity, new ArrayList<>());
        List<PostCommentEntity> comments = new ArrayList<>();
        comments.add(postCommentEntity);
        simplePostEntity.setComments(comments);

        PlayedGameEntity playedGameEntity = new PlayedGameEntity(1L ,1L, "gameName", "gamePictureUrl", "gameCategory", 100, false, "invalid", new Date(), false, userEntity, null, new HashSet<>(), new HashSet<>());
        PlayedGamePostEntity playedGamePostEntity = new PlayedGamePostEntity(1L, 0, 0, 0.0, "no-score", simplePostEntity, playedGameEntity);

        when(playedGamePostRepository.findById(any())).thenReturn(Optional.of(playedGamePostEntity));
        when(playedGamePostRepository.save(any())).thenReturn(playedGamePostEntity);
        doNothing().when(postCommentRepository).delete(any());

        postService.deletePostComment("played-game", 1L, 1L);

        verify(playedGamePostRepository, times(1)).save(any());
        verify(postCommentRepository, times(1)).delete(any());
    }

    @Test
    void deletePostComment_shouldSucceed_postTypePoll() throws IllegalAccessException {
        UserEntity userEntity = new UserEntity();
        userEntity.setId(1L);
        userEntity.setEmail("email");

        Authentication authentication = new UsernamePasswordAuthenticationToken(userEntity, null);
        SecurityContext securityContext = mock(SecurityContext.class);
        lenient().when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        SimplePostEntity simplePostEntity = new SimplePostEntity(1L, "description", new Date(), userEntity, new ArrayList<>(), new HashSet<>(), null, null, new ArrayList<>());
        PostCommentEntity postCommentEntity = new PostCommentEntity(1L, "text", Instant.now(), simplePostEntity, userEntity, new ArrayList<>());
        List<PostCommentEntity> comments = new ArrayList<>();
        comments.add(postCommentEntity);
        simplePostEntity.setComments(comments);

        PollPostEntity pollPostEntity = new PollPostEntity(1L,
                new ArrayList<>(),
                simplePostEntity);

        when(pollPostRepository.findById(any())).thenReturn(Optional.of(pollPostEntity));
        when(pollPostRepository.save(any())).thenReturn(pollPostEntity);
        doNothing().when(postCommentRepository).delete(any());

        postService.deletePostComment("poll", 1L, 1L);

        verify(pollPostRepository, times(1)).save(any());
        verify(postCommentRepository, times(1)).delete(any());
    }

    @Test
    void deletePostComment_shouldFail_postTypeSimple_illegalAccess() {
        UserEntity userEntity = new UserEntity();
        userEntity.setId(1L);
        userEntity.setEmail("email");

        UserEntity userEntity2 = new UserEntity();
        userEntity2.setId(12L);
        userEntity2.setEmail("email2");

        Authentication authentication = new UsernamePasswordAuthenticationToken(userEntity, null);
        SecurityContext securityContext = mock(SecurityContext.class);
        lenient().when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        SimplePostEntity simplePostEntity = new SimplePostEntity(1L, "description", new Date(), userEntity2, new ArrayList<>(), new HashSet<>(), null, null, new ArrayList<>());
        PostCommentEntity postCommentEntity = new PostCommentEntity(1L, "text", Instant.now(), simplePostEntity, userEntity2, new ArrayList<>());
        List<PostCommentEntity> comments = new ArrayList<>();
        comments.add(postCommentEntity);
        simplePostEntity.setComments(comments);

        when(simplePostRepository.findById(any())).thenReturn(Optional.of(simplePostEntity));

        assertThrows(IllegalAccessException.class, () -> postService.deletePostComment("simple", 1L, 1L));
    }

    @Test
    void deletePostComment_shouldFail_postTypePlayedGame_illegalAccess() {
        UserEntity userEntity = new UserEntity();
        userEntity.setId(1L);
        userEntity.setEmail("email");

        UserEntity userEntity2 = new UserEntity();
        userEntity2.setId(12L);
        userEntity2.setEmail("email2");

        Authentication authentication = new UsernamePasswordAuthenticationToken(userEntity, null);
        SecurityContext securityContext = mock(SecurityContext.class);
        lenient().when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        SimplePostEntity simplePostEntity = new SimplePostEntity(1L, "description", new Date(), userEntity2, new ArrayList<>(), new HashSet<>(), null, null, new ArrayList<>());
        PostCommentEntity postCommentEntity = new PostCommentEntity(1L, "text", Instant.now(), simplePostEntity, userEntity2, new ArrayList<>());
        List<PostCommentEntity> comments = new ArrayList<>();
        comments.add(postCommentEntity);
        simplePostEntity.setComments(comments);

        PlayedGameEntity playedGameEntity = new PlayedGameEntity(1L ,1L, "gameName", "gamePictureUrl", "gameCategory", 100, false, "invalid", new Date(), false, userEntity2, null, new HashSet<>(), new HashSet<>());
        PlayedGamePostEntity playedGamePostEntity = new PlayedGamePostEntity(1L, 0, 0, 0.0, "no-score", simplePostEntity, playedGameEntity);

        when(playedGamePostRepository.findById(any())).thenReturn(Optional.of(playedGamePostEntity));

        assertThrows(IllegalAccessException.class, () -> postService.deletePostComment("played-game", 1L, 1L));
    }

    @Test
    void deletePostComment_shouldFail_postTypePoll_illegalAccess() {
        UserEntity userEntity = new UserEntity();
        userEntity.setId(1L);
        userEntity.setEmail("email");

        UserEntity userEntity2 = new UserEntity();
        userEntity2.setId(12L);
        userEntity2.setEmail("email2");

        Authentication authentication = new UsernamePasswordAuthenticationToken(userEntity, null);
        SecurityContext securityContext = mock(SecurityContext.class);
        lenient().when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        SimplePostEntity simplePostEntity = new SimplePostEntity(1L, "description", new Date(), userEntity2, new ArrayList<>(), new HashSet<>(), null, null, new ArrayList<>());
        PostCommentEntity postCommentEntity = new PostCommentEntity(1L, "text", Instant.now(), simplePostEntity, userEntity2, new ArrayList<>());
        List<PostCommentEntity> comments = new ArrayList<>();
        comments.add(postCommentEntity);
        simplePostEntity.setComments(comments);

        PollPostEntity pollPostEntity = new PollPostEntity(1L,
                new ArrayList<>(),
                simplePostEntity);

        when(pollPostRepository.findById(any())).thenReturn(Optional.of(pollPostEntity));

        assertThrows(IllegalAccessException.class, () -> postService.deletePostComment("poll", 1L, 1L));
    }

    @Test
    void deletePostComment_shouldFail_invalidPostType() {
        assertThrows(FieldValidationException.class, () -> postService.deletePostComment("invalid", 1L, 1L));
    }

    @Test
    void reportPost_shouldSucceed_postTypeSimple() {
        UserEntity userEntity = new UserEntity();
        userEntity.setId(1L);
        userEntity.setEmail("email");

        when(userService.getUserEntityByEmail(any())).thenReturn(userEntity);

        SimplePostEntity simplePostEntity = new SimplePostEntity(1L, "description", new Date(), userEntity, new ArrayList<>(),
                new HashSet<>(), null, null, new ArrayList<>());

        PostReportEntity postReportEntity = new PostReportEntity(1L, "reason", simplePostEntity, userEntity);

        when(simplePostRepository.findById(any())).thenReturn(Optional.of(simplePostEntity));

        ReportDto reportDto = new ReportDto(null, "reason", null);

        when(postReportRepository.save(any())).thenReturn(postReportEntity);
        when(postMapper.postReportEntityToDto(any())).thenReturn(reportDto);

        ReportDto result = postService.reportPost("simple", 1L, reportDto);

        assertEquals(reportDto, result);
        verify(postReportRepository, times(1)).save(any());
    }

    @Test
    void reportPost_shouldSucceed_postTypePlayedGame() {
        UserEntity userEntity = new UserEntity();
        userEntity.setId(1L);
        userEntity.setEmail("email");

        when(userService.getUserEntityByEmail(any())).thenReturn(userEntity);

        SimplePostEntity simplePostEntity = new SimplePostEntity(1L, "description", new Date(), userEntity, new ArrayList<>(),
                new HashSet<>(), null, null, new ArrayList<>());

        PlayedGamePostEntity playedGamePostEntity = new PlayedGamePostEntity();
        playedGamePostEntity.setBasePost(simplePostEntity);

        PostReportEntity postReportEntity = new PostReportEntity(1L, "reason", simplePostEntity, userEntity);

        when(playedGamePostRepository.findById(any())).thenReturn(Optional.of(playedGamePostEntity));

        ReportDto reportDto = new ReportDto(null, "reason", null);

        when(postReportRepository.save(any())).thenReturn(postReportEntity);
        when(postMapper.postReportEntityToDto(any())).thenReturn(reportDto);

        ReportDto result = postService.reportPost("played-game", 1L, reportDto);

        assertEquals(reportDto, result);
        verify(postReportRepository, times(1)).save(any());
    }

    @Test
    void reportPost_shouldSucceed_postTypePoll() {
        UserEntity userEntity = new UserEntity();
        userEntity.setId(1L);
        userEntity.setEmail("email");

        when(userService.getUserEntityByEmail(any())).thenReturn(userEntity);

        SimplePostEntity simplePostEntity = new SimplePostEntity(1L, "description", new Date(), userEntity, new ArrayList<>(),
                new HashSet<>(), null, null, new ArrayList<>());

        PollPostEntity pollPostEntity = new PollPostEntity();
        pollPostEntity.setBasePost(simplePostEntity);

        PostReportEntity postReportEntity = new PostReportEntity(1L, "reason", simplePostEntity, userEntity);

        when(pollPostRepository.findById(any())).thenReturn(Optional.of(pollPostEntity));

        ReportDto reportDto = new ReportDto(null, "reason", null);

        when(postReportRepository.save(any())).thenReturn(postReportEntity);
        when(postMapper.postReportEntityToDto(any())).thenReturn(reportDto);

        ReportDto result = postService.reportPost("poll", 1L, reportDto);

        assertEquals(reportDto, result);
        verify(postReportRepository, times(1)).save(any());
    }

    @Test
    void reportPost_shouldFail_postAlreadyReported() {
        UserEntity userEntity = new UserEntity();
        userEntity.setId(1L);
        userEntity.setEmail("email");

        when(userService.getUserEntityByEmail(any())).thenReturn(userEntity);

        SimplePostEntity simplePostEntity = new SimplePostEntity(1L, "description", new Date(), userEntity, new ArrayList<>(),
                new HashSet<>(), null, null, new ArrayList<>());

        PostReportEntity postReportEntity = new PostReportEntity(1L, "reason", simplePostEntity, userEntity);
        List<PostReportEntity> reports = new ArrayList<>();
        reports.add(postReportEntity);
        simplePostEntity.setReports(reports);

        when(simplePostRepository.findById(any())).thenReturn(Optional.of(simplePostEntity));

        assertThrows(DuplicateReportException.class, () -> postService.reportPost("simple", 1L, null));
    }

    @Test
    void reportPost_shouldFail_invalidPostType() {
       assertThrows(FieldValidationException.class, () -> postService.reportPost("invalid", null, null));
    }

    @Test
    void reportComment_shouldSucceed_postTypeSimple() {
        UserEntity userEntity = new UserEntity();
        userEntity.setId(1L);
        userEntity.setEmail("email");

        when(userService.getUserEntityByEmail(any())).thenReturn(userEntity);

        SimplePostEntity simplePostEntity = new SimplePostEntity(1L, "description", new Date(), userEntity, new ArrayList<>(),
                new HashSet<>(), null, null, new ArrayList<>());
        PostCommentEntity postCommentEntity = new PostCommentEntity(1L, "text", Instant.now(), simplePostEntity, userEntity, new ArrayList<>());
        List<PostCommentEntity> comments = new ArrayList<>();
        comments.add(postCommentEntity);
        simplePostEntity.setComments(comments);

        PostCommentReportEntity postCommentReportEntity = new PostCommentReportEntity(1L, "reason", postCommentEntity, userEntity);

        when(simplePostRepository.findById(any())).thenReturn(Optional.of(simplePostEntity));

        ReportDto reportDto = new ReportDto(null, "reason", null);

        when(postCommentReportRepository.save(any())).thenReturn(postCommentReportEntity);
        when(postMapper.postCommentReportEntityToDto(any())).thenReturn(reportDto);

        ReportDto result = postService.reportComment("simple", 1L, 1L, reportDto);

        assertEquals(reportDto, result);
        verify(postCommentReportRepository, times(1)).save(any());
    }

    @Test
    void reportComment_shouldSucceed_postTypePlayedGame() {
        UserEntity userEntity = new UserEntity();
        userEntity.setId(1L);
        userEntity.setEmail("email");

        when(userService.getUserEntityByEmail(any())).thenReturn(userEntity);

        SimplePostEntity simplePostEntity = new SimplePostEntity(1L, "description", new Date(), userEntity, new ArrayList<>(),
                new HashSet<>(), null, null, new ArrayList<>());
        PostCommentEntity postCommentEntity = new PostCommentEntity(1L, "text", Instant.now(), simplePostEntity, userEntity, new ArrayList<>());

        List<PostCommentEntity> comments = new ArrayList<>();
        comments.add(postCommentEntity);
        simplePostEntity.setComments(comments);

        PlayedGamePostEntity playedGamePostEntity = new PlayedGamePostEntity();
        playedGamePostEntity.setBasePost(simplePostEntity);

        PostCommentReportEntity postCommentReportEntity = new PostCommentReportEntity(1L, "reason", postCommentEntity, userEntity);

        when(playedGamePostRepository.findById(any())).thenReturn(Optional.of(playedGamePostEntity));

        ReportDto reportDto = new ReportDto(null, "reason", null);

        when(postCommentReportRepository.save(any())).thenReturn(postCommentReportEntity);
        when(postMapper.postCommentReportEntityToDto(any())).thenReturn(reportDto);

        ReportDto result = postService.reportComment("played-game", 1L, 1L, reportDto);

        assertEquals(reportDto, result);
        verify(postCommentReportRepository, times(1)).save(any());
    }

    @Test
    void reportComment_shouldSucceed_postTypePoll() {
        UserEntity userEntity = new UserEntity();
        userEntity.setId(1L);
        userEntity.setEmail("email");

        when(userService.getUserEntityByEmail(any())).thenReturn(userEntity);

        SimplePostEntity simplePostEntity = new SimplePostEntity(1L, "description", new Date(), userEntity, new ArrayList<>(),
                new HashSet<>(), null, null, new ArrayList<>());
        PostCommentEntity postCommentEntity = new PostCommentEntity(1L, "text", Instant.now(), simplePostEntity, userEntity, new ArrayList<>());

        List<PostCommentEntity> comments = new ArrayList<>();
        comments.add(postCommentEntity);
        simplePostEntity.setComments(comments);

        PollPostEntity pollPostEntity = new PollPostEntity();
        pollPostEntity.setBasePost(simplePostEntity);

        PostCommentReportEntity postCommentReportEntity = new PostCommentReportEntity(1L, "reason", postCommentEntity, userEntity);

        when(pollPostRepository.findById(any())).thenReturn(Optional.of(pollPostEntity));

        ReportDto reportDto = new ReportDto(null, "reason", null);

        when(postCommentReportRepository.save(any())).thenReturn(postCommentReportEntity);
        when(postMapper.postCommentReportEntityToDto(any())).thenReturn(reportDto);

        ReportDto result = postService.reportComment("poll", 1L, 1L, reportDto);

        assertEquals(reportDto, result);
        verify(postCommentReportRepository, times(1)).save(any());
    }

    @Test
    void reportComment_shouldFail_commentAlreadyReported() {
        UserEntity userEntity = new UserEntity();
        userEntity.setId(1L);
        userEntity.setEmail("email");

        when(userService.getUserEntityByEmail(any())).thenReturn(userEntity);

        SimplePostEntity simplePostEntity = new SimplePostEntity(1L, "description", new Date(), userEntity, new ArrayList<>(),
                new HashSet<>(), null, null, new ArrayList<>());
        PostCommentEntity postCommentEntity = new PostCommentEntity(1L, "text", Instant.now(), simplePostEntity, userEntity, new ArrayList<>());

        List<PostCommentEntity> comments = new ArrayList<>();
        comments.add(postCommentEntity);
        simplePostEntity.setComments(comments);

        PostCommentReportEntity postCommentReportEntity = new PostCommentReportEntity(1L, "reason", postCommentEntity, userEntity);

        List<PostCommentReportEntity> reports = new ArrayList<>();
        reports.add(postCommentReportEntity);
        postCommentEntity.setReports(reports);

        when(simplePostRepository.findById(any())).thenReturn(Optional.of(simplePostEntity));

        assertThrows(DuplicateReportException.class, () -> postService.reportComment("simple", 1L, 1L, null));
    }

    @Test
    void reportComment_shouldFail_invalidPostType() {
        assertThrows(FieldValidationException.class, () -> postService.reportComment("invalid", null, null, null));
    }

    @Test
    void deletePostReports_shouldSucceed_postTypeSimple() {
        UserEntity userEntity = new UserEntity();
        userEntity.setId(1L);
        userEntity.setEmail("email");

        SimplePostEntity simplePostEntity = new SimplePostEntity(1L, "description", new Date(), userEntity, new ArrayList<>(),
                new HashSet<>(), null, null, new ArrayList<>());

        when(simplePostRepository.findById(any())).thenReturn(Optional.of(simplePostEntity));

        when(simplePostRepository.save(any())).thenReturn(simplePostEntity);

        postService.deletePostReports("simple", 1L);

        verify(simplePostRepository, times(1)).save(any());
    }

    @Test
    void deletePostReports_shouldSucceed_postTypePlayedGame() {
        UserEntity userEntity = new UserEntity();
        userEntity.setId(1L);
        userEntity.setEmail("email");

        SimplePostEntity simplePostEntity = new SimplePostEntity(1L, "description", new Date(), userEntity, new ArrayList<>(),
                new HashSet<>(), null, null, new ArrayList<>());

        PlayedGamePostEntity playedGamePostEntity = new PlayedGamePostEntity();
        playedGamePostEntity.setBasePost(simplePostEntity);

        when(playedGamePostRepository.findById(any())).thenReturn(Optional.of(playedGamePostEntity));

        when(simplePostRepository.save(any())).thenReturn(simplePostEntity);

        postService.deletePostReports("played-game", 1L);

        verify(simplePostRepository, times(1)).save(any());
    }

    @Test
    void deletePostReports_shouldSucceed_postTypePoll() {
        UserEntity userEntity = new UserEntity();
        userEntity.setId(1L);
        userEntity.setEmail("email");

        SimplePostEntity simplePostEntity = new SimplePostEntity(1L, "description", new Date(), userEntity, new ArrayList<>(),
                new HashSet<>(), null, null, new ArrayList<>());

        PollPostEntity pollPostEntity = new PollPostEntity();
        pollPostEntity.setBasePost(simplePostEntity);

        when(pollPostRepository.findById(any())).thenReturn(Optional.of(pollPostEntity));

        when(simplePostRepository.save(any())).thenReturn(simplePostEntity);

        postService.deletePostReports("poll", 1L);

        verify(simplePostRepository, times(1)).save(any());
    }

    @Test
    void deletePostReports_shouldFail_invalidPostType() {
        assertThrows(FieldValidationException.class, () -> postService.deletePostReports("invalid", 1L));
    }

    @Test
    void deleteCommentReports_shouldSucceed_postTypeSimple() {
        UserEntity userEntity = new UserEntity();
        userEntity.setId(1L);
        userEntity.setEmail("email");

        SimplePostEntity simplePostEntity = new SimplePostEntity(1L, "description", new Date(), userEntity, new ArrayList<>(),
                new HashSet<>(), null, null, new ArrayList<>());
        PostCommentEntity postCommentEntity = new PostCommentEntity(1L, "text", Instant.now(), simplePostEntity, userEntity, new ArrayList<>());

        List<PostCommentEntity> comments = new ArrayList<>();
        comments.add(postCommentEntity);
        simplePostEntity.setComments(comments);

        when(simplePostRepository.findById(any())).thenReturn(Optional.of(simplePostEntity));
        when(postCommentRepository.save(any())).thenReturn(postCommentEntity);

        postService.deleteCommentReports("simple", 1L, 1L);
        verify(postCommentRepository, times(1)).save(any());
    }

    @Test
    void deleteCommentReports_shouldSucceed_postTypePlayedGame() {
        UserEntity userEntity = new UserEntity();
        userEntity.setId(1L);
        userEntity.setEmail("email");

        SimplePostEntity simplePostEntity = new SimplePostEntity(1L, "description", new Date(), userEntity, new ArrayList<>(),
                new HashSet<>(), null, null, new ArrayList<>());
        PostCommentEntity postCommentEntity = new PostCommentEntity(1L, "text", Instant.now(), simplePostEntity, userEntity, new ArrayList<>());

        List<PostCommentEntity> comments = new ArrayList<>();
        comments.add(postCommentEntity);
        simplePostEntity.setComments(comments);

        PlayedGamePostEntity playedGamePostEntity = new PlayedGamePostEntity();
        playedGamePostEntity.setBasePost(simplePostEntity);

        when(playedGamePostRepository.findById(any())).thenReturn(Optional.of(playedGamePostEntity));
        when(postCommentRepository.save(any())).thenReturn(postCommentEntity);

        postService.deleteCommentReports("played-game", 1L, 1L);
        verify(postCommentRepository, times(1)).save(any());
    }

    @Test
    void deleteCommentReports_shouldSucceed_postTypePoll() {
        UserEntity userEntity = new UserEntity();
        userEntity.setId(1L);
        userEntity.setEmail("email");

        SimplePostEntity simplePostEntity = new SimplePostEntity(1L, "description", new Date(), userEntity, new ArrayList<>(),
                new HashSet<>(), null, null, new ArrayList<>());
        PostCommentEntity postCommentEntity = new PostCommentEntity(1L, "text", Instant.now(), simplePostEntity, userEntity, new ArrayList<>());

        List<PostCommentEntity> comments = new ArrayList<>();
        comments.add(postCommentEntity);
        simplePostEntity.setComments(comments);

        PollPostEntity pollPostEntity = new PollPostEntity();
        pollPostEntity.setBasePost(simplePostEntity);

        when(pollPostRepository.findById(any())).thenReturn(Optional.of(pollPostEntity));
        when(postCommentRepository.save(any())).thenReturn(postCommentEntity);

        postService.deleteCommentReports("poll", 1L, 1L);
        verify(postCommentRepository, times(1)).save(any());
    }

    @Test
    void deleteCommentReports_shouldFail_invalidPostType() {
        assertThrows(FieldValidationException.class, () -> postService.deleteCommentReports("invalid", 1L, 1L));
    }
}