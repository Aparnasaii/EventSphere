package com.event.notificationservice.models.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RecipientDetails {
    private Long userId;
    private String email;

    // Unique data for THIS person (e.g., {"seat": "A12", "ticketId": "998"})
    // If this map is empty, the service just uses commonData.
    private Map<String, String> personalData;
}
