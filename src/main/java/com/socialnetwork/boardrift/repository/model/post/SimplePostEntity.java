package com.socialnetwork.boardrift.repository.model.post;

import com.socialnetwork.boardrift.repository.model.post.PostCommentEntity;
import com.socialnetwork.boardrift.repository.model.UserEntity;
import com.socialnetwork.boardrift.repository.model.post.PostLikeEntity;
import jakarta.persistence.CascadeType;
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
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;
import java.util.List;
import java.util.Set;

@Getter
@Setter
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

    @OneToMany(mappedBy = "simplePost", cascade = {CascadeType.ALL})
    private List<PostCommentEntity> comments;

    @OneToMany(mappedBy = "simplePost", cascade = {CascadeType.ALL})
    private Set<PostLikeEntity> likes;

    public void addComment(PostCommentEntity simplePostCommentEntity) {
        comments.add(simplePostCommentEntity);
    }
}
