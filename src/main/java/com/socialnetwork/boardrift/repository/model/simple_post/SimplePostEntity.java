package com.socialnetwork.boardrift.repository.model.simple_post;

import com.socialnetwork.boardrift.repository.model.UserEntity;
import com.socialnetwork.boardrift.repository.model.board_game.PlayedGameEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "simple-posts")
public class SimplePostEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_simple-post")
    private Long id;

    @Column(name = "description")
    private String description;

    @Column(name = "creation-date")
    private Date creationDate;

    @ManyToOne
    @JoinColumn(name = "id_post-creator")
    private UserEntity postCreator;

    @OneToMany(mappedBy = "commentedPost")
    private Set<SimplePostCommentEntity> comments;

    @OneToMany(mappedBy = "likedPost")
    private Set<SimplePostLikeEntity> likes;
}
