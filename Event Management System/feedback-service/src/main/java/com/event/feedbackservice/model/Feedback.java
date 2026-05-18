package com.event.feedbackservice.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

import com.event.feedbackservice.model.enums.FeedbackStatus;
import lombok.Data;

@Data
@Entity
public class Feedback {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long feedbackId;

    private Long eventId;
    private String eventName;

    private Long userId;
    private String email;
    private Integer rating;
    private String comment;

    @Enumerated(EnumType.STRING)
    private FeedbackStatus status;
    private LocalDateTime submittedAt;

    public Feedback() {
    }

}
