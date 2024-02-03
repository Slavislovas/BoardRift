package com.socialnetwork.boardrift.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

@Configuration
public class JavaMailConfiguration {
    private static final int GMAIL_SMTP_PORT = 587;

    @Value("${email.sender.host}")
    private String emailSenderHost;

    @Value("${email.sender.user}")
    private String emailSenderUser;

    @Value("${email.sender.password}")
    private String emailSenderPassword;

    @Value("${email.sender.debug}")
    private Boolean debug;

    @Bean
    public JavaMailSender javaMailSender() {
        JavaMailSenderImpl javaMailSenderImpl = new JavaMailSenderImpl();
        javaMailSenderImpl.setHost(emailSenderHost);
        javaMailSenderImpl.setPort(GMAIL_SMTP_PORT);
        javaMailSenderImpl.setUsername(emailSenderUser);
        javaMailSenderImpl.setPassword(emailSenderPassword);

        Properties properties = javaMailSenderImpl.getJavaMailProperties();
        properties.put("mail.transport.protocol", "smtp");
        properties.put("mail.smtp.auth", true);
        properties.put("mail.smtp.starttls.enable", true);
        properties.put("mail.debug", debug);

        return javaMailSenderImpl;
    }
}
