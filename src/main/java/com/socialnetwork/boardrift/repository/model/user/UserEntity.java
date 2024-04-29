package com.socialnetwork.boardrift.repository.model.user;

import com.socialnetwork.boardrift.enumeration.Role;
import com.socialnetwork.boardrift.enumeration.UserStatus;
import com.socialnetwork.boardrift.repository.model.ChatMessageEntity;
import com.socialnetwork.boardrift.repository.model.WarningEntity;
import com.socialnetwork.boardrift.repository.model.board_game.PlayedGameEntity;
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
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

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

    @Column(name = "date_of_birth")
    private String dateOfBirth;

    @Column(name = "password")
    private String password;

    @Column(name = "bio")
    private String bio;

    @Column(name = "country")
    private String country = "";

    @Column(name = "city")
    private String city = "";

    @Column(name = "public_posts")
    private Boolean publicPosts = true;

    @Column(name = "public_friends_list")
    private Boolean publicFriendsList = false;

    @Column(name = "public_plays")
    private Boolean publicPlays = false;

    @Column(name = "public_statistics")
    private Boolean publicStatistics = false;

    @Column(name = "profile_picture_url")
    private String profilePictureUrl;

    @Column(name = "role")
    private Role role = Role.ROLE_USER;

    @Column(name = "status")
    private UserStatus status = UserStatus.OFFLINE;

    @Column(name = "email_verified")
    private Boolean emailVerified = false;

    @OneToMany(mappedBy = "recipient", fetch = FetchType.EAGER, orphanRemoval = true, cascade = CascadeType.ALL)
    private List<WarningEntity> receivedWarnings;

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

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PlayedGameEntity> playedGames;

    @ManyToMany
    @JoinTable(
            name = "friend_invites",
            joinColumns = @JoinColumn(name = "id_sender", referencedColumnName = "id_user"),
            inverseJoinColumns = @JoinColumn(name = "id_receiver", referencedColumnName = "id_user")
    )
    private Set<UserEntity> sentFriendInvites;

    @ManyToMany
    @JoinTable(
            name = "friend_invites",
            joinColumns = @JoinColumn(name = "id_receiver", referencedColumnName = "id_user"),
            inverseJoinColumns = @JoinColumn(name = "id_sender", referencedColumnName = "id_user")
    )
    private Set<UserEntity> receivedFriendInvites;

    @OneToMany(mappedBy = "recipient")
    private List<ChatMessageEntity> receivedChatMessages;

    @OneToOne(mappedBy = "user", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private SuspensionEntity suspension;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(role.name()));
    }

    @Override
    public String getUsername() {
        return this.email;
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
        return suspension == null;
    }

    public void addSentFriendRequest(UserEntity receiver) {
        sentFriendInvites.add(receiver);
    }

    public void addReceivedFriendRequest(UserEntity sender) {
        receivedFriendInvites.add(sender);
    }

    public void addFriend(UserEntity sender) {
        friends.add(sender);
    }

    public void removeReceivedFriendRequest(UserEntity senderUserEntity) {
        receivedFriendInvites.remove(senderUserEntity);
    }

    public void removeFromFriendsList(Long friendId) {
        friends.removeIf(userEntity -> userEntity.getId().equals(friendId));
        friendOf.removeIf(userEntity -> userEntity.getId().equals(friendId));
    }
}