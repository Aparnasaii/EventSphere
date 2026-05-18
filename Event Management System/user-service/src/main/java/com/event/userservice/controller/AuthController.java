package com.event.userservice.controller;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import com.event.userservice.config.JwtUtil;
import com.event.userservice.dto.AuthResponse;
import com.event.userservice.dto.ForgotPasswordRequest;
import com.event.userservice.dto.LoginRequest;
import com.event.userservice.dto.ResetPasswordRequest;
import com.event.userservice.entity.User;
import com.event.userservice.entity.UserStatus;
import com.event.userservice.repository.UserRepository;
import com.event.userservice.service.UserService;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired private UserRepository userRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private JwtUtil jwtUtil;
    @Autowired private UserService userService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        logger.info("Login attempt for email: {}", request.getEmail());

        User user = userRepository.findByEmail(request.getEmail()).orElse(null);

        if (user == null) {
            logger.warn("Login failed: User with email {} not found", request.getEmail());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not found");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            logger.warn("Login failed: Incorrect password for user {}", request.getEmail());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
        }

        if (user.getStatus() == UserStatus.PENDING) {
            logger.warn("Login blocked: User {} is still in PENDING status", request.getEmail());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Your account is pending Admin approval.");
        }

        if (user.getStatus() == UserStatus.SUSPENDED) {
            logger.warn("Login blocked: User {} has been SUSPENDED", request.getEmail());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Your account has been suspended. Please contact the administrator.");
        }

        if (user.getStatus() == UserStatus.INACTIVE) {
            logger.warn("Login blocked: User {} is INACTIVE", request.getEmail());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Your account is inactive. Please contact the administrator.");
        }

        if (user.getStatus() == UserStatus.REJECTED) {
            logger.warn("Login blocked: User {} was REJECTED", request.getEmail());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Your account application was rejected. Please contact the administrator.");
        }

        String token = jwtUtil.generateToken(user.getEmail(), user.getRole(), user.getUserId());
        logger.info("Login successful for user: {}. Role: {}", user.getEmail(), user.getRole());
        
        return ResponseEntity.ok(new AuthResponse(token, "Login Successful"));
    }
    
    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        logger.info("Forgot password request received for email: {}", request.getEmail());
        try {
            String message = userService.processForgotPassword(request.getEmail());
            logger.info("Reset link generated successfully for: {}", request.getEmail());
            return ResponseEntity.ok(message);
        } catch (RuntimeException e) {
            logger.error("Forgot password failed for {}: {}", request.getEmail(), e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestBody ResetPasswordRequest request) {
        logger.info("Reset password attempt with token.");
        try {
            String message = userService.updatePassword(request.getToken(), request.getNewPassword());
            logger.info("Password successfully updated for token holder.");
            return ResponseEntity.ok(message);
        } catch (RuntimeException e) {
            logger.error("Password reset failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}