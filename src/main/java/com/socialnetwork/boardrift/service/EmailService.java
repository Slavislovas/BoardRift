package com.socialnetwork.boardrift.service;

import com.socialnetwork.boardrift.enumeration.VerificationTokenType;
import com.socialnetwork.boardrift.repository.VerificationTokenRepository;
import com.socialnetwork.boardrift.repository.model.VerificationTokenEntity;
import com.socialnetwork.boardrift.repository.model.user.UserEntity;
import com.socialnetwork.boardrift.util.event.OnRegistrationCompleteEvent;
import com.socialnetwork.boardrift.util.event.OnResetPasswordRequestEvent;
import com.socialnetwork.boardrift.util.exception.EmailVerificationTokenExpiredException;
import com.socialnetwork.boardrift.util.exception.EmailVerificationTokenNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.Optional;

@Setter
@RequiredArgsConstructor
@Service
public class EmailService {
    private final ApplicationEventPublisher eventPublisher;
    private final VerificationTokenRepository verificationTokenRepository;

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

    public VerificationTokenEntity createEmailVerificationToken(UserEntity userEntity) {
        VerificationTokenEntity emailVerificationTokenEntity = new VerificationTokenEntity(null,
                VerificationTokenType.EMAIL_VERIFICATION,
                userEntity,
                emailVerificationTokenExpiryTimeInMinutes);
        return verificationTokenRepository.save(emailVerificationTokenEntity);
    }

    public void validateEmailVerificationTokenExpiration(VerificationTokenEntity emailVerificationTokenEntity) {
        Calendar calendar = Calendar.getInstance();
        if ((emailVerificationTokenEntity.getExpirationDate().getTime() - calendar.getTime().getTime()) <= 0) {
            throw new EmailVerificationTokenExpiredException();
        }
    }

    public VerificationTokenEntity getEmailVerificationToken(String token) {
        Optional<VerificationTokenEntity> optionalEmailVerificationTokenEntity = verificationTokenRepository.findByTokenAndType(token, VerificationTokenType.EMAIL_VERIFICATION);

        if (optionalEmailVerificationTokenEntity.isEmpty()) {
            throw new EmailVerificationTokenNotFoundException();
        }

        return optionalEmailVerificationTokenEntity.get();
    }

    public boolean sendResetPasswordRequest(UserEntity userEntity, VerificationTokenEntity passwordResetTokenEntity) {
        try {
            eventPublisher.publishEvent(new OnResetPasswordRequestEvent(userEntity, passwordResetTokenEntity));
            return true;
        } catch (Exception ex) {
            return false;
        }
    }
}
