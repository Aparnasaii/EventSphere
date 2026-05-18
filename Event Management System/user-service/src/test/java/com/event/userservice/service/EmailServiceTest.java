package com.event.userservice.service;

import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import com.event.userservice.service.EmailService;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private EmailService emailService;

    @Test
    void should_SendEmail_WithCorrectDetails() {
        // Arrange
        String toEmail = "user@example.com";
        String token = "sample-reset-token";

        // ArgumentCaptor allows us to "catch" the message object sent to mailSender
        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);

        // Act
        emailService.sendResetPasswordEmail(toEmail, token);

        // Assert
        // Verify that send() was called exactly once
        verify(mailSender, times(1)).send(messageCaptor.capture());

        SimpleMailMessage sentMessage = messageCaptor.getValue();

        // Check that the recipient and content are correct
        assertEquals(toEmail, sentMessage.getTo()[0]);
        assertEquals("Reset Your Password - EventApp", sentMessage.getSubject());
        assertTrue(sentMessage.getText().contains(token));
        assertTrue(sentMessage.getText().contains("http://localhost:8082"));
    }
}