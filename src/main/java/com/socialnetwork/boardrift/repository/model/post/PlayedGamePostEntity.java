package com.socialnetwork.boardrift.repository.model.post;

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
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "played_game_posts")
public class PlayedGamePostEntity implements Post {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_played_game_post")
    private Long id;

    @Column(name = "highest_score")
    private Integer highestScore;

    @Column(name = "lowest_score")
    private Integer lowestScore;

    @Column(name = "average_score")
    private Double averageScore;

    @Column(name = "scoring_system")
    private String scoringSystem;

    @OneToOne(cascade = {CascadeType.ALL})
    @JoinColumn(name = "id_simple_post")
    private SimplePostEntity basePost;

    @OneToOne(cascade = {CascadeType.ALL})
    @JoinColumn(name = "play")
    private PlayedGameEntity playedGame;

    @Override
    public Date getCreationDate() {
        return basePost.getCreationDate();
    }
}
