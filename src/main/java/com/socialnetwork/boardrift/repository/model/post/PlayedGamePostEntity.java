package com.socialnetwork.boardrift.repository.model.post;

import com.socialnetwork.boardrift.repository.model.UserEntity;
import com.socialnetwork.boardrift.repository.model.board_game.PlayedGameEntity;
import com.socialnetwork.boardrift.repository.model.post.PostCommentEntity;
import com.socialnetwork.boardrift.repository.model.post.PostLikeEntity;
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
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;
import java.util.List;
import java.util.Set;

@Getter
@Setter
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

    @Column(name = "game-name")
    private String gameName;

    @Column(name = "game-picture-url")
    private String gamePictureUrl;

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

    @Column(name = "scoring-system")
    private String scoringSystem;

    @ManyToOne
    @JoinColumn(name = "id_post-creator")
    private UserEntity postCreator;

    @ManyToMany(cascade = {CascadeType.PERSIST})
    @JoinTable(
            name = "posted-plays",
            joinColumns = @JoinColumn(name = "id_played-game-post"),
            inverseJoinColumns = @JoinColumn(name = "id_posted-play")
    )
    private Set<PlayedGameEntity> plays;

    @OneToMany(mappedBy = "playedGamePost", cascade = {CascadeType.ALL})
    private List<PostCommentEntity> comments;

    @OneToMany(mappedBy = "playedGamePost", cascade = {CascadeType.ALL})
    private Set<PostLikeEntity> likes;

    public void addComment(PostCommentEntity playedGamePostCommentEntity) {
        comments.add(playedGamePostCommentEntity);
    }
}
