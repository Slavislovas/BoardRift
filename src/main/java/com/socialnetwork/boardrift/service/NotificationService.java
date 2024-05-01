package com.socialnetwork.boardrift.service;

import com.socialnetwork.boardrift.repository.NotificationRepository;
import com.socialnetwork.boardrift.repository.UserRepository;
import com.socialnetwork.boardrift.repository.model.NotificationEntity;
import com.socialnetwork.boardrift.repository.model.WarningEntity;
import com.socialnetwork.boardrift.repository.model.board_game.PlayedGameEntity;
import com.socialnetwork.boardrift.repository.model.user.UserEntity;
import com.socialnetwork.boardrift.rest.model.NotificationDto;
import com.socialnetwork.boardrift.rest.model.NotificationPageDto;
import com.socialnetwork.boardrift.util.mapper.NotificationMapper;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Service
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final NotificationMapper notificationMapper;
    private final SimpMessagingTemplate messagingTemplate;

    public NotificationPageDto getNotifications(Integer page, Integer pageSize, HttpServletRequest request) {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        UserEntity loggedInUserEntity = userRepository
                .findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new EntityNotFoundException("User with username: " + userDetails.getUsername() + " was not found"));

        Pageable pageable = PageRequest.of(page, pageSize,  Sort.by("creationDate").descending());

        List<NotificationEntity> notificationEntities = notificationRepository.findByRecipientId(loggedInUserEntity.getId(), pageable);

        Boolean unreadMessages = notificationEntities.stream().anyMatch(NotificationEntity::getUnread);

        List<NotificationDto> notificationDtos = notificationEntities.stream()
                .map(notificationMapper::entityToDto).toList();

        String nextPageUrl = notificationDtos.size() == pageSize ? String.format("%s%s?page=%d&pageSize=%d",
                ServletUriComponentsBuilder.fromCurrentContextPath().toUriString(),
                request.getServletPath(),
                page + 1,
                pageSize) : null;

        return new NotificationPageDto(nextPageUrl, unreadMessages, notificationDtos);
    }

    public List<NotificationEntity> createAndSaveNotificationsForAssociatedPlays(PlayedGameEntity playedGameEntity) {
        List<NotificationEntity> notifications = new ArrayList<>();

        for (PlayedGameEntity associatedPlay : playedGameEntity.getAssociatedPlays()) {
            NotificationEntity notification = new NotificationEntity(null,
                    formatAssociatedPlayConfirmationNotificationDescription(playedGameEntity),
                    formatAssociatedPlayConfirmationRedirectUrl(associatedPlay), new Date(),
                    true, associatedPlay.getUser());
            notification = notificationRepository.save(notification);
            notifications.add(notification);

            messagingTemplate.convertAndSendToUser(
                    notification.getRecipient().getId().toString(),
                    "/queue/notifications",
                    notificationMapper.entityToDto(notification));
        }

        return notifications;
    }

    private String formatAssociatedPlayConfirmationRedirectUrl(PlayedGameEntity associatedPlay) {
        return String.format("/plays/%d/confirmation", associatedPlay.getId());
    }

    private String formatAssociatedPlayConfirmationNotificationDescription(PlayedGameEntity parentPlay) {
        return String.format("Your friend, %s %s has just logged a play for %s in which you have participated. Do you want to add this play to your play log? Click this notification to take a look.",
                 parentPlay.getUser().getName(), parentPlay.getUser().getLastname(), parentPlay.getGameName());
    }

    public NotificationEntity createAndSaveWarningNotification(WarningEntity warningEntity) {
        NotificationEntity notification = new NotificationEntity(null,
                formatWarningNotificationDescription(warningEntity),
                null, new Date(),
                true, warningEntity.getRecipient());

        notification = notificationRepository.save(notification);

        messagingTemplate.convertAndSendToUser(
                notification.getRecipient().getId().toString(),
                "/queue/notifications",
                notificationMapper.entityToDto(notification));

        return notification;
    }

    private String formatWarningNotificationDescription(WarningEntity warningEntity) {
        return String.format("An administrator has warned you for %s, you currently have %d warnings. If you get 3 warnings you will automatically be suspended for 90 days",
                warningEntity.getReason(), warningEntity.getRecipient().getReceivedWarnings().size());
    }

    public Map<String, Object> setNotificationToRead(Long notificationId) throws IllegalAccessException {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        UserEntity loggedInUserEntity = userRepository
                .findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new EntityNotFoundException("User with username: " + userDetails.getUsername() + " was not found"));
        NotificationEntity notificationEntity = notificationRepository
                .findById(notificationId)
                .orElseThrow(() -> new EntityNotFoundException("Notification with id: " + notificationId + " was not found"));

        if (!notificationEntity.getRecipient().getId().equals(loggedInUserEntity.getId())) {
            throw new IllegalAccessException("You cannot manipulate another users notifications");
        }

        notificationEntity.setUnread(false);
        notificationRepository.save(notificationEntity);

        return Map.of("unreadNotificationsLeft", notificationRepository.existsByRecipientIdAndUnreadTrue(loggedInUserEntity.getId()));
    }

    public void deleteNotification(Long notificationId) throws IllegalAccessException {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        UserEntity loggedInUserEntity = userRepository
                .findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new EntityNotFoundException("User with username: " + userDetails.getUsername() + " was not found"));
        NotificationEntity notificationEntity = notificationRepository
                .findById(notificationId)
                .orElseThrow(() -> new EntityNotFoundException("Notification with id: " + notificationId + " was not found"));

        if (!notificationEntity.getRecipient().getId().equals(loggedInUserEntity.getId())) {
            throw new IllegalAccessException("You cannot manipulate another users notifications");
        }

        notificationRepository.delete(notificationEntity);
    }
}
