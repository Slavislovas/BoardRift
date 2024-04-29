package com.socialnetwork.boardrift.unit.service;

import com.socialnetwork.boardrift.enumeration.VerificationTokenType;
import com.socialnetwork.boardrift.repository.VerificationTokenRepository;
import com.socialnetwork.boardrift.repository.model.VerificationTokenEntity;
import com.socialnetwork.boardrift.repository.model.user.UserEntity;
import com.socialnetwork.boardrift.service.EmailService;
import com.socialnetwork.boardrift.util.exception.EmailVerificationTokenExpiredException;
import com.socialnetwork.boardrift.util.exception.EmailVerificationTokenNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.annotation.Before;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith({MockitoExtension.class})
public class EmailServiceUnitTests {
    @Mock
    ApplicationEventPublisher eventPublisher;

    @Mock
    VerificationTokenRepository verificationTokenRepository;

    @InjectMocks
    EmailService emailService;

    @BeforeEach
    public void setUp() {
       emailService.setEmailVerificationTokenExpiryTimeInMinutes(200);
    }

    @Test
    void sendEmailVerification_shouldSucceed() {
        HttpServletRequest request = new MockHttpServletRequest();
        Mockito.doNothing().when(eventPublisher).publishEvent(any());
        Boolean result = emailService.sendEmailVerification(request, new UserEntity());
        assertTrue(result);
    }

    @Test
    void sendEmailVerification_shouldFail() {
        HttpServletRequest request = new MockHttpServletRequest();
        Boolean result = emailService.sendEmailVerification(request, null);
        assertFalse(result);
    }

    @Test
    void createEmailVerificationToken_shouldSucceed() {
        UserEntity userEntity = new UserEntity();
        VerificationTokenEntity expected = new VerificationTokenEntity(null, VerificationTokenType.EMAIL_VERIFICATION, userEntity, 200);
        Mockito.when(verificationTokenRepository.save(any())).thenReturn(expected);
        VerificationTokenEntity result = emailService.createEmailVerificationToken(userEntity);
        assertEquals(expected, result);
    }

    @Test
    void validateEmailConfirmationTokenExpiration_shouldSucceed() {
        UserEntity userEntity = new UserEntity();
        VerificationTokenEntity expected = new VerificationTokenEntity(null, VerificationTokenType.EMAIL_VERIFICATION, userEntity, 200);
        emailService.validateEmailVerificationTokenExpiration(expected);
    }

    @Test
    void validateEmailConfirmationTokenExpiration_shouldFail() {
        UserEntity userEntity = new UserEntity();
        VerificationTokenEntity expected = new VerificationTokenEntity(null, VerificationTokenType.EMAIL_VERIFICATION, userEntity, -200);
        assertThrows(EmailVerificationTokenExpiredException.class, () -> emailService.validateEmailVerificationTokenExpiration(expected));
    }

    @Test
    void getEmailConfirmationToken_shouldSucceed() {
        UserEntity userEntity = new UserEntity();
        VerificationTokenEntity expected = new VerificationTokenEntity(null, VerificationTokenType.EMAIL_VERIFICATION, userEntity, 200);
        Mockito.when(verificationTokenRepository.findByTokenAndType(any(), any())).thenReturn(Optional.of(expected));
        VerificationTokenEntity result = emailService.getEmailVerificationToken("token");
        assertEquals(expected, result);
    }

    @Test
    void getEmailConfirmationToken_shouldFail() {
        Mockito.when(verificationTokenRepository.findByTokenAndType(any(), any())).thenReturn(Optional.empty());
        assertThrows(EmailVerificationTokenNotFoundException.class, () -> emailService.getEmailVerificationToken("token"));
    }

    @Test
    void sendPasswordResetRequest_shouldSucceed() {
        UserEntity userEntity = new UserEntity();
        VerificationTokenEntity expected = new VerificationTokenEntity(null, VerificationTokenType.PASSWORD_RESET, userEntity, 200);
        Mockito.doNothing().when(eventPublisher).publishEvent(any());
        Boolean result = emailService.sendResetPasswordRequest(userEntity, expected);
        assertTrue(result);
    }

    @Test
    void sendPasswordResetRequest_shouldFail() {
        UserEntity userEntity = new UserEntity();
        VerificationTokenEntity expected = new VerificationTokenEntity(null, VerificationTokenType.PASSWORD_RESET, userEntity, 200);
        Boolean result = emailService.sendResetPasswordRequest(null, expected);
        assertFalse(result);
    }
}
