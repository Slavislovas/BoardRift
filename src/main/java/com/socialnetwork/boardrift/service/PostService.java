package com.socialnetwork.boardrift.service;

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
import com.socialnetwork.boardrift.repository.model.post.PollPostEntity;
import com.socialnetwork.boardrift.repository.model.post.PostLikeEntity;
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
import java.util.Objects;
import java.util.Set;

import static com.socialnetwork.boardrift.rest.model.played_game_post.PlayedGamePostCreationDto.SelectedPlayerDto;

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

        playedGamePostCreationDto.addPlayer(new SelectedPlayerDto(postCreatorEntity.getId(), postCreatorEntity.getName(), postCreatorEntity.getLastname(), playedGamePostCreationDto.getPostCreatorWon(), playedGamePostCreationDto.getPostCreatorPoints()));

        BGGThingResponse boardGameResponse = boardGameService.getBoardGameById(playedGamePostCreationDto.getPlayedGameId());

        PlayedGamePostEntity playedGamePostEntity;

        switch (playedGamePostCreationDto.getScoringSystem()) {
            case "highest-score":
                playedGamePostEntity = createHighestScorePlayedGamePost(playedGamePostCreationDto, postCreatorEntity, boardGameResponse);
                break;
            case "lowest-score":
                playedGamePostEntity = createLowestScorePlayedGamePost(playedGamePostCreationDto, postCreatorEntity, boardGameResponse);
                break;
            case "no-score":
                playedGamePostEntity = createNoScorePlayedGamePost(playedGamePostCreationDto, postCreatorEntity, boardGameResponse);
                break;
            default:
                throw new FieldValidationException(Map.of("scoringSystem", "Invalid scoring system"));

        }

        playedGamePostEntity = playedGamePostRepository.save(playedGamePostEntity);

        addPostToFeedQueues("played-game", playedGamePostEntity.getId(), postCreatorEntity);

        return postMapper.playedGamePostEntityToRetrievalDto(playedGamePostEntity);
    }

    private void addPostToFeedQueues(String postType, Long id, UserEntity postCreatorEntity) {
        postCreatorEntity.addPostToFeedQueue(postType, id);
        userService.saveUserEntity(postCreatorEntity);

        for (UserEntity friend : postCreatorEntity.getFriends()) {
            friend.addPostToFeedQueue(postType, id);
            userService.saveUserEntity(friend);
        }
    }

    private PlayedGamePostEntity createNoScorePlayedGamePost(PlayedGamePostCreationDto playedGamePostCreationDto, UserEntity postCreatorEntity, BGGThingResponse boardGame) {
        Set<PlayedGameEntity> plays = new HashSet<>();

        for (SelectedPlayerDto player : playedGamePostCreationDto.getPlayers()) {
            UserEntity playerUserEntity = userService.getUserEntityById(player.getId());
            PlayedGameEntity playedGameEntity = playedGameRepository.save(new PlayedGameEntity(null, playedGamePostCreationDto.getPlayedGameId(), 0, player.getWon(), "no-score", playerUserEntity));

            playerUserEntity.addPlayedGame(playedGameEntity);
            plays.add(playedGameEntity);
        }

        return new PlayedGamePostEntity(null, playedGamePostCreationDto.getPlayedGameId(),
                boardGame.getItems().get(0).getNames().get(0).getValue(), boardGame.getItems().get(0).getImage(),
                playedGamePostCreationDto.getDescription(), new Date(),
                0, 0, 0.0, "no-score",
                postCreatorEntity, plays, new ArrayList<>(), new HashSet<>());
    }

    private PlayedGamePostEntity createLowestScorePlayedGamePost(PlayedGamePostCreationDto playedGamePostCreationDto, UserEntity postCreatorEntity, BGGThingResponse boardGame) {
        int min =  calculateLowestScore(playedGamePostCreationDto);
        int max = calculateHighestScore(playedGamePostCreationDto);
        Double average = calculateAverageScore(playedGamePostCreationDto);

        Set<PlayedGameEntity> plays = new HashSet<>();

        for (SelectedPlayerDto player : playedGamePostCreationDto.getPlayers()) {
            UserEntity playerUserEntity = userService.getUserEntityById(player.getId());

            if (player.getPoints().equals(min)) {
                player.setWon(true);
            } else {
                player.setWon(false);
            }

            PlayedGameEntity playedGameEntity = playedGameRepository.save(new PlayedGameEntity(null, playedGamePostCreationDto.getPlayedGameId(), player.getPoints(), player.getWon(), "lowest-score", playerUserEntity));
            playerUserEntity.addPlayedGame(playedGameEntity);
            plays.add(playedGameEntity);
        }

        return new PlayedGamePostEntity(null, playedGamePostCreationDto.getPlayedGameId(),
                boardGame.getItems().get(0).getNames().get(0).getValue(), boardGame.getItems().get(0).getImage(),
                playedGamePostCreationDto.getDescription(), new Date(),
                max, min, average, "lowest-score",
                postCreatorEntity, plays, new ArrayList<>(), new HashSet<>());
    }

    private PlayedGamePostEntity createHighestScorePlayedGamePost(PlayedGamePostCreationDto playedGamePostCreationDto, UserEntity postCreatorEntity, BGGThingResponse boardGame) {
        int min =  calculateLowestScore(playedGamePostCreationDto);
        int max = calculateHighestScore(playedGamePostCreationDto);
        Double average = calculateAverageScore(playedGamePostCreationDto);

        Set<PlayedGameEntity> plays = new HashSet<>();

        for (SelectedPlayerDto player : playedGamePostCreationDto.getPlayers()) {
            UserEntity playerUserEntity = userService.getUserEntityById(player.getId());

            if (player.getPoints().equals(max)) {
                player.setWon(true);
            } else {
                player.setWon(false);
            }

            PlayedGameEntity playedGameEntity = playedGameRepository.save(new PlayedGameEntity(null, playedGamePostCreationDto.getPlayedGameId(), player.getPoints(), player.getWon(), "lowest-score", playerUserEntity));
            playerUserEntity.addPlayedGame(playedGameEntity);
            plays.add(playedGameEntity);
        }

        return new PlayedGamePostEntity(null, playedGamePostCreationDto.getPlayedGameId(),
                boardGame.getItems().get(0).getNames().get(0).getValue(), boardGame.getItems().get(0).getImage(),
                playedGamePostCreationDto.getDescription(), new Date(),
                max, min, average, "highest-score",
                postCreatorEntity, plays, new ArrayList<>(), new HashSet<>());
    }


    private Double calculateAverageScore(PlayedGamePostCreationDto playedGamePostCreationDto) {
        return playedGamePostCreationDto.getPlayers().stream()
                .mapToDouble(player -> player.getPoints() != null ? player.getPoints() : -1.0)
                .average()
                .orElse(-1.0);
    }

    private Integer calculateLowestScore(PlayedGamePostCreationDto playedGamePostCreationDto) {
        return playedGamePostCreationDto.getPlayers().stream()
                .map(SelectedPlayerDto::getPoints)
                .filter(Objects::nonNull)
                .mapToInt(Integer::intValue)
                .min()
                .orElse(-1);
    }

    private Integer calculateHighestScore(PlayedGamePostCreationDto playedGamePostCreationDto) {
        return playedGamePostCreationDto.getPlayers().stream()
                .map(SelectedPlayerDto::getPoints)
                .filter(Objects::nonNull)
                .mapToInt(Integer::intValue)
                .max()
                .orElse(-1);
    }

    public SimplePostRetrievalDto createSimplePost(SimplePostCreationDto simplePostCreationDto) {
        UserDetails postCreatorDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        UserEntity postCreatorEntity = userService.getUserEntityByUsername(postCreatorDetails.getUsername());

        SimplePostEntity simplePostEntity = new SimplePostEntity(null, simplePostCreationDto.getDescription(), new Date(), postCreatorEntity, new ArrayList<>(), new HashSet<>());

        simplePostEntity = simplePostRepository.save(simplePostEntity);

        addPostToFeedQueues("simple", simplePostEntity.getId(), postCreatorEntity);

        return postMapper.simplePostEntityToRetrievalDto(simplePostEntity);
    }

    public PollPostRetrievalDto createPollPost(PollPostCreationDto pollPostCreationDto) {
        UserDetails postCreatorDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        UserEntity postCreatorEntity = userService.getUserEntityByUsername(postCreatorDetails.getUsername());

        PollPostEntity pollPost = new PollPostEntity(null, pollPostCreationDto.getQuestion(), new Date(), postCreatorEntity, new HashSet<>());

        Set<PollOptionEntity> options = new HashSet<>();

        for (PollOptionDto option : pollPostCreationDto.getOptions()) {
            options.add(new PollOptionEntity(null, option.getText(), pollPost, new HashSet<>()));
        }

        pollPost.setOptions(options);

        pollPost = pollPostRepository.save(pollPost);

        addPostToFeedQueues("poll", pollPost.getId(), postCreatorEntity);

        return postMapper.pollPostEntityToRetrievalDto(pollPost, false);
    }

    public void createPollVote(Long pollId, Long optionId) {
        UserDetails voterUserDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        UserEntity voterEntity = userService.getUserEntityByUsername(voterUserDetails.getUsername());

        PollPostEntity pollPost = pollPostRepository.findById(pollId).orElseThrow(() -> new EntityNotFoundException("Poll post with id: " + pollId + " was not found"));

        if (userAlreadyVoted(pollPost, voterEntity)) {
            throw new DuplicatePollVoteException("User with id: " + voterEntity.getId() + " has already voted on this poll");
        }

        pollPost.addVoteByOptionId(optionId, voterEntity);

        pollPostRepository.save(pollPost);
    }

    private boolean userAlreadyVoted(PollPostEntity pollPost, UserEntity voterEntity) {
        return pollPost.getOptions()
                .stream()
                .anyMatch(option -> option.getVotes()
                        .stream()
                        .anyMatch(vote -> vote.getVoter().getId().equals(voterEntity.getId())));
    }

    public PostCommentDto createPostComment(String postType, Long postId, PostCommentDto commentDto) {
        UserDetails commentCreatorUserDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        UserEntity commentCreatorEntity = userService.getUserEntityByUsername(commentCreatorUserDetails.getUsername());

        return switch (postType) {
            case "simple" -> createSimplePostComment(postId, commentDto, commentCreatorEntity);
            case "played-game" -> createPlayedGamePostComment(postId, commentDto, commentCreatorEntity);
            default ->
                    throw new FieldValidationException(Map.of("postType", "This post type does not support comments"));
        };
    }

    private PostCommentDto createPlayedGamePostComment(Long postId, PostCommentDto commentDto, UserEntity commentCreatorEntity) {
        PlayedGamePostEntity playedGamePostEntity = playedGamePostRepository
                .findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("Played game post with id: " + postId + " was not found"));

        PostCommentEntity postCommentEntity = postCommentRepository.save(new PostCommentEntity(null, commentDto.getText(), Instant.now(), null, playedGamePostEntity, null, commentCreatorEntity));

        return postMapper.postCommentEntityToDto(postCommentEntity);
    }

    private PostCommentDto createSimplePostComment(Long postId, PostCommentDto commentDto, UserEntity commentCreatorEntity) {
        SimplePostEntity simplePostEntity = simplePostRepository
                .findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("Simple post with id: " + postId + " was not found"));

        PostCommentEntity postCommentEntity = postCommentRepository.save(new PostCommentEntity(null, commentDto.getText(), Instant.now(), simplePostEntity, null, null, commentCreatorEntity));

        return postMapper.postCommentEntityToDto(postCommentEntity);
    }

    public PostCommentPageDto getPostComments(String postType, Long postId, Integer page, Integer pageSize, HttpServletRequest request) {
        List<PostCommentDto> comments;

        switch (postType) {
            case "simple" -> {
                comments = postCommentRepository
                        .findAllBySimplePostId(postId, PageRequest.of(page, pageSize, Sort.by("creationDate").descending()))
                        .stream()
                        .map(postMapper::postCommentEntityToDto).toList();
            }

            case "played-game" -> {
                comments = postCommentRepository
                        .findAllByPlayedGamePostId(postId, PageRequest.of(page, pageSize, Sort.by("creationDate").descending()))
                        .stream()
                        .map(postMapper::postCommentEntityToDto).toList();
            }

            default ->
                    throw new FieldValidationException(Map.of("postType", "This post type does not support comments"));
        }

        String nexPageUrl = comments.size() == pageSize ? String.format("%s%s?page=%d&pageSize=%d",
                ServletUriComponentsBuilder.fromCurrentContextPath().toUriString(),
                request.getServletPath(),
                page + 1,
                pageSize) : null;

        return new PostCommentPageDto(nexPageUrl, comments);
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

            default -> throw new FieldValidationException(Map.of("postType", "This post type does not support likes"));
        }
    }

    private void likePlayedGamePost(Long postId, UserEntity userEntity) {
        postLikeRepository
                .findByPlayedGamePostId(postId)
                .ifPresentOrElse(
                        postLikeRepository::delete,
                        () -> {
                            PlayedGamePostEntity playedGamePost = playedGamePostRepository.findById(postId).orElseThrow(() -> new EntityNotFoundException("Played game post with id: " + postId + " was not found"));
                            postLikeRepository.save(new PostLikeEntity(null, userEntity, null, playedGamePost, null));
                        }
                );
    }

    private void likeSimplePost(Long postId, UserEntity userEntity) {
        postLikeRepository
                .findBySimplePostId(postId)
                .ifPresentOrElse(
                        postLikeRepository::delete,
                        () -> {
                            SimplePostEntity simplePost = simplePostRepository.findById(postId).orElseThrow(() -> new EntityNotFoundException("Simple post with id: " + postId + " was not found"));
                            postLikeRepository.save(new PostLikeEntity(null, userEntity, simplePost, null, null));
                        }
                );
    }

    public List<Object> getFeed(Integer feedSize) {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        UserEntity userEntity =userService.getUserEntityByUsername(userDetails.getUsername());

        List<Object> objectFeed = new ArrayList<>();

        for (int i = 0; i < feedSize; i++) {
            String feedEntryString = userEntity.getFeedQueue().poll();

            if (feedEntryString == null) {
                break;
            }

            String[] splitFeedEntry = feedEntryString.split(" ");
            String postType = splitFeedEntry[0];
            Long postId = Long.parseLong(splitFeedEntry[1]);

            switch (postType) {
                case "simple" -> objectFeed.add(getSimplePostById(postId));
                case "played-game" -> objectFeed.add(getPlayedGamePostById(postId));
                case "poll" -> objectFeed.add(getPollPostById(postId));
            }
        }

        return objectFeed;
    }

    public SimplePostRetrievalDto getSimplePostById(Long postId) {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        UserEntity userEntity = userService.getUserEntityByUsername(userDetails.getUsername());

        SimplePostEntity simplePost = simplePostRepository.findById(postId).orElseThrow(() -> new EntityNotFoundException("Simple post with id: " + postId + " was not found"));
        SimplePostRetrievalDto simplePostRetrievalDto = postMapper.simplePostEntityToRetrievalDto(simplePost);

        simplePostRetrievalDto.setAlreadyLiked(simplePost
                .getLikes()
                .stream()
                .anyMatch(postLikeEntity -> postLikeEntity.getLikeOwner().getId().equals(userEntity.getId())));

        return simplePostRetrievalDto;
    }

    public PlayedGamePostRetrievalDto getPlayedGamePostById(Long postId) {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        UserEntity userEntity = userService.getUserEntityByUsername(userDetails.getUsername());

        PlayedGamePostEntity playedGamePost = playedGamePostRepository.findById(postId).orElseThrow(() -> new EntityNotFoundException("Played game post with id: " + postId + " was not found"));
        PlayedGamePostRetrievalDto playedGamePostRetrievalDto = postMapper.playedGamePostEntityToRetrievalDto(playedGamePost);

        playedGamePostRetrievalDto.setAlreadyLiked(playedGamePost
                .getLikes()
                .stream()
                .anyMatch(postLikeEntity -> postLikeEntity.getLikeOwner().getId().equals(userEntity.getId())));

        return playedGamePostRetrievalDto;
    }

    public PollPostRetrievalDto getPollPostById(Long postId) {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        UserEntity userEntity = userService.getUserEntityByUsername(userDetails.getUsername());

        PollPostEntity pollPost = pollPostRepository.findById(postId).orElseThrow(() -> new EntityNotFoundException("Poll post with id: " + postId + " was not found"));
        return postMapper.pollPostEntityToRetrievalDto(pollPost, userAlreadyVoted(pollPost, userEntity));
    }
}
