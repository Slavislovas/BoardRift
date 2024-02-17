package com.socialnetwork.boardrift.service;

import com.socialnetwork.boardrift.repository.PlayedGamePostRepository;
import com.socialnetwork.boardrift.repository.PlayedGameRepository;
import com.socialnetwork.boardrift.repository.PollPostRepository;
import com.socialnetwork.boardrift.repository.SimplePostRepository;
import com.socialnetwork.boardrift.repository.model.UserEntity;
import com.socialnetwork.boardrift.repository.model.board_game.PlayedGameEntity;
import com.socialnetwork.boardrift.repository.model.played_game_post.PlayedGamePostEntity;
import com.socialnetwork.boardrift.repository.model.poll_post.PollOptionEntity;
import com.socialnetwork.boardrift.repository.model.poll_post.PollPostEntity;
import com.socialnetwork.boardrift.repository.model.simple_post.SimplePostEntity;
import com.socialnetwork.boardrift.rest.model.BGGThingResponse;
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
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashSet;
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
    private final UserService userService;
    private final BoardGameService boardGameService;
    private final PostMapper postMapper;

    public PlayedGamePostRetrievalDto createPlayedGamePost(PlayedGamePostCreationDto playedGamePostCreationDto) {
        UserDetails postCreatorDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        UserEntity postCreatorEntity = userService.getUserEntityByUsername(postCreatorDetails.getUsername());

        Set<UserEntity> players = new HashSet<>();

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

        return postMapper.playedGamePostEntityToRetrievalDto(playedGamePostRepository.save(playedGamePostEntity));
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
                postCreatorEntity, plays, new HashSet<>(), new HashSet<>());
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
                postCreatorEntity, plays, new HashSet<>(), new HashSet<>());
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
                postCreatorEntity, plays, new HashSet<>(), new HashSet<>());
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

        SimplePostEntity simplePostEntity = new SimplePostEntity(null, simplePostCreationDto.getDescription(), new Date(), postCreatorEntity, new HashSet<>(), new HashSet<>());
        return postMapper.simplePostEntityToRetrievalDto(simplePostRepository.save(simplePostEntity));
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

        return postMapper.pollPostEntityToRetrievalDto(pollPostRepository.save(pollPost), false);
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
}
