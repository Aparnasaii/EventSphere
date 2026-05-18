package com.event.feedbackservice.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class FeedbackRequestDTO {

    @NotNull(message = "Rating cannot be null")
    @Min(value = 1, message = "Rating must be at least 1")
    @Max(value = 5, message = "Rating cannot be more than 5")
    private Integer rating;

    // Comment is optional — users may submit without text
    @Size(max = 1000, message = "Comment must not exceed 1000 characters")
    private String comment;

    public FeedbackRequestDTO() {}
}