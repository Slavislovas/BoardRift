package com.socialnetwork.boardrift.repository.model.played_game_post;

import com.socialnetwork.boardrift.repository.model.UserEntity;
import com.socialnetwork.boardrift.repository.model.played_game_post.PlayedGamePostEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "played-game-post-likes")
public class PlayedGamePostLikeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_played-game-post-like")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "id_like-owner")
    private UserEntity likeOwner;

    @ManyToOne
    @JoinColumn(name = "id_liked-post")
    private PlayedGamePostEntity likedPost;
}
