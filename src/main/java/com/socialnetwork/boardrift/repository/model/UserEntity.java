package com.socialnetwork.boardrift.repository.model;

import com.socialnetwork.boardrift.enumeration.Role;
import com.socialnetwork.boardrift.enumeration.UserStatus;
import com.socialnetwork.boardrift.repository.model.board_game.BoardGameEntity;
import com.socialnetwork.boardrift.repository.model.board_game.BoardGameReviewEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Data;

import java.time.LocalDate;
import java.util.Set;

@Data
@Entity
@Table(name = "users")
public class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_user")
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "lastname")
    private String lastname;

    @Column(name = "email")
    private String email;

    @Column(name = "dateOfBirth")
    private LocalDate dateOfBirth;

    @Column(name = "username")
    private String username;

    @Column(name = "password")
    private String password;

    @Column(name = "publicProfile")
    private Boolean publicProfile;

    @Column(name = "publicFriendsList")
    private Boolean publicFriendsList;

    @Column(name = "profilePictureUrl")
    private String profilePictureUrl;

    @Column(name = "role")
    private Role role;

    @Column(name = "status")
    private UserStatus status;

    @ManyToMany
    @JoinTable(
            name = "friends",
            joinColumns = @JoinColumn(name = "id_user"),
            inverseJoinColumns = @JoinColumn(name = "id_friend")
    )
    private Set<UserEntity> friends;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "friends",
            joinColumns = @JoinColumn(name = "id_friend"),
            inverseJoinColumns = @JoinColumn(name = "id_user")
    )
    private Set<UserEntity> friendOf;

    @ManyToMany
    @JoinTable(
            name = "playlist",
            joinColumns = @JoinColumn(name = "id_user"),
            inverseJoinColumns = @JoinColumn(name = "id_board-game")
    )
    private Set<BoardGameEntity> playlist;

    @OneToMany(mappedBy = "reviewCreator")
    private Set<BoardGameReviewEntity> createdBoardGameReviews;

    @OneToMany(mappedBy = "sender")
    private Set<FriendInviteEntity> sentFriendInvites;

    @OneToMany(mappedBy = "receiver")
    private Set<FriendInviteEntity> receivedFriendInvites;
}