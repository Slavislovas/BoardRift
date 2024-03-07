package com.socialnetwork.boardrift.service;

import com.socialnetwork.boardrift.enumeration.Role;
import com.socialnetwork.boardrift.feign.BGGApiConnection;
import com.socialnetwork.boardrift.repository.PlayedGameRepository;
import com.socialnetwork.boardrift.repository.UserRepository;
import com.socialnetwork.boardrift.repository.model.EmailVerificationTokenEntity;
import com.socialnetwork.boardrift.repository.model.UserEntity;
import com.socialnetwork.boardrift.repository.model.board_game.PlayedGameEntity;
import com.socialnetwork.boardrift.rest.model.BGGThingResponse;
import com.socialnetwork.boardrift.rest.model.FriendRequestDto;
import com.socialnetwork.boardrift.rest.model.PlayedGamePageDto;
import com.socialnetwork.boardrift.rest.model.user.UserEditDto;
import com.socialnetwork.boardrift.rest.model.user.UserRegistrationDto;
import com.socialnetwork.boardrift.rest.model.user.UserRetrievalDto;
import com.socialnetwork.boardrift.rest.model.user.UserRetrievalMinimalDto;
import com.socialnetwork.boardrift.rest.model.post.played_game_post.PlayedGameDto;
import com.socialnetwork.boardrift.util.exception.DuplicateFriendRequestException;
import com.socialnetwork.boardrift.util.exception.FieldValidationException;
import com.socialnetwork.boardrift.util.mapper.PlayedGameMapper;
import com.socialnetwork.boardrift.util.mapper.UserMapper;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
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
    private final BGGApiConnection bggApiConnection;
    private final AWSService awsService;

    @Value("${aws.s3.default-profile-picture-url}")
    private String defaultUserProfilePictureUrl;

    private static final long MAX_PROFILE_PICTURE_SIZE_BYTES = 2 * 1024 * 1024;

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
        userEntity.setBio("");
        userEntity.setProfilePictureUrl(defaultUserProfilePictureUrl);
        userEntity.setCity("");
        userEntity.setCountry("");
        userEntity = userRepository.save(userEntity);

        emailService.sendEmailVerification(servletRequest, userEntity);

        return userMapper.entityToRetrievalDto(userEntity, null, null, null, null);
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
        UserEntity loggedInUserEntity = userRepository.findByUsername(userDetails.getUsername()).orElseThrow(() -> new EntityNotFoundException("User with username: " + userDetails.getUsername() + " was not found"));

        UserEntity targetUserEntity = userRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException("User with id: " + userDetails.getUsername() + " was not found"));

        if (!Role.ROLE_ADMINISTRATOR.name().equals(loggedInUserEntity.getRole().name())) {
            if (!targetUserEntity.getUsername().equals(userDetails.getUsername())) {
                if (!targetUserEntity.getPublicFriendsList() && !receiverAlreadyFriend(loggedInUserEntity, targetUserEntity.getId())) {
                    throw new IllegalAccessException("You cannot view this user's friend list");
                }
            }
        }

        Set<UserRetrievalMinimalDto> friends = targetUserEntity.getFriends()
                .stream()
                .map(userMapper::entityToMinimalRetrievalDto).collect(Collectors.toSet());

        friends.addAll(
                targetUserEntity.getFriendOf()
                        .stream()
                        .map(userMapper::entityToMinimalRetrievalDto).collect(Collectors.toSet())
        );

        return friends;
    }

    public Set<UserRetrievalMinimalDto> searchFriendsByName(Long userId, String query) throws IllegalAccessException {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String role = userDetails.getAuthorities().stream().findFirst().get().getAuthority();

        UserEntity userEntity = userRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException("User with id: " + userDetails.getUsername() + " was not found"));

        if (!Role.ROLE_ADMINISTRATOR.name().equals(role)) {
            if (!userEntity.getUsername().equals(userDetails.getUsername())) {
                if (!userEntity.getPublicFriendsList()) {
                    throw new IllegalAccessException("You cannot search this user's friend list");
                }
            }
        }

        if (query == null) {
            return getFriends(userId);
        }

        Set<UserRetrievalMinimalDto> friends = userEntity.getFriends()
                .stream()
                .filter(friend -> friend.getName().concat(" " + friend.getLastname()).toLowerCase().contains(query.toLowerCase()))
                .map(userMapper::entityToMinimalRetrievalDto).collect(Collectors.toSet());

        friends.addAll(
                userEntity.getFriendOf()
                        .stream()
                        .filter(friend -> friend.getName().concat(" " + friend.getLastname()).toLowerCase().contains(query.toLowerCase()))
                        .map(userMapper::entityToMinimalRetrievalDto).collect(Collectors.toSet()));

        return friends;
    }

    public UserRetrievalDto getUserById(Long userId) {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        UserEntity loggedInUserEntity = userRepository.findByUsername(userDetails.getUsername()).orElseThrow(() -> new EntityNotFoundException("User with username: " + userDetails.getUsername() + " was not found"));

        UserEntity userEntity = userRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException("User with id: " + userId + " was not found"));

        boolean userAlreadyFriend = receiverAlreadyFriend(loggedInUserEntity, userId);
        boolean friendRequestAlreadySent = friendRequestAlreadySent(loggedInUserEntity, userId);
        boolean alreadyReceivedFriendRequest = receiverAlreadySentFriendRequest(loggedInUserEntity, userId);
        boolean personalData = loggedInUserEntity.getId().equals(userEntity.getId());

        return userMapper.entityToRetrievalDto(userEntity, userAlreadyFriend, friendRequestAlreadySent, alreadyReceivedFriendRequest, personalData);
    }

    public PlayedGamePageDto getPlayedGamesByUserId(Long userId, Integer page, Integer pageSize, HttpServletRequest request) throws IllegalAccessException {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String role = userDetails.getAuthorities().stream().findFirst().get().getAuthority();

        UserEntity userEntity = userRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException("User with id: " + userId + " was not found"));

        if (!Role.ROLE_ADMINISTRATOR.name().equals(role)) {
            if (!userEntity.getUsername().equals(userDetails.getUsername())) {
                if (!userEntity.getPublicPlays()) {
                    throw new IllegalAccessException("You cannot view this user's plays");
                }
            }
        }

        PageRequest pageRequest = PageRequest.of(page, pageSize, Sort.by("creationDate").descending());
        List<PlayedGameEntity> playedGames = playedGameRepository.findByUserId(userId, pageRequest);

        List<PlayedGameDto> playedGameDtoList = playedGames.stream()
                .map(playedGame -> {
                    BGGThingResponse boardGame = bggApiConnection.getBoardGameById(playedGame.getBggGameId());
                    return playedGameMapper.entityToDto(playedGame, boardGame.getItems().get(0).getNames().get(0).getValue(), boardGame.getItems().get(0).getImage());
                }).toList();

        String nextPageUrl = playedGameDtoList.size() == pageSize ? String.format("%s%s?page=%d&pageSize=%d",
                ServletUriComponentsBuilder.fromCurrentContextPath().toUriString(),
                request.getServletPath(),
                page + 1,
                pageSize) : null;

        return new PlayedGamePageDto(nextPageUrl, playedGameDtoList);
    }

    public PlayedGameDto logPlayedGame(PlayedGameDto playedGameDto) {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        UserEntity loggedInUserEntity = userRepository.findByUsername(userDetails.getUsername()).orElseThrow(() -> new EntityNotFoundException("User with username: " + userDetails.getUsername() + " was not found"));

        PlayedGameEntity playedGameEntity = switch (playedGameDto.getScoringSystem()) {
            case "highest-score" ->
                    createHighestScorePlayedGameEntity(playedGameDto, loggedInUserEntity);
            case "lowest-score" ->
                    createLowestScorePlayedGameEntity(playedGameDto, loggedInUserEntity);
            case "no-score" ->
                    createNoScorePlayedGameEntity(playedGameDto, loggedInUserEntity);
            default -> throw new FieldValidationException(Map.of("scoringSystem", "Invalid scoring system"));
        };

        BGGThingResponse boardGame = bggApiConnection.getBoardGameById(playedGameDto.getBggGameId());
        return playedGameMapper.entityToDto(playedGameEntity, boardGame.getItems().get(0).getNames().get(0).getValue(), boardGame.getItems().get(0).getImage());
    }

    private PlayedGameEntity createHighestScorePlayedGameEntity(PlayedGameDto playedGameDto, UserEntity loggedInUserEntity) {
        Integer max = calculateHighestScore(playedGameDto);

        PlayedGameEntity playedGameEntity = new PlayedGameEntity(null, playedGameDto.getBggGameId(), playedGameDto.getScore(),
                playedGameDto.getScore().equals(max), playedGameDto.getScoringSystem(),
                new Date(), loggedInUserEntity, null, new HashSet<>());

        for (PlayedGameDto associatedPlayedGameDto : playedGameDto.getAssociatedPlays()) {
            playedGameEntity.addAssociatedPlay(new PlayedGameEntity(null, playedGameDto.getBggGameId(), associatedPlayedGameDto.getScore(),
                    associatedPlayedGameDto.getScore().equals(max), playedGameDto.getScoringSystem(),
                    new Date(), getUserEntityById(associatedPlayedGameDto.getUser().getId()), null, new HashSet<>()));
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

    private PlayedGameEntity createLowestScorePlayedGameEntity(PlayedGameDto playedGameDto, UserEntity loggedInUserEntity) {
        Integer min = calculateLowestScore(playedGameDto);

        PlayedGameEntity playedGameEntity = new PlayedGameEntity(null, playedGameDto.getBggGameId(), playedGameDto.getScore(),
                playedGameDto.getScore().equals(min), playedGameDto.getScoringSystem(),
                new Date(), loggedInUserEntity, null, new HashSet<>());

        for (PlayedGameDto associatedPlayedGameDto : playedGameDto.getAssociatedPlays()) {
            playedGameEntity.addAssociatedPlay(new PlayedGameEntity(null, playedGameDto.getBggGameId(), associatedPlayedGameDto.getScore(),
                    associatedPlayedGameDto.getScore().equals(min), playedGameDto.getScoringSystem(),
                    new Date(), getUserEntityById(associatedPlayedGameDto.getUser().getId()), null, new HashSet<>()));
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

    private PlayedGameEntity createNoScorePlayedGameEntity(PlayedGameDto playedGameDto, UserEntity loggedInUserEntity) {
        PlayedGameEntity playedGameEntity = new PlayedGameEntity(null, playedGameDto.getBggGameId(), playedGameDto.getScore(),
                playedGameDto.getWon(), playedGameDto.getScoringSystem(),
                new Date(), loggedInUserEntity, null, new HashSet<>());

        for (PlayedGameDto associatedPlayedGameDto : playedGameDto.getAssociatedPlays()) {
            playedGameEntity.addAssociatedPlay(new PlayedGameEntity(null, playedGameDto.getBggGameId(), associatedPlayedGameDto.getScore(),
                    associatedPlayedGameDto.getWon(), playedGameDto.getScoringSystem(),
                    new Date(), getUserEntityById(associatedPlayedGameDto.getUser().getId()), null, new HashSet<>()));
        }

        return playedGameRepository.save(playedGameEntity);
    }

    @Transactional
    public void deletePlayedGameById(Long playId) {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        UserEntity loggedInUserEntity = userRepository.findByUsername(userDetails.getUsername()).orElseThrow(() -> new EntityNotFoundException("User with username: " + userDetails.getUsername() + " was not found"));

        playedGameRepository.deleteByIdAndUserId(playId, loggedInUserEntity.getId());
    }

    public UserRetrievalDto editUserById(Long userId, MultipartFile profilePicture, UserEditDto userEditDto) throws IllegalAccessException {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String role = userDetails.getAuthorities().stream().findFirst().get().getAuthority();

        UserEntity userEntity = userRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException("User with id: " + userId + " was not found"));

        if (!Role.ROLE_ADMINISTRATOR.name().equals(role)) {
            if (!userEntity.getUsername().equals(userDetails.getUsername())) {
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

        if (profilePicture != null) {
            if (profilePicture.getSize() > MAX_PROFILE_PICTURE_SIZE_BYTES) {
                throw new FieldValidationException(Map.of("Profile picture", "Profile picture exceeds maximum size limit of 2MB"));
            }

            String profilePictureUrl = awsService.uploadProfilePicture(userId, profilePicture);

            if (profilePictureUrl == null) {
                throw new FieldValidationException(Map.of("Profile picture", "Failed to upload profile picture"));
            }

            userEntity.setProfilePictureUrl(profilePictureUrl);
        }

        return userMapper.entityToRetrievalDto(userRepository.save(userEntity), null, null, null, null);
    }
}
