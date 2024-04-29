package com.socialnetwork.boardrift.repository.model.post;

import com.socialnetwork.boardrift.repository.model.user.UserEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
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
@Table(name = "simple_posts")
public class SimplePostEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "description")
    private String description;

    @Column(name = "creation_date")
    private Date creationDate;

    @ManyToOne
    @JoinColumn(name = "id_post_creator")
    private UserEntity postCreator;

    @OneToMany(mappedBy = "simplePost", orphanRemoval = true, cascade = CascadeType.ALL)
    private List<PostCommentEntity> comments;

    @OneToMany(mappedBy = "simplePost", cascade = CascadeType.ALL)
    private Set<PostLikeEntity> likes;

    @OneToOne(mappedBy = "basePost", cascade = CascadeType.ALL)
    private PlayedGamePostEntity childPlayedGamePost;

    @OneToOne(mappedBy = "basePost", cascade = CascadeType.ALL)
    private PollPostEntity childPollPost;

    @OneToMany(mappedBy = "reportedPost", orphanRemoval = true, cascade = CascadeType.ALL)
    private List<PostReportEntity> reports;
}
