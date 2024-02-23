package com.socialnetwork.boardrift.repository.model.post;

import com.socialnetwork.boardrift.repository.model.post.PostCommentEntity;
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
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "marketplace_posts")
public class MarketplacePostEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_marketplace_post")
    private Long id;

    @Column(name = "title")
    private String title;

    @Column(name = "description")
    private String description;

    @Column(name = "creation_date")
    private Instant creationDate;

    @Column(name = "price")
    private BigDecimal price;

    @ManyToOne
    @JoinColumn(name = "id_post_creator")
    private UserEntity postCreator;

    @OneToMany(mappedBy = "marketplacePost")
    private Set<PostCommentEntity> comments;

    @OneToMany(mappedBy = "marketplacePost")
    private Set<PostLikeEntity> likes;
}
