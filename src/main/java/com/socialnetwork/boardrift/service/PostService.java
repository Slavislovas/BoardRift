package com.socialnetwork.boardrift.service;

import com.socialnetwork.boardrift.enumeration.Role;
import com.socialnetwork.boardrift.repository.PlayedGamePostRepository;
import com.socialnetwork.boardrift.repository.PlayedGameRepository;
import com.socialnetwork.boardrift.repository.PollPostRepository;
import com.socialnetwork.boardrift.repository.PostCommentRepository;
import com.socialnetwork.boardrift.repository.PostLikeRepository;
import com.socialnetwork.boardrift.repository.SimplePostRepository;
import com.socialnetwork.boardrift.repository.model.UserEntity;
import com.socialnetwork.boardrift.repository.model.board_game.PlayedGameEntity;
import com.socialnetwork.boardrift.repository.model.post.PlayedGamePostEntity;
import com.socialnetwork.boardrift.repository.model.post.PollOptionEntity;
import com.socialnetwork.boardrift.repository.model.post.PollPostEntity;
import com.socialnetwork.boardrift.repository.model.post.Post;
import com.socialnetwork.boardrift.repository.model.post.PostCommentEntity;
import com.socialnetwork.boardrift.repository.model.post.PostLikeEntity;
import com.socialnetwork.boardrift.repository.model.post.SimplePostEntity;
import com.socialnetwork.boardrift.rest.model.BGGThingResponse;
import com.socialnetwork.boardrift.rest.model.user.UserRetrievalDto;
import com.socialnetwork.boardrift.rest.model.post.PostCommentDto;
import com.socialnetwork.boardrift.rest.model.post.PostCommentPageDto;
import com.socialnetwork.boardrift.rest.model.post.PostPageDto;
import com.socialnetwork.boardrift.rest.model.post.played_game_post.PlayedGamePostCreationDto;
import com.socialnetwork.boardrift.rest.model.post.played_game_post.PlayedGamePostRetrievalDto;
import com.socialnetwork.boardrift.rest.model.post.poll_post.PollOptionDto;
import com.socialnetwork.boardrift.rest.model.post.poll_post.PollOptionRetrievalDto;
import com.socialnetwork.boardrift.rest.model.post.poll_post.PollPostCreationDto;
import com.socialnetwork.boardrift.rest.model.post.poll_post.PollPostRetrievalDto;
import com.socialnetwork.boardrift.rest.model.post.simple_post.SimplePostCreationDto;
import com.socialnetwork.boardrift.rest.model.post.simple_post.SimplePostRetrievalDto;
import com.socialnetwork.boardrift.util.exception.DuplicatePollVoteException;
import com.socialnetwork.boardrift.util.exception.FieldValidationException;
import com.socialnetwork.boardrift.util.mapper.PostMapper;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
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

import static com.socialnetwork.boardrift.rest.model.post.played_game_post.PlayedGamePostCreationDto.SelectedPlayerDto;

@RequiredArgsConstructor
@Service
public class PostService {
    private final PlayedGamePostRepository playedGamePostRepository;
    private final PlayedGameRepository playedGameRepository;
    private final SimplePostRepository simplePostRepository;
    private final PollPostRepository pollPostRepository;
    private final PostCommentRepository postCommentRepository;
    private final PostLikeRepository postLikeRepository;
    private final UserService userService;
    private final BoardGameService boardGameService;
    private final PostMapper postMapper;

    public PlayedGamePostRetrievalDto createPlayedGamePost(PlayedGamePostCreationDto playedGamePostCreationDto) {
        UserDetails postCreatorDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        UserEntity postCreatorEntity = userService.getUserEntityByUsername(postCreatorDetails.getUsername());

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

        return postMapper.playedGamePostEntityToRetrievalDto(playedGamePostEntity);
    }

    private PlayedGamePostEntity createNoScorePlayedGamePost(PlayedGamePostCreationDto playedGamePostCreationDto, UserEntity postCreatorEntity, BGGThingResponse boardGame) {
        PlayedGameEntity postCreatorPlayedGameEntity = new PlayedGameEntity(null, playedGamePostCreationDto.getPlayedGameId(),
                0, playedGamePostCreationDto.getPostCreatorWon(),
                "no-score", new Date(), postCreatorEntity, null, new HashSet<>());

        for (SelectedPlayerDto player : playedGamePostCreationDto.getPlayers()) {
            UserEntity playerUserEntity = userService.getUserEntityById(player.getId());
            postCreatorPlayedGameEntity.addAssociatedPlay(new PlayedGameEntity(null, playedGamePostCreationDto.getPlayedGameId(),
                    0, player.getWon(), "no-score", new Date(),
                    playerUserEntity, null, new HashSet<>()));
        }

        postCreatorPlayedGameEntity = playedGameRepository.save(postCreatorPlayedGameEntity);

        SimplePostEntity basePost = new SimplePostEntity(null, playedGamePostCreationDto.getDescription(), new Date(), postCreatorEntity, new ArrayList<>(), new HashSet<>(), null, null, null);

        return new PlayedGamePostEntity(null, playedGamePostCreationDto.getPlayedGameId(),
                boardGame.getItems().get(0).getNames().get(0).getValue(), boardGame.getItems().get(0).getImage(),
                0, 0, 0.0, "no-score",
                basePost, postCreatorPlayedGameEntity);
    }

    private PlayedGamePostEntity createLowestScorePlayedGamePost(PlayedGamePostCreationDto playedGamePostCreationDto, UserEntity postCreatorEntity, BGGThingResponse boardGame) {
        int min = calculateLowestScore(playedGamePostCreationDto);
        int max = calculateHighestScore(playedGamePostCreationDto);
        Double average = calculateAverageScore(playedGamePostCreationDto);

        PlayedGameEntity postCreatorPlayedGameEntity = new PlayedGameEntity(null, playedGamePostCreationDto.getPlayedGameId(),
                playedGamePostCreationDto.getPostCreatorPoints(), playedGamePostCreationDto.getPostCreatorPoints().equals(min),
                "lowest-score", new Date(), postCreatorEntity, null, new HashSet<>());

        for (SelectedPlayerDto player : playedGamePostCreationDto.getPlayers()) {
            UserEntity playerUserEntity = userService.getUserEntityById(player.getId());
            postCreatorPlayedGameEntity.addAssociatedPlay(new PlayedGameEntity(null, playedGamePostCreationDto.getPlayedGameId(),
                    player.getPoints(), player.getPoints().equals(min), "lowest-score", new Date(),
                    playerUserEntity, null, new HashSet<>()));
        }

        postCreatorPlayedGameEntity = playedGameRepository.save(postCreatorPlayedGameEntity);

        SimplePostEntity basePost = new SimplePostEntity(null, playedGamePostCreationDto.getDescription(), new Date(), postCreatorEntity, new ArrayList<>(), new HashSet<>(), null, null, null);

        return new PlayedGamePostEntity(null, playedGamePostCreationDto.getPlayedGameId(),
                boardGame.getItems().get(0).getNames().get(0).getValue(), boardGame.getItems().get(0).getImage(),
                max, min, average, "lowest-score",
                basePost, postCreatorPlayedGameEntity);
    }

    private PlayedGamePostEntity createHighestScorePlayedGamePost(PlayedGamePostCreationDto playedGamePostCreationDto, UserEntity postCreatorEntity, BGGThingResponse boardGame) {
        int min = calculateLowestScore(playedGamePostCreationDto);
        int max = calculateHighestScore(playedGamePostCreationDto);
        Double average = calculateAverageScore(playedGamePostCreationDto);

        PlayedGameEntity postCreatorPlayedGameEntity = new PlayedGameEntity(null, playedGamePostCreationDto.getPlayedGameId(),
                playedGamePostCreationDto.getPostCreatorPoints(), playedGamePostCreationDto.getPostCreatorPoints().equals(max),
                "highest-score", new Date(), postCreatorEntity, null, new HashSet<>());

        for (SelectedPlayerDto player : playedGamePostCreationDto.getPlayers()) {
            UserEntity playerUserEntity = userService.getUserEntityById(player.getId());
            postCreatorPlayedGameEntity.addAssociatedPlay(new PlayedGameEntity(null, playedGamePostCreationDto.getPlayedGameId(),
                    player.getPoints(), player.getPoints().equals(max), "highest-score", new Date(),
                    playerUserEntity, null, new HashSet<>()));
        }

        postCreatorPlayedGameEntity = playedGameRepository.save(postCreatorPlayedGameEntity);

        SimplePostEntity basePost = new SimplePostEntity(null, playedGamePostCreationDto.getDescription(), new Date(), postCreatorEntity, new ArrayList<>(), new HashSet<>(), null, null, null);

        return new PlayedGamePostEntity(null, playedGamePostCreationDto.getPlayedGameId(),
                boardGame.getItems().get(0).getNames().get(0).getValue(), boardGame.getItems().get(0).getImage(),
                max, min, average, "highest-score",
                basePost, postCreatorPlayedGameEntity);
    }


    private Double calculateAverageScore(PlayedGamePostCreationDto playedGamePostCreationDto) {
        Double average = playedGamePostCreationDto.getPostCreatorPoints().doubleValue();

        for (SelectedPlayerDto player : playedGamePostCreationDto.getPlayers()) {
           average += player.getPoints().doubleValue();
        }

        return average / (playedGamePostCreationDto.getPlayers().size() + 1);
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
        UserEntity postCreatorEntity = userService.getUserEntityByUsername(postCreatorDetails.getUsername());

        SimplePostEntity simplePostEntity = new SimplePostEntity(null, simplePostCreationDto.getDescription(), new Date(), postCreatorEntity, new ArrayList<>(), new HashSet<>(), null, null, null);

        simplePostEntity = simplePostRepository.save(simplePostEntity);

        return postMapper.simplePostEntityToRetrievalDto(simplePostEntity);
    }

    public PollPostRetrievalDto createPollPost(PollPostCreationDto pollPostCreationDto) {
        UserDetails postCreatorDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        UserEntity postCreatorEntity = userService.getUserEntityByUsername(postCreatorDetails.getUsername());


        SimplePostEntity basePost = new SimplePostEntity(null, pollPostCreationDto.getDescription(), new Date(), postCreatorEntity, new ArrayList<>(), new HashSet<>(), null, null, null);

        PollPostEntity pollPost = new PollPostEntity(null, new HashSet<>(), basePost);

        Set<PollOptionEntity> options = new HashSet<>();

        for (PollOptionDto option : pollPostCreationDto.getOptions()) {
            options.add(new PollOptionEntity(null, option.getText(), pollPost, new HashSet<>()));
        }

        pollPost.setOptions(options);

        pollPost = pollPostRepository.save(pollPost);

        return postMapper.pollPostEntityToRetrievalDto(pollPost);
    }

    public void createPollVote(Long pollId, Long optionId) {
        UserDetails voterUserDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        UserEntity voterEntity = userService.getUserEntityByUsername(voterUserDetails.getUsername());

        PollPostEntity pollPost = pollPostRepository.findById(pollId).orElseThrow(() -> new EntityNotFoundException("Poll post with id: " + pollId + " was not found"));

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
        UserEntity commentCreatorEntity = userService.getUserEntityByUsername(commentCreatorUserDetails.getUsername());

        return switch (postType) {
            case "simple" -> createSimplePostComment(postId, commentDto, commentCreatorEntity);
            case "played-game" -> createPlayedGamePostComment(postId, commentDto, commentCreatorEntity);
            case "poll" -> createPollPostComment(postId, commentDto, commentCreatorEntity);
            default -> throw new FieldValidationException(Map.of("postType", "This post type does not support comments"));
        };
    }

    private PostCommentDto createPollPostComment(Long postId, PostCommentDto commentDto, UserEntity commentCreatorEntity) {
        PollPostEntity pollPostEntity = pollPostRepository
                .findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("Poll post with id: " + postId + " was not found"));

        PostCommentEntity postCommentEntity = postCommentRepository.save(new PostCommentEntity(null, commentDto.getText(), Instant.now(), pollPostEntity.getBasePost(), commentCreatorEntity));

        return postMapper.postCommentEntityToDto(postCommentEntity);
    }

    private PostCommentDto createPlayedGamePostComment(Long postId, PostCommentDto commentDto, UserEntity commentCreatorEntity) {
        PlayedGamePostEntity playedGamePostEntity = playedGamePostRepository
                .findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("Played game post with id: " + postId + " was not found"));

        PostCommentEntity postCommentEntity = postCommentRepository.save(new PostCommentEntity(null, commentDto.getText(), Instant.now(), playedGamePostEntity.getBasePost(), commentCreatorEntity));

        return postMapper.postCommentEntityToDto(postCommentEntity);
    }

    private PostCommentDto createSimplePostComment(Long postId, PostCommentDto commentDto, UserEntity commentCreatorEntity) {
        SimplePostEntity simplePostEntity = simplePostRepository
                .findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("Simple post with id: " + postId + " was not found"));

        PostCommentEntity postCommentEntity = postCommentRepository.save(new PostCommentEntity(null, commentDto.getText(), Instant.now(), simplePostEntity, commentCreatorEntity));

        return postMapper.postCommentEntityToDto(postCommentEntity);
    }

    public PostCommentPageDto getPostComments(String postType, Long postId, Integer page, Integer pageSize, HttpServletRequest request) {
        List<PostCommentDto> comments;

        switch (postType) {
            case "simple" -> {
                comments = getSimplePostComments(postId, page, pageSize);
            }

            case "played-game" -> {
                comments = getPlayedGamePostComments(postId, page, pageSize);
            }

            case "poll" -> {
                comments = getPollPostComments(postId, page, pageSize);
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

    private List<PostCommentDto> getPollPostComments(Long postId, Integer page, Integer pageSize) {
        PollPostEntity pollPostEntity = pollPostRepository
                .findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("Poll post with id: " + postId + " was not found"));

        return postCommentRepository
                .findAllBySimplePostId(pollPostEntity.getBasePost().getId(), PageRequest.of(page, pageSize, Sort.by("creationDate").descending()))
                .stream()
                .map(postMapper::postCommentEntityToDto).toList();
    }

    private List<PostCommentDto> getPlayedGamePostComments(Long postId, Integer page, Integer pageSize) {
        PlayedGamePostEntity playedGamePost = playedGamePostRepository
                .findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("Played game post with id: " + postId + " was not found"));

        return postCommentRepository
                .findAllBySimplePostId(playedGamePost.getBasePost().getId(), PageRequest.of(page, pageSize, Sort.by("creationDate").descending()))
                .stream()
                .map(postMapper::postCommentEntityToDto).toList();
    }

    private List<PostCommentDto> getSimplePostComments(Long postId, Integer page, Integer pageSize) {
        SimplePostEntity simplePost = simplePostRepository
                .findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("Simple post with id: " + postId + " was not found"));

                return postCommentRepository
                        .findAllBySimplePostId(simplePost.getId(), PageRequest.of(page, pageSize, Sort.by("creationDate").descending()))
                        .stream()
                        .map(postMapper::postCommentEntityToDto).toList();

    }

    public void likePost(String postType, Long postId) {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        UserEntity userEntity = userService.getUserEntityByUsername(userDetails.getUsername());

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
        UserEntity userEntity = userService.getUserEntityByUsername(userDetails.getUsername());

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

    private List<PollPostRetrievalDto> retrieveFeedPollPosts(UserEntity userEntity, Integer page, Integer pageSize) {
        PageRequest pageRequest = PageRequest.of(page, pageSize, Sort.by("basePost.creationDate").descending());

        return pollPostRepository.findAllByPostCreatorOrFriends(userEntity, pageRequest)
                .stream().map(pollPostEntity -> mapPollPostEntityToDtoWithAdditionalFields(userEntity.getId(), pollPostEntity)).toList();
    }

    private List<PlayedGamePostRetrievalDto> retrieveFeedPlayedGamePosts(UserEntity userEntity, Integer page, Integer pageSize) {
        PageRequest pageRequest = PageRequest.of(page, pageSize, Sort.by("basePost.creationDate").descending());

        return playedGamePostRepository.findAllByPostCreatorOrFriends(userEntity, pageRequest)
                .stream().map(playedGamePostEntity -> mapPlayedGamePostEntityToDtoWithAdditionalFields(userEntity.getId(), playedGamePostEntity)).toList();
    }

    private List<SimplePostRetrievalDto> retrieveFeedSimplePosts(UserEntity userEntity, Integer page, Integer pageSize) {
        PageRequest pageRequest = PageRequest.of(page, pageSize, Sort.by("creationDate").descending());

        return simplePostRepository.findAllByPostCreatorOrFriends(userEntity, pageRequest)
                .stream().map(simplePostEntity -> mapSimplePostEntityToDtoWithAdditionalFields(userEntity.getId(), simplePostEntity)).toList();
    }

    public PostPageDto getPostsByUserId(Long userId, Integer page, Integer pageSize, HttpServletRequest request) throws IllegalAccessException {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        UserEntity userEntity = userService.getUserEntityByUsername(userDetails.getUsername());

        if (!userEntity.getRole().equals(Role.ROLE_ADMINISTRATOR)) {
            if (!userEntity.getId().equals(userId)) {
                UserRetrievalDto targetUser = userService.getUserById(userId);
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

    private boolean userAlreadyFriend(UserEntity loggedInUserEntity, Long targetUserId) {
        boolean hasFriend =  loggedInUserEntity
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
                .stream().map(simplePostEntity -> mapSimplePostEntityToDtoWithAdditionalFields(loggedInUser.getId(), simplePostEntity)).toList();
    }

    private List<PollPostRetrievalDto> retrievePollPostsByUserId(Long userId, UserEntity loggedInUser, Integer page, Integer pageSize) {
        PageRequest pageRequest = PageRequest.of(page, pageSize, Sort.by("basePost.creationDate").descending());

        return pollPostRepository.findByBasePostPostCreatorId(userId, pageRequest)
                .stream().map(pollPostEntity -> mapPollPostEntityToDtoWithAdditionalFields(loggedInUser.getId(), pollPostEntity)).toList();
    }

    private List<PlayedGamePostRetrievalDto> retrievePlayedGamePostsByUserId(Long userId, UserEntity loggedInUser, Integer page, Integer pageSize) {
        PageRequest pageRequest = PageRequest.of(page, pageSize, Sort.by("basePost.creationDate").descending());

        return playedGamePostRepository.findByBasePostPostCreatorId(userId, pageRequest)
                .stream().map(playedGamePostEntity -> mapPlayedGamePostEntityToDtoWithAdditionalFields(loggedInUser.getId(), playedGamePostEntity)).toList();
    }

    private PlayedGamePostRetrievalDto mapPlayedGamePostEntityToDtoWithAdditionalFields(Long loggedInUserId, PlayedGamePostEntity playedGamePostEntity) {
        PlayedGamePostRetrievalDto playedGamePostRetrievalDto = postMapper.playedGamePostEntityToRetrievalDto(playedGamePostEntity);

        playedGamePostRetrievalDto.setAlreadyLiked(playedGamePostEntity
                .getBasePost()
                .getLikes()
                .stream()
                .anyMatch(postLikeEntity -> postLikeEntity.getLikeOwner().getId().equals(loggedInUserId)));

        return playedGamePostRetrievalDto;
    }

    private SimplePostRetrievalDto mapSimplePostEntityToDtoWithAdditionalFields(Long loggedInUserId, SimplePostEntity simplePostEntity) {
        SimplePostRetrievalDto simplePostRetrievalDto = postMapper.simplePostEntityToRetrievalDto(simplePostEntity);

        simplePostRetrievalDto.setAlreadyLiked(simplePostEntity
                .getLikes()
                .stream()
                .anyMatch(postLikeEntity -> postLikeEntity.getLikeOwner().getId().equals(loggedInUserId)));

        return simplePostRetrievalDto;
    }

    private PollPostRetrievalDto mapPollPostEntityToDtoWithAdditionalFields(Long loggedInUserId, PollPostEntity pollPostEntity) {
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

        return pollPostRetrievalDto;
    }
}
