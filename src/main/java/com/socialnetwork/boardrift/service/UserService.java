package com.socialnetwork.boardrift.service;

import com.socialnetwork.boardrift.enumeration.Role;
import com.socialnetwork.boardrift.repository.PlayedGameRepository;
import com.socialnetwork.boardrift.repository.UserRepository;
import com.socialnetwork.boardrift.repository.model.VerificationTokenEntity;
import com.socialnetwork.boardrift.repository.model.board_game.PlayedGameEntity;
import com.socialnetwork.boardrift.repository.model.post.PlayedGamePostEntity;
import com.socialnetwork.boardrift.repository.model.user.UserEntity;
import com.socialnetwork.boardrift.rest.model.BGGThingResponse;
import com.socialnetwork.boardrift.rest.model.FriendRequestDto;
import com.socialnetwork.boardrift.rest.model.PlayedGamePageDto;
import com.socialnetwork.boardrift.rest.model.post.played_game_post.PlayedGameDto;
import com.socialnetwork.boardrift.rest.model.statistics.FriendStatisticsDto;
import com.socialnetwork.boardrift.rest.model.statistics.GameStatisticsDto;
import com.socialnetwork.boardrift.rest.model.statistics.UserStatisticsDto;
import com.socialnetwork.boardrift.rest.model.user.UserEditDto;
import com.socialnetwork.boardrift.rest.model.user.UserRegistrationDto;
import com.socialnetwork.boardrift.rest.model.user.UserRetrievalDto;
import com.socialnetwork.boardrift.rest.model.user.UserRetrievalMinimalDto;
import com.socialnetwork.boardrift.util.exception.DuplicateFriendRequestException;
import com.socialnetwork.boardrift.util.exception.FieldValidationException;
import com.socialnetwork.boardrift.util.mapper.PlayedGameMapper;
import com.socialnetwork.boardrift.util.mapper.UserMapper;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class UserService {
    private final UserRepository userRepository;
    private final PlayedGameRepository playedGameRepository;
    private final UserMapper userMapper;
    private final PlayedGameMapper playedGameMapper;
    private final BCryptPasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final BoardGameService boardGameService;
    private final AwsService awsService;
    private final ChatRoomService chatRoomService;
    private final NotificationService notificationService;
    private final SimpMessagingTemplate messagingTemplate;

    @Value("${aws.s3.default-profile-picture-url}")
    private String defaultUserProfilePictureUrl;

    private static final long MAX_PROFILE_PICTURE_SIZE_BYTES = 2 * 1024 * 1024;

    public UserEntity getUserEntityById(Long userId) {
        return userRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException("User with id: " + userId + " was not found"));
    }

    public UserEntity getUserEntityByEmail(String email) {
        return userRepository.findByEmail(email).orElseThrow(() -> new EntityNotFoundException("User with email: " + email + " was not found"));
    }

    public UserRetrievalDto createUser(UserRegistrationDto userRegistrationDto, HttpServletRequest servletRequest) {
        verifyIfEmailUnique(userRegistrationDto.getEmail());

        UserEntity userEntity = userMapper.registrationDtoToEntity(userRegistrationDto);
        userEntity.setPassword(passwordEncoder.encode(userEntity.getPassword()));
        userEntity.setBio("");
        userEntity.setProfilePictureUrl(defaultUserProfilePictureUrl);
        userEntity.setCity("");
        userEntity.setCountry("");
        userEntity = userRepository.save(userEntity);

        emailService.sendEmailVerification(servletRequest, userEntity);

        return userMapper.entityToRetrievalDto(userEntity, null, null, null, null);
    }

    private void verifyIfEmailUnique(String email) {
        Optional<UserEntity> optionalUserEntityByEmail = userRepository.findByEmail(email);

        Map<String, String> duplicateValueMap = new HashMap<>();

        optionalUserEntityByEmail.ifPresent(userEntity -> duplicateValueMap.put("email", "Email " + userEntity.getEmail() + " is taken"));

        if (!duplicateValueMap.isEmpty()) {
            throw new FieldValidationException(duplicateValueMap);
        }
    }

    public void confirmUserRegistration(String token) {
        VerificationTokenEntity emailVerificationTokenEntity = emailService.getEmailVerificationToken(token);
        emailService.validateEmailVerificationTokenExpiration(emailVerificationTokenEntity);

        UserEntity userEntity = emailVerificationTokenEntity.getUser();
        userEntity.setEmailVerified(true);
        userRepository.save(userEntity);
    }

    public FriendRequestDto sendFriendRequest(Long receiverId) throws IllegalAccessException {
        UserDetails senderUserDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        UserEntity senderUserEntity = userRepository.findByEmail(senderUserDetails.getUsername()).orElseThrow(() -> new EntityNotFoundException("User with email: " + senderUserDetails.getUsername() + " was not found"));
        UserEntity receiverUserEntity = userRepository.findById(receiverId).orElseThrow(() -> new EntityNotFoundException("User with id: " + receiverId + " was not found"));

        validateFriendRequest(receiverUserEntity, senderUserEntity);

        senderUserEntity.addSentFriendRequest(receiverUserEntity);
        userRepository.save(senderUserEntity);

        messagingTemplate.convertAndSendToUser(
                receiverUserEntity.getId().toString(),
                "/queue/friend-requests/received",
                userMapper.entityToMinimalRetrievalDto(senderUserEntity));

        return new FriendRequestDto(userMapper.entityToMinimalRetrievalDto(senderUserEntity), userMapper.entityToMinimalRetrievalDto(receiverUserEntity));
    }

    private void validateFriendRequest(UserEntity receiverUserEntity, UserEntity senderUserEntity) throws IllegalAccessException {
        if (!receiverUserEntity.isEnabled()) {
            throw new IllegalAccessException("You cannot send a friend request to a suspended user");
        }

        if (senderUserEntity.getId().equals(receiverUserEntity.getId())) {
            throw new DuplicateFriendRequestException("You cannot send a friend request to yourself");
        }

        if (friendRequestAlreadySent(senderUserEntity, receiverUserEntity.getId())) {
            throw new DuplicateFriendRequestException("You have already sent a friend request to this user");
        }

        if (recipientAlreadyFriend(senderUserEntity, receiverUserEntity.getId())) {
            throw new DuplicateFriendRequestException("This user is already your friend");
        }

        if (receiverAlreadySentFriendRequest(senderUserEntity, receiverUserEntity.getId())) {
            throw new DuplicateFriendRequestException("The friend request receiver has already sent you a friend request");
        }
    }

    public boolean recipientAlreadyFriend(UserEntity senderUserEntity, Long receiverId) {
        boolean hasFriend = senderUserEntity
                .getFriends()
                .stream()
                .anyMatch(friend -> friend.getId().equals(receiverId));

        boolean friendOf = senderUserEntity
                .getFriendOf()
                .stream()
                .anyMatch(friend -> friend.getId().equals(receiverId));

        return hasFriend || friendOf;
    }

    public boolean receiverAlreadySentFriendRequest(UserEntity senderUserEntity, Long receiverId) {
        return senderUserEntity
                .getReceivedFriendInvites()
                .stream()
                .anyMatch(userEntity -> userEntity.getId().equals(receiverId));
    }

    public boolean friendRequestAlreadySent(UserEntity senderUserDetails, Long receiverId) {
        return senderUserDetails
                .getSentFriendInvites()
                .stream()
                .anyMatch(userEntity -> userEntity.getId().equals(receiverId));
    }

    public UserRetrievalMinimalDto acceptFriendRequest(Long senderId) {
        UserDetails receiverUserDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        UserEntity receiverUserEntity = userRepository.findByEmail(receiverUserDetails.getUsername()).orElseThrow(() -> new EntityNotFoundException("User with email: " + receiverUserDetails.getUsername() + " was not found"));

        if (senderHasNotSentFriendRequest(receiverUserEntity, senderId)) {
            throw new EntityNotFoundException("Friend request from user with id: " + senderId + " not found");
        }

        UserEntity senderUserEntity = userRepository.findById(senderId).orElseThrow(() -> new EntityNotFoundException("Friend request sender with id: " + senderId + " was not found"));
        receiverUserEntity.addFriend(senderUserEntity);
        receiverUserEntity.removeReceivedFriendRequest(senderUserEntity);
        userRepository.save(receiverUserEntity);

        UserRetrievalMinimalDto senderDto = userMapper.entityToMinimalRetrievalDto(senderUserEntity);
        UserRetrievalMinimalDto receiverDto = userMapper.entityToMinimalRetrievalDto(receiverUserEntity);

        messagingTemplate.convertAndSendToUser(
                receiverUserEntity.getId().toString(),
                "/queue/friend-requests/accepted",
                senderDto);

        messagingTemplate.convertAndSendToUser(
                senderUserEntity.getId().toString(),
                "/queue/friend-requests/accepted",
                receiverDto);

        return senderDto;
    }

    public String declineFriendRequest(Long senderId) {
        UserDetails receiverUserDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        UserEntity receiverUserEntity = userRepository.findByEmail(receiverUserDetails.getUsername()).orElseThrow(() -> new EntityNotFoundException("User with username: " + receiverUserDetails.getUsername() + " was not found"));

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

    public List<UserRetrievalMinimalDto> getReceivedFriendRequests(Integer pageNumber, Integer pageSize) {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        UserEntity userEntity = userRepository.findByEmail(userDetails.getUsername()).orElseThrow(() -> new EntityNotFoundException("User with username: " + userDetails.getUsername() + " was not found"));

        if (pageNumber != null && pageSize != null) {
            int start = pageNumber * pageSize;
            int end = Math.min(start + pageSize, userEntity.getReceivedFriendInvites().size());

            return userEntity.getReceivedFriendInvites()
                    .stream()
                    .skip(start)
                    .limit(end - start)
                    .map(userMapper::entityToMinimalRetrievalDto)
                    .collect(Collectors.toList());
        } else {
            return userEntity.getReceivedFriendInvites()
                    .stream()
                    .map(userMapper::entityToMinimalRetrievalDto)
                    .collect(Collectors.toList());
        }
    }

    public Set<UserRetrievalMinimalDto> getSentFriendRequests() {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        UserEntity userEntity = userRepository.findByEmail(userDetails.getUsername()).orElseThrow(() -> new EntityNotFoundException("User with username: " + userDetails.getUsername() + " was not found"));

        return userEntity.getSentFriendInvites()
                .stream()
                .map(userMapper::entityToMinimalRetrievalDto)
                .collect(Collectors.toSet());
    }

    public Set<UserRetrievalMinimalDto> getFriends(Long userId) throws IllegalAccessException {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        UserEntity loggedInUserEntity = userRepository.findByEmail(userDetails.getUsername()).orElseThrow(() -> new EntityNotFoundException("User with username: " + userDetails.getUsername() + " was not found"));

        UserEntity targetUserEntity = userRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException("User with id: " + userDetails.getUsername() + " was not found"));

        if (!Role.ROLE_ADMIN.name().equals(loggedInUserEntity.getRole().name())) {
            if (!targetUserEntity.getEmail().equals(userDetails.getUsername())) {
                if (!targetUserEntity.getPublicFriendsList() && !recipientAlreadyFriend(loggedInUserEntity, targetUserEntity.getId())) {
                    throw new IllegalAccessException("You cannot view this user's friend list");
                }
            }
        }

        Set<UserRetrievalMinimalDto> friends = targetUserEntity.getFriends()
                .stream()
                .filter(UserEntity::isEnabled)
                .map(friend -> {
                  UserRetrievalMinimalDto friendDto = userMapper.entityToMinimalRetrievalDto(friend);
                  friendDto.setUnreadMessages(
                          targetUserEntity.getReceivedChatMessages()
                                  .stream()
                                  .anyMatch(chatMessageEntity -> chatMessageEntity.getUnread() && chatMessageEntity.getSender().getId().equals(friendDto.getId())));
                  return friendDto;
                }).collect(Collectors.toSet());

        friends.addAll(
                targetUserEntity.getFriendOf()
                        .stream()
                        .filter(UserEntity::isEnabled)
                        .map(friend -> {
                            UserRetrievalMinimalDto friendDto = userMapper.entityToMinimalRetrievalDto(friend);
                            friendDto.setUnreadMessages(
                                    targetUserEntity.getReceivedChatMessages()
                                            .stream()
                                            .anyMatch(chatMessageEntity -> chatMessageEntity.getUnread() && chatMessageEntity.getSender().getId().equals(friendDto.getId())));
                            return friendDto;
                        }).collect(Collectors.toSet())
        );

        friends = friends.stream()
                .sorted(Comparator.comparing(UserRetrievalMinimalDto::getUnreadMessages, Comparator.reverseOrder()))
                .collect(Collectors.toCollection(LinkedHashSet::new));

        return friends;
    }

    public Set<UserRetrievalMinimalDto> searchUsers(String query, Integer page, Integer pageSize) {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String role = userDetails.getAuthorities().stream().findFirst().get().getAuthority();

        Pageable pageable = PageRequest.of(page, pageSize, Sort.by("name").and(Sort.by("lastname")));

        if (query == null || query.trim().isEmpty()) {
            if (role.equals(Role.ROLE_ADMIN.name())) {
                return userRepository.findAll(pageable)
                        .stream()
                        .map(userMapper::entityToMinimalRetrievalDto)
                        .collect(Collectors.toSet());
            }

            return userRepository.findBySuspensionIsNull(pageable)
                    .stream()
                    .map(userMapper::entityToMinimalRetrievalDto)
                    .collect(Collectors.toSet());

        }

        String[] names = query.trim().split("\\s+");
        String name = names[0];
        String lastname = names.length > 1 ? names[1] : "";

        if (lastname.equals("")) {
            if (role.equals(Role.ROLE_ADMIN.name())) {
                return userRepository.findByNameContainingIgnoreCaseOrLastnameContainingIgnoreCase(name, name, pageable)
                        .stream()
                        .map(userMapper::entityToMinimalRetrievalDto)
                        .collect(Collectors.toSet());
            }

            return userRepository.findByNameContainingIgnoreCaseAndSuspensionIsNullOrLastnameContainingIgnoreCaseAndSuspensionIsNull(name, name, pageable)
                    .stream()
                    .map(userMapper::entityToMinimalRetrievalDto)
                    .collect(Collectors.toSet());
        }

        if (role.equals(Role.ROLE_ADMIN.name())) {
            return userRepository.findByNameContainingIgnoreCaseOrLastnameContainingIgnoreCase(name, lastname, pageable)
                    .stream()
                    .map(userMapper::entityToMinimalRetrievalDto)
                    .collect(Collectors.toSet());
        }

        return userRepository.findByNameContainingIgnoreCaseAndSuspensionIsNullOrLastnameContainingIgnoreCaseAndSuspensionIsNull(name, lastname, pageable)
                .stream()
                .map(userMapper::entityToMinimalRetrievalDto)
                .collect(Collectors.toSet());
    }

    public UserRetrievalDto getUserById(Long userId) throws IllegalAccessException {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        UserEntity loggedInUserEntity = userRepository.findByEmail(userDetails.getUsername()).orElseThrow(() -> new EntityNotFoundException("User with username: " + userDetails.getUsername() + " was not found"));
        UserEntity userEntity = userRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException("User with id: " + userId + " was not found"));

        if (!loggedInUserEntity.getRole().equals(Role.ROLE_ADMIN) && !userEntity.isEnabled()) {
            throw new IllegalAccessException("This user has been suspended");
        }

        boolean userAlreadyFriend = recipientAlreadyFriend(loggedInUserEntity, userId);
        boolean friendRequestAlreadySent = friendRequestAlreadySent(loggedInUserEntity, userId);
        boolean alreadyReceivedFriendRequest = receiverAlreadySentFriendRequest(loggedInUserEntity, userId);
        boolean personalData = loggedInUserEntity.getId().equals(userEntity.getId());

        UserRetrievalDto userRetrievalDto = userMapper.entityToRetrievalDto(userEntity, userAlreadyFriend, friendRequestAlreadySent, alreadyReceivedFriendRequest, personalData);

        if (!loggedInUserEntity.getRole().equals(Role.ROLE_ADMIN) && !loggedInUserEntity.getId().equals(userId)) {
            userRetrievalDto.setReceivedWarnings(null);
        }

        return userRetrievalDto;
    }

    public PlayedGamePageDto getPlayedGamesByUserId(Long userId, Integer page, Integer pageSize, HttpServletRequest request) throws IllegalAccessException {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String role = userDetails.getAuthorities().stream().findFirst().get().getAuthority();

        UserEntity userEntity = userRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException("User with id: " + userId + " was not found"));

        if (!Role.ROLE_ADMIN.name().equals(role)) {
            if (!userEntity.getEmail().equals(userDetails.getUsername())) {
                if (!userEntity.getPublicPlays()) {
                    throw new IllegalAccessException("You cannot view this user's plays");
                }
            }
        }

        PageRequest pageRequest = PageRequest.of(page, pageSize, Sort.by("creationDate").descending());
        List<PlayedGameEntity> playedGames = playedGameRepository.findByUserId(userId, pageRequest);

        List<PlayedGameDto> playedGameDtoList = playedGames.stream()
                .map(playedGameMapper::entityToDto).toList();

        String nextPageUrl = playedGameDtoList.size() == pageSize ? String.format("%s%s?page=%d&pageSize=%d",
                ServletUriComponentsBuilder.fromCurrentContextPath().toUriString(),
                request.getServletPath(),
                page + 1,
                pageSize) : null;

        return new PlayedGamePageDto(nextPageUrl, playedGameDtoList);
    }

    public PlayedGameDto getPlayedGameByPlayId(Long playId) throws IllegalAccessException {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        UserEntity loggedInUserEntity = userRepository.findByEmail(userDetails.getUsername()).orElseThrow(() -> new EntityNotFoundException("User with username: " + userDetails.getUsername() + " was not found"));

        PlayedGameEntity playedGameEntity = playedGameRepository.findById(playId).orElseThrow(() -> new EntityNotFoundException("Played game with id " + playId + " was not found"));

        Boolean loggedInUserInAssociatedPlays = playedGameEntity.getAssociatedPlays().stream().anyMatch(play -> play.getUser().getId().equals(loggedInUserEntity.getId()));
        Boolean loggedInUserInAssociatedWWithPlays = playedGameEntity.getAssociatedWith().stream().anyMatch(play -> play.getUser().getId().equals(loggedInUserEntity.getId()));

        if (!Role.ROLE_ADMIN.name().equals(loggedInUserEntity.getRole().name())) {
            if (!loggedInUserEntity.getEmail().equals(userDetails.getUsername()) && !loggedInUserInAssociatedPlays && !loggedInUserInAssociatedWWithPlays) {
                if (!loggedInUserEntity.getPublicPlays()) {
                    throw new IllegalAccessException("You cannot view this user's play");
                }
            }
        }

        return playedGameMapper.entityToDto(playedGameEntity);
    }

    public PlayedGameDto logPlayedGame(PlayedGameDto playedGameDto) {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        UserEntity loggedInUserEntity = userRepository.findByEmail(userDetails.getUsername()).orElseThrow(() -> new EntityNotFoundException("User with username: " + userDetails.getUsername() + " was not found"));


        BGGThingResponse boardGame = boardGameService.getBoardGameById(playedGameDto.getBggGameId());

        PlayedGameEntity playedGameEntity = switch (playedGameDto.getScoringSystem()) {
            case "highest-score" -> createHighestScorePlayedGameEntity(playedGameDto, loggedInUserEntity, boardGame);
            case "lowest-score" -> createLowestScorePlayedGameEntity(playedGameDto, loggedInUserEntity, boardGame);
            case "no-score" -> createNoScorePlayedGameEntity(playedGameDto, loggedInUserEntity, boardGame);
            default -> throw new FieldValidationException(Map.of("scoringSystem", "Invalid scoring system"));
        };

        notificationService.createAndSaveNotificationsForAssociatedPlays(playedGameEntity);

        return playedGameMapper.entityToDto(playedGameEntity);
    }

    private PlayedGameEntity createHighestScorePlayedGameEntity(PlayedGameDto playedGameDto, UserEntity loggedInUserEntity, BGGThingResponse boardGame) {
        Integer max = calculateHighestScore(playedGameDto);

        PlayedGameEntity playedGameEntity = new PlayedGameEntity(playedGameDto.getId() != null ? playedGameDto.getId() : null, playedGameDto.getBggGameId(),
                boardGame.getItems().get(0).getNames().get(0).getValue(), boardGame.getItems().get(0).getImage(),
                boardGame.getItems().get(0).getBoardGameCategory(), playedGameDto.getScore(), playedGameDto.getScore().equals(max),
                playedGameDto.getScoringSystem(), new Date(), true, loggedInUserEntity, null, new HashSet<>(), new HashSet<>());

        for (PlayedGameDto associatedPlayedGameDto : playedGameDto.getAssociatedPlays()) {
            playedGameEntity.addAssociatedPlay(new PlayedGameEntity(associatedPlayedGameDto.getId() != null ? associatedPlayedGameDto.getId() : null, playedGameDto.getBggGameId(), boardGame.getItems().get(0).getNames().get(0).getValue(),
                    boardGame.getItems().get(0).getImage(), boardGame.getItems().get(0).getBoardGameCategory(), associatedPlayedGameDto.getScore(),
                    associatedPlayedGameDto.getScore().equals(max), playedGameDto.getScoringSystem(), new Date(), false, getUserEntityById(associatedPlayedGameDto.getUser().getId()), null, new HashSet<>(), new HashSet<>()));
        }

        return playedGameRepository.save(playedGameEntity);
    }

    private Integer calculateHighestScore(PlayedGameDto playedGameDto) {
        Integer max = playedGameDto.getScore();

        for (PlayedGameDto associatedPlayedGameDto : playedGameDto.getAssociatedPlays()) {
            if (associatedPlayedGameDto.getScore() > max) {
                max = associatedPlayedGameDto.getScore();
            }
        }

        return max;
    }

    private PlayedGameEntity createLowestScorePlayedGameEntity(PlayedGameDto playedGameDto, UserEntity loggedInUserEntity, BGGThingResponse boardGame) {
        Integer min = calculateLowestScore(playedGameDto);

        PlayedGameEntity playedGameEntity = new PlayedGameEntity(playedGameDto.getId() != null ? playedGameDto.getId() : null, playedGameDto.getBggGameId(),
                boardGame.getItems().get(0).getNames().get(0).getValue(), boardGame.getItems().get(0).getImage(),
                boardGame.getItems().get(0).getBoardGameCategory(), playedGameDto.getScore(), playedGameDto.getScore().equals(min),
                playedGameDto.getScoringSystem(), new Date(), true, loggedInUserEntity, null, new HashSet<>(), new HashSet<>());

        for (PlayedGameDto associatedPlayedGameDto : playedGameDto.getAssociatedPlays()) {
            playedGameEntity.addAssociatedPlay(new PlayedGameEntity(associatedPlayedGameDto.getId() != null ? associatedPlayedGameDto.getId() : null, playedGameDto.getBggGameId(),
                    boardGame.getItems().get(0).getNames().get(0).getValue(), boardGame.getItems().get(0).getImage(),
                    boardGame.getItems().get(0).getBoardGameCategory(), associatedPlayedGameDto.getScore(), associatedPlayedGameDto.getScore().equals(min),
                    playedGameDto.getScoringSystem(), new Date(), false, getUserEntityById(associatedPlayedGameDto.getUser().getId()), null, new HashSet<>(), new HashSet<>()));
        }

        return playedGameRepository.save(playedGameEntity);
    }

    private Integer calculateLowestScore(PlayedGameDto playedGameDto) {
        Integer min = playedGameDto.getScore();

        for (PlayedGameDto associatedPlayedGameDto : playedGameDto.getAssociatedPlays()) {
            if (associatedPlayedGameDto.getScore() < min) {
                min = associatedPlayedGameDto.getScore();
            }
        }

        return min;
    }

    private Double calculateAverageScore(PlayedGameDto playedGameDto) {
        if (playedGameDto.getScoringSystem().equals("no-score")) {
            return 0.0;
        }

        Double sum = playedGameDto.getScore().doubleValue();

        for (PlayedGameDto associatedPlayedGameDto : playedGameDto.getAssociatedPlays()) {
            if (associatedPlayedGameDto.getScore() != null) {
                sum += associatedPlayedGameDto.getScore();
            }
        }

        return Math.round(sum / (playedGameDto.getAssociatedPlays().size() + 1) * 100.0) / 100.0;
    }

    private PlayedGameEntity createNoScorePlayedGameEntity(PlayedGameDto playedGameDto, UserEntity loggedInUserEntity, BGGThingResponse boardGame) {
        PlayedGameEntity playedGameEntity = new PlayedGameEntity(playedGameDto.getId() != null ? playedGameDto.getId() : null, playedGameDto.getBggGameId(),
                boardGame.getItems().get(0).getNames().get(0).getValue(), boardGame.getItems().get(0).getImage(),
                boardGame.getItems().get(0).getBoardGameCategory(), playedGameDto.getScore() != null ? playedGameDto.getScore() : 0, playedGameDto.getWon(),
                playedGameDto.getScoringSystem(), new Date(), true, loggedInUserEntity, null, new HashSet<>(), new HashSet<>());

        for (PlayedGameDto associatedPlayedGameDto : playedGameDto.getAssociatedPlays()) {
            playedGameEntity.addAssociatedPlay(new PlayedGameEntity(associatedPlayedGameDto.getId() != null ? associatedPlayedGameDto.getId() : null, playedGameDto.getBggGameId(),
                    boardGame.getItems().get(0).getNames().get(0).getValue(), boardGame.getItems().get(0).getImage(),
                    boardGame.getItems().get(0).getBoardGameCategory(), associatedPlayedGameDto.getScore() != null ? associatedPlayedGameDto.getScore() : 0,
                    associatedPlayedGameDto.getWon(), playedGameDto.getScoringSystem(), new Date(), false,
                    getUserEntityById(associatedPlayedGameDto.getUser().getId()), null, new HashSet<>(), new HashSet<>()));
        }

        return playedGameRepository.save(playedGameEntity);
    }

    public void deletePlayedGameById(Long playId) throws IllegalAccessException {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        UserEntity loggedInUserEntity = userRepository.findByEmail(userDetails.getUsername()).orElseThrow(() -> new EntityNotFoundException("User with username: " + userDetails.getUsername() + " was not found"));

        PlayedGameEntity playedGameEntity = playedGameRepository.findById(playId).orElseThrow(() -> new EntityNotFoundException("Play with id: " + playId + " was not found"));

        if (!playedGameEntity.getUser().getId().equals(loggedInUserEntity.getId())) {
            throw new IllegalAccessException("You cannot delete another players play");
        }

        for (PlayedGameEntity associatedWithPlay : playedGameEntity.getAssociatedWith()) {
            PlayedGameEntity finalPlayedGameEntity = playedGameEntity;
            associatedWithPlay.getAssociatedPlays().removeIf(associatedPlay -> associatedPlay.getId().equals(finalPlayedGameEntity.getId()));
        }

        playedGameEntity.setAssociatedWith(null);

        playedGameEntity = playedGameRepository.save(playedGameEntity);

        playedGameRepository.delete(playedGameEntity);
    }

    public UserRetrievalDto editUserById(Long userId, MultipartFile profilePicture, UserEditDto userEditDto) throws IllegalAccessException {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String role = userDetails.getAuthorities().stream().findFirst().get().getAuthority();

        UserEntity userEntity = userRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException("User with id: " + userId + " was not found"));

        if (!Role.ROLE_ADMIN.name().equals(role)) {
            if (!userEntity.getEmail().equals(userDetails.getUsername())) {
                throw new IllegalAccessException("You cannot edit another users data");
            }
        }

        userEntity.setName(userEditDto.getName());
        userEntity.setLastname(userEditDto.getLastname());
        userEntity.setBio(userEditDto.getBio());
        userEntity.setCountry(userEditDto.getCountry());
        userEntity.setCity(userEditDto.getCity());
        userEntity.setPublicPosts(userEditDto.getPublicPosts());
        userEntity.setPublicFriendsList(userEditDto.getPublicFriendsList());
        userEntity.setPublicPlays(userEditDto.getPublicPlays());
        userEntity.setPublicStatistics(userEditDto.getPublicStatistics());

        if (profilePicture != null) {
            if (profilePicture.getSize() > MAX_PROFILE_PICTURE_SIZE_BYTES) {
                throw new FieldValidationException(Map.of("Profile picture", "Profile picture exceeds maximum size limit of 2MB"));
            }

            if (!userEntity.getProfilePictureUrl().contains("default")) {
                awsService.deleteProfilePicture(userEntity.getProfilePictureUrl());
            }

            String profilePictureUrl = awsService.uploadProfilePicture(userId, profilePicture);

            if (profilePictureUrl == null) {
                throw new FieldValidationException(Map.of("Profile picture", "Failed to upload profile picture"));
            }

            userEntity.setProfilePictureUrl(profilePictureUrl);
        }

        return userMapper.entityToRetrievalDto(userRepository.save(userEntity), null, null, null, null);
    }

    public void removeFromFriendsList(Long userId, Long friendId) throws IllegalAccessException {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String role = userDetails.getAuthorities().stream().findFirst().get().getAuthority();

        UserEntity userEntity = userRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException("User with id: " + userId + " was not found"));

        if (!Role.ROLE_ADMIN.name().equals(role)) {
            if (!userEntity.getEmail().equals(userDetails.getUsername())) {
                throw new IllegalAccessException("You cannot remove friends from another users friends list");
            }
        }

        UserEntity friendEntity = userRepository.findById(friendId).orElseThrow(() -> new EntityNotFoundException("Friend with id: " + userId + " was not found"));

        userEntity.removeFromFriendsList(friendId);

        Optional<String> chatId = chatRoomService.getChatRoomId(userId, friendId, false);
        chatId.ifPresent(chatRoomService::deleteChatRoomByChatId);

        userRepository.save(userEntity);

        messagingTemplate.convertAndSendToUser(
                friendEntity.getId().toString(),
                "/queue/unfriend",
                userMapper.entityToMinimalRetrievalDto(userEntity));

        messagingTemplate.convertAndSendToUser(
                userEntity.getId().toString(),
                "/queue/unfriend",
                userMapper.entityToMinimalRetrievalDto(friendEntity));
    }

    public UserStatisticsDto getStatisticsByUserId(Long userId) throws IllegalAccessException {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String role = userDetails.getAuthorities().stream().findFirst().get().getAuthority();

        UserEntity userEntity = userRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException("User with id: " + userId + " was not found"));

        if (!Role.ROLE_ADMIN.name().equals(role)) {
            if (!userEntity.getPublicStatistics()) {
                if (!userEntity.getEmail().equals(userDetails.getUsername())) {
                    throw new IllegalAccessException("You cannot view another users statistics");
                }
            }
        }

        UserStatisticsDto userStatisticsDto = new UserStatisticsDto();
        int totalGamesWon = 0;
        int totalGamesLost = 0;
        Map<UserRetrievalMinimalDto, Integer> amountOfTimesPlayedWithFriends = new HashMap<>();
        Map<Map<Long, UserRetrievalMinimalDto>, Integer> amountOfTimesPlayedSpecificGameWithFriends = new HashMap<>();
        List<GameStatisticsDto> gameStatisticsDtos = new ArrayList<>();

        List<PlayedGameEntity> filteredPlayedGames = userEntity.getPlayedGames().stream().filter(PlayedGameEntity::getIncludeToStatistics).toList();

        for (PlayedGameEntity playedGame : filteredPlayedGames) {
            GameStatisticsDto gameStatisticsDto = gameStatisticsDtos.stream()
                    .filter(dto -> dto.getBggGameId().equals(playedGame.getBggGameId()))
                    .findFirst()
                    .orElseGet(() -> {
                        GameStatisticsDto newDto = new GameStatisticsDto(
                                playedGame.getBggGameId(),
                                playedGame.getGamePictureUrl(),
                                playedGame.getGameName(),
                                playedGame.getGameCategory(),
                                0, 0, 0,
                                0, 0, 0,
                                new FriendStatisticsDto(null, 0)
                        );
                        gameStatisticsDtos.add(newDto);
                        return newDto;
                    });

            if (playedGame.getWon()) {
                gameStatisticsDto.incrementAmountOfTimesWon();
                totalGamesWon++;
            } else {
                gameStatisticsDto.incrementAmountOfTimesLost();
                totalGamesLost++;
            }

            if (!playedGame.getScoringSystem().equals("no-score")) {
                int score = playedGame.getScore();

                if (score > gameStatisticsDto.getHighestScore()) {
                    gameStatisticsDto.setHighestScore(score);
                }
                if (score < gameStatisticsDto.getLowestScore()) {
                    gameStatisticsDto.setLowestScore(score);
                }
            }

            countAmountOfTimesPlayedWithFriends(amountOfTimesPlayedWithFriends, amountOfTimesPlayedSpecificGameWithFriends, playedGame);
        }

        GameStatisticsDto favouriteGame = gameStatisticsDtos.stream()
                .max(Comparator.comparingInt(GameStatisticsDto::getTotalPlays))
                .orElse(null);

        Map.Entry<UserRetrievalMinimalDto, Integer> favouriteFriend = amountOfTimesPlayedWithFriends.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .orElse(null);

        gameStatisticsDtos.forEach(gameStatisticsDto -> {
            gameStatisticsDto.setLongestWinStreak(calculateLongestWinStreak(gameStatisticsDto, userEntity.getPlayedGames()));
            gameStatisticsDto.setCurrentWinStreak(calculateCurrentWinStreak(gameStatisticsDto, userEntity.getPlayedGames()));
            gameStatisticsDto.setFavouriteFriend(findMostPlayedWithFriendForSpecificGame(gameStatisticsDto, amountOfTimesPlayedSpecificGameWithFriends));
        });

        userStatisticsDto.setTotalGamesWon(totalGamesWon);
        userStatisticsDto.setTotalGamesLost(totalGamesLost);
        userStatisticsDto.setFavouriteFriend(favouriteFriend != null ? new FriendStatisticsDto(favouriteFriend.getKey(), favouriteFriend.getValue()) : null);
        userStatisticsDto.setFavouriteGame(favouriteGame);
        userStatisticsDto.setGameStatistics(gameStatisticsDtos);

        return userStatisticsDto;
    }

    private FriendStatisticsDto findMostPlayedWithFriendForSpecificGame(GameStatisticsDto gameStatisticsDto, Map<Map<Long, UserRetrievalMinimalDto>, Integer> amountOfTimesPlayedSpecificGameWithFriends) {
        return amountOfTimesPlayedSpecificGameWithFriends.entrySet().stream()
                .filter(entry -> entry.getKey().containsKey(gameStatisticsDto.getBggGameId()))
                .max(Comparator.comparingInt(Map.Entry::getValue))
                .map(Map.Entry::getKey)
                .map(favouriteFriendEntry -> {
                    Long friendId = favouriteFriendEntry.keySet().iterator().next();
                    UserRetrievalMinimalDto friend = favouriteFriendEntry.get(friendId);
                    Integer timesPlayed = amountOfTimesPlayedSpecificGameWithFriends.get(favouriteFriendEntry);
                    return new FriendStatisticsDto(friend, timesPlayed);
                })
                .orElse(null);
    }

    private void countAmountOfTimesPlayedWithFriends(Map<UserRetrievalMinimalDto, Integer> amountOfTimesPlayedWithFriends,
                                                     Map<Map<Long, UserRetrievalMinimalDto>, Integer> amountOfTimesPlayedSpecificGameWithFriends,
                                                     PlayedGameEntity playedGame) {
        for (PlayedGameEntity associatedPlay : playedGame.getAssociatedPlays()) {
                UserRetrievalMinimalDto friend = userMapper.entityToMinimalRetrievalDto(associatedPlay.getUser());
                amountOfTimesPlayedWithFriends.merge(friend, 1, Integer::sum);
                amountOfTimesPlayedSpecificGameWithFriends.merge(Map.of(playedGame.getBggGameId(), friend), 1, Integer::sum);
        }

        for (PlayedGameEntity associatedPlay : playedGame.getAssociatedWith()) {
            if (associatedPlay.getIncludeToStatistics()) {
                UserRetrievalMinimalDto friend = userMapper.entityToMinimalRetrievalDto(associatedPlay.getUser());
                amountOfTimesPlayedWithFriends.merge(friend, 1, Integer::sum);
                amountOfTimesPlayedSpecificGameWithFriends.merge(Map.of(playedGame.getBggGameId(), friend), 1, Integer::sum);
            }
        }
    }

    public Integer calculateLongestWinStreak(GameStatisticsDto gameStatisticsDto, List<PlayedGameEntity> playedGames) {
        int longestWinStreak = 0;
        int currentWinStreak = 0;

        for (PlayedGameEntity playedGame : playedGames) {
            if (playedGame.getBggGameId().equals(gameStatisticsDto.getBggGameId())) {
                if (playedGame.getWon()) {
                    currentWinStreak++;
                } else {
                    longestWinStreak = Math.max(longestWinStreak, currentWinStreak);
                    currentWinStreak = 0;
                }
            }
        }

        // Check if the last win streak is the longest
        longestWinStreak = Math.max(longestWinStreak, currentWinStreak);

        return longestWinStreak;
    }

    public Integer calculateCurrentWinStreak(GameStatisticsDto gameStatisticsDto, List<PlayedGameEntity> playedGames) {
        int currentWinStreak = 0;

        for (PlayedGameEntity playedGame : playedGames) {
            if (playedGame.getBggGameId().equals(gameStatisticsDto.getBggGameId())) {
                if (playedGame.getWon()) {
                    currentWinStreak++;
                } else {
                    currentWinStreak = 0;
                }
            }
        }

        return currentWinStreak;
    }

    @jakarta.transaction.Transactional
    public PlayedGameDto editPlayedGameById(Long playId, PlayedGameDto playedGameDto) throws IllegalAccessException {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        UserEntity loggedInUserEntity = getUserEntityByEmail(userDetails.getUsername());

        PlayedGameEntity playedGameEntity = playedGameRepository.findById(playId).orElseThrow(() -> new EntityNotFoundException("Play with id: " + playId + " was not found"));

        if (!Role.ROLE_ADMIN.name().equals(loggedInUserEntity.getRole().name())) {
                if (!loggedInUserEntity.getEmail().equals(playedGameEntity.getUser().getEmail())) {
                    throw new IllegalAccessException("You cannot edit another users play");
                }
        }

        List<Long> currentPlayIds = playedGameEntity.getAssociatedPlays().stream().map(PlayedGameEntity::getId).collect(Collectors.toList());
        PlayedGamePostEntity playedGamePostEntity = playedGameEntity.getPost();
        Date creationDate = playedGameEntity.getCreationDate();

        BGGThingResponse boardGame = boardGameService.getBoardGameById(playedGameDto.getBggGameId());

        PlayedGameEntity editedPlayedGameEntity = switch (playedGameDto.getScoringSystem()) {
            case "highest-score" -> createHighestScorePlayedGameEntity(playedGameDto, loggedInUserEntity, boardGame);
            case "lowest-score" -> createLowestScorePlayedGameEntity(playedGameDto, loggedInUserEntity, boardGame);
            case "no-score" -> createNoScorePlayedGameEntity(playedGameDto, loggedInUserEntity, boardGame);
            default -> throw new FieldValidationException(Map.of("scoringSystem", "Invalid scoring system"));
        };

        editedPlayedGameEntity.setCreationDate(creationDate);
        editedPlayedGameEntity.setIncludeToStatistics(playedGameEntity.getIncludeToStatistics());

        if (playedGamePostEntity != null) {
            playedGamePostEntity.setScoringSystem(editedPlayedGameEntity.getScoringSystem());
            playedGamePostEntity.setHighestScore(calculateHighestScore(playedGameDto));
            playedGamePostEntity.setLowestScore(calculateLowestScore(playedGameDto));
            playedGamePostEntity.setAverageScore(calculateAverageScore(playedGameDto));
            editedPlayedGameEntity.setPost(playedGamePostEntity);
        }

        List<Long> editedPlayIds = editedPlayedGameEntity.getAssociatedPlays().stream().map(PlayedGameEntity::getId).toList();

        currentPlayIds.removeAll(editedPlayIds);

        playedGameRepository.deleteAllById(currentPlayIds);

        playedGameEntity = null;

        editedPlayedGameEntity = playedGameRepository.save(editedPlayedGameEntity);

        notificationService.createAndSaveNotificationsForAssociatedPlays(editedPlayedGameEntity);

        return playedGameMapper.entityToDto(editedPlayedGameEntity);
    }

    public PlayedGameDto includePlayInStatistics(Long playId) throws IllegalAccessException {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        UserEntity loggedInUserEntity = userRepository.findByEmail(userDetails.getUsername()).orElseThrow(() -> new EntityNotFoundException("User with username: " + userDetails.getUsername() + " was not found"));

        PlayedGameEntity playedGameEntity = playedGameRepository.findById(playId).orElseThrow(() -> new EntityNotFoundException("Played game with id: " + playId + " was not found"));

        if (!Role.ROLE_ADMIN.name().equals(loggedInUserEntity.getRole().name())) {
            if (!loggedInUserEntity.getEmail().equals(playedGameEntity.getUser().getEmail())) {
                throw new IllegalAccessException("You approve another user's play");
            }
        }

        playedGameEntity.setIncludeToStatistics(true);

        return playedGameMapper.entityToDto(playedGameRepository.save(playedGameEntity));
    }

    public PlayedGameDto excludePlayFromStatistics(Long playId) throws IllegalAccessException {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        UserEntity loggedInUserEntity = userRepository.findByEmail(userDetails.getUsername()).orElseThrow(() -> new EntityNotFoundException("User with username: " + userDetails.getUsername() + " was not found"));

        PlayedGameEntity playedGameEntity = playedGameRepository.findById(playId).orElseThrow(() -> new EntityNotFoundException("Played game with id: " + playId + " was not found"));

        if (!Role.ROLE_ADMIN.name().equals(loggedInUserEntity.getRole().name())) {
            if (!loggedInUserEntity.getEmail().equals(playedGameEntity.getUser().getEmail())) {
                throw new IllegalAccessException("You approve another user's play");
            }
        }

        playedGameEntity.setIncludeToStatistics(false);

        return playedGameMapper.entityToDto(playedGameRepository.save(playedGameEntity));
    }
}
