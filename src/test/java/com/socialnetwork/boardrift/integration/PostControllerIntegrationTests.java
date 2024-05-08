package com.socialnetwork.boardrift.integration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.socialnetwork.boardrift.enumeration.Role;
import com.socialnetwork.boardrift.feign.BGGV1ApiConnection;
import com.socialnetwork.boardrift.feign.BGGV2ApiConnection;
import com.socialnetwork.boardrift.repository.NotificationRepository;
import com.socialnetwork.boardrift.repository.PlayedGamePostRepository;
import com.socialnetwork.boardrift.repository.PlayedGameRepository;
import com.socialnetwork.boardrift.repository.PollPostRepository;
import com.socialnetwork.boardrift.repository.PostCommentReportRepository;
import com.socialnetwork.boardrift.repository.PostCommentRepository;
import com.socialnetwork.boardrift.repository.PostLikeRepository;
import com.socialnetwork.boardrift.repository.PostReportRepository;
import com.socialnetwork.boardrift.repository.SimplePostRepository;
import com.socialnetwork.boardrift.repository.UserRepository;
import com.socialnetwork.boardrift.repository.model.NotificationEntity;
import com.socialnetwork.boardrift.repository.model.board_game.PlayedGameEntity;
import com.socialnetwork.boardrift.repository.model.post.PlayedGamePostEntity;
import com.socialnetwork.boardrift.repository.model.post.PollOptionEntity;
import com.socialnetwork.boardrift.repository.model.post.PollPostEntity;
import com.socialnetwork.boardrift.repository.model.post.PostCommentEntity;
import com.socialnetwork.boardrift.repository.model.post.PostLikeEntity;
import com.socialnetwork.boardrift.repository.model.post.PostReportEntity;
import com.socialnetwork.boardrift.repository.model.post.SimplePostEntity;
import com.socialnetwork.boardrift.repository.model.user.SuspensionEntity;
import com.socialnetwork.boardrift.repository.model.user.UserEntity;
import com.socialnetwork.boardrift.rest.model.BGGThingResponse;
import com.socialnetwork.boardrift.rest.model.post.PostCommentDto;
import com.socialnetwork.boardrift.rest.model.post.PostPageDto;
import com.socialnetwork.boardrift.rest.model.post.ReportDto;
import com.socialnetwork.boardrift.rest.model.post.played_game_post.PlayedGamePostCreationDto;
import com.socialnetwork.boardrift.rest.model.post.poll_post.PollOptionDto;
import com.socialnetwork.boardrift.rest.model.post.poll_post.PollPostCreationDto;
import com.socialnetwork.boardrift.rest.model.post.poll_post.PollPostRetrievalDto;
import com.socialnetwork.boardrift.rest.model.post.simple_post.SimplePostCreationDto;
import com.socialnetwork.boardrift.rest.model.post.simple_post.SimplePostRetrievalDto;
import com.socialnetwork.boardrift.rest.model.user.UserRegistrationDto;
import com.socialnetwork.boardrift.rest.model.user.UserRetrievalDto;
import com.socialnetwork.boardrift.rest.model.user.UserRetrievalMinimalDto;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@SpringBootTest
public class PostControllerIntegrationTests {
    @MockBean
    PlayedGamePostRepository playedGamePostRepository;
    @MockBean
    PlayedGameRepository playedGameRepository;
    @MockBean
    SimplePostRepository simplePostRepository;
    @MockBean
    PollPostRepository pollPostRepository;
    @MockBean
    PostCommentRepository postCommentRepository;
    @MockBean
    PostLikeRepository postLikeRepository;
    @MockBean
    PostReportRepository postReportRepository;
    @MockBean
    PostCommentReportRepository postCommentReportRepository;
    @MockBean
    UserRepository userRepository;
    @MockBean
    BGGV2ApiConnection bggv2ApiConnection;
    @MockBean
    BGGV1ApiConnection bggv1ApiConnection;
    @MockBean
    NotificationRepository notificationRepository;
    @Autowired
    MockMvc mockMvc;

    UserEntity userEntity;
    UserEntity userEntity2;
    UserRegistrationDto userRegistrationDto;
    UserRetrievalDto userRetrievalDto;
    UserRetrievalDto userRetrievalDto2;
    UserRetrievalMinimalDto userRetrievalMinimalDto;
    UserRetrievalMinimalDto userRetrievalMinimalDto2;
    ObjectMapper objectMapper;
    SimplePostEntity simplePostEntity;
    PlayedGameEntity playedGameEntity;
    PlayedGamePostEntity playedGamePostEntity;
    PollPostEntity pollPostEntity;
    PostCommentEntity postCommentEntity;
    PlayedGamePostCreationDto playedGamePostCreationDto;
    SimplePostCreationDto simplePostCreationDto;
    PollPostCreationDto pollPostCreationDto;
    PostCommentDto postCommentDto;
    ReportDto reportDto;

    @BeforeEach
    void init() {
        Set<UserEntity> userSet = new HashSet<>();
        objectMapper = new ObjectMapper();
        userEntity = new UserEntity(1L, "Name", "Lastname", "email@gmail.com",
                "2001-11-16", "Password@123", "", "", "", true, false, false, false, "",
                Role.ROLE_USER, false, new ArrayList<>(), userSet,
                userSet,  new ArrayList<>(),  userSet, userSet, new ArrayList<>(), null);
        userSet.add(userEntity);
        userEntity2 = new UserEntity(2L, "Name2", "Lastname2", "email2@gmail.com",
                "2001-11-16", "Password@123", "", "", "", true, false, false, false, "",
                Role.ROLE_USER, false, new ArrayList<>(),  userSet,
                userSet,  new ArrayList<>(),  userSet, userSet, new ArrayList<>(), null);

        userRegistrationDto = new UserRegistrationDto("Name", "Lastname",
                "email@gmail.com", "2001-11-16", "Password@123");

        userRetrievalDto = new UserRetrievalDto(1L, "Name", "Lastname", "email@gmail.com", "2001-11-16", "", "", "", "", true, false, false, false, false, false, false, false, false, new ArrayList<>());
        userRetrievalDto2 = new UserRetrievalDto(2L, "Name2", "Lastname2", "email2@gmail.com", "2001-11-16", "", "", "", "", true, false, false, false, false, false, false, false, false, new ArrayList<>());

        userRetrievalMinimalDto = new UserRetrievalMinimalDto(1L, "Name", "Lastname", "", false, 0, false);

        userRetrievalMinimalDto2 = new UserRetrievalMinimalDto(2L, "Name2", "Lastname2", "", false, 0, false);
        simplePostEntity = new SimplePostEntity(1L, "description", new Date(), userEntity, new ArrayList<>(), new HashSet<>(), null, null, new ArrayList<>());
        pollPostEntity = new PollPostEntity();
        pollPostEntity.setId(1L);
        pollPostEntity.setBasePost(simplePostEntity);

        PollOptionEntity pollOptionEntity = new PollOptionEntity();
        pollOptionEntity.setId(1L);
        pollOptionEntity.setVotes(new HashSet<>());
        ArrayList<PollOptionEntity> options = new ArrayList<>();
        pollPostEntity.setOptions(options);

        playedGameEntity = new PlayedGameEntity(1L, 1L, "gameName",
                "gamePictureUrl", "gameCategory",
                0, false, "no-score", new Date(), true, userEntity,
                null, new HashSet<>(), new HashSet<>());

        playedGamePostEntity = new PlayedGamePostEntity(1L, 100, 0, 100.0, "no-score", simplePostEntity, playedGameEntity);
        simplePostEntity.setChildPollPost(pollPostEntity);
        simplePostEntity.setChildPlayedGamePost(playedGamePostEntity);
        postCommentEntity = new PostCommentEntity(1L, "text", Instant.now(), simplePostEntity, userEntity, new ArrayList<>());
        PlayedGamePostCreationDto.SelectedPlayerDto selectedPlayerDto = new PlayedGamePostCreationDto.SelectedPlayerDto(1L, 2L, "name", "lastname", false, 200);
        playedGamePostCreationDto = new PlayedGamePostCreationDto(1L, "description", true, 500, "highest-score", Set.of(selectedPlayerDto));
        simplePostCreationDto = new SimplePostCreationDto("description");
        PollOptionDto pollOptionDto = new PollOptionDto(null, "text0");
        pollPostCreationDto = new PollPostCreationDto("description", List.of(pollOptionDto));
        postCommentDto = new PostCommentDto(1L, "text", "2001-11-19", userRetrievalMinimalDto, false, "type", 1L, new ArrayList<>());
        reportDto = new ReportDto(1L, "reason", userRetrievalMinimalDto);
    }

    private BGGThingResponse createSampleBGGThingResponse() {
        BGGThingResponse.BoardGame.Name name = new BGGThingResponse.BoardGame.Name("primary", 1, "Sample Game");
        List<BGGThingResponse.BoardGame.Name> names = new ArrayList<>();
        names.add(name);

        BGGThingResponse.BoardGame boardGame = new BGGThingResponse.BoardGame("boardgame", "123", "thumbnail_url", "image_url", "category", names,
                "Sample game description", new BGGThingResponse.BoardGame.YearPublished(2022),
                new BGGThingResponse.BoardGame.MinPlayers(2), new BGGThingResponse.BoardGame.MaxPlayers(4));
        List<BGGThingResponse.BoardGame> items = new ArrayList<>();
        items.add(boardGame);

        return new BGGThingResponse(items);
    }

    @WithMockUser(username = "email@gmail.com", authorities = "ROLE_USER")
    @Test
    void getPostsByUserIdShouldSucceed() throws Exception {
        Mockito.when(userRepository.findByEmail(any())).thenReturn(Optional.of(userEntity));
        Mockito.when(simplePostRepository.findByPostCreatorId(any(), any())).thenReturn(List.of(simplePostEntity));
        Mockito.when(pollPostRepository.findByBasePostPostCreatorId(any(), any())).thenReturn(List.of(pollPostEntity));
        Mockito.when(playedGamePostRepository.findByBasePostPostCreatorId(any(), any())).thenReturn(List.of(playedGamePostEntity));

        MvcResult mvcResult = mockMvc.perform(get("/users/1/posts")
                .param("page", "0")
                .param("pageSize", "3"))
                .andExpect(status().isOk())
                .andReturn();
    }

    @WithMockUser(username = "email@gmail.com", authorities = "ROLE_USER")
    @Test
    void getPostsByUserIdShouldFailWhenUserSuspended() throws Exception {
        userEntity.setSuspension(new SuspensionEntity());
        Mockito.when(userRepository.findByEmail(any())).thenReturn(Optional.of(userEntity2));
        Mockito.when(userRepository.findById(any())).thenReturn(Optional.of(userEntity));

        mockMvc.perform(get("/users/1/posts")
                        .param("page", "0")
                        .param("pageSize", "3"))
                .andExpect(status().isForbidden());
    }

    @WithMockUser(username = "email@gmail.com", authorities = "ROLE_USER")
    @Test
    void getPostsByUserIdShouldFailWhenInvalidPermissions() throws Exception {
        userEntity.setFriends(new HashSet<>());
        userEntity2.setFriends(new HashSet<>());
        userEntity.setFriendOf(new HashSet<>());
        userEntity2.setFriendOf(new HashSet<>());
        userEntity.setPublicPosts(false);
        userEntity2.setPublicPosts(false);
        Mockito.when(userRepository.findByEmail(any())).thenReturn(Optional.of(userEntity2));
        Mockito.when(userRepository.findById(any())).thenReturn(Optional.of(userEntity));

        mockMvc.perform(get("/users/1/posts")
                        .param("page", "0")
                        .param("pageSize", "3"))
                .andExpect(status().isForbidden());
    }

    @WithMockUser(username = "email@gmail.com", authorities = "ROLE_USER")
    @Test
    void getFeedShouldSucceed() throws Exception {
        Mockito.when(userRepository.findByEmail(any())).thenReturn(Optional.of(userEntity));
        Mockito.when(simplePostRepository.findAllByPostCreatorOrFriends(any(), any())).thenReturn(List.of(simplePostEntity));
        Mockito.when(pollPostRepository.findAllByPostCreatorOrFriends(any(), any())).thenReturn(List.of(pollPostEntity));
        Mockito.when(playedGamePostRepository.findAllByPostCreatorOrFriends(any(), any())).thenReturn(List.of(playedGamePostEntity));

        mockMvc.perform(get("/posts/feed")
                        .param("page", "0")
                        .param("pageSize", "3"))
                .andExpect(status().isOk());
    }

    @WithMockUser(username = "email@gmail.com", authorities = "ROLE_ADMIN")
    @Test
    void getReportedPostsShouldSucceed() throws Exception {
        userEntity.setRole(Role.ROLE_ADMIN);
        Mockito.when(userRepository.findByEmail(any())).thenReturn(Optional.of(userEntity));
        Mockito.when(simplePostRepository.findReportedPosts(any())).thenReturn(List.of(simplePostEntity));
        Mockito.when(pollPostRepository.findReportedPosts(any())).thenReturn(List.of(pollPostEntity));
        Mockito.when(playedGamePostRepository.findReportedPosts(any())).thenReturn(List.of(playedGamePostEntity));

        mockMvc.perform(get("/posts/reported")
                        .param("page", "0")
                        .param("pageSize", "3"))
                .andExpect(status().isOk());
    }

    @WithMockUser(username = "email@gmail.com", authorities = "ROLE_ADMIN")
    @Test
    void getReportedCommentsShouldSucceed() throws Exception {
        userEntity.setRole(Role.ROLE_ADMIN);
        Mockito.when(postCommentRepository.findReportedComments(any())).thenReturn(List.of(postCommentEntity));


        mockMvc.perform(get("/comments/reported")
                        .param("page", "0")
                        .param("pageSize", "3"))
                .andExpect(status().isOk());
    }

    @WithMockUser(username = "email@gmail.com", authorities = "ROLE_ADMIN")
    @ParameterizedTest
    @ValueSource(strings = {"simple", "poll", "played-game"})
    void getPostCommentsShouldSucceed(String postType) throws Exception {
        Mockito.when(userRepository.findByEmail(any())).thenReturn(Optional.of(userEntity));
        Mockito.when(postCommentRepository.findReportedComments(any())).thenReturn(List.of(postCommentEntity));
        Mockito.lenient().when(simplePostRepository.findById(any())).thenReturn(Optional.of(simplePostEntity));
        Mockito.lenient().when(pollPostRepository.findById(any())).thenReturn(Optional.of(pollPostEntity));
        Mockito.lenient().when(playedGamePostRepository.findById(any())).thenReturn(Optional.of(playedGamePostEntity));
        Mockito.when(postCommentRepository.findAllBySimplePostId(any(), any())).thenReturn(List.of(postCommentEntity));


        mockMvc.perform(get("/posts/" + postType +"/"+ 1 +"/"+ "/comments")
                        .param("page", "0")
                        .param("pageSize", "3"))
                .andExpect(status().isOk());
    }

    @WithMockUser(username = "email@gmail.com", authorities = "ROLE_ADMIN")
    @Test
    void getPostCommentsShouldFail() throws Exception {
        Mockito.when(userRepository.findByEmail(any())).thenReturn(Optional.of(userEntity));

        mockMvc.perform(get("/posts/" + "invalid" +"/"+ 1 +"/"+ "/comments")
                        .param("page", "0")
                        .param("pageSize", "3"))
                .andExpect(status().isBadRequest());
    }

    @WithMockUser(username = "email@gmail.com", authorities = "ROLE_ADMIN")
    @ParameterizedTest
    @ValueSource(strings = {"no-score", "lowest-score", "highest-score"})
    void createPlayedGamePostShouldSucceed(String scoringSystem) throws Exception {
        playedGamePostCreationDto.setScoringSystem(scoringSystem);
        Mockito.when(userRepository.findByEmail(any())).thenReturn(Optional.of(userEntity));
        Mockito.when(bggv1ApiConnection.getBoardGameById(any())).thenReturn(createSampleBGGThingResponse());
        Mockito.lenient().when(playedGamePostRepository.save(any())).thenReturn(playedGamePostEntity);
        Mockito.when(notificationRepository.save(any())).thenReturn(new NotificationEntity());
        Mockito.when(userRepository.findById(any())).thenReturn(Optional.of(userEntity2));

        mockMvc.perform(post("/posts/played-game")
                        .content(objectMapper.writeValueAsString(playedGamePostCreationDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());
    }

    @WithMockUser(username = "email@gmail.com", authorities = "ROLE_ADMIN")
    @Test
    void createSimplePostShouldSucceed() throws Exception {
        Mockito.when(userRepository.findByEmail(any())).thenReturn(Optional.of(userEntity));
        Mockito.when(simplePostRepository.save(any())).thenReturn(simplePostEntity);

        MvcResult mvcResult = mockMvc.perform(post("/posts/simple")
                        .content(objectMapper.writeValueAsString(simplePostCreationDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andReturn();

        Assertions.assertEquals(playedGameEntity.getId(), objectMapper.readValue(mvcResult.getResponse().getContentAsString(), SimplePostRetrievalDto.class).getId());
    }

    @WithMockUser(username = "email@gmail.com", authorities = "ROLE_ADMIN")
    @Test
    void createPollPostShouldSucceed() throws Exception {
        Mockito.when(userRepository.findByEmail(any())).thenReturn(Optional.of(userEntity));
        Mockito.when(pollPostRepository.save(any())).thenReturn(pollPostEntity);

        MvcResult mvcResult = mockMvc.perform(post("/posts/poll")
                        .content(objectMapper.writeValueAsString(pollPostCreationDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andReturn();

        Assertions.assertEquals(pollPostEntity.getId(), objectMapper.readValue(mvcResult.getResponse().getContentAsString(), PollPostRetrievalDto.class).getId());
    }

    @WithMockUser(username = "email@gmail.com", authorities = "ROLE_ADMIN")
    @Test
    void createPollVoteShouldSucceed() throws Exception {
        Mockito.when(userRepository.findByEmail(any())).thenReturn(Optional.of(userEntity));
        Mockito.when(pollPostRepository.findById(any())).thenReturn(Optional.of(pollPostEntity));
        Mockito.when(pollPostRepository.save(any())).thenReturn(pollPostEntity);

        mockMvc.perform(post("/posts/poll/1/vote/1")
                        .content(objectMapper.writeValueAsString(pollPostCreationDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @WithMockUser(username = "email@gmail.com", authorities = "ROLE_ADMIN")
    @ParameterizedTest
    @ValueSource(strings = {"simple", "poll", "played-game"})
    void createPostCommentShouldSucceed(String postType) throws Exception {
        Mockito.when(userRepository.findByEmail(any())).thenReturn(Optional.of(userEntity));
        Mockito.when(pollPostRepository.findById(any())).thenReturn(Optional.of(pollPostEntity));
        Mockito.when(simplePostRepository.findById(any())).thenReturn(Optional.of(simplePostEntity));
        Mockito.when(playedGamePostRepository.findById(any())).thenReturn(Optional.of(playedGamePostEntity));
        Mockito.when(postCommentRepository.save(any())).thenReturn(postCommentEntity);

        mockMvc.perform(post("/posts/" + postType + "/1/comments")
                        .content(objectMapper.writeValueAsString(postCommentDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());
    }

    @WithMockUser(username = "email@gmail.com", authorities = "ROLE_ADMIN")
    @ParameterizedTest
    @ValueSource(strings = {"simple", "poll", "played-game"})
    void likePostShouldSucceed(String postType) throws Exception {
        Mockito.when(userRepository.findByEmail(any())).thenReturn(Optional.of(userEntity));
        Mockito.when(pollPostRepository.findById(any())).thenReturn(Optional.of(pollPostEntity));
        Mockito.when(simplePostRepository.findById(any())).thenReturn(Optional.of(simplePostEntity));
        Mockito.when(playedGamePostRepository.findById(any())).thenReturn(Optional.of(playedGamePostEntity));
        Mockito.when(postCommentRepository.save(any())).thenReturn(postCommentEntity);
        Mockito.when(postLikeRepository.findBySimplePostIdAndLikeOwnerId(any(), any())).thenReturn(Optional.empty());
        Mockito.when(postLikeRepository.save(any())).thenReturn(new PostLikeEntity());

        mockMvc.perform(post("/posts/" + postType + "/1/likes"))
                .andExpect(status().isOk());
    }

    @WithMockUser(username = "email@gmail.com", authorities = "ROLE_ADMIN")
    @ParameterizedTest
    @ValueSource(strings = {"simple", "poll", "played-game"})
    void reportPostShouldSucceed(String postType) throws Exception {
        Mockito.when(userRepository.findByEmail(any())).thenReturn(Optional.of(userEntity));
        Mockito.when(pollPostRepository.findById(any())).thenReturn(Optional.of(pollPostEntity));
        Mockito.when(simplePostRepository.findById(any())).thenReturn(Optional.of(simplePostEntity));
        Mockito.when(playedGamePostRepository.findById(any())).thenReturn(Optional.of(playedGamePostEntity));
        Mockito.when(postCommentRepository.save(any())).thenReturn(postCommentEntity);
        Mockito.when(postReportRepository.save(any())).thenReturn(new PostReportEntity());

        mockMvc.perform(post("/posts/" + postType + "/1/reports")
                        .content(objectMapper.writeValueAsString(reportDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());
    }

    @WithMockUser(username = "email@gmail.com", authorities = "ROLE_ADMIN")
    @ParameterizedTest
    @ValueSource(strings = {"simple", "poll", "played-game"})
    void reportPostCommentShouldSucceed(String postType) throws Exception {
        simplePostEntity.setComments(List.of(postCommentEntity));
        Mockito.when(userRepository.findByEmail(any())).thenReturn(Optional.of(userEntity));
        Mockito.when(pollPostRepository.findById(any())).thenReturn(Optional.of(pollPostEntity));
        Mockito.when(simplePostRepository.findById(any())).thenReturn(Optional.of(simplePostEntity));
        Mockito.when(playedGamePostRepository.findById(any())).thenReturn(Optional.of(playedGamePostEntity));
        Mockito.when(postCommentReportRepository.save(any())).thenReturn(new PostReportEntity());

        mockMvc.perform(post("/posts/" + postType + "/1/comments/1/reports")
                        .content(objectMapper.writeValueAsString(reportDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @WithMockUser(username = "email@gmail.com", authorities = "ROLE_ADMIN")
    @ParameterizedTest
    @ValueSource(strings = {"simple", "poll", "played-game"})
    void editPostCommentShouldSucceed(String postType) throws Exception {
        simplePostEntity.setComments(List.of(postCommentEntity));
        Mockito.when(userRepository.findByEmail(any())).thenReturn(Optional.of(userEntity));
        Mockito.when(pollPostRepository.findById(any())).thenReturn(Optional.of(pollPostEntity));
        Mockito.when(simplePostRepository.findById(any())).thenReturn(Optional.of(simplePostEntity));
        Mockito.when(playedGamePostRepository.findById(any())).thenReturn(Optional.of(playedGamePostEntity));
        Mockito.when(postCommentRepository.save(any())).thenReturn(new PostCommentEntity());

        mockMvc.perform(put("/posts/" + postType + "/1/comments/1")
                        .content(objectMapper.writeValueAsString(postCommentDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @WithMockUser(username = "email@gmail.com", authorities = "ROLE_ADMIN")
    @Test
    void editSimplePostShouldSucceed() throws Exception {
        simplePostEntity.setComments(List.of(postCommentEntity));
        Mockito.when(userRepository.findByEmail(any())).thenReturn(Optional.of(userEntity));
        Mockito.when(simplePostRepository.findById(any())).thenReturn(Optional.of(simplePostEntity));
        Mockito.when(simplePostRepository.save(any())).thenReturn(simplePostEntity);

        mockMvc.perform(put("/posts/simple/1")
                        .content(objectMapper.writeValueAsString(simplePostCreationDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @WithMockUser(username = "email@gmail.com", authorities = "ROLE_ADMIN")
    @Test
    void editPollPostShouldSucceed() throws Exception {
        simplePostEntity.setComments(List.of(postCommentEntity));
        Mockito.when(userRepository.findByEmail(any())).thenReturn(Optional.of(userEntity));
        Mockito.when(pollPostRepository.findById(any())).thenReturn(Optional.of(pollPostEntity));
        Mockito.when(pollPostRepository.save(any())).thenReturn(pollPostEntity);

        mockMvc.perform(put("/posts/poll/1")
                        .content(objectMapper.writeValueAsString(pollPostCreationDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @WithMockUser(username = "email@gmail.com", authorities = "ROLE_ADMIN")
    @ParameterizedTest
    @ValueSource(strings = {"no-score", "lowest-score", "highest-score"})
    void editPlayedGamePostShouldSucceed(String scoringSystem) throws Exception {
        simplePostEntity.setComments(List.of(postCommentEntity));
        playedGamePostCreationDto.setScoringSystem(scoringSystem);
        Mockito.when(userRepository.findByEmail(any())).thenReturn(Optional.of(userEntity));
        Mockito.when(userRepository.findById(any())).thenReturn(Optional.of(userEntity));
        Mockito.when(playedGamePostRepository.findById(any())).thenReturn(Optional.of(playedGamePostEntity));
        Mockito.when(playedGamePostRepository.save(any())).thenReturn(playedGamePostEntity);
        Mockito.when(bggv1ApiConnection.getBoardGameById(any())).thenReturn(createSampleBGGThingResponse());
        Mockito.when(notificationRepository.save(any())).thenReturn(new NotificationEntity());
        Mockito.doNothing().when(playedGameRepository).deleteByIdIn(any());

        mockMvc.perform(put("/posts/played-game/1")
                        .content(objectMapper.writeValueAsString(playedGamePostCreationDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @WithMockUser(username = "email@gmail.com", authorities = "ROLE_ADMIN")
    @ParameterizedTest
    @ValueSource(strings = {"simple", "poll", "played-game"})
    void deletePostCommentShouldSucceed(String postType) throws Exception {
        ArrayList<PostCommentEntity> comments = new ArrayList<>();
        comments.add(postCommentEntity);
        simplePostEntity.setComments(comments);
        Mockito.when(userRepository.findByEmail(any())).thenReturn(Optional.of(userEntity));
        Mockito.when(pollPostRepository.findById(any())).thenReturn(Optional.of(pollPostEntity));
        Mockito.when(simplePostRepository.findById(any())).thenReturn(Optional.of(simplePostEntity));
        Mockito.when(playedGamePostRepository.findById(any())).thenReturn(Optional.of(playedGamePostEntity));

        Mockito.when(pollPostRepository.save(any())).thenReturn(pollPostEntity);
        Mockito.when(simplePostRepository.save(any())).thenReturn(simplePostEntity);
        Mockito.when(playedGamePostRepository.save(any())).thenReturn(playedGamePostEntity);

        Mockito.doNothing().when(postCommentRepository).delete(any());


        mockMvc.perform(delete("/posts/" + postType + "/1/comments/1"))
                .andExpect(status().isOk());
    }

    @WithMockUser(username = "email@gmail.com", authorities = "ROLE_ADMIN")
    @Test
    void deleteSimplePostShouldSucceed() throws Exception {
        simplePostEntity.setComments(List.of(postCommentEntity));
        Mockito.when(userRepository.findByEmail(any())).thenReturn(Optional.of(userEntity));
        Mockito.when(simplePostRepository.findById(any())).thenReturn(Optional.of(simplePostEntity));
        Mockito.doNothing().when(simplePostRepository).delete(any());

        mockMvc.perform(delete("/posts/simple/1"))
                .andExpect(status().isOk());
    }

    @WithMockUser(username = "email@gmail.com", authorities = "ROLE_ADMIN")
    @Test
    void deletePollPostShouldSucceed() throws Exception {
        simplePostEntity.setComments(List.of(postCommentEntity));
        Mockito.when(userRepository.findByEmail(any())).thenReturn(Optional.of(userEntity));
        Mockito.when(pollPostRepository.findById(any())).thenReturn(Optional.of(pollPostEntity));
        Mockito.doNothing().when(pollPostRepository).delete(any());

        mockMvc.perform(delete("/posts/poll/1"))
                .andExpect(status().isOk());
    }

    @WithMockUser(username = "email@gmail.com", authorities = "ROLE_ADMIN")
    @Test
    void deletePlayedGamePostShouldSucceed() throws Exception {
        simplePostEntity.setComments(List.of(postCommentEntity));
        Mockito.when(userRepository.findByEmail(any())).thenReturn(Optional.of(userEntity));
        Mockito.when(playedGamePostRepository.findById(any())).thenReturn(Optional.of(playedGamePostEntity));
        Mockito.doNothing().when(playedGameRepository).delete(any());

        mockMvc.perform(delete("/posts/played-game/1"))
                .andExpect(status().isOk());
    }

}
