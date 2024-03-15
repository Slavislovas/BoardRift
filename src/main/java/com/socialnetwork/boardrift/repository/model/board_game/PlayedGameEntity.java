package com.socialnetwork.boardrift.repository.model.board_game;

import com.socialnetwork.boardrift.repository.model.UserEntity;
import com.socialnetwork.boardrift.repository.model.post.PlayedGamePostEntity;
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
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "played_games")
public class PlayedGameEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "id_bgg_game")
    private Long bggGameId;

    @Column(name = "game_name")
    private String gameName;

    @Column(name = "game_picture_url")
    private String gamePictureUrl;

    @Column(name = "game_category")
    private String gameCategory;

    @Column(name = "score")
    private Integer score;

    @Column(name = "won")
    private Boolean won;

    @Column(name = "scoring_system")
    private String scoringSystem;

    @Column(name = "creation_date")
    private Date creationDate;

    @ManyToOne
    @JoinColumn(name = "id_user")
    private UserEntity user;

    @OneToOne(mappedBy = "playedGame", cascade = {CascadeType.ALL})
    private PlayedGamePostEntity post;

    @ManyToMany(cascade = {CascadeType.ALL})
    @JoinTable(
            name = "associated_plays",
            joinColumns = @JoinColumn(name = "id_play"),
            inverseJoinColumns = @JoinColumn(name = "id_friend_play")
    )
    private List<PlayedGameEntity> associatedPlays;

    public void addAssociatedPlay(PlayedGameEntity playedGame) {
        associatedPlays.add(playedGame);
    }
}
