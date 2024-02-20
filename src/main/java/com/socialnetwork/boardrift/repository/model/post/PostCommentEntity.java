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

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "post-comments")
public class PostCommentEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_simple-post-comment")
    private Long id;

    @Column(name = "text")
    private String text;

    @Column(name = "creation-date")
    private Instant creationDate;

    @ManyToOne
    @JoinColumn(name = "id_simple-post")
    private SimplePostEntity simplePost;

    @ManyToOne
    @JoinColumn(name = "id_played-game-post")
    private PlayedGamePostEntity playedGamePost;

    @ManyToOne
    @JoinColumn(name = "id_marketplace-post")
    private MarketplacePostEntity marketplacePost;

    @ManyToOne
    @JoinColumn(name = "id_comment-creator")
    private UserEntity commentCreator;
}
