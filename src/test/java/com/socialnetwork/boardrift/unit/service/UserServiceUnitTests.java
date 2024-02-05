package com.socialnetwork.boardrift.unit.service;

import com.socialnetwork.boardrift.enumeration.Role;
import com.socialnetwork.boardrift.enumeration.UserStatus;
import com.socialnetwork.boardrift.repository.UserRepository;
import com.socialnetwork.boardrift.repository.model.EmailVerificationTokenEntity;
import com.socialnetwork.boardrift.repository.model.UserEntity;
import com.socialnetwork.boardrift.rest.model.FriendRequestDto;
import com.socialnetwork.boardrift.rest.model.UserRegistrationDto;
import com.socialnetwork.boardrift.rest.model.UserRetrievalDto;
import com.socialnetwork.boardrift.rest.model.UserRetrievalMinimalDto;
import com.socialnetwork.boardrift.service.EmailService;
import com.socialnetwork.boardrift.service.UserService;
import com.socialnetwork.boardrift.util.exception.DuplicateFriendRequestException;
import com.socialnetwork.boardrift.util.exception.EmailVerificationTokenExpiredException;
import com.socialnetwork.boardrift.util.exception.FieldValidationException;
import com.socialnetwork.boardrift.util.mapper.UserMapper;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithSecurityContext;
import org.springframework.security.test.context.support.WithUserDetails;

import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
public class UserServiceUnitTests {
    @Mock
    BCryptPasswordEncoder passwordEncoder;

    @Mock
    UserRepository userRepository;

    @Mock
    UserMapper userMapper;

    @Mock
    EmailService emailService;

    @InjectMocks
    UserService userService;

    UserEntity userEntity;
    UserEntity userEntity2;
    UserRegistrationDto userRegistrationDto;
    UserRetrievalDto userRetrievalDto;
    UserRetrievalMinimalDto userRetrievalMinimalDto;
    UserRetrievalMinimalDto userRetrievalMinimalDto2;
    EmailVerificationTokenEntity emailVerificationTokenEntity;
    SecurityContext securityContext = Mockito.mock(SecurityContext.class);
    Authentication authentication = Mockito.mock(Authentication.class);

    @BeforeEach
    void init(){
        userEntity = new UserEntity(1L, "Name", "Lastname", "email@gmail.com",
                "2001-11-16", "Username", "Password@123", true, false, "",
                Role.ROLE_USER, UserStatus.OFFLINE, false,  new HashSet<>(),
                new HashSet<>(),  new HashSet<>(),  new HashSet<>(),
                new HashSet<>(),  new HashSet<>());

        userEntity2 = new UserEntity(2L, "Name2", "Lastname2", "email2@gmail.com",
                "2001-11-16", "Username2", "Password@123", true, false, "",
                Role.ROLE_USER, UserStatus.OFFLINE, false,  new HashSet<>(),
                new HashSet<>(),  new HashSet<>(),  new HashSet<>(),
                new HashSet<>(),  new HashSet<>());

        userRegistrationDto = new UserRegistrationDto("Name", "Lastname",
                "email@gmail.com", "2001-11-16",
                "Username", "Password@123");

        userRetrievalDto = new UserRetrievalDto(1L, "Name", "Lastname", "email@gmail.com", "2001-11-16", "Username", "");

        userRetrievalMinimalDto = new UserRetrievalMinimalDto(1L, "Name", "Lastname", "2001-11-16", "");

        userRetrievalMinimalDto2 = new UserRetrievalMinimalDto(1L, "Name2", "Lastname2", "2001-11-16", "");

        emailVerificationTokenEntity = new EmailVerificationTokenEntity(1L, "token", userEntity, new Date(new Date().getTime() + 5000));
        securityContext.setAuthentication(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void createUserShouldSucceed() {
        Mockito.when(userRepository.findByUsername(any())).thenReturn(Optional.empty());
        Mockito.when(userRepository.findByEmail(any())).thenReturn(Optional.empty());
        Mockito.when(userMapper.registrationDtoToEntity(any())).thenReturn(userEntity);
        Mockito.when(userRepository.save(any())).thenReturn(userEntity);
        Mockito.when(userMapper.entityToRetrievalDto(any())).thenReturn(userRetrievalDto);
        Mockito.when(passwordEncoder.encode(any())).thenReturn("encodedPassword");
        Mockito.when(emailService.sendEmailVerification(any(), any())).thenReturn(true);

        UserRetrievalDto result = userService.createUser(userRegistrationDto, null);

        Assertions.assertEquals(userRetrievalDto, result);
    }

    @Test
    void createUserShouldFailWhenUsernameIsTaken() {
        Mockito.when(userRepository.findByUsername(any())).thenReturn(Optional.of(userEntity));
        Mockito.when(userRepository.findByEmail(any())).thenReturn(Optional.empty());

        Assertions.assertThrows(FieldValidationException.class, () -> userService.createUser(userRegistrationDto, new MockHttpServletRequest()));
    }

    @Test
    void createUserShouldFailWhenEmailIsTaken() {
        Mockito.when(userRepository.findByUsername(any())).thenReturn(Optional.empty());
        Mockito.when(userRepository.findByEmail(any())).thenReturn(Optional.of(userEntity));

        Assertions.assertThrows(FieldValidationException.class, () -> userService.createUser(userRegistrationDto, new MockHttpServletRequest()));
    }

    @Test
    void createUserShouldFailWhenUsernameAndEmailIsTaken() {
        Mockito.when(userRepository.findByUsername(any())).thenReturn(Optional.of(userEntity));
        Mockito.when(userRepository.findByEmail(any())).thenReturn(Optional.of(userEntity));

        Assertions.assertThrows(FieldValidationException.class, () -> userService.createUser(userRegistrationDto, new MockHttpServletRequest()));
    }

    @Test
    void confirmUserRegistrationShouldSucceed() {
        Mockito.doNothing().when(emailService).validateEmailVerificationTokenExpiration(any());
        Mockito.when(userRepository.save(any())).thenReturn(userEntity);
        Mockito.when(emailService.getEmailVerificationToken(any())).thenReturn(emailVerificationTokenEntity);

        userService.confirmUserRegistration("token");
    }

    @Test
    void confirmUserRegistrationShouldFailWhenTokenExpired() {
        Mockito.doThrow(EmailVerificationTokenExpiredException.class).when(emailService).validateEmailVerificationTokenExpiration(any());
        Mockito.when(emailService.getEmailVerificationToken(any())).thenReturn(emailVerificationTokenEntity);

        Assertions.assertThrows(EmailVerificationTokenExpiredException.class, () -> userService.confirmUserRegistration("token"));
    }

    @Test
    void sendFriendRequestShouldSucceed() {
        Mockito.when(userRepository.findByUsername(any())).thenReturn(Optional.of(userEntity));
        Mockito.when(userRepository.findById(2L)).thenReturn(Optional.of(userEntity2));
        Mockito.when(userRepository.save(userEntity)).thenReturn(userEntity);
        Mockito.when(userMapper.entityToMinimalRetrievalDto(userEntity)).thenReturn(userRetrievalMinimalDto);
        Mockito.when(userMapper.entityToMinimalRetrievalDto(userEntity2)).thenReturn(userRetrievalMinimalDto2);
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        Mockito.when(authentication.getPrincipal()).thenReturn(userEntity);

        FriendRequestDto friendRequestDto = userService.sendFriendRequest(2L);

        Assertions.assertEquals(userRetrievalMinimalDto, friendRequestDto.getSender());
        Assertions.assertEquals(userRetrievalMinimalDto2, friendRequestDto.getReceiver());
    }

    @Test
    void sendFriendRequestShouldFailWhenUserNotFoundByUsername() {
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        Mockito.when(authentication.getPrincipal()).thenReturn(userEntity);
        Mockito.when(userRepository.findByUsername(any())).thenReturn(Optional.empty());

        Assertions.assertThrows(EntityNotFoundException.class, () -> userService.sendFriendRequest(2L));
    }

    @Test
    void sendFriendRequestShouldFailWhenSendingFriendRequestToYourself() {
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        Mockito.when(authentication.getPrincipal()).thenReturn(userEntity);
        Mockito.when(userRepository.findByUsername(any())).thenReturn(Optional.of(userEntity));

        Assertions.assertThrows(DuplicateFriendRequestException.class, () -> userService.sendFriendRequest(1L));
    }

    @Test
    void sendFriendRequestShouldFailWhenFriendRequestAlreadySent() {
        userEntity.addSentFriendRequest(userEntity2);
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        Mockito.when(authentication.getPrincipal()).thenReturn(userEntity);
        Mockito.when(userRepository.findByUsername(any())).thenReturn(Optional.of(userEntity));

        Assertions.assertThrows(DuplicateFriendRequestException.class, () -> userService.sendFriendRequest(2L));
    }

    @Test
    void sendFriendRequestShouldFailWhenReceiverAlreadyFriend() {
        userEntity.addFriend(userEntity2);

        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        Mockito.when(authentication.getPrincipal()).thenReturn(userEntity);
        Mockito.when(userRepository.findByUsername(any())).thenReturn(Optional.of(userEntity));

        Assertions.assertThrows(DuplicateFriendRequestException.class, () -> userService.sendFriendRequest(2L));
    }

    @Test
    void sendFriendRequestShouldFailWhenReceiverAlreadySentFriendRequest() {
        userEntity.addReceivedFriendRequest(userEntity2);

        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        Mockito.when(authentication.getPrincipal()).thenReturn(userEntity);
        Mockito.when(userRepository.findByUsername(any())).thenReturn(Optional.of(userEntity));

        Assertions.assertThrows(DuplicateFriendRequestException.class, () -> userService.sendFriendRequest(2L));
    }

    @Test
    void acceptFriendRequestShouldSucceed() {
        userEntity.addReceivedFriendRequest(userEntity2);
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        Mockito.when(authentication.getPrincipal()).thenReturn(userEntity);
        Mockito.when(userRepository.findByUsername("Username")).thenReturn(Optional.of(userEntity));
        Mockito.when(userRepository.findById(2L)).thenReturn(Optional.of(userEntity2));
        Mockito.when(userRepository.save(userEntity)).thenReturn(userEntity);
        Mockito.when(userMapper.entityToMinimalRetrievalDto(userEntity2)).thenReturn(userRetrievalMinimalDto2);

        UserRetrievalMinimalDto result = userService.acceptFriendRequest(2L);
        Assertions.assertEquals(userRetrievalMinimalDto2, result);
    }

    @Test
    void acceptFriendRequestShouldFailWhenUserNotFoundByUsername() {
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        Mockito.when(authentication.getPrincipal()).thenReturn(userEntity);

        Assertions.assertThrows(EntityNotFoundException.class, () -> userService.acceptFriendRequest(2L));
    }

    @Test
    void acceptFriendRequestShouldFailWhenSenderHasNotSentFriendRequest() {
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        Mockito.when(authentication.getPrincipal()).thenReturn(userEntity);
        Mockito.when(userRepository.findByUsername("Username")).thenReturn(Optional.of(userEntity));

        Assertions.assertThrows(EntityNotFoundException.class, () -> userService.acceptFriendRequest(2L));
    }

    @Test
    void declineFriendRequestShouldSucceed() {
        userEntity.addReceivedFriendRequest(userEntity2);
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        Mockito.when(authentication.getPrincipal()).thenReturn(userEntity);
        Mockito.when(userRepository.findByUsername("Username")).thenReturn(Optional.of(userEntity));
        Mockito.when(userRepository.findById(2L)).thenReturn(Optional.of(userEntity2));
        Mockito.when(userRepository.save(userEntity)).thenReturn(userEntity);

        String result = userService.declineFriendRequest(2L);
        Assertions.assertEquals("Friend request declined successfully", result);
    }

    @Test
    void declineFriendRequestShouldFailWhenUserNotFoundByUsername() {
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        Mockito.when(authentication.getPrincipal()).thenReturn(userEntity);

        Assertions.assertThrows(EntityNotFoundException.class, () -> userService.declineFriendRequest(2L));
    }

    @Test
    void declineFriendRequestShouldFailWhenSenderHasNotSentFriendRequest() {
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        Mockito.when(authentication.getPrincipal()).thenReturn(userEntity);
        Mockito.when(userRepository.findByUsername("Username")).thenReturn(Optional.of(userEntity));

        Assertions.assertThrows(EntityNotFoundException.class, () -> userService.declineFriendRequest(2L));
    }

    @Test
    void getReceivedFriendRequestsShouldSucceed() {
        userEntity.addReceivedFriendRequest(userEntity2);
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        Mockito.when(authentication.getPrincipal()).thenReturn(userEntity);
        Mockito.when(userRepository.findByUsername("Username")).thenReturn(Optional.of(userEntity));
        Mockito.when(userMapper.entityToMinimalRetrievalDto(userEntity2)).thenReturn(userRetrievalMinimalDto2);

        Set<UserRetrievalMinimalDto> result = userService.getReceivedFriendRequests();
        Assertions.assertEquals(Set.of(userRetrievalMinimalDto2), result);
    }

    @Test
    void getReceivedFriendRequestShouldFailWhenUserNotFoundByUsername() {
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        Mockito.when(authentication.getPrincipal()).thenReturn(userEntity);

        Assertions.assertThrows(EntityNotFoundException.class, () -> userService.getReceivedFriendRequests());
    }

    @Test
    void getSentFriendRequestsShouldSucceed() {
        userEntity.addSentFriendRequest(userEntity2);
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        Mockito.when(authentication.getPrincipal()).thenReturn(userEntity);
        Mockito.when(userRepository.findByUsername("Username")).thenReturn(Optional.of(userEntity));
        Mockito.when(userMapper.entityToMinimalRetrievalDto(userEntity2)).thenReturn(userRetrievalMinimalDto2);

        Set<UserRetrievalMinimalDto> result = userService.getSentFriendRequests();
        Assertions.assertEquals(Set.of(userRetrievalMinimalDto2), result);
    }

    @Test
    void getSentFriendRequestShouldFailWhenUserNotFoundByUsername() {
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        Mockito.when(authentication.getPrincipal()).thenReturn(userEntity);

        Assertions.assertThrows(EntityNotFoundException.class, () -> userService.getSentFriendRequests());
    }

    @Test
    void getFriendsShouldSucceed() throws IllegalAccessException {
        userEntity.addFriend(userEntity2);
        Set<UserEntity> friendOfSet = userEntity.getFriendOf();
        friendOfSet.add(userEntity2);
        userEntity.setFriendOf(friendOfSet);
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        Mockito.when(authentication.getPrincipal()).thenReturn(userEntity);
        Mockito.when(userRepository.findById(1L)).thenReturn(Optional.of(userEntity));
        Mockito.when(userMapper.entityToMinimalRetrievalDto(userEntity2)).thenReturn(userRetrievalMinimalDto2);

        Set<UserRetrievalMinimalDto> result = userService.getFriends(1L);
        Assertions.assertEquals(Set.of(userRetrievalMinimalDto2), result);
    }

    @Test
    void getFriendsShouldSucceedWhenUserAdministrator() throws IllegalAccessException {
        userEntity.setRole(Role.ROLE_ADMINISTRATOR);
        userEntity2.addFriend(userEntity);
        Set<UserEntity> friendOfSet = userEntity2.getFriendOf();
        friendOfSet.add(userEntity);
        userEntity2.setFriendOf(friendOfSet);
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        Mockito.when(authentication.getPrincipal()).thenReturn(userEntity);
        Mockito.when(userRepository.findById(2L)).thenReturn(Optional.of(userEntity2));
        Mockito.when(userMapper.entityToMinimalRetrievalDto(userEntity)).thenReturn(userRetrievalMinimalDto);

        Set<UserRetrievalMinimalDto> result = userService.getFriends(2L);
        Assertions.assertEquals(Set.of(userRetrievalMinimalDto), result);
    }

    @Test
    void getFriendsShouldFailWhenTryingToGetOtherUserPrivateFriendsListAndNotAdministrator() {
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        Mockito.when(authentication.getPrincipal()).thenReturn(userEntity);
        Mockito.when(userRepository.findById(2L)).thenReturn(Optional.of(userEntity2));

        Assertions.assertThrows(IllegalAccessException.class, () -> userService.getFriends(2L));
    }
}
