package com.event.userservice.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromAddress;

    /**
     * Sends the password-reset email in a background thread (@Async).
     * This ensures the HTTP response returns to the user immediately after
     * the token is committed to the DB — SMTP latency no longer affects UX
     * and can never cause a DB lock-wait timeout.
     */
    @Async
    public void sendResetPasswordEmail(String to, String token) {
        logger.info("EmailService: Preparing to send password reset email to: {}", to);

        try {
            String resetLink = "http://localhost:4200/auth/reset-password?token=" + token;

            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromAddress);
            message.setTo(to);
            message.setSubject("Reset Your EventSphere Password");

            String content =
                    "Hello,\n\n" +
                    "We received a request to reset your EventSphere password.\n\n" +
                    "Click the link below to set a new password:\n" +
                    resetLink + "\n\n" +
                    "This link will expire in 30 minutes.\n\n" +
                    "If you did not request a password reset, you can safely ignore this email.\n\n" +
                    "— The EventSphere Team";

            message.setText(content);

            mailSender.send(message);

            logger.info("EmailService: Reset email successfully sent to: {}", to);

        } catch (Exception e) {
            logger.error("EmailService: Failed to send reset email to {}. Error: {}", to, e.getMessage());
        }
    }
}
