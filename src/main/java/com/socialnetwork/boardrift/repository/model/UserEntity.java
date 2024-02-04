package com.socialnetwork.boardrift.repository.model;

import com.socialnetwork.boardrift.enumeration.Role;
import com.socialnetwork.boardrift.enumeration.UserStatus;
import com.socialnetwork.boardrift.repository.model.board_game.BoardGameEntity;
import com.socialnetwork.boardrift.repository.model.board_game.BoardGameReviewEntity;
import jakarta.persistence.CascadeType;
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
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users")
public class UserEntity implements UserDetails {
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
    private String dateOfBirth;

    @Column(name = "username")
    private String username;

    @Column(name = "password")
    private String password;

    @Column(name = "publicProfile")
    private Boolean publicProfile = true;

    @Column(name = "publicFriendsList")
    private Boolean publicFriendsList = false;

    @Column(name = "profilePictureUrl")
    private String profilePictureUrl = "";

    @Column(name = "role")
    private Role role = Role.ROLE_USER;

    @Column(name = "status")
    private UserStatus status = UserStatus.OFFLINE;

    @Column(name = "email-verified")
    private Boolean emailVerified = false;

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

//    @OneToMany(mappedBy = "sender")
//    private Set<FriendInviteEntity> sentFriendInvites;
//
//    @OneToMany(mappedBy = "receiver")
//    private Set<FriendInviteEntity> receivedFriendInvites;

    @ManyToMany
    @JoinTable(
            name = "friend-invites",
            joinColumns = @JoinColumn(name = "id_sender", referencedColumnName = "id_user"),
            inverseJoinColumns = @JoinColumn(name = "id_receiver", referencedColumnName = "id_user")
    )
    private Set<UserEntity> sentFriendInvites;

    @ManyToMany
    @JoinTable(
            name = "friend-invites",
            joinColumns = @JoinColumn(name = "id_receiver", referencedColumnName = "id_user"),
            inverseJoinColumns = @JoinColumn(name = "id_sender", referencedColumnName = "id_user")
    )
    private Set<UserEntity> receivedFriendInvites;


    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(role.name()));
    }

    @Override
    public String getUsername() {
        return this.username;
    }

    @Override
    public String getPassword() {
        return this.password;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    public void addSentFriendRequest(UserEntity receiver) {
        sentFriendInvites.add(receiver);
    }

    public void addReceivedFriendRequest(UserEntity sender) {
        receivedFriendInvites.add(sender);
    }
}