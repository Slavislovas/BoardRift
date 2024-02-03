package com.socialnetwork.boardrift.repository.model.board_game;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.Data;

import java.util.Set;

@Data
@Entity
@Table(name = "board-game-categories")
public class BoardGameCategoryEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_board-game-category")
    private Long id;

    @Column(name = "name")
    private String name;

    @ManyToMany
    @JoinTable(
            name = "board-game_category",
            joinColumns = @JoinColumn(name = "id_board-game-category"),
            inverseJoinColumns = @JoinColumn(name = "id_board-game")
    )
    private Set<BoardGameEntity> boardGamesWithCategory;
}
