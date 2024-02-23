package com.socialnetwork.boardrift.repository.model.post;

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
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "post_likes")
public class PostLikeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_post_like")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "id_like_owner")
    private UserEntity likeOwner;

    @ManyToOne
    @JoinColumn(name = "id_simple_post")
    private SimplePostEntity simplePost;

    @ManyToOne
    @JoinColumn(name = "id_played_game_post")
    private PlayedGamePostEntity playedGamePost;

    @ManyToOne
    @JoinColumn(name = "id_marketplace_post")
    private MarketplacePostEntity marketplacePost;
}
