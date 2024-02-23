package com.socialnetwork.boardrift.repository.model.post;

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
@Table(name = "played_game_posts")
public class PlayedGamePostEntity implements Post{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_played_game_post")
    private Long id;

    @Column(name = "id_bgg_game")
    private Long bggGameId;

    @Column(name = "game_name")
    private String gameName;

    @Column(name = "game_picture_url")
    private String gamePictureUrl;

    @Column(name = "description")
    private String description;

    @Column(name = "creation_date")
    private Date creationDate;

    @Column(name = "highest_score")
    private Integer highestScore;

    @Column(name = "lowest_score")
    private Integer lowestScore;

    @Column(name = "average_score")
    private Double averageScore;

    @Column(name = "scoring_system")
    private String scoringSystem;

    @ManyToOne
    @JoinColumn(name = "id_post_creator")
    private UserEntity postCreator;

    @ManyToMany(cascade = {CascadeType.PERSIST})
    @JoinTable(
            name = "posted_plays",
            joinColumns = @JoinColumn(name = "id_played_game_post"),
            inverseJoinColumns = @JoinColumn(name = "id_posted_play")
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
