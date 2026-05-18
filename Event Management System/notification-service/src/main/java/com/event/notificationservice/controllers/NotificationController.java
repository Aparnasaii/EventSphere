package com.event.notificationservice.controllers;

import com.event.notificationservice.models.dtos.BulkNotificationRequestDTO;
import com.event.notificationservice.models.dtos.NotificationRequestDTO;
import com.event.notificationservice.models.dtos.NotificationResponseDTO;
import com.event.notificationservice.services.NotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public String test() {
        log.info("Health check endpoint hit: Controller is reachable.");
        return "Controller Reached!!!";
    }

    /**
     * Endpoint for Ticket and User services to send a single notification.
     */
    @PostMapping("/send")
    public ResponseEntity<NotificationResponseDTO> send(@RequestBody NotificationRequestDTO request) {
        log.info("API Request: POST /send | UserID: {} | Template: {} | Recipient: {}",
                request.getUserId(), request.getTemplateName(), request.getRecipient());

        long startTime = System.currentTimeMillis();
        NotificationResponseDTO response = notificationService.sendNotification(request);
        long duration = System.currentTimeMillis() - startTime;

        log.info("API Success: Notification sent for User {}. Processed in {}ms", request.getUserId(), duration);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Endpoint for Event and Feedback services to send mass notifications.
     */
    @PostMapping("/bulk")
    public ResponseEntity<String> sendBulk(@RequestBody BulkNotificationRequestDTO bulkRequest) {
        int recipientCount = (bulkRequest.getRecipients() != null) ? bulkRequest.getRecipients().size() : 0;

        log.info("API Request: POST /bulk | Template: {} | Type: {} | Total Recipients: {}",
                bulkRequest.getTemplateName(), bulkRequest.getType(), recipientCount);

        notificationService.sendBulkNotifications(bulkRequest);

        log.info("API Success: Bulk notification processing initiated for {} recipients.", recipientCount);
        return ResponseEntity.ok("Bulk notification processing started.");
    }

    /**
     * Endpoint for the Frontend to fetch a user's notification history.
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<NotificationResponseDTO>> getHistory(@PathVariable Long userId) {
        log.info("API Request: GET /user/{} | Fetching all notifications", userId);
        List<NotificationResponseDTO> history = notificationService.getUserNotifications(userId);
        log.info("API Success: Found {} notifications for User {}", history.size(), userId);
        return ResponseEntity.ok(history);
    }

    /**
     * Endpoint for the Frontend to fetch unread notifications (for the red dot/badge).
     */
    @GetMapping("/user/{userId}/unread")
    public ResponseEntity<List<NotificationResponseDTO>> getUnread(@PathVariable Long userId) {
        log.info("API Request: GET /user/{}/unread | Fetching unread notifications", userId);
        List<NotificationResponseDTO> unread = notificationService.getUnreadNotifications(userId);
        log.info("API Success: Found {} unread notifications for User {}", unread.size(), userId);
        return ResponseEntity.ok(unread);
    }

    /**
     * Endpoint to mark a notification as read when the user clicks it.
     */
    @PatchMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable Long id) {
        log.info("API Request: PATCH /api/v1/notifications/{}/read", id);
        notificationService.markAsRead(id);
        log.info("API Success: Notification {} marked as READ", id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Endpoint to delete a specific notification.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        log.info("API Request: DELETE /api/v1/notifications/{}", id);
        notificationService.deleteNotification(id);
        log.info("API Success: Notification {} deleted", id);
        return ResponseEntity.noContent().build();
    }
}