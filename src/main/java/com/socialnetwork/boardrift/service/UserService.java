package com.socialnetwork.boardrift.service;

import com.socialnetwork.boardrift.enumeration.Role;
import com.socialnetwork.boardrift.repository.UserRepository;
import com.socialnetwork.boardrift.repository.model.EmailVerificationTokenEntity;
import com.socialnetwork.boardrift.repository.model.UserEntity;
import com.socialnetwork.boardrift.rest.model.FriendRequestDto;
import com.socialnetwork.boardrift.rest.model.UserRegistrationDto;
import com.socialnetwork.boardrift.rest.model.UserRetrievalDto;
import com.socialnetwork.boardrift.rest.model.UserRetrievalMinimalDto;
import com.socialnetwork.boardrift.util.exception.DuplicateFriendRequestException;
import com.socialnetwork.boardrift.util.exception.FieldValidationException;
import com.socialnetwork.boardrift.util.mapper.UserMapper;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final BCryptPasswordEncoder passwordEncoder;
    private final EmailService emailService;

    public UserEntity getUserEntityById(Long userId) {
        return userRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException("User with id: " + userId + " was not found"));
    }

    public UserEntity getUserEntityByUsername(String username) {
        return userRepository.findByUsername(username).orElseThrow(() -> new EntityNotFoundException("User with username: " + username + " was not found"));
    }

    public UserRetrievalDto createUser(UserRegistrationDto userRegistrationDto, HttpServletRequest servletRequest) {
        verifyIfUsernameAndEmailIsUnique(userRegistrationDto.getUsername(), userRegistrationDto.getEmail());

        UserEntity userEntity = userMapper.registrationDtoToEntity(userRegistrationDto);
        userEntity.setPassword(passwordEncoder.encode(userEntity.getPassword()));
        userEntity = userRepository.save(userEntity);

        emailService.sendEmailVerification(servletRequest, userEntity);

        return userMapper.entityToRetrievalDto(userEntity);
    }

    private void verifyIfUsernameAndEmailIsUnique(String username, String email) {
        Optional<UserEntity> optionalUserEntityByUsername = userRepository.findByUsername(username);
        Optional<UserEntity> optionalUserEntityByEmail = userRepository.findByEmail(email);

        Map<String, String> duplicateValueMap = new HashMap<>();

        optionalUserEntityByUsername.ifPresent(userEntity -> duplicateValueMap.put("username", "Username " + userEntity.getUsername() + " is taken"));
        optionalUserEntityByEmail.ifPresent(userEntity -> duplicateValueMap.put("email", "Email " + userEntity.getEmail() + " is taken"));

        if (!duplicateValueMap.isEmpty()) {
            throw new FieldValidationException(duplicateValueMap);
        }
    }

    public void confirmUserRegistration(String token) {
        EmailVerificationTokenEntity emailVerificationTokenEntity = emailService.getEmailVerificationToken(token);
        emailService.validateEmailVerificationTokenExpiration(emailVerificationTokenEntity);

        UserEntity userEntity = emailVerificationTokenEntity.getUserEntity();
        userEntity.setEmailVerified(true);
        userRepository.save(userEntity);
    }

    public FriendRequestDto sendFriendRequest(Long receiverId) {
        UserDetails senderUserDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        UserEntity senderUserEntity = userRepository.findByUsername(senderUserDetails.getUsername()).orElseThrow(() -> new EntityNotFoundException("User with username: " + senderUserDetails.getUsername() + " was not found"));

        validateFriendRequest(receiverId, senderUserEntity);

        UserEntity receiverUserEntity = userRepository.findById(receiverId).orElseThrow(() -> new EntityNotFoundException("User with id: " + receiverId + " was not found"));
        senderUserEntity.addSentFriendRequest(receiverUserEntity);
        userRepository.save(senderUserEntity);

        return new FriendRequestDto(userMapper.entityToMinimalRetrievalDto(senderUserEntity), userMapper.entityToMinimalRetrievalDto(receiverUserEntity));
    }

    private void validateFriendRequest(Long receiverId, UserEntity senderUserEntity) {
        if (senderUserEntity.getId().equals(receiverId)) {
            throw new DuplicateFriendRequestException("You cannot send a friend request to yourself");
        }

        if (friendRequestAlreadySent(senderUserEntity, receiverId)) {
            throw new DuplicateFriendRequestException("You have already sent a friend request to this user");
        }

        if (receiverAlreadyFriend(senderUserEntity, receiverId)) {
            throw new DuplicateFriendRequestException("This user is already your friend");
        }

        if (receiverAlreadySentFriendRequest(senderUserEntity, receiverId)) {
            throw new DuplicateFriendRequestException("The friend request receiver has already sent you a friend request");
        }
    }

    private boolean receiverAlreadyFriend(UserEntity senderUserEntity, Long receiverId) {
        boolean hasFriend =  senderUserEntity
                .getFriends()
                .stream()
                .anyMatch(friend -> friend.getId().equals(receiverId));

        boolean friendOf = senderUserEntity
                .getFriendOf()
                .stream()
                .anyMatch(friend -> friend.getId().equals(receiverId));

        return hasFriend || friendOf;
    }

    private boolean receiverAlreadySentFriendRequest(UserEntity senderUserEntity, Long receiverId) {
        return senderUserEntity
                .getReceivedFriendInvites()
                .stream()
                .anyMatch(userEntity -> userEntity.getId().equals(receiverId));
    }

    private boolean friendRequestAlreadySent(UserEntity senderUserDetails, Long receiverId) {
        return senderUserDetails
                .getSentFriendInvites()
                .stream()
                .anyMatch(userEntity -> userEntity.getId().equals(receiverId));
    }

    public UserRetrievalMinimalDto acceptFriendRequest(Long senderId) {
        UserDetails receiverUserDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        UserEntity receiverUserEntity = userRepository.findByUsername(receiverUserDetails.getUsername()).orElseThrow(() -> new EntityNotFoundException("User with username: " + receiverUserDetails.getUsername() + " was not found"));

        if (senderHasNotSentFriendRequest(receiverUserEntity, senderId)) {
            throw new EntityNotFoundException("Friend request from user with id: " + senderId + " not found");
        }

        UserEntity senderUserEntity = userRepository.findById(senderId).orElseThrow(() -> new EntityNotFoundException("Friend request sender with id: " + senderId + " was not found"));
        receiverUserEntity.addFriend(senderUserEntity);
        receiverUserEntity.removeReceivedFriendRequest(senderUserEntity);
        userRepository.save(receiverUserEntity);

        return userMapper.entityToMinimalRetrievalDto(senderUserEntity);
    }

    public String declineFriendRequest(Long senderId) {
        UserDetails receiverUserDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        UserEntity receiverUserEntity = userRepository.findByUsername(receiverUserDetails.getUsername()).orElseThrow(() -> new EntityNotFoundException("User with username: " + receiverUserDetails.getUsername() + " was not found"));

        if (senderHasNotSentFriendRequest(receiverUserEntity, senderId)) {
            throw new EntityNotFoundException("Friend request from user with id: " + senderId + " not found");
        }

        UserEntity senderUserEntity = userRepository.findById(senderId).orElseThrow(() -> new EntityNotFoundException("Friend request sender with id: " + senderId + " was not found"));
        receiverUserEntity.removeReceivedFriendRequest(senderUserEntity);
        userRepository.save(receiverUserEntity);

        return "Friend request declined successfully";
    }

    private boolean senderHasNotSentFriendRequest(UserEntity receiverUserEntity, Long senderId) {
        return receiverUserEntity
                .getReceivedFriendInvites()
                .stream()
                .noneMatch(userEntity -> userEntity.getId().equals(senderId));
    }

    public Set<UserRetrievalMinimalDto> getReceivedFriendRequests() {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        UserEntity userEntity = userRepository.findByUsername(userDetails.getUsername()).orElseThrow(() -> new EntityNotFoundException("User with username: " + userDetails.getUsername() + " was not found"));

        return userEntity.getReceivedFriendInvites()
                .stream()
                .map(userMapper::entityToMinimalRetrievalDto)
                .collect(Collectors.toSet());
    }

    public Set<UserRetrievalMinimalDto> getSentFriendRequests() {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        UserEntity userEntity = userRepository.findByUsername(userDetails.getUsername()).orElseThrow(() -> new EntityNotFoundException("User with username: " + userDetails.getUsername() + " was not found"));

        return userEntity.getSentFriendInvites()
                .stream()
                .map(userMapper::entityToMinimalRetrievalDto)
                .collect(Collectors.toSet());
    }

    public Set<UserRetrievalMinimalDto> getFriends(Long userId) throws IllegalAccessException {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String role = userDetails.getAuthorities().stream().findFirst().get().getAuthority();

        UserEntity userEntity = userRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException("User with id: " + userDetails.getUsername() + " was not found"));

        if (!Role.ROLE_ADMINISTRATOR.name().equals(role)) {
            if (!userEntity.getUsername().equals(userDetails.getUsername())) {
                if (!userEntity.getPublicFriendsList()) {
                    throw new IllegalAccessException("You cannot view this user's friend list");
                }
            }
        }

        Set<UserRetrievalMinimalDto> friends = userEntity.getFriends()
                .stream()
                .map(userMapper::entityToMinimalRetrievalDto).collect(Collectors.toSet());

        friends.addAll(
                userEntity.getFriendOf()
                        .stream()
                        .map(userMapper::entityToMinimalRetrievalDto).collect(Collectors.toSet())
        );

        return friends;
    }
}
