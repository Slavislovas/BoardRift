package com.socialnetwork.boardrift.repository.model.played_game_post;

import com.socialnetwork.boardrift.repository.model.board_game.BoardGameEntity;
import com.socialnetwork.boardrift.repository.model.UserEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Data;

import java.time.Instant;
import java.util.Set;

@Data
@Entity
@Table(name = "played-game-posts")
public class PlayedGamePostEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_played-game-post")
    private Long id;

    @Column(name = "title")
    private String title;

    @Column(name = "description")
    private String description;

    @Column(name = "creation-date")
    private Instant creationDate;

    @Column(name = "top-score")
    private Integer topScore;

    @Column(name = "lowest-score")
    private Integer lowestScore;

    @Column(name = "average-score")
    private Double averageScore;

    @Column(name = "time-played")
    private String timePlayed;

    @ManyToOne
    @JoinColumn(name = "id_post-creator")
    private UserEntity postCreator;

    @ManyToMany
    @JoinTable(
            name = "played-game-post-players",
            joinColumns = @JoinColumn(name = "id_user"),
            inverseJoinColumns = @JoinColumn(name = "id_played-game-post")
    )
    private Set<UserEntity> players;

    @OneToMany(mappedBy = "commentedPost")
    private Set<PlayedGamePostCommentEntity> comments;

    @OneToMany(mappedBy = "likedPost")
    private Set<PlayedGamePostLikeEntity> likes;

    @ManyToOne
    @JoinColumn(name = "id_played-game")
    private BoardGameEntity playedGame;
}
