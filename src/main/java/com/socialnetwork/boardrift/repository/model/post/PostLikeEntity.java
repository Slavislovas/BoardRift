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
@Table(name = "post-likes")
public class PostLikeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_post-like")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "id_like-owner")
    private UserEntity likeOwner;

    @ManyToOne
    @JoinColumn(name = "id_simple-post")
    private SimplePostEntity simplePost;

    @ManyToOne
    @JoinColumn(name = "id_played-game-post")
    private PlayedGamePostEntity playedGamePost;

    @ManyToOne
    @JoinColumn(name = "id_marketplace-post")
    private MarketplacePostEntity marketplacePost;
}
