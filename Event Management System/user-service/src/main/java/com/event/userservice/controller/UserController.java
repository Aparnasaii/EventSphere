package com.event.userservice.controller;

import com.event.userservice.clients.NotificationClient;
import com.event.userservice.dto.*;
import com.event.userservice.entity.Role;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.event.userservice.entity.User;
import com.event.userservice.service.UserService;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private NotificationClient notificationClient;

    @PostMapping("/register")
    public ResponseEntity<UserDTO> register(@RequestBody User user) {
        logger.info("Controller: Registration request received for email: {}", user.getEmail());
        User registeredUser = userService.registerUser(user);

        NotificationRequestDTO notificationRequestDTO = NotificationRequestDTO.builder()
                .userId(registeredUser.getUserId())
                .recipient(registeredUser.getEmail())
                .templateName("WELCOME_USER")
                .type(NotificationType.EMAIL)
                .templateData(Map.of("userName", registeredUser.getFullName()))
                .build();

        try {
            logger.info("Calling Notification Service to send welcome email to: {}", registeredUser.getEmail());
            ResponseEntity<NotificationResponseDTO> notificationResponse =
                    notificationClient.sendNotification(notificationRequestDTO);

            if (notificationResponse.getStatusCode().is2xxSuccessful()) {
                logger.info("Successfully sent welcome notification. Response: {}", notificationResponse.getBody());
            } else {
                logger.warn("Notification service responded with status: {}", notificationResponse.getStatusCode());
            }
        } catch (Exception e) {
            logger.error("Failed to send welcome notification to {}. Error: {}", registeredUser.getEmail(), e.getMessage());
        }

        return ResponseEntity.ok(convertToDTO(registeredUser));
    }

    @PutMapping("/admin/approve/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<UserDTO> approve(@PathVariable Long id) {
        logger.info("Controller: Admin action - Approving user ID: {}", id);
        User approvedUser = userService.approveOrganizer(id);
        return ResponseEntity.ok(convertToDTO(approvedUser));
    }

    @PutMapping("/admin/reject/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<UserDTO> reject(@PathVariable Long id) {
        logger.info("Controller: Admin action - Rejecting organizer ID: {}", id);
        User rejected = userService.rejectOrganizer(id);
        return ResponseEntity.ok(convertToDTO(rejected));
    }

    @PutMapping("/admin/suspend/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<UserDTO> suspend(@PathVariable Long id) {
        logger.info("Controller: Admin action - Suspending user ID: {}", id);
        User suspended = userService.suspendUser(id);
        return ResponseEntity.ok(convertToDTO(suspended));
    }

    @PutMapping("/admin/reactivate/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<UserDTO> reactivate(@PathVariable Long id) {
        logger.info("Controller: Admin action - Reactivating user ID: {}", id);
        User reactivated = userService.reactivateUser(id);
        return ResponseEntity.ok(convertToDTO(reactivated));
    }

    @DeleteMapping("/admin/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<String> deleteUser(@PathVariable Long id) {
        logger.info("Controller: Admin action - Deleting user ID: {}", id);
        userService.deleteUser(id);
        return ResponseEntity.ok("User deleted successfully");
    }

    @GetMapping("/admin/pending")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<List<UserDTO>> getPending() {
        logger.info("Controller: Admin request - Fetching pending organizers");
        List<User> users = userService.getPendingOrganizers();
        return ResponseEntity.ok(
                users.stream()
                        .map(this::convertToDTO)
                        .collect(Collectors.toList())
        );
    }

    @GetMapping("/all")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        logger.info("Controller: Request for all user records.");
        List<User> users = userService.findAllUsers();
        List<UserDTO> dtos = users.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getUserById(@PathVariable Long id) {
        logger.info("Fetching profile details for user ID: {}", id);
        User user = userService.findById(id);
        return ResponseEntity.ok(convertToDTO(user));
    }

    @PostMapping("/batch")
    public ResponseEntity<List<UserDTO>> getUsersByIds(@RequestBody List<Long> ids) {
        logger.info("Fetching profile details for user IDs: {}", ids);
        List<User> users = new ArrayList<>();
        ids.forEach(id -> {
            try {
                users.add(userService.findById(id));
            } catch (Exception e) {
                logger.warn("User ID {} not found. Skipping.", id);
            }
        });
        return ResponseEntity.ok(
                users.stream()
                        .map(this::convertToDTO)
                        .collect(Collectors.toList())
        );
    }

    @GetMapping("/{id}/event-context")
    public ResponseEntity<EventUserDTO> getEventUser(@PathVariable Long id) {
        logger.info("Controller: Feign request from Event-Service for ID: {}", id);
        User user = userService.findById(id);
        EventUserDTO dto = new EventUserDTO(user.getUserId(), user.getRole());
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/{id}/ticket-context")
    public ResponseEntity<TicketUserDTO> getTicketUser(@PathVariable Long id) {
        logger.info("Controller: Feign request from Ticket-Service for ID: {}", id);
        User user = userService.findById(id);
        TicketUserDTO dto = new TicketUserDTO(
                user.getUserId(),
                user.getFullName(),
                user.getEmail(),
                user.getContactNumber()
        );
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/recipients")
    public ResponseEntity<List<RecipientDetails>> getRecipients() {
        List<User> attendees = userService.findUsersByRole(Role.ROLE_ATTENDEE);
        List<RecipientDetails> recipientDetails = attendees.stream()
                .map(user -> new RecipientDetails(
                        user.getUserId(),
                        user.getEmail()
                )).collect(Collectors.toList());
        return ResponseEntity.ok(recipientDetails);
    }

    private UserDTO convertToDTO(User user) {
        return new UserDTO(
                user.getUserId(),
                user.getFullName(),
                user.getEmail(),
                user.getContactNumber(),
                user.getRole(),
                user.getStatus()
        );
    }
}