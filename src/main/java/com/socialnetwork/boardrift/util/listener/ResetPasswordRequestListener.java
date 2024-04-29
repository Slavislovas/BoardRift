package com.socialnetwork.boardrift.util.listener;

import com.socialnetwork.boardrift.repository.model.user.UserEntity;
import com.socialnetwork.boardrift.util.event.OnResetPasswordRequestEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class ResetPasswordRequestListener implements ApplicationListener<OnResetPasswordRequestEvent> {
    @Value("${client.domain}")
    private String clientDomain;
    private final JavaMailSender mailSender;

    @Override
    public void onApplicationEvent(OnResetPasswordRequestEvent event) {
        this.sendResetPasswordRequestEmail(event);
    }

    private void sendResetPasswordRequestEmail(OnResetPasswordRequestEvent event) {
        UserEntity userEntity = event.getUserEntity();

        String recipientEmailAddress = userEntity.getEmail();
        String subject = "Board Rift Reset Password Request";
        String resetPasswordUrl = clientDomain + "/reset-password/" + event.getPasswordResetTokenEntity().getToken();
        String message = buildMessage(userEntity, resetPasswordUrl);

        SimpleMailMessage email = new SimpleMailMessage();
        email.setTo(recipientEmailAddress);
        email.setSubject(subject);
        email.setText(message);
        mailSender.send(email);
    }

    private String buildMessage(UserEntity userEntity, String passwordResetUrl) {
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append("Dear ").append(userEntity.getName()).append(" ").append(userEntity.getLastname()).append(",\n\n");
        stringBuilder.append("Thank you for reaching out to reset your password. To proceed with the password reset process, please click on the following link:\n\n");
        stringBuilder.append(passwordResetUrl).append("\n\n");
        stringBuilder.append("If you didn't request this password reset or believe it's an error, please disregard this email.\n\n");
        stringBuilder.append("Best Regards,\n");
        stringBuilder.append("Board Rift");

        return stringBuilder.toString();
    }
}
