package com.socialnetwork.boardrift.repository.model.poll_post;

import com.socialnetwork.boardrift.repository.model.UserEntity;
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
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "poll-posts")
public class PollPostEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_poll-post")
    private Long id;

    @Column(name = "question")
    private String question;

    @Column(name = "creation-date")
    private Date creationDate;

    @ManyToOne
    @JoinColumn(name = "id_post-creator")
    private UserEntity postCreator;

    @OneToMany(mappedBy = "post", cascade = {CascadeType.ALL})
    private Set<PollOptionEntity> options;

    public void addVoteByOptionId(Long optionId, UserEntity voterEntity) {
        for (PollOptionEntity pollOption : options) {
            if (pollOption.getId().equals(optionId)) {
                pollOption.addVote(voterEntity);
            }
        }
    }
}
