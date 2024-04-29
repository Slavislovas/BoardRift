package com.socialnetwork.boardrift.util.listener;

import com.socialnetwork.boardrift.repository.model.VerificationTokenEntity;
import com.socialnetwork.boardrift.repository.model.user.UserEntity;
import com.socialnetwork.boardrift.service.EmailService;
import com.socialnetwork.boardrift.util.event.OnRegistrationCompleteEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import java.util.UUID;

@RequiredArgsConstructor
@Component
public class RegistrationListener implements ApplicationListener<OnRegistrationCompleteEvent> {
    @Value("${application.domain}")
    private String applicationDomain;
    private final EmailService emailService;
    private final JavaMailSender mailSender;

    @Override
    public void onApplicationEvent(OnRegistrationCompleteEvent event) {
        this.confirmRegistration(event);
    }

    private void confirmRegistration(OnRegistrationCompleteEvent event) {
        UserEntity userEntity = event.getUserEntity();
        VerificationTokenEntity emailVerificationTokenEntity = emailService.createEmailVerificationToken(userEntity);

        String recipientEmailAddress = userEntity.getEmail();
        String subject = "Board Rift Registration Confirmation";
        String confirmationUrl = event.getAppUrl() + "/users/register/confirm?token=" + emailVerificationTokenEntity.getToken();
        String message = buildMessage(userEntity, confirmationUrl);

        SimpleMailMessage email = new SimpleMailMessage();
        email.setTo(recipientEmailAddress);
        email.setSubject(subject);
        email.setText(message);
        mailSender.send(email);
    }

    private String buildMessage(UserEntity userEntity, String confirmationUrl) {
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append("Dear ").append(userEntity.getName()).append(" ").append(userEntity.getLastname()).append(",\n");
        stringBuilder.append("\n");
        stringBuilder.append("Thank you for registering with our service. To complete the registration process and verify your email address, please click on the following link:\n");
        stringBuilder.append("\n");
        stringBuilder.append(applicationDomain).append(confirmationUrl).append("\n");
        stringBuilder.append("\n");
        stringBuilder.append("If you did not sign up for our service, please ignore this email.\n");
        stringBuilder.append("\n");
        stringBuilder.append("Best regards,\n");
        stringBuilder.append("Board Rift");

        return stringBuilder.toString();
    }
}
