package com.event.eventservice.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "events")
@Data
public class Event {

   @Id
   @GeneratedValue(strategy = GenerationType.IDENTITY)
   private Long eventId;

   @NotBlank(message = "Event name is required")
   @Size(max = 150, message = "Event name must not exceed 150 characters")
   @Column(nullable = false)
   private String name;

   @NotBlank(message = "Status is required")
   @Column(nullable = false)
   private String status;

   @NotBlank(message = "Category is required")
   @Column(nullable = false)
   private String category;

   @Size(max = 2000, message = "Description must not exceed 2000 characters")
   @Column(columnDefinition = "TEXT")
   private String description;

   @NotBlank(message = "Location is required")
   @Column(nullable = false)
   private String location;

   @NotNull(message = "Event date is required")
   @Column(name = "event_date", nullable = false)
   private LocalDate eventDate;

   @NotNull(message = "Start time is required")
   @Column(name = "start_time", nullable = false)
   private LocalDateTime startTime;

   @NotNull(message = "End time is required")
   @Column(name = "end_time", nullable = false)
   private LocalDateTime endTime;

   @NotNull(message = "Total capacity is required")
   @Min(value = 1, message = "Total capacity must be at least 1")
   @Column(name = "total_capacity", nullable = false)
   private Integer totalCapacity;

   @NotNull(message = "Available seats is required")
   @Min(value = 0, message = "Available seats cannot be negative")
   @Column(name = "available_seats", nullable = false)
   private Integer availableSeats;

   @NotNull(message = "Ticket price is required")
   @DecimalMin(value = "0.0", message = "Ticket price must be zero or greater")
   @Column(name = "ticket_price", nullable = false)
   private Double ticketPrice;

   @NotNull(message = "Organizer ID is required")
   @Column(name = "organizer_id", nullable = false)
   private Long organizerId;

   @Column(name = "created_at", nullable = false)
   private LocalDateTime createdAt;

   @Column(name = "updated_at")
   private LocalDateTime updatedAt;

   private String image;

   @Column(name = "is_deleted", nullable = false)
   private Boolean isDeleted = false;
}

