package com.socialnetwork.boardrift.service;

import com.socialnetwork.boardrift.enumeration.Role;
import com.socialnetwork.boardrift.repository.PlayedGamePostRepository;
import com.socialnetwork.boardrift.repository.PlayedGameRepository;
import com.socialnetwork.boardrift.repository.PollPostRepository;
import com.socialnetwork.boardrift.repository.PostCommentReportRepository;
import com.socialnetwork.boardrift.repository.PostCommentRepository;
import com.socialnetwork.boardrift.repository.PostLikeRepository;
import com.socialnetwork.boardrift.repository.PostReportRepository;
import com.socialnetwork.boardrift.repository.SimplePostRepository;
import com.socialnetwork.boardrift.repository.model.board_game.PlayedGameEntity;
import com.socialnetwork.boardrift.repository.model.post.PlayedGamePostEntity;
import com.socialnetwork.boardrift.repository.model.post.PollOptionEntity;
import com.socialnetwork.boardrift.repository.model.post.PollPostEntity;
import com.socialnetwork.boardrift.repository.model.post.Post;
import com.socialnetwork.boardrift.repository.model.post.PostCommentEntity;
import com.socialnetwork.boardrift.repository.model.post.PostCommentReportEntity;
import com.socialnetwork.boardrift.repository.model.post.PostLikeEntity;
import com.socialnetwork.boardrift.repository.model.post.PostReportEntity;
import com.socialnetwork.boardrift.repository.model.post.SimplePostEntity;
import com.socialnetwork.boardrift.repository.model.user.UserEntity;
import com.socialnetwork.boardrift.rest.model.BGGThingResponse;
import com.socialnetwork.boardrift.rest.model.post.PostCommentDto;
import com.socialnetwork.boardrift.rest.model.post.PostCommentPageDto;
import com.socialnetwork.boardrift.rest.model.post.PostPageDto;
import com.socialnetwork.boardrift.rest.model.post.ReportDto;
import com.socialnetwork.boardrift.rest.model.post.played_game_post.PlayedGamePostCreationDto;
import com.socialnetwork.boardrift.rest.model.post.played_game_post.PlayedGamePostRetrievalDto;
import com.socialnetwork.boardrift.rest.model.post.poll_post.PollOptionDto;
import com.socialnetwork.boardrift.rest.model.post.poll_post.PollOptionRetrievalDto;
import com.socialnetwork.boardrift.rest.model.post.poll_post.PollPostCreationDto;
import com.socialnetwork.boardrift.rest.model.post.poll_post.PollPostRetrievalDto;
import com.socialnetwork.boardrift.rest.model.post.simple_post.SimplePostCreationDto;
import com.socialnetwork.boardrift.rest.model.post.simple_post.SimplePostRetrievalDto;
import com.socialnetwork.boardrift.util.exception.DuplicatePollVoteException;
import com.socialnetwork.boardrift.util.exception.DuplicateReportException;
import com.socialnetwork.boardrift.util.exception.FieldValidationException;
import com.socialnetwork.boardrift.util.mapper.NotificationMapper;
import com.socialnetwork.boardrift.util.mapper.PostMapper;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.socialnetwork.boardrift.rest.model.post.played_game_post.PlayedGamePostCreationDto.SelectedPlayerDto;
import static java.util.stream.Collectors.toList;

@RequiredArgsConstructor
@Service
public class PostService {
    private final PlayedGamePostRepository playedGamePostRepository;
    private final PlayedGameRepository playedGameRepository;
    private final SimplePostRepository simplePostRepository;
    private final PollPostRepository pollPostRepository;
    private final PostCommentRepository postCommentRepository;
    private final PostLikeRepository postLikeRepository;
    private final PostReportRepository postReportRepository;
    private final PostCommentReportRepository postCommentReportRepository;
    private final UserService userService;
    private final NotificationService notificationService;
    private final BoardGameService boardGameService;
    private final PostMapper postMapper;
    private final NotificationMapper notificationMapper;
    private final SimpMessagingTemplate messagingTemplate;

    public PlayedGamePostRetrievalDto createPlayedGamePost(PlayedGamePostCreationDto playedGamePostCreationDto) {
        UserDetails postCreatorDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        UserEntity postCreatorEntity = userService.getUserEntityByEmail(postCreatorDetails.getUsername());

        BGGThingResponse boardGameResponse = boardGameService.getBoardGameById(playedGamePostCreationDto.getPlayedGameId());

        PlayedGamePostEntity playedGamePostEntity = switch (playedGamePostCreationDto.getScoringSystem()) {
            case "highest-score" ->
                    createHighestScorePlayedGamePost(playedGamePostCreationDto, postCreatorEntity, boardGameResponse);
            case "lowest-score" ->
                    createLowestScorePlayedGamePost(playedGamePostCreationDto, postCreatorEntity, boardGameResponse);
            case "no-score" ->
                    createNoScorePlayedGamePost(playedGamePostCreationDto, postCreatorEntity, boardGameResponse);
            default -> throw new FieldValidationException(Map.of("scoringSystem", "Invalid scoring system"));
        };

        playedGamePostEntity = playedGamePostRepository.save(playedGamePostEntity);

        notificationService.createAndSaveNotificationsForAssociatedPlays(playedGamePostEntity.getPlayedGame());

        return postMapper.playedGamePostEntityToRetrievalDto(playedGamePostEntity);
    }

    private PlayedGamePostEntity createNoScorePlayedGamePost(PlayedGamePostCreationDto playedGamePostCreationDto, UserEntity postCreatorEntity, BGGThingResponse boardGame) {
        PlayedGameEntity postCreatorPlayedGameEntity = new PlayedGameEntity(null, playedGamePostCreationDto.getPlayedGameId(),
                boardGame.getItems().get(0).getNames().get(0).getValue(), boardGame.getItems().get(0).getImage(), boardGame.getItems().get(0).getBoardGameCategory(),
                0, playedGamePostCreationDto.getPostCreatorWon(), "no-score", new Date(), true, postCreatorEntity, null, new HashSet<>(), new HashSet<>());

        for (SelectedPlayerDto player : playedGamePostCreationDto.getPlayers()) {
            UserEntity playerUserEntity = userService.getUserEntityById(player.getId());
            postCreatorPlayedGameEntity.addAssociatedPlay(new PlayedGameEntity(player.getPlayedGameId() != null ?
                    player.getPlayedGameId() :
                    null, playedGamePostCreationDto.getPlayedGameId(),
                    boardGame.getItems().get(0).getNames().get(0).getValue(), boardGame.getItems().get(0).getImage(),
                    boardGame.getItems().get(0).getBoardGameCategory(), 0, player.getWon(),
                    "no-score", new Date(), false, playerUserEntity, null, new HashSet<>(), new HashSet<>()));
        }

        SimplePostEntity basePost = new SimplePostEntity(null, playedGamePostCreationDto.getDescription(),
                new Date(), postCreatorEntity, new ArrayList<>(),
                new HashSet<>(), null,
                null, new ArrayList<>());

        return new PlayedGamePostEntity(null, 0, 0, 0.0, "no-score", basePost, postCreatorPlayedGameEntity);
    }

    private PlayedGamePostEntity createLowestScorePlayedGamePost(PlayedGamePostCreationDto playedGamePostCreationDto, UserEntity postCreatorEntity, BGGThingResponse boardGame) {
        int min = calculateLowestScore(playedGamePostCreationDto);
        int max = calculateHighestScore(playedGamePostCreationDto);
        Double average = calculateAverageScore(playedGamePostCreationDto);

        PlayedGameEntity postCreatorPlayedGameEntity = new PlayedGameEntity(null, playedGamePostCreationDto.getPlayedGameId(),
                boardGame.getItems().get(0).getNames().get(0).getValue(), boardGame.getItems().get(0).getImage(),
                boardGame.getItems().get(0).getBoardGameCategory(), playedGamePostCreationDto.getPostCreatorPoints(),
                playedGamePostCreationDto.getPostCreatorPoints().equals(min), "lowest-score", new Date(),
                true, postCreatorEntity, null, new HashSet<>(), new HashSet<>());

        for (SelectedPlayerDto player : playedGamePostCreationDto.getPlayers()) {
            UserEntity playerUserEntity = userService.getUserEntityById(player.getId());
            postCreatorPlayedGameEntity.addAssociatedPlay(new PlayedGameEntity(player.getPlayedGameId() != null ? player.getPlayedGameId() : null, playedGamePostCreationDto.getPlayedGameId(),
                    boardGame.getItems().get(0).getNames().get(0).getValue(), boardGame.getItems().get(0).getImage(), boardGame.getItems().get(0).getBoardGameCategory(),
                    player.getPoints(), player.getPoints().equals(min), "lowest-score", new Date(), false,
                    playerUserEntity, null, new HashSet<>(), new HashSet<>()));
        }

        SimplePostEntity basePost = new SimplePostEntity(null, playedGamePostCreationDto.getDescription(),
                new Date(), postCreatorEntity, new ArrayList<>(),
                new HashSet<>(), null, null,
                new ArrayList<>());

        return new PlayedGamePostEntity(null, max, min, average, "lowest-score", basePost, postCreatorPlayedGameEntity);
    }

    private PlayedGamePostEntity createHighestScorePlayedGamePost(PlayedGamePostCreationDto playedGamePostCreationDto, UserEntity postCreatorEntity, BGGThingResponse boardGame) {
        int min = calculateLowestScore(playedGamePostCreationDto);
        int max = calculateHighestScore(playedGamePostCreationDto);
        Double average = calculateAverageScore(playedGamePostCreationDto);

        PlayedGameEntity postCreatorPlayedGameEntity = new PlayedGameEntity(null, playedGamePostCreationDto.getPlayedGameId(),
                boardGame.getItems().get(0).getNames().get(0).getValue(), boardGame.getItems().get(0).getImage(),
                boardGame.getItems().get(0).getBoardGameCategory(), playedGamePostCreationDto.getPostCreatorPoints(),
                playedGamePostCreationDto.getPostCreatorPoints().equals(max),
                "highest-score", new Date(), true, postCreatorEntity, null, new HashSet<>(), new HashSet<>());

        for (SelectedPlayerDto player : playedGamePostCreationDto.getPlayers()) {
            UserEntity playerUserEntity = userService.getUserEntityById(player.getId());
            postCreatorPlayedGameEntity.addAssociatedPlay(new PlayedGameEntity(player.getPlayedGameId() != null ?
                    player.getPlayedGameId() :
                    null, playedGamePostCreationDto.getPlayedGameId(),
                    boardGame.getItems().get(0).getNames().get(0).getValue(), boardGame.getItems().get(0).getImage(), boardGame.getItems().get(0).getBoardGameCategory(),
                    player.getPoints(), player.getPoints().equals(max), "highest-score",
                    new Date(), false, playerUserEntity,
                    null, new HashSet<>(), new HashSet<>()));
        }

        SimplePostEntity basePost = new SimplePostEntity(null, playedGamePostCreationDto.getDescription(), new Date(),
                postCreatorEntity, new ArrayList<>(), new HashSet<>(),
                null, null, new ArrayList<>());

        return new PlayedGamePostEntity(null, max, min, average, "highest-score", basePost, postCreatorPlayedGameEntity);
    }


    private Double calculateAverageScore(PlayedGamePostCreationDto playedGamePostCreationDto) {
        Double sum = playedGamePostCreationDto.getPostCreatorPoints().doubleValue();
        int numOfPlayers = playedGamePostCreationDto.getPlayers().size();

        for (SelectedPlayerDto player : playedGamePostCreationDto.getPlayers()) {
            sum += player.getPoints().doubleValue();
        }

        double average = sum / (numOfPlayers + 1);

        // Rounding up to two decimal places
        double roundedAverage = Math.ceil(average * 100) / 100;

        return roundedAverage;
    }

    private Integer calculateLowestScore(PlayedGamePostCreationDto playedGamePostCreationDto) {
        Integer min = playedGamePostCreationDto.getPostCreatorPoints();

        for (SelectedPlayerDto player : playedGamePostCreationDto.getPlayers()) {
            if (player.getPoints() < min) {
                min = player.getPoints();
            }
        }

        return min;
    }

    private Integer calculateHighestScore(PlayedGamePostCreationDto playedGamePostCreationDto) {
        Integer max = playedGamePostCreationDto.getPostCreatorPoints();

        for (SelectedPlayerDto player : playedGamePostCreationDto.getPlayers()) {
            if (player.getPoints() > max) {
                max = player.getPoints();
            }
        }

        return max;
    }

    public SimplePostRetrievalDto createSimplePost(SimplePostCreationDto simplePostCreationDto) {
        UserDetails postCreatorDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        UserEntity postCreatorEntity = userService.getUserEntityByEmail(postCreatorDetails.getUsername());

        SimplePostEntity simplePostEntity = new SimplePostEntity(null, simplePostCreationDto.getDescription(),
                new Date(), postCreatorEntity, new ArrayList<>(),
                new HashSet<>(), null,
                null, new ArrayList<>());

        simplePostEntity = simplePostRepository.save(simplePostEntity);

        return postMapper.simplePostEntityToRetrievalDto(simplePostEntity);
    }

    public PollPostRetrievalDto createPollPost(PollPostCreationDto pollPostCreationDto) {
        UserDetails postCreatorDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        UserEntity postCreatorEntity = userService.getUserEntityByEmail(postCreatorDetails.getUsername());


        SimplePostEntity basePost = new SimplePostEntity(null, pollPostCreationDto.getDescription(),
                new Date(), postCreatorEntity,
                new ArrayList<>(), new HashSet<>(),
                null, null,
                new ArrayList<>());

        PollPostEntity pollPost = new PollPostEntity(null, new ArrayList<>(), basePost);

        List<PollOptionEntity> options = new ArrayList<>();

        for (PollOptionDto option : pollPostCreationDto.getOptions()) {
            options.add(new PollOptionEntity(null, option.getText(), pollPost, new HashSet<>()));
        }

        pollPost.setOptions(options);

        pollPost = pollPostRepository.save(pollPost);

        return postMapper.pollPostEntityToRetrievalDto(pollPost);
    }

    public void createPollVote(Long pollId, Long optionId) {
        UserDetails voterUserDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        UserEntity voterEntity = userService.getUserEntityByEmail(voterUserDetails.getUsername());

        PollPostEntity pollPost = pollPostRepository
                .findById(pollId)
                .orElseThrow(() -> new EntityNotFoundException("Poll post with id: " + pollId + " was not found"));

        if (userAlreadyVoted(pollPost, voterEntity.getId())) {
            throw new DuplicatePollVoteException("User with id: " + voterEntity.getId() + " has already voted on this poll");
        }

        pollPost.addVoteByOptionId(optionId, voterEntity);

        pollPostRepository.save(pollPost);
    }

    private boolean userAlreadyVoted(PollPostEntity pollPost, Long voterId) {
        return pollPost.getOptions()
                .stream()
                .anyMatch(option -> option.getVotes()
                        .stream()
                        .anyMatch(vote -> vote.getVoter().getId().equals(voterId)));
    }

    public PostCommentDto createPostComment(String postType, Long postId, PostCommentDto commentDto) {
        UserDetails commentCreatorUserDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        UserEntity commentCreatorEntity = userService.getUserEntityByEmail(commentCreatorUserDetails.getUsername());

        return switch (postType) {
            case "simple" -> createSimplePostComment(postId, commentDto, commentCreatorEntity);
            case "played-game" -> createPlayedGamePostComment(postId, commentDto, commentCreatorEntity);
            case "poll" -> createPollPostComment(postId, commentDto, commentCreatorEntity);
            default ->
                    throw new FieldValidationException(Map.of("postType", "This post type does not support comments"));
        };
    }

    private PostCommentDto createPollPostComment(Long postId, PostCommentDto commentDto, UserEntity commentCreatorEntity) {
        PollPostEntity pollPostEntity = pollPostRepository
                .findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("Poll post with id: " + postId + " was not found"));

        PostCommentEntity postCommentEntity = postCommentRepository.save(new PostCommentEntity(null, commentDto.getText(), Instant.now(),
                pollPostEntity.getBasePost(), commentCreatorEntity, new ArrayList<>()));

        return postMapper.postCommentEntityToDto(postCommentEntity);
    }

    private PostCommentDto createPlayedGamePostComment(Long postId, PostCommentDto commentDto, UserEntity commentCreatorEntity) {
        PlayedGamePostEntity playedGamePostEntity = playedGamePostRepository
                .findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("Played game post with id: " + postId + " was not found"));

        PostCommentEntity postCommentEntity = postCommentRepository.save(new PostCommentEntity(null, commentDto.getText(), Instant.now(),
                playedGamePostEntity.getBasePost(), commentCreatorEntity, new ArrayList<>()));

        return postMapper.postCommentEntityToDto(postCommentEntity);
    }

    private PostCommentDto createSimplePostComment(Long postId, PostCommentDto commentDto, UserEntity commentCreatorEntity) {
        SimplePostEntity simplePostEntity = simplePostRepository
                .findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("Simple post with id: " + postId + " was not found"));

        PostCommentEntity postCommentEntity = postCommentRepository.save(new PostCommentEntity(null, commentDto.getText(), Instant.now(),
                simplePostEntity, commentCreatorEntity, new ArrayList<>()));

        return postMapper.postCommentEntityToDto(postCommentEntity);
    }

    public PostCommentPageDto getPostComments(String postType, Long postId, Integer page, Integer pageSize, HttpServletRequest request) {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        UserEntity userEntity = userService.getUserEntityByEmail(userDetails.getUsername());

        List<PostCommentDto> comments;

        switch (postType) {
            case "simple" -> {
                comments = getSimplePostComments(postId, page, pageSize, userEntity);
            }

            case "played-game" -> {
                comments = getPlayedGamePostComments(postId, page, pageSize, userEntity);
            }

            case "poll" -> {
                comments = getPollPostComments(postId, page, pageSize, userEntity);
            }

            default ->
                    throw new FieldValidationException(Map.of("postType", "This post type does not support comments"));
        }

        String nextPageUrl = comments.size() == pageSize ? String.format("%s%s?page=%d&pageSize=%d",
                ServletUriComponentsBuilder.fromCurrentContextPath().toUriString(),
                request.getServletPath(),
                page + 1,
                pageSize) : null;

        return new PostCommentPageDto(nextPageUrl, comments);
    }

    private List<PostCommentDto> getPollPostComments(Long postId, Integer page, Integer pageSize, UserEntity loggedInUser) {
        PollPostEntity pollPostEntity = pollPostRepository
                .findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("Poll post with id: " + postId + " was not found"));

        return postCommentRepository
                .findAllBySimplePostId(pollPostEntity.getBasePost().getId(), PageRequest.of(page, pageSize, Sort.by("creationDate").descending()))
                .stream()
                .filter(comment -> comment.getCommentCreator().isEnabled())
                .map(postMapper::postCommentEntityToDto)
                .peek(postCommentDto ->
                        {
                            postCommentDto.setAlreadyReported(postCommentDto
                                    .getReports()
                                    .stream()
                                    .anyMatch(reportDto -> reportDto.getReporter().getId().equals(loggedInUser.getId())));
                            postCommentDto.setPostId(postId);
                            postCommentDto.setPostType("poll");
                        }
                ).toList();
    }

    private List<PostCommentDto> getPlayedGamePostComments(Long postId, Integer page, Integer pageSize, UserEntity loggedInUser) {
        PlayedGamePostEntity playedGamePost = playedGamePostRepository
                .findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("Played game post with id: " + postId + " was not found"));

        return postCommentRepository
                .findAllBySimplePostId(playedGamePost.getBasePost().getId(), PageRequest.of(page, pageSize, Sort.by("creationDate").descending()))
                .stream()
                .filter(comment -> comment.getCommentCreator().isEnabled())
                .map(postMapper::postCommentEntityToDto)
                .peek(postCommentDto ->
                        {
                            postCommentDto.setAlreadyReported(postCommentDto
                                    .getReports()
                                    .stream()
                                    .anyMatch(reportDto -> reportDto.getReporter().getId().equals(loggedInUser.getId())));
                            postCommentDto.setPostId(postId);
                            postCommentDto.setPostType("played-game");
                        }
                ).toList();
    }

    private List<PostCommentDto> getSimplePostComments(Long postId, Integer page, Integer pageSize, UserEntity loggedInUser) {
        SimplePostEntity simplePost = simplePostRepository
                .findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("Simple post with id: " + postId + " was not found"));

        return postCommentRepository
                .findAllBySimplePostId(simplePost.getId(), PageRequest.of(page, pageSize, Sort.by("creationDate").descending()))
                .stream()
                .filter(comment -> comment.getCommentCreator().isEnabled())
                .map(postMapper::postCommentEntityToDto)
                .peek(postCommentDto ->
                        {
                            postCommentDto.setAlreadyReported(postCommentDto
                                    .getReports()
                                    .stream()
                                    .anyMatch(reportDto -> reportDto.getReporter().getId().equals(loggedInUser.getId())));
                            postCommentDto.setPostId(postId);
                            postCommentDto.setPostType("simple");
                        }
                ).toList();

    }

    public void likePost(String postType, Long postId) {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        UserEntity userEntity = userService.getUserEntityByEmail(userDetails.getUsername());

        switch (postType) {
            case "simple" -> {
                likeSimplePost(postId, userEntity);
            }

            case "played-game" -> {
                likePlayedGamePost(postId, userEntity);
            }

            case "poll" -> {
                likePollPost(postId, userEntity);
            }

            default -> throw new FieldValidationException(Map.of("postType", "This post type does not support likes"));
        }
    }

    private void likePollPost(Long postId, UserEntity userEntity) {
        PollPostEntity pollPostEntity = pollPostRepository
                .findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("Poll post with id: " + postId + " was not found"));

        postLikeRepository
                .findBySimplePostIdAndLikeOwnerId(pollPostEntity.getBasePost().getId(), userEntity.getId())
                .ifPresentOrElse(
                        postLikeRepository::delete,
                        () -> {
                            postLikeRepository.save(new PostLikeEntity(null, userEntity, pollPostEntity.getBasePost()));
                        }
                );
    }

    private void likePlayedGamePost(Long postId, UserEntity userEntity) {
        PlayedGamePostEntity playedGamePost = playedGamePostRepository
                .findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("Played game post with id: " + postId + " was not found"));

        postLikeRepository
                .findBySimplePostIdAndLikeOwnerId(playedGamePost.getBasePost().getId(), userEntity.getId())
                .ifPresentOrElse(
                        postLikeRepository::delete,
                        () -> {
                            postLikeRepository.save(new PostLikeEntity(null, userEntity, playedGamePost.getBasePost()));
                        }
                );
    }

    private void likeSimplePost(Long postId, UserEntity userEntity) {
        SimplePostEntity simplePost = simplePostRepository
                .findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("Simple post with id: " + postId + " was not found"));

        postLikeRepository
                .findBySimplePostIdAndLikeOwnerId(postId, userEntity.getId())
                .ifPresentOrElse(
                        postLikeRepository::delete,
                        () -> {
                            postLikeRepository.save(new PostLikeEntity(null, userEntity, simplePost));
                        }
                );
    }

    public PostPageDto getFeed(Integer page, Integer pageSize, HttpServletRequest request) {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        UserEntity userEntity = userService.getUserEntityByEmail(userDetails.getUsername());

        List<Post> posts = new ArrayList<>();

        List<PlayedGamePostRetrievalDto> userAndFriendsPlayedGamePosts = retrieveFeedPlayedGamePosts(userEntity, page, pageSize);
        List<SimplePostRetrievalDto> userAndFriendsSimplePosts = retrieveFeedSimplePosts(userEntity, page, pageSize);
        List<PollPostRetrievalDto> userAndFriendsPollPosts = retrieveFeedPollPosts(userEntity, page, pageSize);

        posts.addAll(userAndFriendsSimplePosts);
        posts.addAll(userAndFriendsPlayedGamePosts);
        posts.addAll(userAndFriendsPollPosts);

        posts.sort((post1, post2) -> post2.getCreationDate().compareTo(post1.getCreationDate()));

        if (posts.size() > pageSize) {
            posts = posts.subList(0, pageSize);
        }

        String nexPageUrl = posts.size() == pageSize ? String.format("%s%s?page=%d&pageSize=%d",
                ServletUriComponentsBuilder.fromCurrentContextPath().toUriString(),
                request.getServletPath(),
                page + 1,
                pageSize) : null;

        return new PostPageDto(nexPageUrl, posts);
    }

    public List<PollPostRetrievalDto> retrieveFeedPollPosts(UserEntity userEntity, Integer page, Integer pageSize) {
        PageRequest pageRequest = PageRequest.of(page, pageSize, Sort.by("basePost.creationDate").descending());

        return pollPostRepository.findAllByPostCreatorOrFriends(userEntity, pageRequest)
                .stream()
                .map(pollPostEntity -> mapPollPostEntityToDtoWithAdditionalFields(userEntity.getId(), pollPostEntity, false))
                .toList();
    }

    public List<PlayedGamePostRetrievalDto> retrieveFeedPlayedGamePosts(UserEntity userEntity, Integer page, Integer pageSize) {
        PageRequest pageRequest = PageRequest.of(page, pageSize, Sort.by("basePost.creationDate").descending());

        return playedGamePostRepository.findAllByPostCreatorOrFriends(userEntity, pageRequest)
                .stream()
                .map(playedGamePostEntity -> mapPlayedGamePostEntityToDtoWithAdditionalFields(userEntity.getId(), playedGamePostEntity, false))
                .toList();
    }

    public List<SimplePostRetrievalDto> retrieveFeedSimplePosts(UserEntity userEntity, Integer page, Integer pageSize) {
        PageRequest pageRequest = PageRequest.of(page, pageSize, Sort.by("creationDate").descending());

        return simplePostRepository.findAllByPostCreatorOrFriends(userEntity, pageRequest)
                .stream()
                .map(simplePostEntity -> mapSimplePostEntityToDtoWithAdditionalFields(userEntity.getId(), simplePostEntity, false))
                .toList();
    }

    public PostPageDto getPostsByUserId(Long userId, Integer page, Integer pageSize, HttpServletRequest request) throws IllegalAccessException {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        UserEntity userEntity = userService.getUserEntityByEmail(userDetails.getUsername());

        if (!userEntity.getRole().equals(Role.ROLE_ADMIN)) {
            if (!userEntity.getId().equals(userId)) {
                UserEntity targetUser = userService.getUserEntityById(userId);

                if (!targetUser.isEnabled()) {
                    throw new IllegalAccessException("This user is suspended, you cannot view their posts");
                }

                if (!targetUser.getPublicPosts() && !userAlreadyFriend(userEntity, targetUser.getId())) {
                    throw new IllegalAccessException("This user's posts are private");
                }
            }
        }

        List<Post> posts = new ArrayList<>();

        posts.addAll(retrieveSimplePostsByUserId(userId, userEntity, page, pageSize));
        posts.addAll(retrievePollPostsByUserId(userId, userEntity, page, pageSize));
        posts.addAll(retrievePlayedGamePostsByUserId(userId, userEntity, page, pageSize));

        posts.sort((post1, post2) -> post2.getCreationDate().compareTo(post1.getCreationDate()));

        if (posts.size() > pageSize) {
            posts = posts.subList(0, pageSize);
        }

        String nextPageUrl = posts.size() == pageSize ? String.format("%s%s?page=%d&pageSize=%d",
                ServletUriComponentsBuilder.fromCurrentContextPath().toUriString(),
                request.getServletPath(),
                page + 1,
                pageSize) : null;

        return new PostPageDto(nextPageUrl, posts);
    }

    public boolean userAlreadyFriend(UserEntity loggedInUserEntity, Long targetUserId) {
        boolean hasFriend = loggedInUserEntity
                .getFriends()
                .stream()
                .anyMatch(friend -> friend.getId().equals(targetUserId));

        boolean friendOf = loggedInUserEntity
                .getFriendOf()
                .stream()
                .anyMatch(friend -> friend.getId().equals(targetUserId));

        return hasFriend || friendOf;
    }

    private List<SimplePostRetrievalDto> retrieveSimplePostsByUserId(Long userId, UserEntity loggedInUser, Integer page, Integer pageSize) {
        PageRequest pageRequest = PageRequest.of(page, pageSize, Sort.by("creationDate").descending());

        return simplePostRepository.findByPostCreatorId(userId, pageRequest)
                .stream()
                .map(simplePostEntity -> mapSimplePostEntityToDtoWithAdditionalFields(loggedInUser.getId(), simplePostEntity, false))
                .toList();
    }

    private List<PollPostRetrievalDto> retrievePollPostsByUserId(Long userId, UserEntity loggedInUser, Integer page, Integer pageSize) {
        PageRequest pageRequest = PageRequest.of(page, pageSize, Sort.by("basePost.creationDate").descending());

        return pollPostRepository.findByBasePostPostCreatorId(userId, pageRequest)
                .stream()
                .map(pollPostEntity -> mapPollPostEntityToDtoWithAdditionalFields(loggedInUser.getId(), pollPostEntity, false))
                .toList();
    }

    private List<PlayedGamePostRetrievalDto> retrievePlayedGamePostsByUserId(Long userId, UserEntity loggedInUser, Integer page, Integer pageSize) {
        PageRequest pageRequest = PageRequest.of(page, pageSize, Sort.by("basePost.creationDate").descending());

        return playedGamePostRepository.findByBasePostPostCreatorId(userId, pageRequest)
                .stream()
                .map(playedGamePostEntity -> mapPlayedGamePostEntityToDtoWithAdditionalFields(loggedInUser.getId(), playedGamePostEntity, false))
                .toList();
    }

    private PlayedGamePostRetrievalDto mapPlayedGamePostEntityToDtoWithAdditionalFields(Long loggedInUserId, PlayedGamePostEntity playedGamePostEntity, Boolean includeReports) {
        PlayedGamePostRetrievalDto playedGamePostRetrievalDto = postMapper.playedGamePostEntityToRetrievalDto(playedGamePostEntity);

        playedGamePostRetrievalDto.setAlreadyLiked(playedGamePostEntity
                .getBasePost()
                .getLikes()
                .stream()
                .anyMatch(postLikeEntity -> postLikeEntity.getLikeOwner().getId().equals(loggedInUserId)));

        playedGamePostRetrievalDto.setAlreadyReported(userAlreadyReportedPost(loggedInUserId, playedGamePostEntity.getBasePost()));

        if (!includeReports) {
            playedGamePostRetrievalDto.setReports(null);
        }

        return playedGamePostRetrievalDto;
    }

    private SimplePostRetrievalDto mapSimplePostEntityToDtoWithAdditionalFields(Long loggedInUserId, SimplePostEntity simplePostEntity, Boolean includeReports) {
        SimplePostRetrievalDto simplePostRetrievalDto = postMapper.simplePostEntityToRetrievalDto(simplePostEntity);

        simplePostRetrievalDto.setAlreadyLiked(simplePostEntity
                .getLikes()
                .stream()
                .anyMatch(postLikeEntity -> postLikeEntity.getLikeOwner().getId().equals(loggedInUserId)));

        simplePostRetrievalDto.setAlreadyReported(userAlreadyReportedPost(loggedInUserId, simplePostEntity));

        if (!includeReports) {
            simplePostRetrievalDto.setReports(null);
        }

        return simplePostRetrievalDto;
    }

    private PollPostRetrievalDto mapPollPostEntityToDtoWithAdditionalFields(Long loggedInUserId, PollPostEntity pollPostEntity, Boolean includeReports) {
        PollPostRetrievalDto pollPostRetrievalDto = postMapper.pollPostEntityToRetrievalDto(pollPostEntity);

        if (userAlreadyVoted(pollPostEntity, loggedInUserId)) {
            pollPostRetrievalDto.setAlreadyVoted(true);

            pollPostRetrievalDto.setSelectedOption(pollPostEntity.getOptions()
                    .stream()
                    .filter(pollOptionEntity -> pollOptionEntity.getVotes()
                            .stream()
                            .anyMatch(pollOptionVoteEntity -> pollOptionVoteEntity.getVoter().getId().equals(loggedInUserId)))
                    .findFirst()
                    .map(pollOptionEntity -> new PollOptionRetrievalDto(pollOptionEntity.getId(), pollOptionEntity.getText(), pollOptionEntity.getVotes().size()))
                    .get());
        }

        pollPostRetrievalDto.setAlreadyLiked(pollPostEntity
                .getBasePost()
                .getLikes()
                .stream()
                .anyMatch(postLikeEntity -> postLikeEntity.getLikeOwner().getId().equals(loggedInUserId)));

        pollPostRetrievalDto.setAlreadyReported(userAlreadyReportedPost(loggedInUserId, pollPostEntity.getBasePost()));

        if (!includeReports) {
            pollPostRetrievalDto.setReports(null);
        }

        return pollPostRetrievalDto;
    }

    public PostCommentPageDto getReportedComments(Integer page, Integer pageSize, HttpServletRequest request) {
        PageRequest pageRequest = PageRequest.of(page, pageSize);
        List<PostCommentEntity> reportedComments = postCommentRepository.findReportedComments(pageRequest);

        List<PostCommentDto> reportedPostCommentDtos = reportedComments
                .stream()
                .map(reportedCommentEntity -> {
                    PostCommentDto postCommentDto = postMapper.postCommentEntityToDto(reportedCommentEntity);

                    if (reportedCommentEntity.getSimplePost().getChildPlayedGamePost() != null) {
                        postCommentDto.setPostId(reportedCommentEntity.getSimplePost().getChildPlayedGamePost().getId());
                        postCommentDto.setPostType("played-game");
                    }

                    if (reportedCommentEntity.getSimplePost().getChildPollPost() != null) {
                        postCommentDto.setPostId(reportedCommentEntity.getSimplePost().getChildPollPost().getId());
                        postCommentDto.setPostType("poll");
                    }

                    if (reportedCommentEntity.getSimplePost().getChildPlayedGamePost() == null &&
                            reportedCommentEntity.getSimplePost().getChildPollPost() == null) {
                        postCommentDto.setPostId(reportedCommentEntity.getSimplePost().getId());
                        postCommentDto.setPostType("simple");
                    }

                    return postCommentDto;
                }).toList();

        String nexPageUrl = reportedPostCommentDtos.size() == pageSize ? String.format("%s%s?page=%d&pageSize=%d",
                ServletUriComponentsBuilder.fromCurrentContextPath().toUriString(),
                request.getServletPath(),
                page + 1,
                pageSize) : null;

        return new PostCommentPageDto(nexPageUrl, reportedPostCommentDtos);
    }

    public PostPageDto getReportedPosts(Integer page, Integer pageSize, HttpServletRequest request) {
        List<Post> reportedPosts = new ArrayList<>();

        List<PlayedGamePostRetrievalDto> reportedPlayedGamePosts = retrieveReportedPlayedGamePosts(page, pageSize);
        List<SimplePostRetrievalDto> reportedSimplePosts = retrieveReportedSimplePosts(page, pageSize);
        List<PollPostRetrievalDto> reportedPollPosts = retrieveReportedPollPosts(page, pageSize);

        reportedPosts.addAll(reportedPlayedGamePosts);
        reportedPosts.addAll(reportedSimplePosts);
        reportedPosts.addAll(reportedPollPosts);

        reportedPosts.sort((post1, post2) -> Integer.compare(post2.getReports().size(), post1.getReports().size()));

        if (reportedPosts.size() > pageSize) {
            reportedPosts = reportedPosts.subList(0, pageSize);
        }

        String nexPageUrl = reportedPosts.size() == pageSize ? String.format("%s%s?page=%d&pageSize=%d",
                ServletUriComponentsBuilder.fromCurrentContextPath().toUriString(),
                request.getServletPath(),
                page + 1,
                pageSize) : null;

        return new PostPageDto(nexPageUrl, reportedPosts);
    }

    private List<PollPostRetrievalDto> retrieveReportedPollPosts(Integer page, Integer pageSize) {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        UserEntity userEntity = userService.getUserEntityByEmail(userDetails.getUsername());

        PageRequest pageRequest = PageRequest.of(page, pageSize);

        return pollPostRepository.findReportedPosts(pageRequest)
                .stream()
                .map(pollPostEntity -> mapPollPostEntityToDtoWithAdditionalFields(userEntity.getId(), pollPostEntity, true))
                .toList();
    }

    private List<SimplePostRetrievalDto> retrieveReportedSimplePosts(Integer page, Integer pageSize) {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        UserEntity userEntity = userService.getUserEntityByEmail(userDetails.getUsername());

        PageRequest pageRequest = PageRequest.of(page, pageSize);

        return simplePostRepository.findReportedPosts(pageRequest)
                .stream()
                .map(simplePostEntity -> mapSimplePostEntityToDtoWithAdditionalFields(userEntity.getId(), simplePostEntity, true))
                .toList();
    }

    private List<PlayedGamePostRetrievalDto> retrieveReportedPlayedGamePosts(Integer page, Integer pageSize) {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        UserEntity userEntity = userService.getUserEntityByEmail(userDetails.getUsername());

        PageRequest pageRequest = PageRequest.of(page, pageSize);

        return playedGamePostRepository.findReportedPosts(pageRequest)
                .stream()
                .map(playedGamePostEntity -> mapPlayedGamePostEntityToDtoWithAdditionalFields(userEntity.getId(), playedGamePostEntity, true))
                .toList();
    }

    public SimplePostRetrievalDto editSimplePost(Long simplePostId, SimplePostCreationDto simplePostCreationDto) throws IllegalAccessException {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        UserEntity userEntity = userService.getUserEntityByEmail(userDetails.getUsername());

        SimplePostEntity simplePostEntity = simplePostRepository
                .findById(simplePostId)
                .orElseThrow(() -> new EntityNotFoundException("Simple post with id: " + simplePostId + " was not found"));

        if (!Role.ROLE_ADMIN.name().equals(userEntity.getRole().name())) {
            if (!simplePostEntity.getPostCreator().getEmail().equals(userEntity.getEmail())) {
                throw new IllegalAccessException("You cannot edit another users post");
            }
        }

        simplePostEntity.setDescription(simplePostCreationDto.getDescription());

        return mapSimplePostEntityToDtoWithAdditionalFields(userEntity.getId(), simplePostRepository.save(simplePostEntity), false);
    }

    public void deleteSimplePost(Long simplePostId) throws IllegalAccessException {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String role = userDetails.getAuthorities().stream().findFirst().get().getAuthority();

        SimplePostEntity simplePostEntity = simplePostRepository
                .findById(simplePostId)
                .orElseThrow(() -> new EntityNotFoundException("Simple post with id: " + simplePostId + " was not found"));

        if (!Role.ROLE_ADMIN.name().equals(role)) {
            if (!simplePostEntity.getPostCreator().getEmail().equals(userDetails.getUsername())) {
                throw new IllegalAccessException("You cannot delete another users post");
            }
        }

        simplePostRepository.delete(simplePostEntity);
    }

    public PollPostRetrievalDto editPollPost(Long pollPostId, PollPostCreationDto pollPostCreationDto) throws IllegalAccessException {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        UserEntity userEntity = userService.getUserEntityByEmail(userDetails.getUsername());

        PollPostEntity pollPostEntity = pollPostRepository
                .findById(pollPostId)
                .orElseThrow(() -> new EntityNotFoundException("Poll post with id: " + pollPostId + " was not found"));

        if (!Role.ROLE_ADMIN.name().equals(userEntity.getRole().name())) {
            if (!pollPostEntity.getBasePost().getPostCreator().getEmail().equals(userEntity.getEmail())) {
                throw new IllegalAccessException("You cannot edit another users post");
            }
        }

        if (!pollPostIsEditable(pollPostEntity)) {
            throw new IllegalAccessException("You cannot edit a poll which has votes");
        }

        Set<Long> editedOptionIds = new HashSet<>();

        for (PollOptionDto optionDto : pollPostCreationDto.getOptions()) {
            if (optionDto.getId() == null) { //new option
                pollPostEntity.getOptions().add(new PollOptionEntity(null, optionDto.getText(), pollPostEntity, new HashSet<>()));
                continue;
            }

            for (PollOptionEntity optionEntity : pollPostEntity.getOptions()) {
                if (optionEntity.getId() != null && optionEntity.getId().equals(optionDto.getId())) { //update existing options
                    optionEntity.setText(optionDto.getText());
                    editedOptionIds.add(optionEntity.getId()); //track which options were edited
                }
            }
        }

        Set<Long> optionsToDeleteIds = pollPostEntity.getOptions()
                .stream()
                .map(PollOptionEntity::getId)
                .collect(Collectors.toSet()); //remove options that were deleted during editing
        optionsToDeleteIds.removeAll(editedOptionIds);
        pollPostEntity.getOptions().removeIf(option -> option.getId() != null && optionsToDeleteIds.contains(option.getId()));

        pollPostEntity.getBasePost().setDescription(pollPostCreationDto.getDescription());

        return mapPollPostEntityToDtoWithAdditionalFields(userEntity.getId(), pollPostRepository.save(pollPostEntity), false);
    }

    private boolean pollPostIsEditable(PollPostEntity entity) {
        return entity
                .getOptions()
                .stream()
                .allMatch(pollOptionEntity -> pollOptionEntity.getVotes().isEmpty());
    }

    public void deletePollPost(Long pollPostId) throws IllegalAccessException {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String role = userDetails.getAuthorities().stream().findFirst().get().getAuthority();

        PollPostEntity pollPostEntity = pollPostRepository
                .findById(pollPostId)
                .orElseThrow(() -> new EntityNotFoundException("Poll post with id: " + pollPostId + " was not found"));

        if (!Role.ROLE_ADMIN.name().equals(role)) {
            if (!pollPostEntity.getBasePost().getPostCreator().getEmail().equals(userDetails.getUsername())) {
                throw new IllegalAccessException("You cannot delete another users post");
            }
        }

        pollPostRepository.delete(pollPostEntity);
    }

    public PlayedGamePostRetrievalDto editPlayedGamePost(Long playedGamePostId, PlayedGamePostCreationDto playedGamePostCreationDto) throws IllegalAccessException {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        UserEntity userEntity = userService.getUserEntityByEmail(userDetails.getUsername());

        PlayedGamePostEntity playedGamePostEntity = playedGamePostRepository
                .findById(playedGamePostId)
                .orElseThrow(() -> new EntityNotFoundException("Played game post with id: " + playedGamePostId + "was not found"));
        Date playedGameCreationDate = playedGamePostEntity.getPlayedGame().getCreationDate();

        if (!Role.ROLE_ADMIN.name().equals(userEntity.getRole().name())) {
            if (!playedGamePostEntity.getBasePost().getPostCreator().getEmail().equals(userEntity.getUsername())) {
                throw new IllegalAccessException("You cannot edit another users post");
            }
        }

        BGGThingResponse boardGameResponse = boardGameService.getBoardGameById(playedGamePostCreationDto.getPlayedGameId());

        PlayedGamePostEntity editedPlayedGamePostEntity = switch (playedGamePostCreationDto.getScoringSystem()) {
            case "highest-score" ->
                    createHighestScorePlayedGamePost(playedGamePostCreationDto, userEntity, boardGameResponse);
            case "lowest-score" ->
                    createLowestScorePlayedGamePost(playedGamePostCreationDto, userEntity, boardGameResponse);
            case "no-score" ->
                    createNoScorePlayedGamePost(playedGamePostCreationDto, userEntity, boardGameResponse);
            default -> throw new FieldValidationException(Map.of("scoringSystem", "Invalid scoring system"));
        };

        editedPlayedGamePostEntity.setId(playedGamePostEntity.getId());
        editedPlayedGamePostEntity.getBasePost().setId(playedGamePostEntity.getBasePost().getId());
        editedPlayedGamePostEntity.getBasePost().setCreationDate(playedGamePostEntity.getBasePost().getCreationDate());
        editedPlayedGamePostEntity.getPlayedGame().setId(playedGamePostEntity.getPlayedGame().getId());
        editedPlayedGamePostEntity.getPlayedGame().setCreationDate(playedGameCreationDate);
        editedPlayedGamePostEntity.getBasePost().setLikes(playedGamePostEntity.getBasePost().getLikes());
        editedPlayedGamePostEntity.getBasePost().setComments(playedGamePostEntity.getBasePost().getComments());
        editedPlayedGamePostEntity.getBasePost().setReports(playedGamePostEntity.getBasePost().getReports());

        List<Long> currentPlayIds = playedGamePostEntity.getPlayedGame().getAssociatedPlays()
                .stream()
                .map(PlayedGameEntity::getId)
                .collect(toList());
        List<Long> editedPlayIds = editedPlayedGamePostEntity.getPlayedGame().getAssociatedPlays().stream().map(PlayedGameEntity::getId).toList();

        currentPlayIds.removeAll(editedPlayIds);
        playedGameRepository.deleteByIdIn(currentPlayIds);
        editedPlayedGamePostEntity = playedGamePostRepository.save(editedPlayedGamePostEntity);

        notificationService.createAndSaveNotificationsForAssociatedPlays(editedPlayedGamePostEntity.getPlayedGame());

        return mapPlayedGamePostEntityToDtoWithAdditionalFields(userEntity.getId(), editedPlayedGamePostEntity, false);
    }

    public void deletePlayedGamePost(Long playedGamePostId) throws IllegalAccessException {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String role = userDetails.getAuthorities().stream().findFirst().get().getAuthority();

        PlayedGamePostEntity playedGamePostEntity = playedGamePostRepository
                .findById(playedGamePostId)
                .orElseThrow(() -> new EntityNotFoundException("Played game post with id: " + playedGamePostId + " was not found"));

        if (!Role.ROLE_ADMIN.name().equals(role)) {
            if (!playedGamePostEntity.getBasePost().getPostCreator().getEmail().equals(userDetails.getUsername())) {
                throw new IllegalAccessException("You cannot delete another users post");
            }
        }

        playedGamePostRepository.delete(playedGamePostEntity);
    }

    public PostCommentDto editPostComment(String postType, Long postId, Long commentId, PostCommentDto postCommentDto) throws IllegalAccessException {
        PostCommentDto editedPostComment;

        switch (postType) {
            case "simple" -> {
                editedPostComment = editSimplePostComment(postId, commentId, postCommentDto);
            }

            case "played-game" -> {
                editedPostComment = editPlayedGamePostComment(postId, commentId, postCommentDto);
            }

            case "poll" -> {
                editedPostComment = editPollPostComment(postId, commentId, postCommentDto);
            }

            default ->
                    throw new FieldValidationException(Map.of("postType", "This post type does not support comments"));
        }

        return editedPostComment;
    }

    public PostCommentDto editSimplePostComment(Long postId, Long commentId, PostCommentDto postCommentDto) throws IllegalAccessException {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String role = userDetails.getAuthorities().stream().findFirst().get().getAuthority();

        SimplePostEntity simplePost = simplePostRepository
                .findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("Simple post with id: " + postId + " was not found"));

        PostCommentEntity postCommentEntity = simplePost
                .getComments()
                .stream()
                .filter(comment -> comment.getId().equals(commentId))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("Comment with id: " + commentId + " was not found"));

        if (!Role.ROLE_ADMIN.name().equals(role)) {
            if (!postCommentEntity.getCommentCreator().getEmail().equals(userDetails.getUsername())) {
                throw new IllegalAccessException("You cannot edit another users comment");
            }
        }

        postCommentEntity.setText(postCommentDto.getText());

        return postMapper.postCommentEntityToDto(postCommentRepository.save(postCommentEntity));
    }

    public PostCommentDto editPlayedGamePostComment(Long postId, Long commentId, PostCommentDto postCommentDto) throws IllegalAccessException {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String role = userDetails.getAuthorities().stream().findFirst().get().getAuthority();

        PlayedGamePostEntity playedGamePostEntity = playedGamePostRepository
                .findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("Played game post with id: " + postId + " was not found"));

        PostCommentEntity postCommentEntity = playedGamePostEntity
                .getBasePost()
                .getComments()
                .stream()
                .filter(comment -> comment.getId().equals(commentId))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("Comment with id: " + commentId + " was not found"));

        if (!Role.ROLE_ADMIN.name().equals(role)) {
            if (!postCommentEntity.getCommentCreator().getEmail().equals(userDetails.getUsername())) {
                throw new IllegalAccessException("You cannot edit another users comment");
            }
        }

        postCommentEntity.setText(postCommentDto.getText());

        return postMapper.postCommentEntityToDto(postCommentRepository.save(postCommentEntity));
    }

    public PostCommentDto editPollPostComment(Long postId, Long commentId, PostCommentDto postCommentDto) throws IllegalAccessException {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String role = userDetails.getAuthorities().stream().findFirst().get().getAuthority();

        PollPostEntity pollPostEntity = pollPostRepository
                .findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("Poll post with id: " + postId + " was not found"));

        PostCommentEntity postCommentEntity = pollPostEntity
                .getBasePost()
                .getComments()
                .stream()
                .filter(comment -> comment.getId().equals(commentId))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("Comment with id: " + commentId + " was not found"));

        if (!Role.ROLE_ADMIN.name().equals(role)) {
            if (!postCommentEntity.getCommentCreator().getEmail().equals(userDetails.getUsername())) {
                throw new IllegalAccessException("You cannot edit another users comment");
            }
        }

        postCommentEntity.setText(postCommentDto.getText());

        return postMapper.postCommentEntityToDto(postCommentRepository.save(postCommentEntity));
    }

    public void deletePostComment(String postType, Long postId, Long commentId) throws IllegalAccessException {
        switch (postType) {
            case "simple" -> {
                deleteSimplePostComment(postId, commentId);
            }

            case "played-game" -> {
                deletePlayedGamePostComment(postId, commentId);
            }

            case "poll" -> {
                deletePollPostComment(postId, commentId);
            }

            default ->
                    throw new FieldValidationException(Map.of("postType", "This post type does not support comments"));
        }
    }

    public void deleteSimplePostComment(Long postId, Long commentId) throws IllegalAccessException {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String role = userDetails.getAuthorities().stream().findFirst().get().getAuthority();

        SimplePostEntity simplePostEntity = simplePostRepository
                .findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("Simple post with id: " + postId + " was not found"));

        PostCommentEntity postCommentEntity = simplePostEntity
                .getComments()
                .stream()
                .filter(comment -> comment.getId().equals(commentId))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("Comment with id: " + commentId + " was not found"));

        if (!Role.ROLE_ADMIN.name().equals(role)) {
            if (!postCommentEntity.getCommentCreator().getEmail().equals(userDetails.getUsername())) {
                throw new IllegalAccessException("You cannot delete another users comment");
            }
        }

        simplePostEntity.getComments().remove(postCommentEntity);
        simplePostRepository.save(simplePostEntity);
        postCommentRepository.delete(postCommentEntity);
    }

    public void deletePlayedGamePostComment(Long postId, Long commentId) throws IllegalAccessException {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String role = userDetails.getAuthorities().stream().findFirst().get().getAuthority();

        PlayedGamePostEntity playedGamePostEntity = playedGamePostRepository
                .findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("Played game post with id: " + postId + " was not found"));

        PostCommentEntity postCommentEntity = playedGamePostEntity
                .getBasePost()
                .getComments()
                .stream()
                .filter(comment -> comment.getId().equals(commentId))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("Comment with id: " + commentId + " was not found"));

        if (!Role.ROLE_ADMIN.name().equals(role)) {
            if (!postCommentEntity.getCommentCreator().getEmail().equals(userDetails.getUsername())) {
                throw new IllegalAccessException("You cannot delete another users comment");
            }
        }

        playedGamePostEntity.getBasePost().getComments().remove(postCommentEntity);
        playedGamePostRepository.save(playedGamePostEntity);
        postCommentRepository.delete(postCommentEntity);
    }

    public void deletePollPostComment(Long postId, Long commentId) throws IllegalAccessException {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String role = userDetails.getAuthorities().stream().findFirst().get().getAuthority();

        PollPostEntity pollPostEntity = pollPostRepository
                .findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("Poll post with id: " + postId + " was not found"));

        PostCommentEntity postCommentEntity = pollPostEntity
                .getBasePost()
                .getComments()
                .stream()
                .filter(comment -> comment.getId().equals(commentId))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("Comment with id: " + commentId + " was not found"));

        if (!Role.ROLE_ADMIN.name().equals(role)) {
            if (!postCommentEntity.getCommentCreator().getEmail().equals(userDetails.getUsername())) {
                throw new IllegalAccessException("You cannot delete another users comment");
            }
        }

        pollPostEntity.getBasePost().getComments().remove(postCommentEntity);
        pollPostRepository.save(pollPostEntity);
        postCommentRepository.delete(postCommentEntity);
    }

    public ReportDto reportPost(String postType, Long postId, ReportDto postReportDto) {
        SimplePostEntity simplePostEntity = null;

        switch (postType) {
            case "simple" -> {
                simplePostEntity = simplePostRepository
                        .findById(postId)
                        .orElseThrow(() -> new EntityNotFoundException("Simple post with id: " + postId + " was not found"));
            }

            case "played-game" -> {
                simplePostEntity = playedGamePostRepository
                        .findById(postId)
                        .orElseThrow(() -> new EntityNotFoundException("Played game post with id: " + postId + " was not found"))
                        .getBasePost();
            }

            case "poll" -> {
                simplePostEntity = pollPostRepository
                        .findById(postId)
                        .orElseThrow(() -> new EntityNotFoundException("Poll post with id: " + postId + " was not found"))
                        .getBasePost();
            }

            default -> throw new FieldValidationException(Map.of("postType", "Post type not supported"));
        }

        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        UserEntity userEntity = userService.getUserEntityByEmail(userDetails.getUsername());

        if (userAlreadyReportedPost(userEntity.getId(), simplePostEntity)) {
            throw new DuplicateReportException("You have already reported simple post with id: " + postId);
        }

        PostReportEntity postReportEntity = new PostReportEntity(null, postReportDto.getReason(), simplePostEntity, userEntity);

        return postMapper.postReportEntityToDto(postReportRepository.save(postReportEntity));
    }

    public ReportDto reportComment(String postType, Long postId, Long commentId, ReportDto commentReportDto) {
        SimplePostEntity simplePostEntity = null;

        switch (postType) {
            case "simple" -> {
                simplePostEntity = simplePostRepository
                        .findById(postId)
                        .orElseThrow(() -> new EntityNotFoundException("Simple post with id: " + postId + " was not found"));
            }

            case "played-game" -> {
                simplePostEntity = playedGamePostRepository
                        .findById(postId)
                        .orElseThrow(() -> new EntityNotFoundException("Played game post with id: " + postId + " was not found"))
                        .getBasePost();
            }

            case "poll" -> {
                simplePostEntity = pollPostRepository
                        .findById(postId)
                        .orElseThrow(() -> new EntityNotFoundException("Poll post with id: " + postId + " was not found"))
                        .getBasePost();
            }

            default -> throw new FieldValidationException(Map.of("postType", "Post type not supported"));
        }

        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        UserEntity userEntity = userService.getUserEntityByEmail(userDetails.getUsername());

        PostCommentEntity commentEntity = simplePostEntity
                .getComments()
                .stream()
                .filter(comment -> comment.getId().equals(commentId))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("Post does not have comment with id: " + commentId));

        if (userAlreadyReportedComment(userEntity.getId(), commentEntity)) {
            throw new DuplicateReportException("You have already reported comment with id: " + commentId);
        }

        PostCommentReportEntity commentReportEntity = new PostCommentReportEntity(null, commentReportDto.getReason(), commentEntity, userEntity);

        return postMapper.postCommentReportEntityToDto(postCommentReportRepository.save(commentReportEntity));
    }

    private boolean userAlreadyReportedComment(Long userId, PostCommentEntity commentEntity) {
        return commentEntity
                .getReports()
                .stream()
                .anyMatch(report -> report.getReporter().getId().equals(userId));
    }

    private boolean userAlreadyReportedPost(Long userId, SimplePostEntity simplePostEntity) {
        return simplePostEntity
                .getReports()
                .stream()
                .anyMatch(report -> report.getReporter().getId().equals(userId));
    }

    public void deletePostReports(String postType, Long postId) {
        SimplePostEntity simplePostEntity = null;

        switch (postType) {
            case "simple" -> {
                simplePostEntity = simplePostRepository
                        .findById(postId)
                        .orElseThrow(() -> new EntityNotFoundException("Simple post with id: " + postId + " was not found"));
            }

            case "played-game" -> {
                simplePostEntity = playedGamePostRepository
                        .findById(postId)
                        .orElseThrow(() -> new EntityNotFoundException("Played game post with id: " + postId + " was not found"))
                        .getBasePost();
            }

            case "poll" -> {
                simplePostEntity = pollPostRepository
                        .findById(postId)
                        .orElseThrow(() -> new EntityNotFoundException("Poll post with id: " + postId + " was not found"))
                        .getBasePost();
            }

            default ->
                    throw new FieldValidationException(Map.of("postType", "This post type does not support comments"));
        }

        simplePostEntity.getReports().clear();

        simplePostRepository.save(simplePostEntity);
    }

    public void deleteCommentReports(String postType, Long postId, Long commentId) {
        SimplePostEntity simplePostEntity = null;

        switch (postType) {
            case "simple" -> {
                simplePostEntity = simplePostRepository
                        .findById(postId)
                        .orElseThrow(() -> new EntityNotFoundException("Simple post with id: " + postId + " was not found"));
            }

            case "played-game" -> {
                simplePostEntity = playedGamePostRepository
                        .findById(postId)
                        .orElseThrow(() -> new EntityNotFoundException("Played game post with id: " + postId + " was not found"))
                        .getBasePost();
            }

            case "poll" -> {
                simplePostEntity = pollPostRepository
                        .findById(postId)
                        .orElseThrow(() -> new EntityNotFoundException("Poll post with id: " + postId + " was not found"))
                        .getBasePost();
            }

            default ->
                    throw new FieldValidationException(Map.of("postType", "This post type does not support comments"));
        }
        PostCommentEntity commentEntity = simplePostEntity
                .getComments()
                .stream()
                .filter(comment -> comment.getId().equals(commentId))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("Post does not have comment with id: " + commentId));

        commentEntity.getReports().clear();

        postCommentRepository.save(commentEntity);
    }
}
