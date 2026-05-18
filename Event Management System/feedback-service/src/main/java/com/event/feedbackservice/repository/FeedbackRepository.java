package com.event.feedbackservice.repository;

import com.event.feedbackservice.model.Feedback;
import com.event.feedbackservice.model.enums.FeedbackStatus; // Ensure this is imported
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FeedbackRepository extends JpaRepository<Feedback, Long> {

    // Useful for validating that the user editing the feedback actually owns it
    Optional<Feedback> findByFeedbackIdAndUserId(Long feedbackId, Long userId);

    // Find all feedback records where status is PENDING
    List<Feedback> findByStatus(FeedbackStatus status);

    // Calculate average rating - using a parameter for the status is safer for Enums
    @Query("SELECT AVG(f.rating) FROM Feedback f WHERE f.eventId = :eventId AND f.status = com.event.feedbackservice.model.enums.FeedbackStatus.COMPLETED")
    Double getAverageRatingByEventId(@Param("eventId") Long eventId);

    // Fetch all comments for the organizer
    @Query("SELECT f.comment FROM Feedback f WHERE f.eventId = :eventId AND f.status = :status AND f.comment IS NOT NULL")
    List<String> findAllCommentsByEventId(@Param("eventId") Long eventId, @Param("status") FeedbackStatus status
    );

    // Find specific record by Event and User
    Optional<Feedback> findByEventIdAndUserId(Long eventId, Long userId);

    // Find all feedback records submitted by a specific user
    List<Feedback> findByUserId(Long userId);

}