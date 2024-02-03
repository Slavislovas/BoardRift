package com.socialnetwork.boardrift.repository.model.marketplace_post;

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
@Table(name = "marketplace-post-comments")
public class MarketplacePostCommentEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_marketplace-post-comment")
    private Long id;

    @Column(name = "text")
    private String text;

    @Column(name = "creation-date")
    private Instant creationDate;

    @ManyToOne
    @JoinColumn(name = "id_comment-creator")
    private UserEntity commentCreator;

    @ManyToOne
    @JoinColumn(name = "id_commented-marketplace-post")
    private MarketplacePostEntity commentedPost;
}
