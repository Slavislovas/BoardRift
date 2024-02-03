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
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "board-game-reviews")
public class BoardGameReviewEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_board-game-review")
    private Long id;

    @Column(name = "rating")
    private Double rating;

    @Column(name = "description")
    private String description;

    @Column(name = "creation-date")
    private Instant creationDate;

    @ManyToOne
    @JoinColumn(name = "id_review-creator")
    private UserEntity reviewCreator;

    @ManyToOne
    @JoinColumn(name = "id_reviewed-board-game")
    private BoardGameEntity boardGame;
}
