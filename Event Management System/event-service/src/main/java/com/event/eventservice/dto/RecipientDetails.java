package com.event.eventservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecipientDetails {
    private Long userId;
    private String email;
    // Unique data for THIS person (e.g., {"seat": "A12", "ticketId": "998"})
    // If this map is empty, the service just uses commonData.
    private Map<String, String> personalData;

    public RecipientDetails(Long userId, String email) {
        this.userId = userId;
        this.email = email;
        this.personalData = null;
    }
}
