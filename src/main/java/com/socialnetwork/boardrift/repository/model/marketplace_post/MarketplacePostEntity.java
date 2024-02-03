package com.socialnetwork.boardrift.repository.model.marketplace_post;

import com.socialnetwork.boardrift.repository.model.board_game.BoardGameEntity;
import com.socialnetwork.boardrift.repository.model.UserEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Set;

@Data
@Entity
@Table(name = "marketplace-posts")
public class MarketplacePostEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_marketplace-post")
    private Long id;

    @Column(name = "title")
    private String title;

    @Column(name = "description")
    private String description;

    @Column(name = "creation-date")
    private Instant creationDate;

    @Column(name = "price")
    private BigDecimal price;

    @ManyToOne
    @JoinColumn(name = "id_post-creator")
    private UserEntity postCreator;

    @OneToMany(mappedBy = "commentedPost")
    private Set<MarketplacePostCommentEntity> comments;

    @OneToMany(mappedBy = "likedPost")
    private Set<MarketplacePostLikeEntity> likes;

    @ManyToOne
    @JoinColumn(name = "id_game-on-sale")
    private BoardGameEntity gameOnSale;
}
