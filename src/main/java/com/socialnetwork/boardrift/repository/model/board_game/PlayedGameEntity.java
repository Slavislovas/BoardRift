package com.socialnetwork.boardrift.repository.model.board_game;

import com.socialnetwork.boardrift.repository.model.user.UserEntity;
import com.socialnetwork.boardrift.repository.model.post.PlayedGamePostEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
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
import java.util.Objects;
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

    @Column(name = "include_to_statistics")
    private Boolean includeToStatistics;

    @ManyToOne
    @JoinColumn(name = "id_user")
    private UserEntity user;

    @OneToOne(mappedBy = "playedGame", fetch = FetchType.EAGER, cascade = {CascadeType.ALL})
    private PlayedGamePostEntity post;

    @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinTable(
            name = "associated_plays",
            joinColumns = @JoinColumn(name = "id_play"),
            inverseJoinColumns = @JoinColumn(name = "id_friend_play")
    )
    private Set<PlayedGameEntity> associatedPlays;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "associated_plays",
            joinColumns = @JoinColumn(name = "id_friend_play"),
            inverseJoinColumns = @JoinColumn(name = "id_play")
    )
    private Set<PlayedGameEntity> associatedWith;

    public void addAssociatedPlay(PlayedGameEntity playedGame) {
        if (!associatedPlays.contains(playedGame)) {
            associatedPlays.add(playedGame);
            playedGame.addAssociatedWithPlay(this);
        }
    }

    public void addAssociatedWithPlay(PlayedGameEntity playedGame) {
        if (!associatedWith.contains(playedGame)) {
            associatedWith.add(playedGame);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PlayedGameEntity that = (PlayedGameEntity) o;

        if (user != null && that.getUser() != null) {
            return Objects.equals(id, that.id) && Objects.equals(user.getId(), that.getUser().getId());
        }

        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {

        if (user != null) {
            return Objects.hash(id, user.getId());
        }

        return Objects.hash(id);
    }
}
