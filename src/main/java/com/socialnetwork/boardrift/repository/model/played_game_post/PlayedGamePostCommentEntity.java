package com.socialnetwork.boardrift.repository.model.played_game_post;

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
@Table(name = "played-game-post-comments")
public class PlayedGamePostCommentEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_played-game-post-comment")
    private Long id;

    @Column(name = "text")
    private String text;

    @Column(name = "creation-date")
    private Instant creationDate;

    @ManyToOne
    @JoinColumn(name = "id_commented-played-game-post")
    private PlayedGamePostEntity commentedPost;

    @ManyToOne
    @JoinColumn(name = "id_comment-creator")
    private UserEntity commentCreator;
}
