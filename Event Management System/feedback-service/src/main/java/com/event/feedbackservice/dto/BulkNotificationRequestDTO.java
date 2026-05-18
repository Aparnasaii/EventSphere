package com.event.feedbackservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BulkNotificationRequestDTO {

    private String templateName;  // e.g., "EVENT_UPDATE"
    private NotificationType type; // EMAIL or IN_APP

    // Data shared by everyone (e.g., {"eventName": "Tech Conf", "date": "Oct 10"})
    private Map<String, String> commonData;

    // The list of people getting the message
    private List<RecipientDetails> recipients;
}