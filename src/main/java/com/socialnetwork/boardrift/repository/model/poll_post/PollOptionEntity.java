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

import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "poll-post-options")
public class PollOptionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_option")
    private Long id;

    @Column(name = "text")
    private String text;

    @ManyToOne
    @JoinColumn(name = "id_poll-post")
    private PollPostEntity post;

    @OneToMany(mappedBy = "option", cascade = {CascadeType.ALL})
    private Set<PollOptionVoteEntity> votes;

    public void addVote(UserEntity voterEntity) {
        votes.add(new PollOptionVoteEntity(null, this, voterEntity));
    }
}
