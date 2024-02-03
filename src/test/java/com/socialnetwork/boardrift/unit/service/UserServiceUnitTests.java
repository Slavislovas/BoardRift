package com.socialnetwork.boardrift.unit.service;

import com.socialnetwork.boardrift.enumeration.Role;
import com.socialnetwork.boardrift.enumeration.UserStatus;
import com.socialnetwork.boardrift.repository.UserRepository;
import com.socialnetwork.boardrift.repository.model.UserEntity;
import com.socialnetwork.boardrift.rest.model.UserRegistrationDto;
import com.socialnetwork.boardrift.rest.model.UserRetrievalDto;
import com.socialnetwork.boardrift.service.UserService;
import com.socialnetwork.boardrift.util.exception.FieldValidationException;
import com.socialnetwork.boardrift.util.mapper.UserMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Collections;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
public class UserServiceUnitTests {
    @Mock
    BCryptPasswordEncoder passwordEncoder;

    @Mock
    UserRepository userRepository;

    @Mock
    UserMapper userMapper;

    @InjectMocks
    UserService userService;

    UserEntity userEntity;
    UserRegistrationDto userRegistrationDto;
    UserRetrievalDto userRetrievalDto;

    @BeforeEach
    void init(){
        userEntity = new UserEntity(1L, "Name", "Lastname", "email@gmail.com",
                "2001-11-16", "Username", "Password@123", true, false, "",
                Role.ROLE_USER, UserStatus.OFFLINE, Collections.EMPTY_SET,
                Collections.EMPTY_SET, Collections.EMPTY_SET, Collections.EMPTY_SET,
                Collections.EMPTY_SET, Collections.EMPTY_SET);

        userRegistrationDto = new UserRegistrationDto("Name", "Lastname",
                "email@gmail.com", "2001-11-16",
                "Username", "Password@123");

        userRetrievalDto = new UserRetrievalDto(1L, "Name", "Lastname", "email@gmail.com", "2001-11-16", "Username");

    }

    @Test
    void createUserShouldSucceed() {
        Mockito.when(userRepository.findByUsername(any())).thenReturn(Optional.empty());
        Mockito.when(userRepository.findByEmail(any())).thenReturn(Optional.empty());
        Mockito.when(userMapper.registrationDtoToEntity(any())).thenReturn(userEntity);
        Mockito.when(userRepository.save(any())).thenReturn(userEntity);
        Mockito.when(userMapper.entityToRetrievalDto(any())).thenReturn(userRetrievalDto);
        Mockito.when(passwordEncoder.encode(any())).thenReturn("encodedPassword");

        UserRetrievalDto result = userService.createUser(userRegistrationDto);

        Assertions.assertEquals(userRetrievalDto, result);
    }

    @Test
    void createUserShouldFailWhenUsernameIsTaken() {
        Mockito.when(userRepository.findByUsername(any())).thenReturn(Optional.of(userEntity));
        Mockito.when(userRepository.findByEmail(any())).thenReturn(Optional.empty());

        Assertions.assertThrows(FieldValidationException.class, () -> userService.createUser(userRegistrationDto));
    }

    @Test
    void createUserShouldFailWhenEmailIsTaken() {
        Mockito.when(userRepository.findByUsername(any())).thenReturn(Optional.empty());
        Mockito.when(userRepository.findByEmail(any())).thenReturn(Optional.of(userEntity));

        Assertions.assertThrows(FieldValidationException.class, () -> userService.createUser(userRegistrationDto));
    }

    @Test
    void createUserShouldFailWhenUsernameAndEmailIsTaken() {
        Mockito.when(userRepository.findByUsername(any())).thenReturn(Optional.of(userEntity));
        Mockito.when(userRepository.findByEmail(any())).thenReturn(Optional.of(userEntity));

        Assertions.assertThrows(FieldValidationException.class, () -> userService.createUser(userRegistrationDto));
    }
}
