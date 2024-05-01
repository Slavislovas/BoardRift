package com.socialnetwork.boardrift.unit.service;

import com.socialnetwork.boardrift.enumeration.Role;
import com.socialnetwork.boardrift.repository.NotificationRepository;
import com.socialnetwork.boardrift.repository.UserRepository;
import com.socialnetwork.boardrift.repository.model.NotificationEntity;
import com.socialnetwork.boardrift.repository.model.WarningEntity;
import com.socialnetwork.boardrift.repository.model.board_game.PlayedGameEntity;
import com.socialnetwork.boardrift.repository.model.user.UserEntity;
import com.socialnetwork.boardrift.rest.model.NotificationDto;
import com.socialnetwork.boardrift.rest.model.NotificationPageDto;
import com.socialnetwork.boardrift.rest.model.post.PostPageDto;
import com.socialnetwork.boardrift.service.NotificationService;
import com.socialnetwork.boardrift.util.mapper.NotificationMapper;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;

@ExtendWith({MockitoExtension.class})
public class NotificationServiceUnitTests {
    @Mock
    NotificationRepository notificationRepository;

    @Mock
    UserRepository userRepository;

    @Mock
    NotificationMapper notificationMapper;

    @Mock
    SimpMessagingTemplate simpMessagingTemplate;

    @InjectMocks
    NotificationService notificationService;

    UserEntity userEntity;
    UserEntity userEntity2;
    NotificationEntity notificationEntity;
    NotificationEntity notificationEntity2;
    PlayedGameEntity playedGameEntity;
    PlayedGameEntity playedGameEntity2;
    WarningEntity warningEntity;
    NotificationDto notificationDto;
    NotificationDto notificationDto2;
    NotificationPageDto notificationPageDto;
    SecurityContext securityContext = Mockito.mock(SecurityContext.class);
    Authentication authentication = Mockito.mock(Authentication.class);

    @BeforeEach
    void init() {
        userEntity = new UserEntity(1L, "Name", "Lastname", "email@gmail.com",
                "2001-11-16", "Password@123", "", "", "", true, false, false, false, "",
                Role.ROLE_USER, false, new ArrayList<>(),  new HashSet<>(),
                new HashSet<>(),  new ArrayList<>(),  new HashSet<>(), new HashSet<>(), new ArrayList<>(), null);

        userEntity2 = new UserEntity(2L, "Name2", "Lastname2", "email2@gmail.com",
                "2001-11-16", "Password@123", "", "", "", true, false, false, false, "",
                Role.ROLE_USER, false, new ArrayList<>(),  new HashSet<>(),
                new HashSet<>(),  new ArrayList<>(), new HashSet<>(), new HashSet<>(), new ArrayList<>(), null);
        notificationEntity = new NotificationEntity(1L, "description", "redirectUrl", new Date(), false, userEntity);
        notificationEntity2 = new NotificationEntity(2L, "description", "redirectUrl", new Date(), false, userEntity);
        playedGameEntity = new PlayedGameEntity(1L, 1L, "gameName",
                "gamePictureUrl", "gameCategory",
                0, false, "no-score", new Date(), true, userEntity,
                null, new HashSet<>(), new HashSet<>());
        playedGameEntity2 = new PlayedGameEntity(2L, 1L, "gameName",
                "gamePictureUrl", "gameCategory",
                0, false, "no-score", new Date(), true, userEntity2,
                null, Set.of(playedGameEntity), new HashSet<>());
        warningEntity = new WarningEntity(1L, userEntity2, "reason", new Date(), userEntity);
        notificationDto = new NotificationDto(1L, "description", "redirectUrl", 1L, new Date(), false);
        notificationDto2 = new NotificationDto(2L, "description", "redirectUrl", 1L, new Date(), false);
        notificationPageDto = new NotificationPageDto("nextPage", true, List.of(notificationDto, notificationDto2));
        securityContext.setAuthentication(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void getNotificationsShouldSucceed() {
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        Mockito.when(authentication.getPrincipal()).thenReturn(userEntity);
        Mockito.when(userRepository.findByEmail(any())).thenReturn(Optional.of(userEntity));
        Mockito.when(notificationRepository.findByRecipientId(any(), any())).thenReturn(List.of(notificationEntity, notificationEntity2));
        Mockito.when(notificationMapper.entityToDto(notificationEntity)).thenReturn(notificationDto);
        Mockito.when(notificationMapper.entityToDto(notificationEntity2)).thenReturn(notificationDto2);

        try (MockedStatic mockedStatic = Mockito.mockStatic(ServletUriComponentsBuilder.class)) {
            mockedStatic.when(ServletUriComponentsBuilder::fromCurrentContextPath).thenReturn(mock(ServletUriComponentsBuilder.class));

            NotificationPageDto result = notificationService.getNotifications(0, 3, new MockHttpServletRequest());
            assertTrue(result.getNotifications().containsAll(List.of(notificationDto, notificationDto2)));
        }
    }

    @Test
    void getNotificationsShouldFail() {
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        Mockito.when(authentication.getPrincipal()).thenReturn(userEntity);
        Mockito.when(userRepository.findByEmail(any())).thenReturn(Optional.empty());

        Assertions.assertThrows(EntityNotFoundException.class, () -> notificationService.getNotifications(0, 3, new MockHttpServletRequest()));
    }

    @Test
    void createAndSaveNotificationsForAssociatedPlaysShouldSucceed() {
        Mockito.when(notificationRepository.save(any())).thenReturn(notificationEntity2);
        Mockito.doNothing().when(simpMessagingTemplate).convertAndSendToUser(any(), any(), any());
        Mockito.when(notificationMapper.entityToDto(any())).thenReturn(notificationDto);

        List<NotificationEntity> result = notificationService.createAndSaveNotificationsForAssociatedPlays(playedGameEntity2);
        Assertions.assertEquals(userEntity, result.get(0).getRecipient());
    }

    @Test
    void createAndSaveWarningNotificationShouldSucceed() {
        Mockito.when(notificationRepository.save(any())).thenReturn(notificationEntity2);
        Mockito.doNothing().when(simpMessagingTemplate).convertAndSendToUser(any(), any(), any());
        Mockito.when(notificationMapper.entityToDto(any())).thenReturn(notificationDto);

        NotificationEntity result = notificationService.createAndSaveWarningNotification(warningEntity);

        Assertions.assertEquals(userEntity, result.getRecipient());
    }

    @Test
    void setNotificationToReadShouldSucceed() throws IllegalAccessException {
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        Mockito.when(authentication.getPrincipal()).thenReturn(userEntity);
        Mockito.when(userRepository.findByEmail(any())).thenReturn(Optional.of(userEntity));
        Mockito.when(notificationRepository.findById(any())).thenReturn(Optional.of(notificationEntity2));
        Mockito.when(notificationRepository.save(any())).thenReturn(notificationEntity2);
        Mockito.when(notificationRepository.existsByRecipientIdAndUnreadTrue(any())).thenReturn(false);

        Map<String, Object> result = notificationService.setNotificationToRead(1L);

        Assertions.assertEquals(Map.of("unreadNotificationsLeft", false), result);
    }

    @Test
    void setNotificationToReadShouldFailWhenUserNotFound() {
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        Mockito.when(authentication.getPrincipal()).thenReturn(userEntity);
        Mockito.when(userRepository.findByEmail(any())).thenReturn(Optional.empty());

        Assertions.assertThrows(EntityNotFoundException.class, () -> notificationService.setNotificationToRead(1L));
    }

    @Test
    void setNotificationToReadShouldFailWhenNotificationNotFound() {
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        Mockito.when(authentication.getPrincipal()).thenReturn(userEntity);
        Mockito.when(userRepository.findByEmail(any())).thenReturn(Optional.of(userEntity));
        Mockito.when(notificationRepository.findById(any())).thenReturn(Optional.empty());

        Assertions.assertThrows(EntityNotFoundException.class, () -> notificationService.setNotificationToRead(1L));
    }

    @Test
    void setNotificationToReadShouldFailWhenManipulatingOtherUsersNotification() {
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        Mockito.when(authentication.getPrincipal()).thenReturn(userEntity2);
        Mockito.when(userRepository.findByEmail(any())).thenReturn(Optional.of(userEntity2));
        Mockito.when(notificationRepository.findById(any())).thenReturn(Optional.of(notificationEntity));

        Assertions.assertThrows(IllegalAccessException.class, () -> notificationService.setNotificationToRead(1L));
    }

    @Test
    void deleteNotificationShouldSucceed() throws IllegalAccessException {
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        Mockito.when(authentication.getPrincipal()).thenReturn(userEntity);
        Mockito.when(userRepository.findByEmail(any())).thenReturn(Optional.of(userEntity));
        Mockito.when(notificationRepository.findById(any())).thenReturn(Optional.of(notificationEntity));
        Mockito.doNothing().when(notificationRepository).delete(any());

        notificationService.deleteNotification(1L);
        Mockito.verify(notificationRepository, times(1)).delete(any());
    }

    @Test
    void deleteNotificationShouldFailWhenUserNotFound() {
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        Mockito.when(authentication.getPrincipal()).thenReturn(userEntity);
        Mockito.when(userRepository.findByEmail(any())).thenReturn(Optional.empty());

        Assertions.assertThrows(EntityNotFoundException.class, () -> notificationService.deleteNotification(1L));
    }

    @Test
    void deleteNotificationShouldFailWhenNotificationNotFound() {
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        Mockito.when(authentication.getPrincipal()).thenReturn(userEntity);
        Mockito.when(userRepository.findByEmail(any())).thenReturn(Optional.of(userEntity));
        Mockito.when(notificationRepository.findById(any())).thenReturn(Optional.empty());

        Assertions.assertThrows(EntityNotFoundException.class, () -> notificationService.deleteNotification(1L));
    }

    @Test
    void deleteNotificationShouldFailWhenTryingToDeleteOtherUsersNotification() {
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        Mockito.when(authentication.getPrincipal()).thenReturn(userEntity2);
        Mockito.when(userRepository.findByEmail(any())).thenReturn(Optional.of(userEntity2));
        Mockito.when(notificationRepository.findById(any())).thenReturn(Optional.of(notificationEntity));

        Assertions.assertThrows(IllegalAccessException.class, () -> notificationService.deleteNotification(1L));
    }
}
