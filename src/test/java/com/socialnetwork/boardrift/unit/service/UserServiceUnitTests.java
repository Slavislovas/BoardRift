package com.socialnetwork.boardrift.unit.service;

import com.socialnetwork.boardrift.enumeration.Role;
import com.socialnetwork.boardrift.enumeration.UserStatus;
import com.socialnetwork.boardrift.enumeration.VerificationTokenType;
import com.socialnetwork.boardrift.repository.PlayedGameRepository;
import com.socialnetwork.boardrift.repository.UserRepository;
import com.socialnetwork.boardrift.repository.model.VerificationTokenEntity;
import com.socialnetwork.boardrift.repository.model.board_game.PlayedGameEntity;
import com.socialnetwork.boardrift.repository.model.post.PlayedGamePostEntity;
import com.socialnetwork.boardrift.repository.model.post.SimplePostEntity;
import com.socialnetwork.boardrift.repository.model.user.SuspensionEntity;
import com.socialnetwork.boardrift.repository.model.user.UserEntity;
import com.socialnetwork.boardrift.rest.model.BGGThingResponse;
import com.socialnetwork.boardrift.rest.model.FriendRequestDto;
import com.socialnetwork.boardrift.rest.model.PlayedGamePageDto;
import com.socialnetwork.boardrift.rest.model.post.played_game_post.PlayedGameDto;
import com.socialnetwork.boardrift.rest.model.statistics.UserStatisticsDto;
import com.socialnetwork.boardrift.rest.model.user.UserEditDto;
import com.socialnetwork.boardrift.rest.model.user.UserRegistrationDto;
import com.socialnetwork.boardrift.rest.model.user.UserRetrievalDto;
import com.socialnetwork.boardrift.rest.model.user.UserRetrievalMinimalDto;
import com.socialnetwork.boardrift.service.AwsService;
import com.socialnetwork.boardrift.service.BoardGameService;
import com.socialnetwork.boardrift.service.ChatRoomService;
import com.socialnetwork.boardrift.service.EmailService;
import com.socialnetwork.boardrift.service.NotificationService;
import com.socialnetwork.boardrift.service.UserService;
import com.socialnetwork.boardrift.util.exception.DuplicateFriendRequestException;
import com.socialnetwork.boardrift.util.exception.EmailVerificationTokenExpiredException;
import com.socialnetwork.boardrift.util.exception.FieldValidationException;
import com.socialnetwork.boardrift.util.mapper.PlayedGameMapper;
import com.socialnetwork.boardrift.util.mapper.UserMapper;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceUnitTests {
    @Mock
    AwsService awsService;
    @Mock
    SimpMessagingTemplate messagingTemplate;
    @Mock
    BCryptPasswordEncoder passwordEncoder;
    @Mock
    PlayedGameRepository playedGameRepository;
    @Mock
    BoardGameService boardGameService;
    @Mock
    PlayedGameMapper playedGameMapper;
    @Mock
    UserRepository userRepository;

    @Mock
    UserMapper userMapper;

    @Mock
    EmailService emailService;

    @Mock
    ChatRoomService chatRoomService;

    @Mock
    NotificationService notificationService;

    @InjectMocks
    UserService userService;

    UserEntity userEntity;
    UserEntity userEntity2;
    UserRegistrationDto userRegistrationDto;
    UserRetrievalDto userRetrievalDto;
    UserRetrievalMinimalDto userRetrievalMinimalDto;
    UserRetrievalMinimalDto userRetrievalMinimalDto2;
    VerificationTokenEntity emailVerificationTokenEntity;
    PlayedGameEntity playedGameEntity;
    PlayedGameEntity playedGameEntity2;
    PlayedGameDto playedGameDto;
    PlayedGameDto playedGameDto2;
    UserEditDto userEditDto;
    BGGThingResponse boardGame;
    PlayedGamePostEntity playedGamePostEntity;
    SimplePostEntity simplePostEntity;
    SecurityContext securityContext = Mockito.mock(SecurityContext.class);
    Authentication authentication = Mockito.mock(Authentication.class);

    @BeforeEach
    void init(){
        userEntity = new UserEntity(1L, "Name", "Lastname", "email@gmail.com",
                "2001-11-16", "Password@123", "", "", "", true, false, false, false, "",
                Role.ROLE_USER, false, new ArrayList<>(),  new HashSet<>(),
                new HashSet<>(),  new ArrayList<>(),  new HashSet<>(), new HashSet<>(), new ArrayList<>(), null);

        userEntity2 = new UserEntity(2L, "Name2", "Lastname2", "email2@gmail.com",
                "2001-11-16", "Password@123", "", "", "", true, false, false, false, "",
                Role.ROLE_USER, false, new ArrayList<>(),  new HashSet<>(),
                new HashSet<>(),  new ArrayList<>(),  new HashSet<>(), new HashSet<>(), new ArrayList<>(), null);

        userRegistrationDto = new UserRegistrationDto("Name", "Lastname",
                "email@gmail.com", "2001-11-16", "Password@123");

        userRetrievalDto = new UserRetrievalDto(1L, "Name", "Lastname", "email@gmail.com", "2001-11-16", null, "", "", "", false, false, false, false, false, false, false, false, false, new ArrayList<>());

        userRetrievalMinimalDto = new UserRetrievalMinimalDto(1L, "Name", "Lastname", "", false, 0, false);

        userRetrievalMinimalDto2 = new UserRetrievalMinimalDto(2L, "Name2", "Lastname2", "", false, 0, false);

        emailVerificationTokenEntity = new VerificationTokenEntity("token", VerificationTokenType.EMAIL_VERIFICATION, userEntity, 5000);
        playedGameEntity = new PlayedGameEntity(1L, 1L, "gameName",
                "gamePictureUrl", "gameCategory",
                0, false, "no-score", new Date(), true, userEntity,
                null, new HashSet<>(), new HashSet<>());
        playedGameEntity2 = new PlayedGameEntity(2L, 1L, "gameName",
                "gamePictureUrl", "gameCategory",
                0, false, "no-score", new Date(), true, userEntity2,
                null, new HashSet<>(), new HashSet<>());
        playedGameDto =new PlayedGameDto(1L, 1L, "gameName",
                "gamePictureUrl", 0, false, "no-score",
                0, 0, 0.0, new Date(), userRetrievalMinimalDto, new HashSet<>(),
                null, null);
        playedGameDto2 =new PlayedGameDto(2L, 1L, "gameName",
                "gamePictureUrl", 0, false, "no-score",
                0, 0, 0.0, new Date(), userRetrievalMinimalDto, new HashSet<>(),
                null, null);
        simplePostEntity = new SimplePostEntity(1L, "description", new Date(), userEntity, new ArrayList<>(), new HashSet<>(), playedGamePostEntity, null, new ArrayList<>());
        playedGamePostEntity = new PlayedGamePostEntity(1L, 0, 0, 0.0, "no-score", simplePostEntity, playedGameEntity);
        userEditDto = new UserEditDto();
        boardGame = createSampleBGGThingResponse();
        securityContext.setAuthentication(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    private  BGGThingResponse createSampleBGGThingResponse() {
        BGGThingResponse.BoardGame.Name name = new BGGThingResponse.BoardGame.Name("primary", 1, "Sample Game");
        List<BGGThingResponse.BoardGame.Name> names = new ArrayList<>();
        names.add(name);

        BGGThingResponse.BoardGame boardGame = new BGGThingResponse.BoardGame("boardgame", "123", "thumbnail_url", "image_url", "category", names,
                "Sample game description", new BGGThingResponse.BoardGame.YearPublished(2022),
                new BGGThingResponse.BoardGame.MinPlayers(2), new BGGThingResponse.BoardGame.MaxPlayers(4));
        List<BGGThingResponse.BoardGame> items = new ArrayList<>();
        items.add(boardGame);

        return new BGGThingResponse(items);
    }

    @Test
    void createUserShouldSucceed() {
        Mockito.when(userRepository.findByEmail(any())).thenReturn(Optional.empty());
        Mockito.when(userMapper.registrationDtoToEntity(any())).thenReturn(userEntity);
        Mockito.when(userRepository.save(any())).thenReturn(userEntity);
        Mockito.when(userMapper.entityToRetrievalDto(any(), any(), any(), any(), any())).thenReturn(userRetrievalDto);
        Mockito.when(passwordEncoder.encode(any())).thenReturn("encodedPassword");
        Mockito.when(emailService.sendEmailVerification(any(), any())).thenReturn(true);

        UserRetrievalDto result = userService.createUser(userRegistrationDto, null);

        Assertions.assertEquals(userRetrievalDto, result);
    }

    @Test
    void createUserShouldFailWhenUsernameIsTaken() {
        Mockito.when(userRepository.findByEmail(any())).thenReturn(Optional.of(userEntity2));

        Assertions.assertThrows(FieldValidationException.class, () -> userService.createUser(userRegistrationDto, new MockHttpServletRequest()));
    }

    @Test
    void createUserShouldFailWhenEmailIsTaken() {
        Mockito.when(userRepository.findByEmail(any())).thenReturn(Optional.of(userEntity));

        Assertions.assertThrows(FieldValidationException.class, () -> userService.createUser(userRegistrationDto, new MockHttpServletRequest()));
    }

    @Test
    void createUserShouldFailWhenUsernameAndEmailIsTaken() {
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
    void sendFriendRequestShouldSucceed() throws IllegalAccessException {
        Mockito.when(userRepository.findById(2L)).thenReturn(Optional.of(userEntity2));
        Mockito.when(userRepository.save(userEntity)).thenReturn(userEntity);
        Mockito.when(userMapper.entityToMinimalRetrievalDto(userEntity)).thenReturn(userRetrievalMinimalDto);
        Mockito.when(userMapper.entityToMinimalRetrievalDto(userEntity2)).thenReturn(userRetrievalMinimalDto2);
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        Mockito.when(authentication.getPrincipal()).thenReturn(userEntity);
        Mockito.when((userRepository.findByEmail(any()))).thenReturn(Optional.of(userEntity));
        Mockito.doNothing().when(messagingTemplate).convertAndSendToUser(any(), any(), any());
        FriendRequestDto friendRequestDto = userService.sendFriendRequest(2L);

        Assertions.assertEquals(userRetrievalMinimalDto, friendRequestDto.getSender());
        Assertions.assertEquals(userRetrievalMinimalDto2, friendRequestDto.getReceiver());
    }

    @Test
    void sendFriendRequestShouldFailWhenUserNotFoundByUsername() {
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        Mockito.when(authentication.getPrincipal()).thenReturn(userEntity);

        Assertions.assertThrows(EntityNotFoundException.class, () -> userService.sendFriendRequest(2L));
    }

    @Test
    void sendFriendRequestShouldFailWhenSendingFriendRequestToYourself() {
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        Mockito.when(authentication.getPrincipal()).thenReturn(userEntity);
        Mockito.when(userRepository.findByEmail(any())).thenReturn(Optional.of(userEntity));
        Mockito.when(userRepository.findById(any())).thenReturn(Optional.of(userEntity));

        Assertions.assertThrows(DuplicateFriendRequestException.class, () -> userService.sendFriendRequest(1L));
    }

    @Test
    void sendFriendRequestShouldFailWhenFriendRequestAlreadySent() {
        userEntity.addSentFriendRequest(userEntity2);
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        Mockito.when(authentication.getPrincipal()).thenReturn(userEntity);
        Mockito.when(userRepository.findByEmail(any())).thenReturn(Optional.of(userEntity));
        Mockito.when(userRepository.findById(any())).thenReturn(Optional.of(userEntity2));

        Assertions.assertThrows(DuplicateFriendRequestException.class, () -> userService.sendFriendRequest(2L));
    }

    @Test
    void sendFriendRequestShouldFailWhenReceiverAlreadyFriend() {
        userEntity.addFriend(userEntity2);

        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        Mockito.when(authentication.getPrincipal()).thenReturn(userEntity);
        Mockito.when(userRepository.findByEmail(any())).thenReturn(Optional.of(userEntity));
        Mockito.when(userRepository.findById(any())).thenReturn(Optional.of(userEntity2));

        Assertions.assertThrows(DuplicateFriendRequestException.class, () -> userService.sendFriendRequest(2L));
    }

    @Test
    void sendFriendRequestShouldFailWhenReceiverAlreadySentFriendRequest() {
        userEntity.addReceivedFriendRequest(userEntity2);

        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        Mockito.when(authentication.getPrincipal()).thenReturn(userEntity);
        Mockito.when(userRepository.findByEmail(any())).thenReturn(Optional.of(userEntity));
        Mockito.when(userRepository.findById(any())).thenReturn(Optional.of(userEntity2));

        Assertions.assertThrows(DuplicateFriendRequestException.class, () -> userService.sendFriendRequest(2L));
    }

    @Test
    void acceptFriendRequestShouldSucceed() {
        userEntity.addReceivedFriendRequest(userEntity2);
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        Mockito.when(authentication.getPrincipal()).thenReturn(userEntity);
        Mockito.when(userRepository.findById(2L)).thenReturn(Optional.of(userEntity2));
        Mockito.when(userRepository.save(userEntity)).thenReturn(userEntity);
        Mockito.when(userMapper.entityToMinimalRetrievalDto(userEntity2)).thenReturn(userRetrievalMinimalDto2);
        Mockito.when(userRepository.findByEmail(any())).thenReturn(Optional.of(userEntity));

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

        Assertions.assertThrows(EntityNotFoundException.class, () -> userService.acceptFriendRequest(2L));
    }

    @Test
    void declineFriendRequestShouldSucceed() {
        userEntity.addReceivedFriendRequest(userEntity2);
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        Mockito.when(authentication.getPrincipal()).thenReturn(userEntity);
        Mockito.when(userRepository.findById(2L)).thenReturn(Optional.of(userEntity2));
        Mockito.when(userRepository.save(userEntity)).thenReturn(userEntity);
        Mockito.when(userRepository.findByEmail(any())).thenReturn(Optional.of(userEntity));

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

        Assertions.assertThrows(EntityNotFoundException.class, () -> userService.declineFriendRequest(2L));
    }

    @Test
    void getReceivedFriendRequestsShouldSucceed() {
        userEntity.addReceivedFriendRequest(userEntity2);
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        Mockito.when(authentication.getPrincipal()).thenReturn(userEntity);
        Mockito.when(userMapper.entityToMinimalRetrievalDto(userEntity2)).thenReturn(userRetrievalMinimalDto2);
        Mockito.when(userRepository.findByEmail(any())).thenReturn(Optional.of(userEntity));

        List<UserRetrievalMinimalDto> result = userService.getReceivedFriendRequests(0, 1);
        Assertions.assertEquals(List.of(userRetrievalMinimalDto2), result);
    }

    @Test
    void getReceivedFriendRequestsShouldSucceedWhenNullPageAndPageSize() {
        userEntity.addReceivedFriendRequest(userEntity2);
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        Mockito.when(authentication.getPrincipal()).thenReturn(userEntity);
        Mockito.when(userMapper.entityToMinimalRetrievalDto(userEntity2)).thenReturn(userRetrievalMinimalDto2);
        Mockito.when(userRepository.findByEmail(any())).thenReturn(Optional.of(userEntity));

        List<UserRetrievalMinimalDto> result = userService.getReceivedFriendRequests(null, null);
        Assertions.assertEquals(List.of(userRetrievalMinimalDto2), result);
    }

    @Test
    void getReceivedFriendRequestShouldFailWhenUserNotFoundByUsername() {
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        Mockito.when(authentication.getPrincipal()).thenReturn(userEntity);

        Assertions.assertThrows(EntityNotFoundException.class, () -> userService.getReceivedFriendRequests(0, 0));
    }

    @Test
    void getSentFriendRequestsShouldSucceed() {
        userEntity.addSentFriendRequest(userEntity2);
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        Mockito.when(authentication.getPrincipal()).thenReturn(userEntity);
        Mockito.when(userMapper.entityToMinimalRetrievalDto(userEntity2)).thenReturn(userRetrievalMinimalDto2);
        Mockito.when(userRepository.findByEmail(any())).thenReturn(Optional.of(userEntity));
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
        Mockito.when(userMapper.entityToMinimalRetrievalDto(userEntity2)).thenReturn(userRetrievalMinimalDto2);
        Mockito.when(userRepository.findByEmail(any())).thenReturn(Optional.of(userEntity));
        Mockito.when(userRepository.findById(any())).thenReturn(Optional.of(userEntity));

        Set<UserRetrievalMinimalDto> result = userService.getFriends(1L);
        Assertions.assertEquals(Set.of(userRetrievalMinimalDto2), result);
    }

    @Test
    void getFriendsShouldSucceedWhenUserAdministrator() throws IllegalAccessException {
        userEntity.setRole(Role.ROLE_ADMIN);
        userEntity2.addFriend(userEntity);
        Set<UserEntity> friendOfSet = userEntity2.getFriendOf();
        friendOfSet.add(userEntity);
        userEntity2.setFriendOf(friendOfSet);
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        Mockito.when(authentication.getPrincipal()).thenReturn(userEntity);
        Mockito.when(userRepository.findById(2L)).thenReturn(Optional.of(userEntity2));
        Mockito.when(userMapper.entityToMinimalRetrievalDto(userEntity)).thenReturn(userRetrievalMinimalDto);
        Mockito.when(userRepository.findByEmail(any())).thenReturn(Optional.of(userEntity));

        Set<UserRetrievalMinimalDto> result = userService.getFriends(2L);
        Assertions.assertEquals(Set.of(userRetrievalMinimalDto), result);
    }

    @Test
    void getFriendsShouldFailWhenTryingToGetOtherUserPrivateFriendsListAndNotAdministrator() {
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        Mockito.when(authentication.getPrincipal()).thenReturn(userEntity);
        Mockito.when(userRepository.findById(2L)).thenReturn(Optional.of(userEntity2));
        Mockito.when(userRepository.findByEmail(any())).thenReturn(Optional.of(userEntity));

        Assertions.assertThrows(IllegalAccessException.class, () -> userService.getFriends(2L));
    }

    @Test
    void getUserEntityByIdShouldSucceed() {
        Mockito.when(userRepository.findById(any())).thenReturn(Optional.of(userEntity));
        UserEntity result = userService.getUserEntityById(1L);
        Assertions.assertEquals(userEntity, result);
    }

    @Test
    void getUserEntityByIdShouldFail() {
        Mockito.when(userRepository.findById(any())).thenReturn(Optional.empty());
        Assertions.assertThrows(EntityNotFoundException.class, () -> userService.getUserEntityById(1L));
    }

    @Test
    void getUserEntityByUsernameShouldSucceed() {
        Mockito.when(userRepository.findByEmail(any())).thenReturn(Optional.of(userEntity));
        UserEntity result = userService.getUserEntityByEmail("username");
        Assertions.assertEquals(userEntity, result);
    }

    @Test
    void getUserEntityByUsernameShouldFail() {
        Assertions.assertThrows(EntityNotFoundException.class, () -> userService.getUserEntityByEmail(""));
    }

    @Test
    void searchUsersShouldSucceedWhenEmptyQueryAndNotAdministrator() {
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        Mockito.when(authentication.getPrincipal()).thenReturn(userEntity);
        Mockito.when(userRepository.findBySuspensionIsNull(PageRequest.of(0, 10, Sort.by("name").and(Sort.by("lastname"))))).thenReturn(List.of(userEntity, userEntity2));
        Mockito.when(userMapper.entityToMinimalRetrievalDto(userEntity)).thenReturn(userRetrievalMinimalDto);
        Mockito.when(userMapper.entityToMinimalRetrievalDto(userEntity2)).thenReturn(userRetrievalMinimalDto2);

        Set<UserRetrievalMinimalDto> result = userService.searchUsers(null, 0, 10);
        Assertions.assertTrue(result.containsAll(Set.of(userRetrievalMinimalDto, userRetrievalMinimalDto2)));
        Assertions.assertTrue(Set.of(userRetrievalMinimalDto, userRetrievalMinimalDto2).containsAll(result));
    }

    @Test
    void searchUsersShouldSucceedWhenEmptyQueryAndAdministrator() {
        userEntity.setRole(Role.ROLE_ADMIN);
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        Mockito.when(authentication.getPrincipal()).thenReturn(userEntity);
        Mockito.when(userRepository.findAll(PageRequest.of(0, 10, Sort.by("name").and(Sort.by("lastname"))))).thenReturn(new PageImpl<>(List.of(userEntity, userEntity2)));
        Mockito.when(userMapper.entityToMinimalRetrievalDto(userEntity)).thenReturn(userRetrievalMinimalDto);
        Mockito.when(userMapper.entityToMinimalRetrievalDto(userEntity2)).thenReturn(userRetrievalMinimalDto2);

        Set<UserRetrievalMinimalDto> result = userService.searchUsers(null, 0, 10);
        Assertions.assertTrue(result.containsAll(Set.of(userRetrievalMinimalDto, userRetrievalMinimalDto2)));
        Assertions.assertTrue(Set.of(userRetrievalMinimalDto, userRetrievalMinimalDto2).containsAll(result));
    }

    @Test
    void searchUsersShouldSucceedWhenOnlyNameSpecifiedInQueryAndNotAdministrator() {
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        Mockito.when(authentication.getPrincipal()).thenReturn(userEntity);
        Mockito.when(userRepository.findByNameContainingIgnoreCaseAndSuspensionIsNullOrLastnameContainingIgnoreCaseAndSuspensionIsNull("test", "test", PageRequest.of(0, 10, Sort.by("name").and(Sort.by("lastname"))))).thenReturn(List.of(userEntity, userEntity2));
        Mockito.when(userMapper.entityToMinimalRetrievalDto(userEntity)).thenReturn(userRetrievalMinimalDto);
        Mockito.when(userMapper.entityToMinimalRetrievalDto(userEntity2)).thenReturn(userRetrievalMinimalDto2);

        Set<UserRetrievalMinimalDto> result = userService.searchUsers("test", 0, 10);
        Assertions.assertTrue(result.containsAll(Set.of(userRetrievalMinimalDto, userRetrievalMinimalDto2)));
        Assertions.assertTrue(Set.of(userRetrievalMinimalDto, userRetrievalMinimalDto2).containsAll(result));
    }

    @Test
    void searchUsersShouldSucceedWhenOnlyNameSpecifiedInQueryAndAdministrator() {
        userEntity.setRole(Role.ROLE_ADMIN);
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        Mockito.when(authentication.getPrincipal()).thenReturn(userEntity);
        Mockito.when(userRepository.findByNameContainingIgnoreCaseOrLastnameContainingIgnoreCase("test", "test", PageRequest.of(0, 10, Sort.by("name").and(Sort.by("lastname"))))).thenReturn(List.of(userEntity, userEntity2));
        Mockito.when(userMapper.entityToMinimalRetrievalDto(userEntity)).thenReturn(userRetrievalMinimalDto);
        Mockito.when(userMapper.entityToMinimalRetrievalDto(userEntity2)).thenReturn(userRetrievalMinimalDto2);

        Set<UserRetrievalMinimalDto> result = userService.searchUsers("test", 0, 10);
        Assertions.assertTrue(result.containsAll(Set.of(userRetrievalMinimalDto, userRetrievalMinimalDto2)));
        Assertions.assertTrue(Set.of(userRetrievalMinimalDto, userRetrievalMinimalDto2).containsAll(result));
    }

    @Test
    void searchUsersShouldSucceedWhenNameAndLastnameSpecifiedInQueryAndNotAdministrator() {
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        Mockito.when(authentication.getPrincipal()).thenReturn(userEntity);
        Mockito.when(userRepository.findByNameContainingIgnoreCaseAndSuspensionIsNullOrLastnameContainingIgnoreCaseAndSuspensionIsNull("test", "test", PageRequest.of(0, 10, Sort.by("name").and(Sort.by("lastname"))))).thenReturn(List.of(userEntity, userEntity2));
        Mockito.when(userMapper.entityToMinimalRetrievalDto(userEntity)).thenReturn(userRetrievalMinimalDto);
        Mockito.when(userMapper.entityToMinimalRetrievalDto(userEntity2)).thenReturn(userRetrievalMinimalDto2);

        Set<UserRetrievalMinimalDto> result = userService.searchUsers("test test", 0, 10);
        Assertions.assertTrue(result.containsAll(Set.of(userRetrievalMinimalDto, userRetrievalMinimalDto2)));
        Assertions.assertTrue(Set.of(userRetrievalMinimalDto, userRetrievalMinimalDto2).containsAll(result));
    }

    @Test
    void searchUsersShouldSucceedWhenNameAndLastnameSpecifiedInQueryAndAdministrator() {
        userEntity.setRole(Role.ROLE_ADMIN);
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        Mockito.when(authentication.getPrincipal()).thenReturn(userEntity);
        Mockito.when(userRepository.findByNameContainingIgnoreCaseOrLastnameContainingIgnoreCase("test", "test", PageRequest.of(0, 10, Sort.by("name").and(Sort.by("lastname"))))).thenReturn(List.of(userEntity, userEntity2));
        Mockito.when(userMapper.entityToMinimalRetrievalDto(userEntity)).thenReturn(userRetrievalMinimalDto);
        Mockito.when(userMapper.entityToMinimalRetrievalDto(userEntity2)).thenReturn(userRetrievalMinimalDto2);

        Set<UserRetrievalMinimalDto> result = userService.searchUsers("test test", 0, 10);
        Assertions.assertTrue(result.containsAll(Set.of(userRetrievalMinimalDto, userRetrievalMinimalDto2)));
        Assertions.assertTrue(Set.of(userRetrievalMinimalDto, userRetrievalMinimalDto2).containsAll(result));
    }

    @Test
    void getUserByIdShouldSucceed() throws IllegalAccessException {
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        Mockito.when(authentication.getPrincipal()).thenReturn(userEntity);
        Mockito.when(userRepository.findByEmail(any())).thenReturn(Optional.of(userEntity));
        Mockito.when(userRepository.findById(any())).thenReturn(Optional.of(userEntity2));
        Mockito.when(userMapper.entityToRetrievalDto(any(), any(), any(), any(), any())).thenReturn(userRetrievalDto);

        UserRetrievalDto result = userService.getUserById(2L);
        userRetrievalDto.setReceivedWarnings(null);

        Assertions.assertEquals(userRetrievalDto, result);
    }

    @Test
    void getUserByIdShouldFail() {
        userEntity.setSuspension(new SuspensionEntity());
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        Mockito.when(authentication.getPrincipal()).thenReturn(userEntity);
        Mockito.when(userRepository.findByEmail(any())).thenReturn(Optional.of(userEntity));
        Mockito.when(userRepository.findById(any())).thenReturn(Optional.of(userEntity));

        Assertions.assertThrows(IllegalAccessException.class, () -> userService.getUserById(1L));
    }

    @Test
    void getPlayedGamesByUserIdShouldSucceed() throws IllegalAccessException {
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        Mockito.when(authentication.getPrincipal()).thenReturn(userEntity);
        Mockito.when(userRepository.findById(any())).thenReturn(Optional.of(userEntity));
        Mockito.when(playedGameRepository.findByUserId(any(), any())).thenReturn(List.of(playedGameEntity, playedGameEntity2));
        Mockito.when(playedGameMapper.entityToDto(playedGameEntity)).thenReturn(playedGameDto);
        Mockito.when(playedGameMapper.entityToDto(playedGameEntity2)).thenReturn(playedGameDto2);
        HttpServletRequest request = new MockHttpServletRequest();

        try (MockedStatic mockedStatic = Mockito.mockStatic(ServletUriComponentsBuilder.class)) {
            mockedStatic.when(ServletUriComponentsBuilder::fromCurrentContextPath).thenReturn(mock(ServletUriComponentsBuilder.class));

            PlayedGamePageDto result = userService.getPlayedGamesByUserId(1L, 0, 2, request);

            verify(userRepository, times(1)).findById(any());
            assertEquals("null?page=1&pageSize=2", result.getNextPageUrl());
            assertTrue(result.getPlays().containsAll(List.of(playedGameDto, playedGameDto2)));
            assertTrue(List.of(playedGameDto, playedGameDto2).containsAll(result.getPlays()));
        }
    }

    @Test
    void getPlayedGamesByUserIdShouldFail() {
        userEntity2.setPublicPlays(false);
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        Mockito.when(authentication.getPrincipal()).thenReturn(userEntity);
        Mockito.when(userRepository.findById(any())).thenReturn(Optional.of(userEntity2));
        HttpServletRequest request = new MockHttpServletRequest();

        assertThrows(IllegalAccessException.class, () -> userService.getPlayedGamesByUserId(2L, 0, 2, request));
    }

    @Test
    void logPlayedGame_shouldReturnPlayedGameDto_noScore() {
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        Mockito.when(authentication.getPrincipal()).thenReturn(userEntity);
        Mockito.when(userRepository.findByEmail(any())).thenReturn(Optional.of(userEntity));
        Mockito.when(boardGameService.getBoardGameById(any())).thenReturn(boardGame);
        Mockito.when(playedGameRepository.save(any())).thenReturn(playedGameEntity);
        Mockito.when(playedGameMapper.entityToDto(any())).thenReturn(playedGameDto);
        Mockito.when(userRepository.findById(any())).thenReturn(Optional.of(userEntity));
        Mockito.when(notificationService.createAndSaveNotificationsForAssociatedPlays(any())).thenReturn(Collections.emptyList());
        playedGameDto.setAssociatedPlays(Set.of(playedGameDto2));

        PlayedGameDto result = userService.logPlayedGame(playedGameDto);

        assertEquals(playedGameDto, result);
    }

    @Test
    void logPlayedGame_shouldReturnPlayedGameDto_lowestScore() {
        playedGameDto.setScoringSystem("lowest-score");
        playedGameDto2.setScore(-100);
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        Mockito.when(authentication.getPrincipal()).thenReturn(userEntity);
        Mockito.when(userRepository.findByEmail(any())).thenReturn(Optional.of(userEntity));
        Mockito.when(boardGameService.getBoardGameById(any())).thenReturn(boardGame);
        Mockito.when(playedGameRepository.save(any())).thenReturn(playedGameEntity);
        Mockito.when(playedGameMapper.entityToDto(any())).thenReturn(playedGameDto);
        Mockito.when(userRepository.findById(any())).thenReturn(Optional.of(userEntity));
        Mockito.when(notificationService.createAndSaveNotificationsForAssociatedPlays(any())).thenReturn(Collections.emptyList());
        playedGameDto.setAssociatedPlays(Set.of(playedGameDto2));

        PlayedGameDto result = userService.logPlayedGame(playedGameDto);

        assertEquals(playedGameDto, result);
    }

    @Test
    void logPlayedGame_shouldReturnPlayedGameDto_highestScore() {
        playedGameDto.setScoringSystem("highest-score");
        playedGameDto2.setScore(5000);
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        Mockito.when(authentication.getPrincipal()).thenReturn(userEntity);
        Mockito.when(userRepository.findByEmail(any())).thenReturn(Optional.of(userEntity));
        Mockito.when(boardGameService.getBoardGameById(any())).thenReturn(boardGame);
        Mockito.when(playedGameRepository.save(any())).thenReturn(playedGameEntity);
        Mockito.when(playedGameMapper.entityToDto(any())).thenReturn(playedGameDto);
        Mockito.when(userRepository.findById(any())).thenReturn(Optional.of(userEntity));
        Mockito.when(notificationService.createAndSaveNotificationsForAssociatedPlays(any())).thenReturn(Collections.emptyList());
        playedGameDto.setAssociatedPlays(Set.of(playedGameDto2));

        PlayedGameDto result = userService.logPlayedGame(playedGameDto);

        assertEquals(playedGameDto, result);
    }

    @Test
    void logPlayedGame_shouldFail_invalidScoringSystem() {
        playedGameDto.setScoringSystem("invalid");
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        Mockito.when(authentication.getPrincipal()).thenReturn(userEntity);
        Mockito.when(userRepository.findByEmail(any())).thenReturn(Optional.of(userEntity));

        assertThrows(FieldValidationException.class, () -> userService.logPlayedGame(playedGameDto));
    }

    @Test
    void deletePlayedGameById_shouldSucceed() throws IllegalAccessException {
        playedGameEntity.setAssociatedWith(Set.of(playedGameEntity2));
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        Mockito.when(authentication.getPrincipal()).thenReturn(userEntity);
        Mockito.when(userRepository.findByEmail(any())).thenReturn(Optional.of(userEntity));
        Mockito.when(playedGameRepository.findById(any())).thenReturn(Optional.of(playedGameEntity));
        Mockito.when(playedGameRepository.save(any())).thenReturn(playedGameEntity);
        Mockito.doNothing().when(playedGameRepository).delete(any());

        userService.deletePlayedGameById(1L);
        verify(playedGameRepository).delete(any());
        verify(playedGameRepository, times(1)).findById(any());
        verify(playedGameRepository, times(1)).save(any());
    }

    @Test
    void deletePlayedGameById_shouldFail_illegalAccessException() {
        playedGameEntity.setUser(userEntity2);
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        Mockito.when(authentication.getPrincipal()).thenReturn(userEntity);
        Mockito.when(userRepository.findByEmail(any())).thenReturn(Optional.of(userEntity));
        Mockito.when(playedGameRepository.findById(any())).thenReturn(Optional.of(playedGameEntity));
        assertThrows(IllegalAccessException.class, () -> userService.deletePlayedGameById(1L));
    }

    @Test
    void editUserById_shouldFail_illegalAccessException() {
        MultipartFile multipartFile = new MockMultipartFile("name", new byte[]{});
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        Mockito.when(authentication.getPrincipal()).thenReturn(userEntity);
        Mockito.when(userRepository.findById(any())).thenReturn(Optional.of(userEntity2));
        assertThrows(IllegalAccessException.class, () -> userService.editUserById(1L, multipartFile, new UserEditDto()));
    }

    @Test
    void editUserById_shouldSucceed() throws IllegalAccessException {
        MultipartFile multipartFile = new MockMultipartFile("name", new byte[]{});
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        Mockito.when(authentication.getPrincipal()).thenReturn(userEntity);
        Mockito.when(userRepository.findById(any())).thenReturn(Optional.of(userEntity));
        Mockito.doNothing().when(awsService).deleteProfilePicture(any());
        Mockito.when(awsService.uploadProfilePicture(any(), any())).thenReturn("profilePictureUrl");
        Mockito.when(userRepository.save(any())).thenReturn(userEntity);
        Mockito.when(userMapper.entityToRetrievalDto(any(), any(), any(), any(), any())).thenReturn(userRetrievalDto);

        UserRetrievalDto result = userService.editUserById(1L, multipartFile, userEditDto);

        assertEquals(userRetrievalDto, result);
        verify(awsService, times(1)).deleteProfilePicture(any());
        verify(awsService, times(1)).uploadProfilePicture(any(), any());
        verify(userRepository, times(1)).save(any());
    }

    @Test
    void editUserById_shouldFail_profilePictureTooBig() {
        MultipartFile multipartFile = mock(MultipartFile.class);
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        Mockito.when(authentication.getPrincipal()).thenReturn(userEntity);
        Mockito.when(userRepository.findById(any())).thenReturn(Optional.of(userEntity));
        Mockito.when(multipartFile.getSize()).thenReturn(2097153L);

        assertThrows(FieldValidationException.class, () -> userService.editUserById(1L, multipartFile, userEditDto));
    }

    @Test
    void editUserById_shouldFail_failedToUploadPicture() {
        MultipartFile multipartFile = new MockMultipartFile("name", new byte[]{});
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        Mockito.when(authentication.getPrincipal()).thenReturn(userEntity);
        Mockito.when(userRepository.findById(any())).thenReturn(Optional.of(userEntity));
        Mockito.doNothing().when(awsService).deleteProfilePicture(any());
        Mockito.when(awsService.uploadProfilePicture(any(), any())).thenReturn(null);

        assertThrows(FieldValidationException.class, () -> userService.editUserById(1L, multipartFile, userEditDto));
    }

    @Test
    void removeFromFriendsList_shouldFail() {
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        Mockito.when(authentication.getPrincipal()).thenReturn(userEntity);
        Mockito.when(userRepository.findById(any())).thenReturn(Optional.of(userEntity2));

        assertThrows(IllegalAccessException.class, () -> userService.removeFromFriendsList(1L, 2L));
    }

    @Test
    void removeFromFriendsList_shouldSucceed() throws IllegalAccessException {
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        Mockito.when(authentication.getPrincipal()).thenReturn(userEntity);
        Mockito.when(userRepository.findById(2L)).thenReturn(Optional.of(userEntity2));
        Mockito.when(userRepository.findById(1L)).thenReturn(Optional.of(userEntity));
        Mockito.when(userRepository.save(any())).thenReturn(userEntity);
        Mockito.when(chatRoomService.getChatRoomId(1L, 2L, false)).thenReturn(Optional.of("chatId"));
        Mockito.doNothing().when(chatRoomService).deleteChatRoomByChatId(any());
        Mockito.doNothing().when(messagingTemplate).convertAndSendToUser(any(), any(), any());

        userService.removeFromFriendsList(1L, 2L);

        verify(userRepository, times(1)).save(any());
        verify(messagingTemplate, times(2)).convertAndSendToUser(any(), any(), any());
    }

    @Test
    void getStatisticsByUserId_shouldFail_illegalAccess() {
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        Mockito.when(authentication.getPrincipal()).thenReturn(userEntity);
        Mockito.when(userRepository.findById(any())).thenReturn(Optional.of(userEntity2));

        assertThrows(IllegalAccessException.class, () -> userService.getStatisticsByUserId(1L));
    }

    @Test
    void getStatisticsByUserId_shouldSucceed() throws IllegalAccessException {
        playedGameEntity.setAssociatedPlays(Set.of(playedGameEntity2));
        userEntity.setPlayedGames(List.of(playedGameEntity));
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        Mockito.when(authentication.getPrincipal()).thenReturn(userEntity);
        Mockito.when(userRepository.findById(any())).thenReturn(Optional.of(userEntity));
        Mockito.when(userMapper.entityToMinimalRetrievalDto(any())).thenReturn(userRetrievalMinimalDto2);

        UserStatisticsDto result = userService.getStatisticsByUserId(1L);

        assertEquals(playedGameEntity2.getUser().getId(), result.getFavouriteFriend().getUserData().getId());
        assertEquals(0, result.getTotalGamesWon());
        assertEquals(1, result.getTotalGamesLost());
        assertEquals(1, result.getFavouriteGame().getBggGameId());
    }

    @ParameterizedTest
    @ValueSource(strings = {"highest-score", "lowest-score", "no-score"})
    void editPlayedGame_shouldSucceed(String scoringSystem) throws IllegalAccessException {
        playedGameDto.setScoringSystem(scoringSystem);
        playedGameEntity.setPost(playedGamePostEntity);
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        Mockito.when(authentication.getPrincipal()).thenReturn(userEntity);
        Mockito.when(userRepository.findByEmail(any())).thenReturn(Optional.of(userEntity));
        Mockito.when(boardGameService.getBoardGameById(any())).thenReturn(boardGame);
        Mockito.when(playedGameRepository.save(any())).thenReturn(playedGameEntity);
        Mockito.when(playedGameMapper.entityToDto(any())).thenReturn(playedGameDto);
        Mockito.when(userRepository.findById(any())).thenReturn(Optional.of(userEntity));
        Mockito.when(playedGameRepository.findById(any())).thenReturn(Optional.of(playedGameEntity));
        Mockito.doNothing().when(playedGameRepository).deleteAllById(any());
        Mockito.when(notificationService.createAndSaveNotificationsForAssociatedPlays(any())).thenReturn(Collections.emptyList());
        playedGameDto.setAssociatedPlays(Set.of(playedGameDto2));

        PlayedGameDto result = userService.editPlayedGameById(1L, playedGameDto);

        assertEquals(playedGameDto, result);
    }

    @Test
    void editPlayedGame_shouldFail_playedGameNotFound() {
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        Mockito.when(authentication.getPrincipal()).thenReturn(userEntity);
        Mockito.when(userRepository.findByEmail(any())).thenReturn(Optional.of(userEntity));
        Mockito.when(playedGameRepository.findById(any())).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> userService.editPlayedGameById(1L, playedGameDto));
    }

    @Test
    void editPlayedGame_shouldFail_invalidScoringSystem() {
        playedGameDto.setScoringSystem("invalid");
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        Mockito.when(authentication.getPrincipal()).thenReturn(userEntity);
        Mockito.when(userRepository.findByEmail(any())).thenReturn(Optional.of(userEntity));
        Mockito.when(boardGameService.getBoardGameById(any())).thenReturn(boardGame);
        Mockito.when(playedGameRepository.findById(any())).thenReturn(Optional.of(playedGameEntity));

        assertThrows(FieldValidationException.class, () -> userService.editPlayedGameById(1L, playedGameDto));
    }

    @Test
    void includePlayInStatistics_shouldSucceed() throws IllegalAccessException {
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        Mockito.when(authentication.getPrincipal()).thenReturn(userEntity);
        Mockito.when(userRepository.findByEmail(any())).thenReturn(Optional.of(userEntity));
        Mockito.when(playedGameRepository.findById(any())).thenReturn(Optional.of(playedGameEntity));
        Mockito.when(playedGameMapper.entityToDto(any())).thenReturn(playedGameDto);
        Mockito.when(playedGameRepository.save(any())).thenReturn(playedGameEntity);

        PlayedGameDto result = userService.includePlayInStatistics(1L);

        assertEquals(playedGameDto, result);
    }

    @Test
    void includePlayInStatistics_shouldFail() {
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        Mockito.when(authentication.getPrincipal()).thenReturn(userEntity2);
        Mockito.when(userRepository.findByEmail(any())).thenReturn(Optional.of(userEntity2));
        Mockito.when(playedGameRepository.findById(any())).thenReturn(Optional.of(playedGameEntity));
       assertThrows(IllegalAccessException.class, () -> userService.includePlayInStatistics(1L));
    }

    @Test
    void excludePlayFromStatistics_shouldSucceed() throws IllegalAccessException {
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        Mockito.when(authentication.getPrincipal()).thenReturn(userEntity);
        Mockito.when(userRepository.findByEmail(any())).thenReturn(Optional.of(userEntity));
        Mockito.when(playedGameRepository.findById(any())).thenReturn(Optional.of(playedGameEntity));
        Mockito.when(playedGameMapper.entityToDto(any())).thenReturn(playedGameDto);
        Mockito.when(playedGameRepository.save(any())).thenReturn(playedGameEntity);

        PlayedGameDto result = userService.excludePlayFromStatistics(1L);

        assertEquals(playedGameDto, result);
    }

    @Test
    void excludePlayFromStatistics_shouldFail() {
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        Mockito.when(authentication.getPrincipal()).thenReturn(userEntity2);
        Mockito.when(userRepository.findByEmail(any())).thenReturn(Optional.of(userEntity2));
        Mockito.when(playedGameRepository.findById(any())).thenReturn(Optional.of(playedGameEntity));
        assertThrows(IllegalAccessException.class, () -> userService.excludePlayFromStatistics(1L));
    }
}
