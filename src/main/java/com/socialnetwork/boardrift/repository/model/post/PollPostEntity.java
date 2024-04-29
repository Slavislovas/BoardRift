package com.socialnetwork.boardrift.repository.model.post;

import com.socialnetwork.boardrift.repository.model.user.UserEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "poll_posts")
public class PollPostEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_poll_post")
    private Long id;

    @OneToMany(mappedBy = "post", cascade = {CascadeType.ALL}, orphanRemoval = true)
    private List<PollOptionEntity> options;

    @OneToOne(cascade = {CascadeType.ALL})
    @JoinColumn(name = "id_simple_post")
    private SimplePostEntity basePost;

    public void addVoteByOptionId(Long optionId, UserEntity voterEntity) {
        for (PollOptionEntity pollOption : options) {
            if (pollOption.getId().equals(optionId)) {
                pollOption.addVote(voterEntity);
            }
        }
    }
}
