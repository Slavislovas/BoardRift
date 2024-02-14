package com.socialnetwork.boardrift.repository.model.played_game_post;

import com.socialnetwork.boardrift.repository.model.UserEntity;
import com.socialnetwork.boardrift.repository.model.board_game.PlayedGameEntity;
import jakarta.persistence.CascadeType;
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
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "played-game-posts")
public class PlayedGamePostEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_played-game-post")
    private Long id;

    @Column(name = "id_bgg-game")
    private Long bggGameId;

    @Column(name = "description")
    private String description;

    @Column(name = "creation-date")
    private Date creationDate;

    @Column(name = "highest-score")
    private Integer highestScore;

    @Column(name = "lowest-score")
    private Integer lowestScore;

    @Column(name = "average-score")
    private Double averageScore;

    @Column(name = "stats-added")
    private Boolean statsAdded;

    @Column(name = "stats-string")
    private String statsString;

    @ManyToOne
    @JoinColumn(name = "id_post-creator")
    private UserEntity postCreator;

    @ManyToMany(cascade = {CascadeType.PERSIST})
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
}
