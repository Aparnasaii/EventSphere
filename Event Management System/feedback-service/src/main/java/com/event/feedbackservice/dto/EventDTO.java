package com.event.feedbackservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class EventDTO{
    @NotNull(message = "Event ID is required")
    private Long eventId;

    @NotBlank(message = "Event Name is required")
    private String eventName;

}