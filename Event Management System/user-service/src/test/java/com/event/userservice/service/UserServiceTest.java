package com.event.userservice.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.event.userservice.entity.*;
import com.event.userservice.repository.TokenRepository;
import com.event.userservice.repository.UserRepository;
import com.event.userservice.service.EmailService;
import com.event.userservice.service.UserService;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private TokenRepository tokenRepository;
    @Mock private EmailService emailService;

    @InjectMocks
    private UserService userService;

    private User mockUser;

    @BeforeEach
    void setUp() {
        mockUser = new User();
        mockUser.setEmail("organizer@event.com");
        mockUser.setPassword("rawPassword");
        mockUser.setRole(Role.ROLE_ORGANIZER);
    }

    @Test
    void should_SetStatusToPending_When_OrganizerRegisters() {
        // Arrange
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArguments()[0]);

        // Act
        User savedUser = userService.registerUser(mockUser);

        // Assert
        assertEquals(UserStatus.PENDING, savedUser.getStatus());
        assertEquals("encodedPassword", savedUser.getPassword());
        verify(userRepository, times(1)).save(any());
    }

    @Test
    void should_ApproveOrganizer_And_ChangeStatusToActive() {
        // Arrange
        mockUser.setStatus(UserStatus.PENDING);
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        when(userRepository.save(any(User.class))).thenReturn(mockUser);

        // Act
        User result = userService.approveOrganizer(1L);

        // Assert
        assertEquals(UserStatus.ACTIVE, result.getStatus());
        assertNotNull(result.getUpdatedAt());
        verify(userRepository).save(mockUser);
    }

    @Test
    void should_ThrowException_When_UpdatingNonExistentToken() {
        // Arrange
        when(tokenRepository.findByToken("invalid-token")).thenReturn(Optional.empty());

        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () -> {
            userService.updatePassword("invalid-token", "newPass");
        });

        assertEquals("Invalid or non-existent token.", exception.getMessage());
    }

    @Test
    void should_ThrowException_When_TokenIsExpired() {
        // Arrange
        PasswordResetToken expiredToken = new PasswordResetToken("expired", mockUser);
        // Manually set expiry to the past for testing
        expiredToken.setExpiryDate(LocalDateTime.now().minusHours(1));
        
        when(tokenRepository.findByToken("expired")).thenReturn(Optional.of(expiredToken));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            userService.updatePassword("expired", "newPass");
        });

        assertTrue(exception.getMessage().contains("expired"));
        verify(tokenRepository).delete(expiredToken);
    }
}