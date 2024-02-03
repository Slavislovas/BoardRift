package com.socialnetwork.boardrift.service;

import com.socialnetwork.boardrift.repository.EmailVerificationTokenRepository;
import com.socialnetwork.boardrift.repository.UserRepository;
import com.socialnetwork.boardrift.repository.model.EmailVerificationTokenEntity;
import com.socialnetwork.boardrift.repository.model.UserEntity;
import com.socialnetwork.boardrift.rest.model.UserRegistrationDto;
import com.socialnetwork.boardrift.rest.model.UserRetrievalDto;
import com.socialnetwork.boardrift.util.event.OnRegistrationCompleteEvent;
import com.socialnetwork.boardrift.util.exception.EmailVerificationTokenExpiredException;
import com.socialnetwork.boardrift.util.exception.EmailVerificationTokenNotFoundException;
import com.socialnetwork.boardrift.util.exception.FieldValidationException;
import com.socialnetwork.boardrift.util.mapper.UserMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.WebRequest;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class UserService {
    private final UserRepository userRepository;
    private final EmailVerificationTokenRepository emailVerificationTokenRepository;
    private final UserMapper userMapper;
    private final BCryptPasswordEncoder passwordEncoder;
    private final ApplicationEventPublisher eventPublisher;

    @Value("${email.verification-token.expiry-time-in-minutes}")
    private Integer emailVerificationTokenExpiryTimeInMinutes;

    public UserRetrievalDto createUser(UserRegistrationDto userRegistrationDto, HttpServletRequest servletRequest) {
        verifyIfUsernameAndEmailIsUnique(userRegistrationDto.getUsername(), userRegistrationDto.getEmail());

        UserEntity userEntity = userMapper.registrationDtoToEntity(userRegistrationDto);
        userEntity.setPassword(passwordEncoder.encode(userEntity.getPassword()));
        userEntity = userRepository.save(userEntity);

        String appUrl = servletRequest.getContextPath();
        eventPublisher.publishEvent(new OnRegistrationCompleteEvent(userEntity, servletRequest.getLocale(), appUrl));

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

    public void createEmailVerificationToken(UserEntity userEntity, String token) {
        EmailVerificationTokenEntity emailVerificationTokenEntity = new EmailVerificationTokenEntity(token, userEntity, emailVerificationTokenExpiryTimeInMinutes);
        emailVerificationTokenRepository.save(emailVerificationTokenEntity);
    }

    public void confirmUserRegistration(String token) {
        EmailVerificationTokenEntity emailVerificationTokenEntity = getEmailVerificationToken(token);
        validateEmailVerificationTokenExpiration(emailVerificationTokenEntity);

        UserEntity userEntity = emailVerificationTokenEntity.getUserEntity();
        userEntity.setEmailVerified(true);
        userRepository.save(userEntity);
    }

    private void validateEmailVerificationTokenExpiration(EmailVerificationTokenEntity emailVerificationTokenEntity) {
        Calendar calendar = Calendar.getInstance();
        if ((emailVerificationTokenEntity.getExpiryDate().getTime() - calendar.getTime().getTime()) <= 0) {
            throw new EmailVerificationTokenExpiredException();
        }
    }

    public EmailVerificationTokenEntity getEmailVerificationToken(String token) {
        Optional<EmailVerificationTokenEntity> optionalEmailVerificationTokenEntity = emailVerificationTokenRepository.findByToken(token);

        if (optionalEmailVerificationTokenEntity.isEmpty()) {
            throw new EmailVerificationTokenNotFoundException();
        }

        return optionalEmailVerificationTokenEntity.get();
    }
}
