package com.socialnetwork.boardrift.service;

import com.socialnetwork.boardrift.repository.UserRepository;
import com.socialnetwork.boardrift.repository.model.EmailVerificationTokenEntity;
import com.socialnetwork.boardrift.repository.model.UserEntity;
import com.socialnetwork.boardrift.rest.model.UserRegistrationDto;
import com.socialnetwork.boardrift.rest.model.UserRetrievalDto;
import com.socialnetwork.boardrift.util.exception.FieldValidationException;
import com.socialnetwork.boardrift.util.mapper.UserMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
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
}
