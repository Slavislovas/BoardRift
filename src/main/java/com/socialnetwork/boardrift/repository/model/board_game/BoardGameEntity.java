package com.socialnetwork.boardrift.repository.model.board_game;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Data;

import java.util.Set;

@Data
@Entity
@Table(name = "board-games")
public class BoardGameEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_board-game")
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "year-published")
    private Integer yearPublished;

    @Column(name = "game-difficulty-rating")
    private Integer gameDifficultyRating;

    @Column(name = "minimum-players")
    private Integer minimumPlayers;

    @Column(name = "maximum-players")
    private Integer maximumPlayers;

    @Column(name = "image-path")
    private String imagePath;

    @OneToMany(mappedBy = "boardGame")
    private Set<BoardGameReviewEntity> reviews;

    @ManyToMany
    @JoinTable(
            name = "board-game_category",
            joinColumns = @JoinColumn(name = "id_board-game"),
            inverseJoinColumns = @JoinColumn(name = "id_board-game-category")
    )
    private Set<BoardGameCategoryEntity> categories;
}
