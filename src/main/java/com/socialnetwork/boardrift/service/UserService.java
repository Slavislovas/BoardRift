package com.socialnetwork.boardrift.service;

import com.socialnetwork.boardrift.repository.UserRepository;
import com.socialnetwork.boardrift.repository.model.EmailVerificationTokenEntity;
import com.socialnetwork.boardrift.repository.model.UserEntity;
import com.socialnetwork.boardrift.rest.model.FriendRequestDto;
import com.socialnetwork.boardrift.rest.model.UserRegistrationDto;
import com.socialnetwork.boardrift.rest.model.UserRetrievalDto;
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

@RequiredArgsConstructor
@Service
public class UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final BCryptPasswordEncoder passwordEncoder;
    private final EmailService emailService;

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

        return new FriendRequestDto(userMapper.entityToRetrievalDto(senderUserEntity), userMapper.entityToRetrievalDto(receiverUserEntity));
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
        return senderUserEntity
                .getFriends()
                .stream()
                .anyMatch(friend -> friend.getId().equals(receiverId));
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
}
