package com.socialnetwork.boardrift.service;

import com.socialnetwork.boardrift.enumeration.Role;
import com.socialnetwork.boardrift.repository.SuspensionRepository;
import com.socialnetwork.boardrift.repository.WarningRepository;
import com.socialnetwork.boardrift.repository.model.user.SuspensionEntity;
import com.socialnetwork.boardrift.repository.model.user.UserEntity;
import com.socialnetwork.boardrift.repository.model.WarningEntity;
import com.socialnetwork.boardrift.rest.model.WarningDto;
import com.socialnetwork.boardrift.rest.model.user.SuspensionDto;
import com.socialnetwork.boardrift.util.exception.TooManyWarningsException;
import com.socialnetwork.boardrift.util.exception.UserAlreadySuspendedException;
import com.socialnetwork.boardrift.util.mapper.UserMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RequiredArgsConstructor
@Service
public class ModeratorService {
    private final WarningRepository warningRepository;
    private final SuspensionRepository suspensionRepository;
    private final UserMapper userMapper;
    private final UserService userService;
    private final JwtService jwtService;
    private final NotificationService notificationService;
    private final SimpMessagingTemplate messagingTemplate;

    public WarningDto warnUser(Long userId, WarningDto warningDto) throws IllegalAccessException {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        UserEntity administratorEntity = userService.getUserEntityByEmail(userDetails.getUsername());

        UserEntity recipientEntity = userService.getUserEntityById(userId);

        if (recipientEntity.getRole().equals(Role.ROLE_ADMIN)) {
            throw new IllegalAccessException("You cannot warn an administrator");
        }

        if (recipientEntity.getReceivedWarnings().size() == 3) {
            throw new TooManyWarningsException("This user already has 3 warnings");
        }

        WarningEntity warningEntity = new WarningEntity(null, recipientEntity, warningDto.getReason(), new Date(), administratorEntity);
        warningEntity = warningRepository.save(warningEntity);

        recipientEntity.getReceivedWarnings().add(warningEntity);

        if (recipientEntity.getReceivedWarnings().size() >= 3) {
            suspendUser(userId, new SuspensionDto(90, formatSuspensionReasonFromWarnings(recipientEntity.getReceivedWarnings())));
        } else {
            notificationService.createAndSaveWarningNotification(warningEntity);
        }

        return new WarningDto(warningEntity.getId(), warningDto.getReason(), warningEntity.getIssuedDate());
    }

    private String formatSuspensionReasonFromWarnings(List<WarningEntity> warnings) {
        StringBuilder stringBuilder = new StringBuilder();

        for (WarningEntity warning : warnings) {
            stringBuilder.append("* ").append(warning.getReason()).append("\n");
        }

        return stringBuilder.toString();
    }

    public void suspendUser(Long userId, SuspensionDto suspensionDto) throws IllegalAccessException {
        UserEntity userEntity = userService.getUserEntityById(userId);

        if (userEntity.getRole().equals(Role.ROLE_ADMIN)) {
            throw new IllegalAccessException("You cannot suspend an administrator");
        }

        if (!userEntity.isEnabled()) {
            throw new UserAlreadySuspendedException();
        }

        Set<UserEntity> userFriends = new HashSet<>();
        userFriends.addAll(userEntity.getFriends());
        userFriends.addAll(userEntity.getFriendOf());

        SuspensionEntity suspensionEntity = new SuspensionEntity(null, LocalDate.now().plusDays(suspensionDto.getDaysOfSuspension()), suspensionDto.getReason(), userEntity);
        suspensionRepository.save(suspensionEntity);

        userFriends.forEach(friend -> messagingTemplate.convertAndSendToUser(
                friend.getId().toString(),
                "/queue/unfriend",
                userMapper.entityToMinimalRetrievalDto(userEntity)));

        messagingTemplate.convertAndSendToUser(
              userEntity.getId().toString(),
                "/queue/disconnect",
                userMapper.entityToMinimalRetrievalDto(userEntity));

        jwtService.deleteRefreshTokenByUserId(userEntity.getId());
    }

    @Transactional
    public void deleteWarning(Long userId, Long warningId) {
        UserEntity userEntity = userService.getUserEntityById(userId);

        warningRepository.deleteByIdAndRecipientId(userId, warningId);

        if (userEntity.getReceivedWarnings().size() == 3) {
            deleteSuspension(userId);
        }
    }

    @Transactional
    public void deleteSuspension(Long userId) {
        UserEntity userEntity = userService.getUserEntityById(userId);

        if (userEntity.getReceivedWarnings().size() == 3) {
            userEntity.getReceivedWarnings().clear();
        }

        suspensionRepository.delete(userEntity.getSuspension());
        userEntity.setSuspension(null);
    }
}
