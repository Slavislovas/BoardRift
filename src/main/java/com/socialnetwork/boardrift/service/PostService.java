package com.socialnetwork.boardrift.service;

import com.socialnetwork.boardrift.repository.PlayedGamePostRepository;
import com.socialnetwork.boardrift.repository.model.UserEntity;
import com.socialnetwork.boardrift.repository.model.board_game.PlayedGameEntity;
import com.socialnetwork.boardrift.repository.model.played_game_post.PlayedGamePostEntity;
import com.socialnetwork.boardrift.rest.model.PlayedGamePostCreationDto;
import com.socialnetwork.boardrift.rest.model.PlayedGamePostRetrievalDto;
import com.socialnetwork.boardrift.util.mapper.PostMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static com.socialnetwork.boardrift.rest.model.PlayedGamePostCreationDto.*;

@RequiredArgsConstructor
@Service
public class PostService {
    private final PlayedGamePostRepository playedGamePostRepository;
    private final UserService userService;
    private final BoardGameService boardGameService;
    private final PostMapper postMapper;

    public PlayedGamePostRetrievalDto createPlayedGamePost(PlayedGamePostCreationDto playedGamePostCreationDto) {
        UserDetails postCreatorDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        UserEntity postCreatorEntity = userService.getUserEntityByUsername(postCreatorDetails.getUsername());

        Set<UserEntity> players = new HashSet<>();

        playedGamePostCreationDto.addPlayer(new SelectedPlayerDto(postCreatorEntity.getId(), postCreatorEntity.getName(), postCreatorEntity.getLastname(), playedGamePostCreationDto.getPostCreatorPlace(), playedGamePostCreationDto.getPostCreatorPoints()));

        if (playedGamePostCreationDto.getPlayers() != null) {
            for (SelectedPlayerDto player : playedGamePostCreationDto.getPlayers()) {
                UserEntity userEntity = userService.getUserEntityById(player.getId());
                if (playedGamePostCreationDto.getStatsAdded()) {
                    userEntity.addPlayedGame(new PlayedGameEntity(null, playedGamePostCreationDto.getPlayedGameId(), player.getPoints(), player.getPlace(), userEntity));
                }
                players.add(userEntity);
            }
        } else {
            players = new HashSet<>();
            players.add(postCreatorEntity);
        }

        Integer highestScore = playedGamePostCreationDto.getStatsAdded() ? calculateHighestScore(playedGamePostCreationDto) : 0;
        Integer lowestScore = playedGamePostCreationDto.getStatsAdded() ? calculateLowestScore(playedGamePostCreationDto) : 0;
        Double averageScore = playedGamePostCreationDto.getStatsAdded() ? calculateAverageScore(playedGamePostCreationDto) : 0.0;
        String stats = playedGamePostCreationDto.getStatsAdded() ? buildStatsString(playedGamePostCreationDto, postCreatorEntity) : "";

        PlayedGamePostEntity playedGamePostEntity = new PlayedGamePostEntity(null, playedGamePostCreationDto.getPlayedGameId(),
                playedGamePostCreationDto.getDescription(), new Date(),
                highestScore, lowestScore, averageScore, playedGamePostCreationDto.getStatsAdded(), stats,
                postCreatorEntity, players, new HashSet<>(), new HashSet<>());

        return postMapper.playedGamePostEntityToRetrievalDto(playedGamePostRepository.save(playedGamePostEntity));
    }

    private String buildStatsString(PlayedGamePostCreationDto playedGamePostCreationDto, UserEntity postCreatorEntity) {
        Set<SelectedPlayerDto> players = playedGamePostCreationDto.getPlayers();
        players.add(new SelectedPlayerDto(postCreatorEntity.getId(), postCreatorEntity.getName(), postCreatorEntity.getLastname(), playedGamePostCreationDto.getPostCreatorPlace(), playedGamePostCreationDto.getPostCreatorPoints()));

        StringBuilder statsBuilder = new StringBuilder();
        if (players.size() == 1) {
            players.forEach(player -> {
                        statsBuilder.append("Points: ").append(player.getPoints());
                        statsBuilder.append(String.format(" (%s %s)", player.getName(), player.getLastname()));
                        statsBuilder.append("\n");
                    });
        } else {
            players.stream()
                    .sorted(Comparator.comparingInt(SelectedPlayerDto::getPlace))
                    .forEach(player -> {
                        if (player.getPlace() != null) {
                            statsBuilder.append("Place: ").append(player.getPlace());
                        }
                        if (player.getPoints() != null) {
                            statsBuilder.append(", Points: ").append(player.getPoints());
                        }
                        statsBuilder.append(String.format(" (%s %s)", player.getName(), player.getLastname()));
                        statsBuilder.append("\n");
                    });
        }
        return statsBuilder.toString();
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
}
