package com.event.userservice.service;

import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.event.userservice.entity.*;
import com.event.userservice.repository.TokenRepository;
import com.event.userservice.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    @Autowired private UserRepository userRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private TokenRepository tokenRepository;
    @Autowired private EmailService emailService;

    public User registerUser(User user) {
        logger.info("Service: Registering user with email: {}", user.getEmail());
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        if (user.getRole() == Role.ROLE_ORGANIZER) {
            user.setStatus(UserStatus.PENDING);
            logger.info("User {} is an ORGANIZER. Setting status to PENDING.", user.getEmail());
        } else {
            user.setStatus(UserStatus.ACTIVE);
            logger.debug("User {} is an ATTENDEE. Setting status to ACTIVE.", user.getEmail());
        }
        return userRepository.save(user);
    }

    public List<User> getPendingOrganizers() {
        logger.info("Service: Fetching all pending organizer accounts.");
        return userRepository.findByRoleAndStatus(Role.ROLE_ORGANIZER, UserStatus.PENDING);
    }

    public User approveOrganizer(Long id) {
        logger.info("Service: Admin is approving user ID: {}", id);
        User user = userRepository.findById(id)
            .orElseThrow(() -> {
                logger.error("Service Error: User ID {} not found for approval.", id);
                return new RuntimeException("User not found");
            });
        user.setStatus(UserStatus.ACTIVE);
        user.setUpdatedAt(LocalDateTime.now());
        logger.info("Service: User ID {} successfully approved.", id);
        return userRepository.save(user);
    }

    // NOT @Transactional — each repo call commits in its own short transaction.
    // Keeping the email send outside any DB transaction prevents MySQL lock-wait
    // timeouts when SMTP is slow (a 30-50 s hold would block every concurrent
    // forgot-password request on the same user row).
    public String processForgotPassword(String email) {
        logger.info("Service: Forgot password request for: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    logger.warn("Service Warn: Email {} not found in database.", email);
                    return new RuntimeException("Email not found");
                });

        // 1. Delete any existing token — committed immediately (own @Transactional)
        tokenRepository.deleteByUser(user);

        // 2. Save new token — committed immediately (Spring Data default transaction)
        String token = UUID.randomUUID().toString();
        PasswordResetToken resetToken = new PasswordResetToken(token, user);
        tokenRepository.save(resetToken);

        // 3. Send email AFTER both commits — SMTP delay no longer holds any DB lock
        emailService.sendResetPasswordEmail(user.getEmail(), token);

        logger.info("Service: Reset token generated and email sent to {}", email);
        return "Reset link sent to your email.";
    }

    @Transactional
    public String updatePassword(String token, String newPassword) {
        logger.info("Service: Attempting password reset with token.");
        PasswordResetToken resetToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> {
                    logger.error("Service Error: Invalid reset token provided.");
                    return new RuntimeException("Invalid or non-existent token.");
                });

        if (resetToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            tokenRepository.delete(resetToken);
            logger.warn("Service Warn: Reset token for user {} has expired.", resetToken.getUser().getEmail());
            throw new RuntimeException("Reset link has expired. Please request a new one.");
        }

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
        tokenRepository.delete(resetToken);
        logger.info("Service: Password successfully reset for user: {}", user.getEmail());
        return "Password has been reset successfully.";
    }

    /**
     * Finds a user by ID. 
     * Used by Feign clients (Event/Ticket) for specific verification.
     */
    public User findById(Long id) {
        logger.info("Service: Fetching user details for ID: {}", id);
        return userRepository.findById(id)
                .orElseThrow(() -> {
                    logger.error("Service Error: User not found with ID: {}", id);
                    return new RuntimeException("User not found with id: " + id);
                });
    }

    /**
     * Fetches all users.
     * Used for admin lists and friend's retrieval request.
     */
    public List<User> findAllUsers() {
        logger.info("Service: Fetching all users from database.");
        return userRepository.findAll();
    }
    public List<User> findUsersByRole(Role role) {
        return userRepository.findByRole(role);
    }

    /** Admin: suspend a user (sets status to SUSPENDED). */
    @Transactional
    public User suspendUser(Long id) {
        logger.info("Service: Admin is suspending user ID: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
        if (user.getRole() == Role.ROLE_ADMIN) {
            throw new RuntimeException("Admin accounts cannot be suspended.");
        }
        user.setStatus(UserStatus.SUSPENDED);
        user.setUpdatedAt(LocalDateTime.now());
        return userRepository.save(user);
    }

    /** Admin: reactivate a suspended/inactive user (sets status to ACTIVE). */
    @Transactional
    public User reactivateUser(Long id) {
        logger.info("Service: Admin is reactivating user ID: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
        user.setStatus(UserStatus.ACTIVE);
        user.setUpdatedAt(LocalDateTime.now());
        return userRepository.save(user);
    }

    /** Admin: reject a pending organizer. */
    @Transactional
    public User rejectOrganizer(Long id) {
        logger.info("Service: Admin is rejecting organizer ID: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
        user.setStatus(UserStatus.REJECTED);
        user.setUpdatedAt(LocalDateTime.now());
        return userRepository.save(user);
    }

    /** Admin: delete a user. Cleans up reset tokens first to avoid FK violation. */
    @Transactional
    public void deleteUser(Long id) {
        logger.info("Service: Admin is deleting user ID: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
        if (user.getRole() == Role.ROLE_ADMIN) {
            throw new RuntimeException("Admin accounts cannot be deleted.");
        }
        try { tokenRepository.deleteByUser(user); } catch (Exception ignored) {}
        userRepository.delete(user);
        logger.info("Service: User ID {} deleted successfully.", id);
    }
}