package com.event.notificationservice.models.entities;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "notification_templates")
public class NotificationTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // The unique key other services use (e.g., "TICKET_PURCHASED")
    @Column(unique = true, nullable = false)
    private String name;

    // The "Subject" line with placeholders
    // Example: "Your Ticket for {{eventName}}"
    @Column(nullable = false)
    private String subjectTemplate;

    // The "Body" with placeholders
    // Example: "Hi {{userName}}, your seat is {{seatNumber}}."
    @Column(columnDefinition = "TEXT", nullable = false)
    private String bodyTemplate;

    @UpdateTimestamp
    private LocalDateTime lastUpdated;
}