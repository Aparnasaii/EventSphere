package com.event.eventservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationRequestDTO {
    private Long userId;          // ID of the user in your system
    private String recipient;     // email address or phone number
    private String templateName;  // e.g., "TICKET_CONFIRMATION", "WELCOME_EMAIL"
    private NotificationType type; // EMAIL or IN_APP

    // Dynamic data: e.g., {"userName": "John", "price": "50.00"}
    private Map<String, String> templateData;

}
