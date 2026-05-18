package com.event.userservice.dto;

import lombok.*;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NotificationResponseDTO {

    private Long id;              // Needed for "Mark as Read" functionality
    private Long userId;          // To verify ownership on the frontend
    private String subject;       // The final rendered subject (e.g., "Welcome John!")
    private String message;       // The final rendered HTML/Text body
    private String type;          // EMAIL or IN_APP
    private String status;        // UNREAD, READ, or SENT
    private LocalDateTime sentAt; // For showing "5 minutes ago" in the UI

    // Optional: Metadata if the frontend needs to redirect the user
    private String templateName;
}