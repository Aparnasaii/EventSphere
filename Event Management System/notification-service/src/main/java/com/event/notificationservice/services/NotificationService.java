package com.event.notificationservice.services;

import com.event.notificationservice.models.dtos.BulkNotificationRequestDTO;
import com.event.notificationservice.models.dtos.NotificationRequestDTO;
import com.event.notificationservice.models.dtos.NotificationResponseDTO;

import java.util.List;

public interface NotificationService {

    /**
     * Processes a single notification request (e.g., Welcome Email, Ticket Purchase).
     * Maps DTO to Entity, renders template, persists, and dispatches.
     */
    NotificationResponseDTO sendNotification(NotificationRequestDTO request);

    /**
     * Processes multiple notifications at once (e.g., Event Updates, Feedback).
     * Optimized for batch database operations.
     */
    void sendBulkNotifications(BulkNotificationRequestDTO bulkRequest);

    /**
     * Retrieves all In-App notifications for a specific user.
     * Used by the frontend 'Bell Icon'.
     */
    List<NotificationResponseDTO> getUserNotifications(Long userId);

    /**
     * Retrieves only unread notifications for a specific user.
     */
    List<NotificationResponseDTO> getUnreadNotifications(Long userId);

    /**
     * Updates the status of a notification to 'READ'.
     */
    void markAsRead(Long notificationId);

    /**
     * Deletes a notification record (Optional - for user cleanup).
     */
    void deleteNotification(Long notificationId);
}