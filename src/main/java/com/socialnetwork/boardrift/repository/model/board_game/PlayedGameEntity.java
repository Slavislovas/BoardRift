package com.socialnetwork.boardrift.repository.model.board_game;

import com.socialnetwork.boardrift.repository.model.UserEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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

    @Column(name = "score")
    private Integer score;

    @Column(name = "won")
    private Boolean won;

    @Column(name = "scoring_system")
    private String scoringSystem;

    @ManyToOne
    @JoinColumn(name = "id_user")
    private UserEntity user;
}
