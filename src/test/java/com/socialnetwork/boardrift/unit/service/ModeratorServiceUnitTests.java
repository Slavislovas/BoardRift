package com.socialnetwork.boardrift.unit.service;

import com.socialnetwork.boardrift.enumeration.Role;
import com.socialnetwork.boardrift.repository.SuspensionRepository;
import com.socialnetwork.boardrift.repository.WarningRepository;
import com.socialnetwork.boardrift.repository.model.NotificationEntity;
import com.socialnetwork.boardrift.repository.model.WarningEntity;
import com.socialnetwork.boardrift.repository.model.user.SuspensionEntity;
import com.socialnetwork.boardrift.repository.model.user.UserEntity;
import com.socialnetwork.boardrift.rest.model.WarningDto;
import com.socialnetwork.boardrift.rest.model.user.SuspensionDto;
import com.socialnetwork.boardrift.rest.model.user.UserRetrievalMinimalDto;
import com.socialnetwork.boardrift.service.JwtService;
import com.socialnetwork.boardrift.service.ModeratorService;
import com.socialnetwork.boardrift.service.NotificationService;
import com.socialnetwork.boardrift.service.UserService;
import com.socialnetwork.boardrift.util.exception.TooManyWarningsException;
import com.socialnetwork.boardrift.util.exception.UserAlreadySuspendedException;
import com.socialnetwork.boardrift.util.mapper.UserMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;

import static org.mockito.ArgumentMatchers.any;

@ExtendWith({MockitoExtension.class})
public class ModeratorServiceUnitTests {
    @Mock
    WarningRepository warningRepository;

    @Mock
    SuspensionRepository suspensionRepository;

    @Mock
    UserMapper userMapper;

    @Mock
    UserService userService;

    @Mock
    JwtService jwtService;

    @Mock
    NotificationService notificationService;

    @Mock
    SimpMessagingTemplate simpMessagingTemplate;

    @InjectMocks
    @Spy
    ModeratorService moderatorService;

    UserEntity userEntity;
    UserEntity userEntity2;
    UserRetrievalMinimalDto userRetrievalMinimalDto;
    UserRetrievalMinimalDto userRetrievalMinimalDto2;
    WarningEntity warningEntity;
    WarningDto warningDto;
    SuspensionDto suspensionDto;
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
        userRetrievalMinimalDto = new UserRetrievalMinimalDto(1L, "Name", "Lastname", "", false, 0, false);

        userRetrievalMinimalDto2 = new UserRetrievalMinimalDto(2L, "Name2", "Lastname2", "", false, 0, false);
        warningEntity = new WarningEntity(1L, userEntity2, "reason", new Date(), userEntity);
        warningDto = new WarningDto(1L, "reason", new Date());
        suspensionDto = new SuspensionDto(1, "reason");
        securityContext.setAuthentication(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void warnUserShouldSucceed() throws IllegalAccessException {
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        Mockito.when(authentication.getPrincipal()).thenReturn(userEntity);
        Mockito.when(userService.getUserEntityByEmail(any())).thenReturn(userEntity);
        Mockito.when(userService.getUserEntityById(any())).thenReturn(userEntity2);
        Mockito.when(warningRepository.save(any())).thenReturn(warningEntity);
        Mockito.when(notificationService.createAndSaveWarningNotification(any())).thenReturn(new NotificationEntity());

        WarningDto result = moderatorService.warnUser(1L, warningDto);

        Assertions.assertEquals(warningDto.getId(), result.getId());
    }

    @Test
    void warnUserShouldSucceedAndRecipientShouldGetSuspended() throws IllegalAccessException {
        ArrayList<WarningEntity> warningEntities = new ArrayList<>();
        warningEntities.add(warningEntity);
        warningEntities.add(warningEntity);
        userEntity2.setReceivedWarnings(warningEntities);
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        Mockito.when(authentication.getPrincipal()).thenReturn(userEntity);
        Mockito.when(userService.getUserEntityByEmail(any())).thenReturn(userEntity);
        Mockito.when(userService.getUserEntityById(any())).thenReturn(userEntity2);
        Mockito.when(warningRepository.save(any())).thenReturn(warningEntity);

        Mockito.when(userService.getUserEntityById(any())).thenReturn(userEntity2);
        Mockito.when(suspensionRepository.save(any())).thenReturn(new SuspensionEntity());
        Mockito.doNothing().when(simpMessagingTemplate).convertAndSendToUser(any(), any(), any());
        Mockito.doNothing().when(jwtService).deleteRefreshTokenByUserId(any());
        Mockito.when(userMapper.entityToMinimalRetrievalDto(any())).thenReturn(userRetrievalMinimalDto);
        WarningDto result = moderatorService.warnUser(1L, warningDto);

        Assertions.assertEquals(warningDto.getId(), result.getId());
        Mockito.verify(moderatorService, Mockito.times(1)).suspendUser(any(), any());
    }

    @Test
    void warnUserShouldFailWhenTryingToWarnModerator() {
        userEntity2.setRole(Role.ROLE_ADMIN);
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        Mockito.when(authentication.getPrincipal()).thenReturn(userEntity);
        Mockito.when(userService.getUserEntityByEmail(any())).thenReturn(userEntity);
        Mockito.when(userService.getUserEntityById(any())).thenReturn(userEntity2);

        Assertions.assertThrows(IllegalAccessException.class, () -> moderatorService.warnUser(1L, warningDto));
    }

    @Test
    void warnUserShouldFailWhenTooManyWarnings() {
        ArrayList<WarningEntity> warningEntities = new ArrayList<>();
        warningEntities.add(warningEntity);
        warningEntities.add(warningEntity);
        warningEntities.add(warningEntity);

        userEntity2.setReceivedWarnings(warningEntities);

        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        Mockito.when(authentication.getPrincipal()).thenReturn(userEntity);
        Mockito.when(userService.getUserEntityByEmail(any())).thenReturn(userEntity);
        Mockito.when(userService.getUserEntityById(any())).thenReturn(userEntity2);

        Assertions.assertThrows(TooManyWarningsException.class, () -> moderatorService.warnUser(1L, warningDto));
    }

    @Test
    void suspendUserShouldSucceed() throws IllegalAccessException {
        Mockito.when(userService.getUserEntityById(any())).thenReturn(userEntity2);
        Mockito.when(suspensionRepository.save(any())).thenReturn(new SuspensionEntity());
        Mockito.doNothing().when(simpMessagingTemplate).convertAndSendToUser(any(), any(), any());
        Mockito.doNothing().when(jwtService).deleteRefreshTokenByUserId(any());
        Mockito.when(userMapper.entityToMinimalRetrievalDto(any())).thenReturn(userRetrievalMinimalDto);

        moderatorService.suspendUser(1L, suspensionDto);
    }

    @Test
    void suspendUserShouldFailWhenTryingToSuspendModerator() {
        userEntity2.setRole(Role.ROLE_ADMIN);
        Mockito.when(userService.getUserEntityById(any())).thenReturn(userEntity2);

        Assertions.assertThrows(IllegalAccessException.class, () -> moderatorService.suspendUser(1L, suspensionDto));
    }

    @Test
    void suspendUserShouldFailWhenUserAlreadySuspended() {
        userEntity2.setSuspension(new SuspensionEntity());
        Mockito.when(userService.getUserEntityById(any())).thenReturn(userEntity2);

        Assertions.assertThrows(UserAlreadySuspendedException.class, () -> moderatorService.suspendUser(1L, suspensionDto));
    }

    @Test
    void deleteWarningShouldSucceedAndDeleteSuspension() {
        ArrayList<WarningEntity> warningEntities = new ArrayList<>();
        warningEntities.add(warningEntity);
        warningEntities.add(warningEntity);
        warningEntities.add(warningEntity);

        userEntity2.setReceivedWarnings(warningEntities);
        Mockito.when(userService.getUserEntityById(any())).thenReturn(userEntity2);
        Mockito.doNothing().when(warningRepository).deleteByIdAndRecipientId(any(), any());
        Mockito.doNothing().when(moderatorService).deleteSuspension(any());

        moderatorService.deleteWarning(1L, 1L);
        Mockito.verify(moderatorService, Mockito.times(1)).deleteSuspension(any());
    }

    @Test
    void deleteSuspensionShouldSucceedAndClearWarnings() {
        ArrayList<WarningEntity> warningEntities = new ArrayList<>();
        warningEntities.add(warningEntity);
        warningEntities.add(warningEntity);
        warningEntities.add(warningEntity);

        userEntity2.setReceivedWarnings(warningEntities);
        Mockito.when(userService.getUserEntityById(any())).thenReturn(userEntity2);
        Mockito.doNothing().when(suspensionRepository).delete(any());

        moderatorService.deleteSuspension(1L);
    }
}
