package com.socialnetwork.boardrift.service;

import com.socialnetwork.boardrift.repository.EmailVerificationTokenRepository;
import com.socialnetwork.boardrift.repository.model.EmailVerificationTokenEntity;
import com.socialnetwork.boardrift.repository.model.UserEntity;
import com.socialnetwork.boardrift.util.event.OnRegistrationCompleteEvent;
import com.socialnetwork.boardrift.util.exception.EmailVerificationTokenExpiredException;
import com.socialnetwork.boardrift.util.exception.EmailVerificationTokenNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class EmailService {
    private final ApplicationEventPublisher eventPublisher;
    private final EmailVerificationTokenRepository emailVerificationTokenRepository;

    @Value("${email.verification-token.expiry-time-in-minutes}")
    private Integer emailVerificationTokenExpiryTimeInMinutes;

    public Boolean sendEmailVerification(HttpServletRequest servletRequest, UserEntity userEntity) {
        try {
            String appUrl = servletRequest.getContextPath();
            eventPublisher.publishEvent(new OnRegistrationCompleteEvent(userEntity, servletRequest.getLocale(), appUrl));
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    public EmailVerificationTokenEntity createEmailVerificationToken(UserEntity userEntity, String token) {
        EmailVerificationTokenEntity emailVerificationTokenEntity = new EmailVerificationTokenEntity(token, userEntity, emailVerificationTokenExpiryTimeInMinutes);
        return emailVerificationTokenRepository.save(emailVerificationTokenEntity);
    }

    public void validateEmailVerificationTokenExpiration(EmailVerificationTokenEntity emailVerificationTokenEntity) {
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
